package com.example.pickii.ui.chat

import androidx.lifecycle.viewModelScope
import com.example.pickii.R
import com.example.pickii.data.remote.dto.ApiException
import com.example.pickii.data.remote.dto.PublishChatMessage
import com.example.pickii.domain.model.CalendarSchedule
import com.example.pickii.domain.model.TeamSchedule
import com.example.pickii.ui.common.RecruitUiEvent
import com.example.pickii.util.toDisplayString
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private const val ERROR_CODE_UNANSWERED_EXISTS = "UNANSWERED_EXISTS"

private val MeetingTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

/** 확정된 팀 일정(회의) 목록을 이번 달 기준으로 불러온다(7-15). 개인 채팅방(projectId 없음)은 건너뛴다. */
internal fun ChatRoomViewModel.loadMeetings() {
    val projectId = _uiState.value.projectId ?: return
    viewModelScope.launch {
        meetingPollRepository
            .getTeamSchedules(projectId, YearMonth.now())
            .onSuccess { schedules ->
                _uiState.update { it.copy(meetings = schedules.map { schedule -> schedule.toUiModel() }) }
            }
    }
}

/** 프로젝트 색상 지정(7-19)에 쓸 개인 캘린더 태그 목록을 불러온다(7-1, 개인 캘린더와 동일한 카테고리). */
internal fun ChatRoomViewModel.loadScheduleCategories() {
    if (_uiState.value.projectId == null) return
    viewModelScope.launch {
        calendarRepository.loadCategories()
        _uiState.update { it.copy(scheduleCategories = calendarRepository.categories.value) }
    }
}

/**
 * 이 프로젝트의 팀 일정이 내 캘린더에서 보일 색상을 지정한다(7-19). 되읽기 API가 없어 성공하면
 * 방금 고른 값을 로컬 상태로만 반영한다(앱을 다시 켜면 선택 표시가 초기화됨 — 알려진 제약).
 *
 * "없음"(categoryId == null)은 API 자체가 해제를 표현할 방법이 없어(요청 바디가 non-null Long),
 * 서버 호출 없이 로컬 상태만 초기화한다 — 이미 이 값 전체가 로컬 상태로만 관리되는 것과 같은 원칙.
 */
internal fun ChatRoomViewModel.onSelectProjectColor(categoryId: Long?) {
    if (categoryId == null) {
        _uiState.update { it.copy(selectedProjectCategoryId = null) }
        return
    }
    val projectId = _uiState.value.projectId ?: return
    viewModelScope.launch {
        meetingPollRepository
            .setProjectScheduleColor(projectId, categoryId)
            .onSuccess { _uiState.update { it.copy(selectedProjectCategoryId = categoryId) } }
            .onFailure { emitEvent(RecruitUiEvent.ShowToast(R.string.chat_toast_generic_error)) }
    }
}

/**
 * 회의 조율을 개설하고(7-10), 채팅방에 안내 카드를 브로드캐스트한다.
 *
 * 서버가 개설 시 보내는 SYSTEM 메시지·알림에는 pollId가 없어(알려진 API 제약), 다른 팀원은 이 poll에
 * 응답할 방법 자체가 없다. 그래서 pollId를 [encodeMeetingNoticeMessage]로 인코딩해 평범한 채팅
 * 메시지로 실제 전송한다 — [ChatRoomViewModel.sendMessage]와 동일하게 서버 echo로 돌아온 메시지가
 * 각자 화면에 "회의 조율 등록" 카드로 렌더링된다(`ChatRoomViewModel.kt`의 `toUiModel()` 두 곳 참고).
 */
