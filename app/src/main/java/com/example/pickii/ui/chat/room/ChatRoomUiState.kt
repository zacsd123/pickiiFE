package com.example.pickii.ui.chat

import com.example.pickii.domain.model.CalendarSchedule
import com.example.pickii.domain.model.ChatRoomType
import com.example.pickii.domain.model.MeetingPollDetail
import com.example.pickii.domain.model.ProjectStatus
import com.example.pickii.domain.model.ScheduleCategory

/**
 * 프로젝트 상태를 화면 표시용 문구로 변환한다.
 */
fun ProjectStatus.toDisplayText(): String =
    when (this) {
        ProjectStatus.IN_PROGRESS -> "진행 중"
        ProjectStatus.END -> "종료"
    }

/**
 * 채팅방 화면 상태이다.
 *
 * @property roomId 채팅방 식별자
 * @property roomTitle 채팅방 제목
 * @property roomType 그룹 또는 개인 채팅 여부
 * @property memberCount 채팅방 전체 참여자 수
 * @property members 채팅방 참여자 목록
 * @property personalChatMemberName 개인 채팅 상대방 이름
 * @property isNotificationEnabled 채팅방 알림 활성화 여부
 * @property projectStatus 프로젝트 진행 상태
 * @property isCurrentUserLeader 현재 사용자의 팀장 여부
 * @property messages 채팅 메시지 목록
 * @property messageInput 현재 입력 중인 메시지
 * @property isActionMenuExpanded 추가 기능 메뉴 표시 여부
 * @property isNoticeExpanded 공지 내용 표시 여부
 * @property pollDetails 채팅에 등장한 회의 조율(poll)의 최신 상태(7-11 재조회 결과), pollId 기준
 * @property confirmedMeetings 확정 브로드캐스트 메시지에서 뽑아낸 확정 정보, pollId 기준. 별도 메시지로
 * 리스트에 넣는 대신 이 맵에 흡수해서 [MeetingProgressCard]가 원래 등록공지 카드 안에서 확정 화면을 그리게 한다.
 * @property acknowledgedPollIds "등록했어요"를 눌러 응답 카드를 펼쳐본 poll id 목록(로컬 상태, API 없음)
 * @property savedMeetingScheduleIds "내 캘린더에 저장"을 눌러 저장한 확정 일정의 scheduleId 목록(로컬 상태,
 * 되읽기 API 없어 앱을 다시 켜면 초기화됨 — 알려진 제약)
 * @property myPollSelections poll별 지금 화면에서 체크 중인 불가 슬롯 id 목록(제출 전 임시 상태)
 * @property pendingForceConfirm 미응답자가 있는 채로 확정을 시도해 확인이 필요한 (pollId, slotId)
 * @property scheduleCategories 개인 캘린더 카테고리 목록(7-19 프로젝트 색상 지정에서 그대로 재사용)
 * @property selectedProjectCategoryId 이번 세션에서 이 프로젝트에 지정한 카테고리(되읽기 API가 없어 로컬 상태만)
 * @property myCalendarSchedules 회의 조율 날짜 선택기에서 이미 등록된 날짜를 비활성화하기 위해 불러온 내 캘린더 일정
 */

data class ChatRoomUiState(
    val roomId: Long = 0L,
    val projectId: Long? = null,
    val roomTitle: String = "",
    val roomType: ChatRoomType = ChatRoomType.DIRECT,
    val isLoading: Boolean = true,
    val memberCount: Int = 0,
    val members: List<ChatRoomMemberUiModel> = emptyList(),
    val personalChatMemberName: String = "",
    val isNotificationEnabled: Boolean = false,
    val projectStatus: ProjectStatus = ProjectStatus.IN_PROGRESS,
    val isCurrentUserLeader: Boolean = false,
    val messages: List<ChatMessageUiModel> = emptyList(),
    val nextMessageCursor: String? = null,
    val hasMoreMessages: Boolean = false,
    val isLoadingMoreMessages: Boolean = false,
    val messageInput: String = "",
    val isActionMenuExpanded: Boolean = false,
    val isNoticeExpanded: Boolean = false,
    val noticeContent: String = "",
    val noticeWriter: String = "",
    val noticeRegisteredAt: String = "",
    val projectInfo: ChatProjectInfoUiModel =
        ChatProjectInfoUiModel(
            projectTitle = "",
            startDate = "",
            endDate = "",
            memberCount = 0,
            leaderName = "",
            projectStatus = ProjectStatus.IN_PROGRESS
        ),
    val meetings: List<ManagedMeetingUiModel> = emptyList(),
    val pollDetails: Map<Long, MeetingPollDetail> = emptyMap(),
    val confirmedMeetings: Map<Long, MeetingConfirmedUiModel> = emptyMap(),
    val acknowledgedPollIds: Set<Long> = emptySet(),
    val savedMeetingScheduleIds: Set<Long> = emptySet(),
    val myPollSelections: Map<Long, Set<Long>> = emptyMap(),
    val pendingForceConfirm: Pair<Long, Long>? = null,
    val scheduleCategories: List<ScheduleCategory> = emptyList(),
    val selectedProjectCategoryId: Long? = null,
    val myCalendarSchedules: List<CalendarSchedule> = emptyList()
)
