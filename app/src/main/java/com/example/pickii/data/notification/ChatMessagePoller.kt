package com.example.pickii.data.notification

import com.example.pickii.domain.repository.NotificationRepository
import com.example.pickii.domain.repository.SessionRepository
import com.example.pickii.ui.chat.room.ActiveChatRoomTracker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

private const val POLL_INTERVAL_MS = 15_000L
private const val CHAT_NOTIFICATION_TYPE = "CHAT"

/**
 * 앱 프로세스가 살아있는 동안 새 채팅 알림(9-1, type=CHAT)을 주기적으로 확인해 시스템 알림으로 띄운다.
 *
 * WebSocket은 지금 열려 있는 채팅방 하나만 구독하므로(8번 문서에 전체 방을 아우르는 실시간 채널이 없다),
 * 다른 방에 새 메시지가 왔는지는 이미 연동돼 있는 알림함 API를 폴링해서 대신 확인한다. 진짜 푸시(FCM)는
 * Firebase 프로젝트 연동과 백엔드의 발송 로직이 별도로 필요해 이번 범위 밖이다.
 */
@Singleton
class ChatMessagePoller
    @Inject
    constructor(
        private val notificationRepository: NotificationRepository,
        private val sessionRepository: SessionRepository,
        private val localChatNotifier: LocalChatNotifier
    ) {
        private val scope = CoroutineScope(SupervisorJob())
        private var pollingJob: Job? = null

        private val notifiedIds = mutableSetOf<String>()
        private var hasBaseline = false

        fun start() {
            if (pollingJob != null) return
            pollingJob =
                scope.launch {
                    sessionRepository.isLoggedIn.collectLatest { loggedIn ->
                        if (loggedIn) {
                            pollLoop()
                        } else {
                            notifiedIds.clear()
                            hasBaseline = false
                        }
                    }
                }
        }

        private suspend fun pollLoop() {
            while (true) {
                pollOnce()
                delay(POLL_INTERVAL_MS)
            }
        }

        private suspend fun pollOnce() {
            notificationRepository.getNotifications().onSuccess { notifications ->
                val unreadChat = notifications.filter { it.type == CHAT_NOTIFICATION_TYPE && !it.isRead }

                if (!hasBaseline) {
                    // 폴링을 처음 시작하는 시점에 이미 쌓여 있던 미읽음 알림까지 한꺼번에 띄우지 않도록
                    // 기준선만 세운다. 이후 새로 나타나는 것만 알림으로 띄운다.
                    notifiedIds += unreadChat.map { it.id }
                    hasBaseline = true
                    return@onSuccess
                }

                val activeRoomId = ActiveChatRoomTracker.activeRoomId.value?.toString()
                unreadChat
                    .filterNot { it.id in notifiedIds }
                    .filterNot { it.referenceId == activeRoomId }
                    .forEach { entry ->
                        notifiedIds += entry.id
                        localChatNotifier.notify(entry)
                    }
            }
        }
    }