internal fun ChatRoomViewModel.createMeetingPoll(meeting: QuickMeetingForm) {
    val roomId = _uiState.value.roomId
    val projectId = _uiState.value.projectId
    if (projectId == null) {
        emitEvent(RecruitUiEvent.ShowToast(R.string.chat_toast_generic_error))
        return
    }

    viewModelScope.launch {
        meetingPollRepository
            .createPoll(
                projectId = projectId,
                title = meeting.title,
                durationMin = meeting.durationMinutes,
                rangeStart = meeting.startDateMillis.toLocalDate(),
                rangeEnd = meeting.endDateMillis.toLocalDate(),
                dayStart = meeting.dayStartMinuteOfDay.toLocalTimeOfDay(),
                dayEnd = meeting.dayEndMinuteOfDay.toLocalTimeOfDay(),
                deadlineHours = meeting.deadlineHours,
                memberIds = meeting.memberIds
            ).onSuccess { created ->
                _uiState.update { it.copy(isActionMenuExpanded = false) }
                val noticeContent =
                    encodeMeetingNoticeMessage(
                        pollId = created.pollId,
                        title = meeting.title,
                        deadlineMillis = created.deadline.toEpochMillis()
                    )
                chatStompClient.sendMessage(roomId, PublishChatMessage(type = "TEXT", message = noticeContent))
            }.onFailure {
                emitEvent(RecruitUiEvent.ShowToast(R.string.chat_toast_generic_error))
            }
    }
}

/**
 * 카드1("등록했어요") 버튼을 누른 표시를 남긴다(버튼이 옅게 바뀌며 비활성화됨). 이 표시가 있어야
 * 카드2(응답 폼)가 뜬다 — 아직 응답 안 한 채 방에 새로 들어왔을 때 캘린더 등록 없이 곧장 슬롯
 * 선택 화면이 뜨는 걸 막기 위한 게이트다. 서버가 이 클릭 자체를 추적하진 않는다(7-11 "개인 일정
 * 등록은 필수가 아니다") — 실제 캘린더 반영은 [ensurePollDetailsLoaded]가 항상 미리 불러오는
 * `GET /meeting-polls/{pollId}`의 `prefilledByCalendar` 값으로 이미 처리된다. 이미 응답을
 * 제출했거나(`myResponded`) 조율이 끝난 경우는 이 로컬 플래그와 무관하게 항상 카드가 보이므로
 * (렌더링 조건 참고, `ChatRoomScreen.kt`), 세션이 바뀌어도 응답/종료 상태 자체는 숨겨지지 않는다.
 */
internal fun ChatRoomViewModel.onAcknowledgeMeetingNotice(pollId: Long) {
    if (pollId in _uiState.value.acknowledgedPollIds) return
    _uiState.update { it.copy(acknowledgedPollIds = it.acknowledgedPollIds + pollId) }
}

/**
 * 확정된 회의 일정(카드4)을 내 개인 캘린더에도 저장한다. 다른 멤버 대신 저장해줄 API가 없어(권한상
 * 본인 캘린더만 쓸 수 있음) 채팅방에 있는 각자가 이 버튼을 눌러야 한다 — 알려진 제약.
 */
internal fun ChatRoomViewModel.onSaveMeetingToMyCalendar(confirmed: MeetingConfirmedUiModel) {
    if (confirmed.scheduleId in _uiState.value.savedMeetingScheduleIds) return
    val start = Instant.ofEpochMilli(confirmed.slotStartMillis).atZone(ZoneId.systemDefault()).toLocalDateTime()
    val end = Instant.ofEpochMilli(confirmed.slotEndMillis).atZone(ZoneId.systemDefault()).toLocalDateTime()

    viewModelScope.launch {
        calendarRepository
            .addSchedule(
                CalendarSchedule(
                    id = 0L,
                    title = confirmed.meetingTitle,
                    categoryId = _uiState.value.selectedProjectCategoryId,
                    startDate = start.toLocalDate(),
                    endDate = end.toLocalDate(),
                    startTime = start.toLocalTime(),
                    endTime = end.toLocalTime()
                )
            ).onSuccess {
                _uiState.update {
                    it.copy(savedMeetingScheduleIds = it.savedMeetingScheduleIds + confirmed.scheduleId)
                }
                savedMeetingScheduleStore.markSaved(confirmed.scheduleId)
                emitEvent(RecruitUiEvent.ShowToast(R.string.chat_toast_meeting_saved_to_calendar))
            }.onFailure {
                emitEvent(RecruitUiEvent.ShowToast(R.string.chat_toast_generic_error))
            }
    }
}

