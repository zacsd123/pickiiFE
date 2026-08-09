package com.example.pickii.ui.chat

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pickii.BuildConfig
import com.example.pickii.R
import com.example.pickii.data.remote.dto.ApiException
import com.example.pickii.data.remote.dto.ChatMessageDto
import com.example.pickii.data.remote.dto.PublishChatMessage
import com.example.pickii.data.remote.socket.ChatConnectionState
import com.example.pickii.data.remote.socket.ChatStompClient
import com.example.pickii.domain.model.ChatMessageContentType
import com.example.pickii.domain.model.ChatRoomDetail
import com.example.pickii.domain.model.TeamSchedule
import com.example.pickii.domain.repository.CalendarRepository
import com.example.pickii.domain.repository.ChatRepository
import com.example.pickii.domain.repository.MeetingPollRepository
import com.example.pickii.domain.repository.ProjectRepository
import com.example.pickii.domain.repository.SessionRepository
import com.example.pickii.ui.common.RecruitUiEvent
import com.example.pickii.util.toDisplayString
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import com.example.pickii.domain.model.ChatMessage as DomainChatMessage

private const val MESSAGE_PAGE_SIZE = 20

/** 발신자 정보가 없는 시스템 메시지(예: 회의 조율 개설 알림)에 표시할 기본 발신자명. */
private const val SYSTEM_SENDER_NICKNAME = "시스템"
private const val ERROR_CODE_LEADER_CANNOT_LEAVE = "LEADER_CANNOT_LEAVE"
private const val ERROR_CODE_UNANSWERED_EXISTS = "UNANSWERED_EXISTS"

/**
 * 채팅방의 메시지 및 사용자 동작 상태를 관리한다.
 */
