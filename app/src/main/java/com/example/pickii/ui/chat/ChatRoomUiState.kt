package com.example.pickii.ui.chat

/**
 * 채팅방 종류를 나타낸다.
 */
enum class ChatRoomType {
    GROUP,
    PERSONAL,
}

/**
 * 프로젝트 진행 상태를 나타낸다.
 */
enum class ProjectStatus {
    BEFORE_START,
    IN_PROGRESS,
    COMPLETED,
}


/**
 * 프로젝트 상태를 화면 표시용 문구로 변환한다.
 */
fun ProjectStatus.toDisplayText(): String {
    return when (this) {
        ProjectStatus.BEFORE_START -> "진행 전"
        ProjectStatus.IN_PROGRESS -> "진행 중"
        ProjectStatus.COMPLETED -> "진행 완료"
    }
}

/**
 * 채팅방 화면 상태이다.
 *
 * @property roomId 채팅방 식별자
 * @property roomTitle 채팅방 제목
 * @property roomType 그룹 또는 개인 채팅 여부
 * @property memberCount 채팅방 전체 참여자 수
 * @property members 채팅방 참여자 목록
 * @property leaderName 채팅방 팀장 이름
 * @property personalChatMemberName 개인 채팅 상대방 이름
 * @property isNotificationEnabled 채팅방 알림 활성화 여부
 * @property projectStatus 프로젝트 진행 상태
 * @property isCurrentUserLeader 현재 사용자의 팀장 여부
 * @property messages 채팅 메시지 목록
 * @property messageInput 현재 입력 중인 메시지
 * @property isActionMenuExpanded 추가 기능 메뉴 표시 여부
 * @property isNoticeExpanded 공지 내용 표시 여부
 */


data class ChatRoomUiState(
    val roomId: Long = 0L,
    val roomTitle: String = "",
    val roomType: ChatRoomType = ChatRoomType.PERSONAL,
    val memberCount: Int = 2,

    val members: List<ChatRoomMemberUiModel> = emptyList(),
    val leaderName: String = "",
    val personalChatMemberName: String = "",
    val isNotificationEnabled: Boolean = false,
    val projectStatus: ProjectStatus = ProjectStatus.IN_PROGRESS,
    val isCurrentUserLeader: Boolean = false,

    val messages: List<ChatMessageUiModel> = emptyList(),
    val messageInput: String = "",
    val isActionMenuExpanded: Boolean = false,
    val isNoticeExpanded: Boolean = false,
    val noticeContent: String = "",
    val noticeWriter: String = "",
    val noticeRegisteredAt: String = "",
    val projectInfo: ChatProjectInfoUiModel = ChatProjectInfoUiModel(
        projectTitle = "캡스톤 디자인 프로젝트",
        startDate = "2026.03.02",
        endDate = "2026.08.31",
        memberCount = 5,
        leaderName = "민준",
        progressPercent = 62,
        projectStatus = ProjectStatus.IN_PROGRESS,
    ),
    val meetings: List<ManagedMeetingUiModel> = emptyList(),
)