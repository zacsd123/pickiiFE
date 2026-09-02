package com.example.pickii.ui.chat

/** 채팅 목록 화면에서 발생하는 1회성 네비게이션 이벤트. */
sealed interface ChatNavigationEvent {
    /** 지정한 채팅방으로 이동한다(예: 1:1 채팅방 생성 직후). */
    data class OpenRoom(
        val roomId: Long
    ) : ChatNavigationEvent
}