/**
 * [messages] 중 회의 조율 공지가 있으면 아직 안 불러온 poll만 골라 [loadPollDetail]을 건다.
 * 카드1("등록했어요")을 눌렀는지와 무관하게 항상 호출해야 카드2/3이 세션을 넘나들며 계속
 * 보인다 — 예전엔 그 클릭에만 의존해서, 이미 응답을 제출했거나 조율이 이미 끝난 채로 방에
 * 다시 들어와도 클릭 기록이 세션마다 초기화돼 카드가 안 보이는 게 버그였다.
 */
internal fun ChatRoomViewModel.ensurePollDetailsLoaded(messages: List<ChatMessageUiModel>) {
    messages
        .mapNotNull { it.meetingNotice?.pollId }
        .distinct()
        .filterNot { it in _uiState.value.pollDetails }
        .forEach { pollId -> loadPollDetail(pollId) }
}

/**
 * poll 상태를 다시 조회해 [ChatRoomUiState.pollDetails]에 반영한다(7-11). [ChatRoomUiState.myPollSelections]는
 * "체크 = 가능한 시간"으로 다룬다(UI 문구 "가능한 시간을 모두 선택해 주세요"와 일치시킴 — 예전엔
 * 반대로 "체크 = 불가"였는데, 화면 문구와 실제 동작이 어긋나 사용자가 자기 뜻과 반대로 응답을 내는
 * 버그였다). 아직 응답한 적 없는 poll(`myResponded == false`)은 서버의 `myAvailable` 기본값과
 * 무관하게 빈 선택으로 시작해 "아무것도 안 골랐다"가 곧바로 "전부 가능"으로 오인되지 않게 한다.
 */
internal fun ChatRoomViewModel.loadPollDetail(pollId: Long) {
    viewModelScope.launch {
        meetingPollRepository.getPoll(pollId).onSuccess { poll ->
            _uiState.update { state ->
                state.copy(
                    pollDetails = state.pollDetails + (pollId to poll),
                    myPollSelections =
                        state.myPollSelections +
                            (
                                pollId to
                                    if (poll.myResponded) {
                                        poll.slots
                                            .filter { slot -> slot.myAvailable }
                                            .map { slot -> slot.slotId }
                                            .toSet()
                                    } else {
                                        emptySet()
                                    }
                            )
                )
            }
        }
    }
}

/** 카드2에서 슬롯 하나의 가능 체크를 토글한다(제출 전 임시 상태). */
internal fun ChatRoomViewModel.toggleMeetingPollSlot(
    pollId: Long,
    slotId: Long
) {
    _uiState.update { state ->
        val current = state.myPollSelections[pollId].orEmpty()
        val updated = if (slotId in current) current - slotId else current + slotId
        state.copy(myPollSelections = state.myPollSelections + (pollId to updated))
    }
}

/** "회의 가능한 날짜 없음" — 선택을 한 번에 비우거나(없음 선언), 다시 전체 선택으로 되돌린다. */
internal fun ChatRoomViewModel.toggleMeetingPollNoneAvailable(pollId: Long) {
    val allSlotIds =
        _uiState.value.pollDetails[pollId]
            ?.slots
            ?.map { it.slotId }
            ?.toSet() ?: return
    _uiState.update { state ->
        val current = state.myPollSelections[pollId].orEmpty()
        val updated = if (current.isEmpty()) allSlotIds else emptySet()
        state.copy(myPollSelections = state.myPollSelections + (pollId to updated))
    }
}

/** 카드2 "제출하기" — 가능하다고 체크한 슬롯을 뒤집어(전체 - 체크) 불가 슬롯 목록으로 제출한다(7-12,
 * API가 "불가능한 슬롯만" 받는다). */