@HiltViewModel
class ChatRoomViewModel
    @Inject
    constructor(
        private val chatRepository: ChatRepository,
        private val chatStompClient: ChatStompClient,
        private val sessionRepository: SessionRepository,
        private val meetingPollRepository: MeetingPollRepository,
        private val calendarRepository: CalendarRepository,
        private val projectRepository: ProjectRepository
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(ChatRoomUiState())
        val uiState: StateFlow<ChatRoomUiState> = _uiState.asStateFlow()

        private val _events = Channel<RecruitUiEvent>(Channel.BUFFERED)
        val events: Flow<RecruitUiEvent> = _events.receiveAsFlow()

        private val _navigationEvents = Channel<ChatRoomNavigationEvent>(Channel.BUFFERED)
        val navigationEvents: Flow<ChatRoomNavigationEvent> = _navigationEvents.receiveAsFlow()

        private val currentMemberId: Long?
            get() =
                sessionRepository.currentUser.value
                    ?.id
                    ?.toLongOrNull()

        /**
         * 진입한 채팅방의 상세 정보와 최근 메시지를 서버에서 불러오고 실시간 연결을 시작한다.
         * 방 제목/종류도 항상 이 상세 조회로 채운다(목록 화면을 거치지 않고 딥링크로 들어와도 동작하도록).
         */
        fun initializeRoom(roomId: Long) {
            if (_uiState.value.roomId == roomId) return
            _uiState.value = ChatRoomUiState(roomId = roomId, isLoading = true)

            viewModelScope.launch {
                val detail =
                    chatRepository.getChatRoomDetail(roomId).getOrNull()
                if (detail == null) {
                    _uiState.update { it.copy(isLoading = false) }
                    emitEvent(RecruitUiEvent.ShowToast(R.string.chat_toast_generic_error))
                    return@launch
                }

                val historyPage =
                    chatRepository
                        .getMessages(
                            roomId,
                            cursor = null,
                            size = MESSAGE_PAGE_SIZE
                        ).getOrNull()
                val messages =
                    historyPage
                        ?.messages
                        .orEmpty()
                        .map { it.toUiModel() }
                        .filterNot { it.isMeetingPollServerNotice() }
                        .sortedBy { it.createdAt }

                _uiState.update { state ->
                    state
                        .applyDetail(detail)
                        .copy(
                            messages = messages,
                            nextMessageCursor = historyPage?.nextCursor,
                            hasMoreMessages = historyPage?.hasNext == true,
                            isLoading = false
                        )
                }

                messages.lastOrNull()?.let { lastMessage ->
                    chatRepository.markAsRead(roomId, lastMessage.id)
                }

                loadMeetings()
                loadScheduleCategories()
                connectSocket(roomId)
            }
        }

        /** 화면이 다시 보일 때(ON_RESUME) 확정된 회의 목록을 최신 상태로 갱신한다. */
        fun refreshMeetings() {
            if (_uiState.value.projectId != null) loadMeetings()
        }

        /** 확정된 팀 일정(회의) 목록을 이번 달 기준으로 불러온다(7-15). 개인 채팅방(projectId 없음)은 건너뛴다. */
        private fun loadMeetings() {
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
        private fun loadScheduleCategories() {
            if (_uiState.value.projectId == null) return
            viewModelScope.launch {
                calendarRepository.loadCategories()
                _uiState.update { it.copy(scheduleCategories = calendarRepository.categories.value) }
            }
        }

        /**
         * 이 프로젝트의 팀 일정이 내 캘린더에서 보일 색상을 지정한다(7-19). 되읽기 API가 없어 성공하면
         * 방금 고른 값을 로컬 상태로만 반영한다(앱을 다시 켜면 선택 표시가 초기화됨 — 알려진 제약).
         */
        fun onSelectProjectColor(categoryId: Long) {
            val projectId = _uiState.value.projectId ?: return
            viewModelScope.launch {
                meetingPollRepository
                    .setProjectScheduleColor(projectId, categoryId)
                    .onSuccess { _uiState.update { it.copy(selectedProjectCategoryId = categoryId) } }
                    .onFailure { emitEvent(RecruitUiEvent.ShowToast(R.string.chat_toast_generic_error)) }
            }
        }

        /** 스크롤을 위로 올렸을 때 이전 메시지를 커서 기반으로 이어서 불러온다. */
        fun loadMoreMessages() {
            val state = _uiState.value
            val cursor = state.nextMessageCursor
            if (!state.hasMoreMessages || state.isLoadingMoreMessages || cursor == null) return

            _uiState.update { it.copy(isLoadingMoreMessages = true) }
            viewModelScope.launch {
                chatRepository
                    .getMessages(state.roomId, cursor = cursor, size = MESSAGE_PAGE_SIZE)
                    .onSuccess { page ->
                        _uiState.update { current ->
                            val merged =
                                (
                                    current.messages +
                                        page.messages
                                            .map {
                                                it.toUiModel()
                                            }.filterNot { it.isMeetingPollServerNotice() }
                                ).distinctBy { it.id }
                                    .sortedBy { it.createdAt }
                            current.copy(
                                messages = merged,
                                nextMessageCursor = page.nextCursor,
                                hasMoreMessages = page.hasNext,
                                isLoadingMoreMessages = false
                            )
                        }
                    }.onFailure {
                        _uiState.update { it.copy(isLoadingMoreMessages = false) }
                        emitEvent(RecruitUiEvent.ShowToast(R.string.chat_room_toast_load_more_failed))
                    }
            }
        }

        /**
         * 방에 연결하고, 새 메시지 수신 시 화면에 반영한다. 상대가 보낸 메시지를 보고 있는 동안 실시간으로
         * 읽음 처리를 발행한다(REST 8-6과 같은 로직, WebSocket 경로).
         */
        private fun connectSocket(roomId: Long) {
            viewModelScope.launch { chatStompClient.connect(roomId) }

            viewModelScope.launch {
                chatStompClient.connectionState.collect { state ->
                    if (state == ChatConnectionState.FAILED) {
                        emitEvent(RecruitUiEvent.ShowToast(R.string.chat_toast_connection_failed))
                    }
                }
            }

            viewModelScope.launch {
                chatStompClient.incomingMessages.collect { dto ->
                    if (_uiState.value.roomId != roomId) return@collect
                    val newMessage = dto.toUiModel()

                    // 서버가 poll 개설/확정 시 자동으로 보내는 안내 문구는 이미 [MeetingRegistrationNoticeCard]/
                    // [MeetingConfirmedNoticeCard]와 내용이 겹쳐서 화면에 중복으로 보이지 않게 걸러낸다. 읽음
                    // 처리는 그대로 해서 안 보이는 메시지 때문에 안 읽음 배지가 남지 않게 한다.
                    if (!newMessage.isMeetingPollServerNotice()) {
                        _uiState.update { state ->
                            if (state.messages.any { it.id == newMessage.id }) {
                                state
                            } else {
                                state.copy(messages = (state.messages + newMessage).sortedBy { it.createdAt })
                            }
                        }
                    }

                    if (!newMessage.isMine) {
                        chatStompClient.sendRead(roomId, newMessage.id)
                    }
                }
            }

            viewModelScope.launch {
                chatStompClient.incomingErrors.collect {
                    emitEvent(RecruitUiEvent.ShowToast(R.string.chat_toast_generic_error))
                }
            }
        }

        /**
         * 메시지 입력값을 변경한다.
         */
        fun updateMessageInput(message: String) {
            _uiState.update { currentState ->
                currentState.copy(
                    messageInput = message
                )
            }
        }

        /**
         * 추가 기능 메뉴를 열거나 닫는다.
         */
        fun toggleActionMenu() {
            _uiState.update { currentState ->
                currentState.copy(
                    isActionMenuExpanded = !currentState.isActionMenuExpanded
                )
            }
        }

        /**
         * 채팅방 공지를 열거나 닫는다.
         */
        fun toggleNotice() {
            _uiState.update { currentState ->
                currentState.copy(
                    isNoticeExpanded = !currentState.isNoticeExpanded
                )
            }
        }

        /** 입력한 텍스트 메시지를 WebSocket으로 발행한다. 실제 말풍선은 서버가 되돌려주는 메시지로 렌더링된다. */
        fun sendMessage() {
            val roomId = _uiState.value.roomId
            val content = _uiState.value.messageInput.trim()
            if (content.isBlank()) return

            _uiState.update { it.copy(messageInput = "", isActionMenuExpanded = false) }

            viewModelScope.launch {
                chatStompClient.sendMessage(roomId, PublishChatMessage(type = "TEXT", message = content))
            }
        }

        /**
         * 갤러리에서 고르거나 카메라로 찍은 사진을 업로드한 뒤 WebSocket으로 이미지 메시지를 전송한다.
         * 허용되지 않는 형식이거나 10MB를 초과하면 안내 토스트를 보여준다(8-4 Validation).
         */
        fun sendImageMessages(uris: List<Uri>) {
            if (uris.isEmpty()) return
            val roomId = _uiState.value.roomId
            _uiState.update { it.copy(isActionMenuExpanded = false) }

            viewModelScope.launch {
                uris.forEach { uri ->
                    chatRepository
                        .uploadImage(roomId, uri)
                        .onSuccess { imageUrl ->
                            chatStompClient.sendMessage(roomId, PublishChatMessage(type = "IMAGE", imageUrl = imageUrl))
                        }.onFailure {
                            emitEvent(RecruitUiEvent.ShowToast(R.string.chat_toast_send_invalid_file))
                        }
                }
            }
        }

        /**
         * 회의 조율을 개설하고(7-10), 채팅방에 안내 카드를 브로드캐스트한다.
         *
         * 서버가 개설 시 보내는 SYSTEM 메시지·알림에는 pollId가 없어(알려진 API 제약), 다른 팀원은 이 poll에
         * 응답할 방법 자체가 없다. 그래서 pollId를 [encodeMeetingNoticeMessage]로 인코딩해 평범한 채팅
         * 메시지로 실제 전송한다 — [sendMessage]와 동일하게 서버 echo로 돌아온 메시지가 각자 화면에
         * "회의 조율 등록" 카드로 렌더링된다(아래 `toUiModel()` 두 곳 참고).
         */
        fun createMeetingPoll(meeting: QuickMeetingForm) {
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
         * 카드1("등록했어요")을 눌러 카드2(응답 카드)를 펼친다. 실제 API 호출은 없다 — 개인 캘린더 등록
         * 여부를 서버가 추적하지 않기 때문에(7-11 "개인 일정 등록은 필수가 아니다"), 로컬로만 펼침 상태를
         * 기록하고 최신 poll 상태(7-11)를 가져와 카드2를 채운다.
         */
        fun onAcknowledgeMeetingNotice(pollId: Long) {
            if (pollId in _uiState.value.acknowledgedPollIds) return
            _uiState.update { it.copy(acknowledgedPollIds = it.acknowledgedPollIds + pollId) }
            loadPollDetail(pollId)
        }

        /** poll 상태를 다시 조회해 [ChatRoomUiState.pollDetails]에 반영한다(7-11). */
        private fun loadPollDetail(pollId: Long) {
            viewModelScope.launch {
                meetingPollRepository.getPoll(pollId).onSuccess { poll ->
                    _uiState.update { state ->
                        state.copy(
                            pollDetails = state.pollDetails + (pollId to poll),
                            myPollSelections =
                                state.myPollSelections +
                                    (
                                        pollId to
                                            poll.slots
                                                .filterNot { slot -> slot.myAvailable }
                                                .map { slot -> slot.slotId }
                                                .toSet()
                                    )
                        )
                    }
                }
            }
        }

        /** 카드2에서 슬롯 하나의 가능/불가 체크를 토글한다(제출 전 임시 상태). */
        fun toggleMeetingPollSlot(
            pollId: Long,
            slotId: Long
        ) {
            _uiState.update { state ->
                val current = state.myPollSelections[pollId].orEmpty()
                val updated = if (slotId in current) current - slotId else current + slotId
                state.copy(myPollSelections = state.myPollSelections + (pollId to updated))
            }
        }

        /** "회의 가능한 날짜 없음" — 이 poll의 전체 슬롯을 한 번에 불가로(또는 다시 전부 가능으로) 토글한다. */
        fun toggleMeetingPollNoneAvailable(pollId: Long) {
            val allSlotIds =
                _uiState.value.pollDetails[pollId]
                    ?.slots
                    ?.map { it.slotId }
                    ?.toSet() ?: return
            _uiState.update { state ->
                val current = state.myPollSelections[pollId].orEmpty()
                val updated = if (current == allSlotIds) emptySet() else allSlotIds
                state.copy(myPollSelections = state.myPollSelections + (pollId to updated))
            }
        }

        /** 카드2 "제출하기" — 지금까지 체크한 불가 슬롯으로 응답을 제출한다(7-12). */
        fun submitMeetingPollResponse(pollId: Long) {
            val unavailableSlotIds =
                _uiState.value.myPollSelections[pollId]
                    .orEmpty()
                    .toList()
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
        fun onConfirmSlotClick(
            pollId: Long,
            slotId: Long
        ) {
            confirmMeetingPollSlot(pollId, slotId, force = false)
        }

        /** 미응답자가 있어도 그대로 확정한다. */
        fun onForceConfirmConfirm() {
            val (pollId, slotId) = _uiState.value.pendingForceConfirm ?: return
            confirmMeetingPollSlot(pollId, slotId, force = true)
        }

        /** 미응답자 확인 팝업을 닫는다. */
        fun onForceConfirmDismiss() {
            _uiState.update { it.copy(pendingForceConfirm = null) }
        }

        /** 진행 중인 조율을 취소한다(7-14, 프로젝트장 전용). 확정 전이면 그냥 취소, 확정 후라면 팀 일정도 함께 제거된다. */
        fun cancelMeetingPoll(pollId: Long) {
            viewModelScope.launch {
                meetingPollRepository
                    .cancelPoll(pollId)
                    .onSuccess {
                        _uiState.update { it.copy(pollDetails = it.pollDetails - pollId) }
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
        private fun confirmMeetingPollSlot(
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
        fun deleteMeeting(meetingId: Long) {
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
        fun updateMeetingAttendance(
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
        fun registerScheduleDirectly(
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
            viewModelScope.launch {
                meetingPollRepository
                    .registerScheduleDirectly(projectId, title, date, startTime, endTime)
                    .onSuccess {
                        emitEvent(RecruitUiEvent.ShowToast(R.string.meeting_poll_toast_confirmed))
                        loadMeetings()
                    }.onFailure {
                        emitEvent(RecruitUiEvent.ShowToast(R.string.chat_toast_generic_error))
                    }
            }
        }

        fun registerNotice(content: String) {
            if (content.isBlank()) return

            val registeredAt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())

            _uiState.update { currentState ->
                currentState.copy(
                    noticeContent = content.trim(),
                    noticeWriter =
                        sessionRepository.currentUser.value
                            ?.nickname
                            .orEmpty(),
                    noticeRegisteredAt = registeredAt,
                    isNoticeExpanded = false,
                    isActionMenuExpanded = false
                )
            }
        }

        /**
         * (미확정) 선택한 팀원에게 팀장 권한을 위임한다.
         */
        fun delegateLeader(memberId: Long) {
            val roomId = _uiState.value.roomId
            viewModelScope.launch {
                chatRepository
                    .delegateLeader(roomId, memberId)
                    .onSuccess { refreshDetail(roomId) }
                    .onFailure { emitEvent(RecruitUiEvent.ShowToast(R.string.chat_toast_generic_error)) }
            }
        }

        /**
         * (미확정) 선택한 팀원을 채팅방에서 내보낸다.
         */
        fun removeMember(memberId: Long) {
            val roomId = _uiState.value.roomId
            viewModelScope.launch {
                chatRepository
                    .removeMember(roomId, memberId)
                    .onSuccess { refreshDetail(roomId) }
                    .onFailure { emitEvent(RecruitUiEvent.ShowToast(R.string.chat_toast_generic_error)) }
            }
        }

        /** 프로젝트를 종료한다(6-4). 성공하면 방 상세를 다시 불러와 상태 뱃지("종료")를 반영한다. */
        fun closeProject() {
            val roomId = _uiState.value.roomId
            val projectId = _uiState.value.projectId ?: return
            viewModelScope.launch {
                projectRepository
                    .closeProject(projectId)
                    .onSuccess {
                        refreshDetail(roomId)
                        emitEvent(RecruitUiEvent.ShowToast(R.string.chat_toast_project_closed))
                    }.onFailure { emitEvent(RecruitUiEvent.ShowToast(R.string.chat_toast_generic_error)) }
            }
        }

        /** 채팅방 나가기를 시도한다. 성공하면 [ChatRoomNavigationEvent.LeftRoom]을 발행해 목록으로 돌아가게 한다. */
        fun leaveChatRoom() {
            val roomId = _uiState.value.roomId
            viewModelScope.launch {
                chatRepository
                    .leaveChatRoom(roomId)
                    .onSuccess { _navigationEvents.send(ChatRoomNavigationEvent.LeftRoom) }
                    .onFailure { error ->
                        val messageRes =
                            if (error is ApiException && error.code == ERROR_CODE_LEADER_CANNOT_LEAVE) {
                                R.string.chat_toast_leader_must_delegate
                            } else {
                                R.string.chat_toast_generic_error
                            }
                        emitEvent(RecruitUiEvent.ShowToast(messageRes))
                    }
            }
        }

        /** 이 채팅방의 알림 수신 여부를 서버에 반영한다(낙관적 갱신 후 실패 시 되돌린다). */
        fun updateNotificationSetting(enabled: Boolean) {
            val roomId = _uiState.value.roomId
            val previous = _uiState.value.isNotificationEnabled
            _uiState.update { it.copy(isNotificationEnabled = enabled) }

            viewModelScope.launch {
                chatRepository.updateNotification(roomId, enabled).onFailure {
                    _uiState.update { it.copy(isNotificationEnabled = previous) }
                    emitEvent(RecruitUiEvent.ShowToast(R.string.chat_toast_generic_error))
                }
            }
        }

        override fun onCleared() {
            super.onCleared()
            chatStompClient.disconnectAsync()
        }

        private fun refreshDetail(roomId: Long) {
            viewModelScope.launch {
                chatRepository.getChatRoomDetail(roomId).onSuccess { detail ->
                    _uiState.update { it.applyDetail(detail) }
                }
            }
        }

        private fun emitEvent(event: RecruitUiEvent) {
            viewModelScope.launch { _events.send(event) }
        }

        /**
         * 채팅방 상세 조회 결과를 상태에 반영한다.
         *
         * 상세 응답(8-2)에는 방별 알림 설정(notiEnabled)이 내려오지 않아, 서버 문서상 기본값인 true를 쓴다
         * (알려진 API 제약 — 목록에서 들어온 경우와 값이 다를 수 있다).
         */
        private fun ChatRoomUiState.applyDetail(detail: ChatRoomDetail): ChatRoomUiState {
            val members =
                detail.members.map { member ->
                    ChatRoomMemberUiModel(
                        memberId = member.memberId,
                        name = member.nickname,
                        isLeader = member.isLeader
                    )
                }
            val leader = members.firstOrNull { it.isLeader }
            val counterpartName =
                if (detail.type == ChatRoomType.DIRECT) {
                    members.firstOrNull { it.memberId != currentMemberId }?.name.orEmpty()
                } else {
                    ""
                }

            return copy(
                projectId = detail.projectId,
                roomTitle = detail.title,
                roomType = detail.type,
                memberCount = members.size,
                members = members,
                personalChatMemberName = counterpartName,
                isNotificationEnabled = true,
                isCurrentUserLeader = detail.isCurrentUserLeader,
                projectStatus = detail.projectStatus ?: projectStatus,
                projectInfo =
                    if (detail.type == ChatRoomType.GROUP) {
                        ChatProjectInfoUiModel(
                            projectTitle = detail.title,
                            startDate = detail.startDate?.toDisplayString().orEmpty(),
                            endDate = detail.endDate?.toDisplayString().orEmpty(),
                            memberCount = members.size,
                            leaderName = leader?.name.orEmpty(),
                            projectStatus = detail.projectStatus ?: ProjectStatus.IN_PROGRESS
                        )
                    } else {
                        projectInfo
                    }
            )
        }

        /**
         * 서버가 poll 개설(7-10)/확정(7-13) 시 그룹 채팅방에 자동으로 보내는 SYSTEM 문구인지 판별한다.
         * 이 문구엔 pollId 등 구조화된 정보가 없어([MeetingNoticeMessageCodec] 문서 참고) 클라이언트가
         * 직접 브로드캐스트하는 카드([MeetingRegistrationNoticeCard]/[MeetingConfirmedNoticeCard])로 대체했고,
         * 서버 쪽 원문은 내용이 겹쳐 화면에서 숨긴다. 참석/불참 등 다른 SYSTEM 메시지는 그대로 보여준다.
         */
        private fun ChatMessageUiModel.isMeetingPollServerNotice(): Boolean =
            type == ChatMessageType.TEXT &&
                (content.contains("회의 시간 조율이 시작되었습니다") || content.contains("확정되었습니다"))

        /** REST 히스토리 조회(8-3)로 얻은 메시지를 화면 표시 모델로 바꾼다. */
        private fun DomainChatMessage.toUiModel(): ChatMessageUiModel {
            val rawContent = text.orEmpty()
            val meetingNotice = decodeMeetingNoticeMessage(rawContent)?.toUiModel(senderNickname)
            val meetingConfirmed = decodeMeetingConfirmedMessage(rawContent)?.toUiModel()
            val isEncoded = meetingNotice != null || meetingConfirmed != null
            return ChatMessageUiModel(
                id = messageId,
                senderId = senderId,
                senderNickname = senderNickname,
                content = if (isEncoded) "" else rawContent,
                createdAt = createdAt,
                isMine = senderId == currentMemberId,
                // 상대의 읽음 여부를 알려주는 신호가 REST/WebSocket 어디에도 문서화돼 있지 않아, 허위로
                // "읽음"을 표시하지 않기 위해 항상 false로 둔다(§0-4 알려진 제약).
                isReadByCounterpart = false,
                type =
                    when {
                        meetingNotice != null -> ChatMessageType.MEETING_NOTICE
                        meetingConfirmed != null -> ChatMessageType.MEETING_CONFIRMED
                        type == ChatMessageContentType.IMAGE -> ChatMessageType.IMAGE
                        else -> ChatMessageType.TEXT
                    },
                meetingNotice = meetingNotice,
                meetingConfirmed = meetingConfirmed,
                imageUri = imageUrl?.toAbsoluteImageUrl()
            )
        }

        /** WebSocket으로 실시간 수신한 메시지를 화면 표시 모델로 바꾼다. */
        private fun ChatMessageDto.toUiModel(): ChatMessageUiModel {
            val nickname = senderNickname ?: SYSTEM_SENDER_NICKNAME
            val rawContent = message.orEmpty()
            val meetingNotice = decodeMeetingNoticeMessage(rawContent)?.toUiModel(nickname)
            val meetingConfirmed = decodeMeetingConfirmedMessage(rawContent)?.toUiModel()
            val isEncoded = meetingNotice != null || meetingConfirmed != null
            return ChatMessageUiModel(
                id = messageId,
                senderId = senderId ?: 0L,
                senderNickname = nickname,
                content = if (isEncoded) "" else rawContent,
                createdAt =
                    runCatching {
                        OffsetDateTime
                            .parse(
                                createdAt
                            ).toLocalDateTime()
                    }.getOrDefault(LocalDateTime.now()),
                isMine = senderId == currentMemberId,
                isReadByCounterpart = false,
                type =
                    when {
                        meetingNotice != null -> ChatMessageType.MEETING_NOTICE
                        meetingConfirmed != null -> ChatMessageType.MEETING_CONFIRMED
                        type == "IMAGE" -> ChatMessageType.IMAGE
                        else -> ChatMessageType.TEXT
                    },
                meetingNotice = meetingNotice,
                meetingConfirmed = meetingConfirmed,
                imageUri = imageUrl?.toAbsoluteImageUrl()
            )
        }

        private fun DecodedMeetingNotice.toUiModel(requesterName: String): MeetingNoticeUiModel =
            MeetingNoticeUiModel(
                meetingTitle = title,
                requesterName = requesterName,
                pollId = pollId,
                deadlineMillis = deadlineMillis
            )

        private fun DecodedMeetingConfirmed.toUiModel(): MeetingConfirmedUiModel =
            MeetingConfirmedUiModel(
                meetingTitle = title,
                pollId = pollId,
                slotStartMillis = slotStartMillis,
                slotEndMillis = slotEndMillis,
                scheduleId = scheduleId
            )

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
        private fun Long.toLocalDate(): LocalDate =
            Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()

        /** 서버가 내려준 [LocalDateTime](KST)을 카운트다운 계산용 epoch millis로 바꾼다. */
        private fun LocalDateTime.toEpochMillis(): Long = atOffset(ZoneOffset.ofHours(9)).toInstant().toEpochMilli()

        /** 자정 기준 분(0~1439)을 [LocalTime]으로 바꾼다(회의 조율 탐색 시간대 입력값). */
        private fun Int.toLocalTimeOfDay(): LocalTime = LocalTime.of(this / 60, this % 60)
    }

private val MeetingTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

/** 이미지 업로드(8-4) 응답의 `imageUrl`이 `/static-uploads/...`처럼 API_BASE_URL(`.../api/v1/`) 기준
 * 상대경로라서(실제 파일은 `.../api/v1/static-uploads/...`에 있음, origin 바로 아래가 아니다 — 직접
 * curl로 확인함), 실제 로드하려면 API_BASE_URL을 그대로 앞에 붙여야 한다. */
private val ApiBaseUrlNoTrailingSlash: String by lazy { BuildConfig.API_BASE_URL.trimEnd('/') }

/** 서버가 내려준 이미지 경로를 실제 로드 가능한 절대 URL로 바꾼다. 이미 절대 URL이면 그대로 둔다. */
private fun String.toAbsoluteImageUrl(): String =
    if (startsWith("http://") || startsWith("https://")) this else ApiBaseUrlNoTrailingSlash + this
