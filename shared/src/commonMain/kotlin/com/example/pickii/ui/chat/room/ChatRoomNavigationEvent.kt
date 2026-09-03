package com.example.pickii.ui.chat

/** 채팅방 화면에서 발생하는 1회성 네비게이션 이벤트. */
sealed interface ChatRoomNavigationEvent {
    /** 채팅방 나가기가 서버에 성공적으로 반영되어 목록 화면으로 돌아가야 한다. */
    data object LeftRoom : ChatRoomNavigationEvent
}
