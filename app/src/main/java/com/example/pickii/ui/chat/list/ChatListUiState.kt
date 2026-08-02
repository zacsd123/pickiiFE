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
 */
data class ChatListUiState(
    val selectedTab: ChatListTab = ChatListTab.DIRECT,
    val groupChatRooms: List<ChatRoomPreviewUiModel> = emptyList(),
    val directChatRooms: List<ChatRoomPreviewUiModel> = emptyList()
)