internal fun ChatRoomViewModel.submitMeetingPollResponse(pollId: Long) {
    val poll = _uiState.value.pollDetails[pollId] ?: return
    val myAvailableSlotIds = _uiState.value.myPollSelections[pollId].orEmpty()
    val unavailableSlotIds = poll.slots.map { it.slotId }.filterNot { it in myAvailableSlotIds }
    viewModelScope.launch {
        meetingPollRepository
            .submitResponse(pollId, unavailableSlotIds)
            .onSuccess {
                emitEvent(RecruitUiEvent.ShowToast(R.string.meeting_poll_toast_response_submitted))
                loadPollDetail(pollId)
            }.onFailure {
                emitEvent(RecruitUiEvent.ShowToast(R.string.chat_toast_generic_error))
            }
    }
}

/** 카드3(집계) "확정" — 프로젝트장 전용, 그 자리에서 바로 7-13을 호출한다. */
internal fun ChatRoomViewModel.onConfirmSlotClick(
    pollId: Long,
    slotId: Long
) {
    confirmMeetingPollSlot(pollId, slotId, force = false)
}

/** 미응답자가 있어도 그대로 확정한다. */
internal fun ChatRoomViewModel.onForceConfirmConfirm() {
    val (pollId, slotId) = _uiState.value.pendingForceConfirm ?: return
    confirmMeetingPollSlot(pollId, slotId, force = true)
}

/** 미응답자 확인 팝업을 닫는다. */
internal fun ChatRoomViewModel.onForceConfirmDismiss() {
    _uiState.update { it.copy(pendingForceConfirm = null) }
}

/** 진행 중인 조율을 취소한다(7-14, 프로젝트장 전용). 확정 전이면 그냥 취소, 확정 후라면 팀 일정도 함께 제거된다. */
internal fun ChatRoomViewModel.cancelMeetingPoll(pollId: Long) {
    viewModelScope.launch {
        meetingPollRepository
            .cancelPoll(pollId)
            .onSuccess {
                // pollDetails에서 지우면 카드2가 통째로 사라진다("이 조율은 취소됐어요" 상태를
                // 보여줘야 하는데 그럴 데이터 자체가 없어짐) — CANCELLED 상태로 다시 불러온다.
                loadPollDetail(pollId)
                loadMeetings()
            }.onFailure {
                emitEvent(RecruitUiEvent.ShowToast(R.string.chat_toast_generic_error))
            }
    }
}

/**
 * 최종 슬롯을 확정한다(7-13, 프로젝트장 전용). 성공하면 확정 정보를 카드4로 브로드캐스트한다
 * ([encodeMeetingConfirmedMessage] — 서버 자체 SYSTEM 메시지에는 구조화된 정보가 없어서).
 */
private fun ChatRoomViewModel.confirmMeetingPollSlot(
    pollId: Long,
    slotId: Long,
    force: Boolean
) {
    _uiState.update { it.copy(pendingForceConfirm = null) }
    val poll = _uiState.value.pollDetails[pollId]
    val slot = poll?.slots?.find { it.slotId == slotId }
    if (poll == null || slot == null) {
        emitEvent(RecruitUiEvent.ShowToast(R.string.chat_toast_generic_error))
        return
    }
    val roomId = _uiState.value.roomId

    viewModelScope.launch {
        meetingPollRepository
            .confirmPoll(pollId, slotId, force)
            .onSuccess { scheduleId ->
                emitEvent(RecruitUiEvent.ShowToast(R.string.meeting_poll_toast_confirmed))
                val confirmedContent =
                    encodeMeetingConfirmedMessage(
                        pollId = pollId,
                        title = poll.title,
                        slotStartMillis = slot.startAt.toEpochMillis(),
                        slotEndMillis = slot.endAt.toEpochMillis(),
                        scheduleId = scheduleId
                    )
                chatStompClient.sendMessage(
                    roomId,
                    PublishChatMessage(type = "TEXT", message = confirmedContent)
                )
                loadPollDetail(pollId)
                loadMeetings()
            }.onFailure { error ->
                if (!force && error is ApiException && error.code == ERROR_CODE_UNANSWERED_EXISTS) {
                    _uiState.update { it.copy(pendingForceConfirm = pollId to slotId) }
                } else {
                    emitEvent(RecruitUiEvent.ShowToast(R.string.chat_toast_generic_error))
                }
            }
    }
}

