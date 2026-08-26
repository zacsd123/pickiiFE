package com.example.pickii.data.notification

import kotlin.concurrent.Volatile

/**
 * 현재 화면에 열려 있는 채팅방 id를 추적한다. `FcmService`가 그 방으로 온 메시지 알림을
 * 억제할지 판단하는 데 쓴다(`ChatRoomViewModel`이 진입/이탈 시 갱신한다).
 *
 * Koin 싱글턴(`di/InfraModule.kt`).
 */
class ActiveChatRoomTracker {
    @Volatile
    var activeRoomId: Long? = null
        private set

    fun onRoomEntered(roomId: Long) {
        activeRoomId = roomId
    }

    fun onRoomExited(roomId: Long) {
        if (activeRoomId == roomId) {
            activeRoomId = null
        }
    }
}
