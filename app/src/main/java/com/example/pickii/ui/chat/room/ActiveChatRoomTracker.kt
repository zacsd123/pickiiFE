package com.example.pickii.ui.chat.room

import kotlinx.coroutines.flow.MutableStateFlow

/**
 * 지금 화면에 열려 있는 채팅방 id(없으면 null). [com.example.pickii.ui.chat.ChatRoute]가 방
 * 화면을 보여주는 동안 갱신하고, [com.example.pickii.data.notification.ChatMessagePoller]가 지금 보고
 * 있는 방의 알림은 건너뛰는 데 사용한다.
 */
object ActiveChatRoomTracker {
    val activeRoomId = MutableStateFlow<Long?>(null)
}