/**
 * 확정된 회의(팀 일정)를 삭제한다(7-18).
 *
 * @param meetingId 삭제할 팀 일정의 ID
 */
internal fun ChatRoomViewModel.deleteMeeting(meetingId: Long) {
    viewModelScope.launch {
        meetingPollRepository
            .deleteTeamSchedule(meetingId)
            .onSuccess {
                _uiState.update { current ->
                    current.copy(meetings = current.meetings.filterNot { it.id == meetingId })
                }
            }.onFailure {
                emitEvent(RecruitUiEvent.ShowToast(R.string.chat_toast_generic_error))
            }
    }
}

/** 회의(팀 일정) 참석/불참 여부를 변경한다(7-20). */
internal fun ChatRoomViewModel.updateMeetingAttendance(
    meetingId: Long,
    attending: Boolean
) {
    viewModelScope.launch {
        meetingPollRepository
            .updateAttendance(meetingId, attending)
            .onFailure { emitEvent(RecruitUiEvent.ShowToast(R.string.chat_toast_generic_error)) }
    }
}

/**
 * 조율 없이 팀 일정을 직접 등록한다(7-16, 프로젝트장 전용 — 이미 오프라인 등으로 확정된 예외적인
 * 일정을 등록할 때 쓴다). 성공하면 조율로 확정한 회의와 동일하게 취급된다(7-16 Business Logic 5번).
 */
internal fun ChatRoomViewModel.registerScheduleDirectly(
    title: String,
    date: LocalDate,
    startTime: LocalTime,
    endTime: LocalTime
) {
    val projectId = _uiState.value.projectId
    if (projectId == null) {
        emitEvent(RecruitUiEvent.ShowToast(R.string.chat_toast_generic_error))
        return
    }
    val roomId = _uiState.value.roomId

    viewModelScope.launch {
        meetingPollRepository
            .registerScheduleDirectly(projectId, title, date, startTime, endTime)
            .onSuccess { scheduleId ->
                emitEvent(RecruitUiEvent.ShowToast(R.string.meeting_poll_toast_confirmed))
                // 조율 흐름과 마찬가지로 구조화된 정보를 실제 채팅 메시지로 브로드캐스트해, 서버 echo로
                // 돌아온 메시지가 팀원 전원 화면에 "내 캘린더에 저장" 카드로 뜨게 한다.
                val directMeetingContent =
                    encodeDirectMeetingMessage(
                        title = title,
                        slotStartMillis = LocalDateTime.of(date, startTime).toEpochMillis(),
                        slotEndMillis = LocalDateTime.of(date, endTime).toEpochMillis(),
                        scheduleId = scheduleId
                    )
                chatStompClient.sendMessage(roomId, PublishChatMessage(type = "TEXT", message = directMeetingContent))
                loadMeetings()
            }.onFailure {
                emitEvent(RecruitUiEvent.ShowToast(R.string.chat_toast_generic_error))
            }
    }
}

/** 팀 일정(7-15)을 회의 관리 화면 표시 모델로 바꾼다. */
private fun TeamSchedule.toUiModel(): ManagedMeetingUiModel =
    ManagedMeetingUiModel(
        id = scheduleId,
        title = title,
        date = startDate.toDisplayString(),
        startTime = startTime?.format(MeetingTimeFormatter) ?: "시간 미정",
        endTime = endTime?.format(MeetingTimeFormatter) ?: "시간 미정"
    )

/** 밀리초(날짜 선택기 결과)를 기기 시간대 기준 [LocalDate]로 바꾼다. */
private fun Long.toLocalDate(): LocalDate = Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()

/** 서버가 내려준 [LocalDateTime](KST)을 카운트다운 계산용 epoch millis로 바꾼다. */
private fun LocalDateTime.toEpochMillis(): Long = atOffset(ZoneOffset.ofHours(9)).toInstant().toEpochMilli()

/** 자정 기준 분(0~1439)을 [LocalTime]으로 바꾼다(회의 조율 탐색 시간대 입력값). */
private fun Int.toLocalTimeOfDay(): LocalTime = LocalTime.of(this / 60, this % 60)
