package com.example.pickii.ui.chat

/**
 * 채팅 목록 화면에서 선택할 수 있는 탭이다.
 */
enum class ChatListTab {
    GROUP,
    DIRECT
}

/**
 * 채팅 목록 화면의 UI 상태다.
 *
 * @property selectedTab 현재 선택된 탭
 * @property groupChatRooms 그룹 채팅방 목록
 * @property directChatRooms 개인 채팅방 목록
 * @property isGroupLoading 그룹 탭 첫 페이지 로딩 여부
 * @property isDirectLoading 개인 탭 첫 페이지 로딩 여부
 * @property isGroupLoadingMore 그룹 탭 다음 페이지 로딩 여부
 * @property isDirectLoadingMore 개인 탭 다음 페이지 로딩 여부
 * @property groupPage 그룹 탭에서 마지막으로 불러온 페이지 번호
 * @property directPage 개인 탭에서 마지막으로 불러온 페이지 번호
 * @property groupHasNext 그룹 탭에 다음 페이지가 있는지 여부
 * @property directHasNext 개인 탭에 다음 페이지가 있는지 여부
 * @property notificationCount 상단 헤더에 표시할 미읽음 알림 수
 */
data class ChatListUiState(
    val selectedTab: ChatListTab = ChatListTab.DIRECT,
    val groupChatRooms: List<ChatRoomPreviewUiModel> = emptyList(),
    val directChatRooms: List<ChatRoomPreviewUiModel> = emptyList(),
    val isGroupLoading: Boolean = false,
    val isDirectLoading: Boolean = false,
    val isGroupLoadingMore: Boolean = false,
    val isDirectLoadingMore: Boolean = false,
    val groupPage: Int = 0,
    val directPage: Int = 0,
    val groupHasNext: Boolean = false,
    val directHasNext: Boolean = false,
    val notificationCount: Int = 0
)
