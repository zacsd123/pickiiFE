package com.example.pickii.data.notification

import com.example.pickii.domain.repository.NotificationRepository
import com.example.pickii.domain.repository.SessionRepository
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await

/**
 * 로그인 상태가 될 때(최초 로그인, 자동 로그인으로 앱 재실행 등) 이 기기의 FCM 토큰을 서버에
 * 등록한다(9-8). 토큰 자체가 로그인 중에 갱신되는 경우는 [onNewToken]으로 처리한다.
 *
 * 로그아웃 시 토큰 삭제(9-9)는 여기서 반응형으로 하지 않는다 — Access Token이 이미 지워진
 * 뒤라 요청이 401로 거부되기 때문에, [com.example.pickii.data.repository.RecruitAuthSessionRepository.logout]이
 * 세션을 지우기 전에 직접 호출한다. 여기서는 다음 로그인 때 재등록되도록 캐시만 비운다.
 *
 * 앱을 처음 설치한 직후에는 [start]가 부르는 `FirebaseMessaging.getInstance().token`이 토큰을 새로
 * 발급받는데, 이 발급 자체가 [onNewToken] 콜백도 함께 트리거한다. 같은 토큰이 두 경로로 동시에 등록
 * 요청되는 걸 막기 위해 [mutex]로 직렬화하고 마지막으로 등록한 토큰과 같으면 건너뛴다.
 *
 * Koin 싱글턴(`di/InfraModule.kt`).
 */
class FcmTokenRegistrar(
    private val notificationRepository: NotificationRepository,
    private val sessionRepository: SessionRepository
) {
    private val scope = CoroutineScope(SupervisorJob())
    private val mutex = Mutex()
    private var job: Job? = null
    private var lastRegisteredToken: String? = null

    fun start() {
        if (job != null) return
        job =
            scope.launch {
                sessionRepository.isLoggedIn.collectLatest { loggedIn ->
                    if (loggedIn) registerCurrentToken() else mutex.withLock { lastRegisteredToken = null }
                }
            }
    }

    /** [com.google.firebase.messaging.FirebaseMessagingService.onNewToken]에서 호출한다. */
    fun onNewToken(token: String) {
        if (!sessionRepository.isLoggedIn.value) return
        scope.launch { registerToken(token) }
    }

    private suspend fun registerCurrentToken() {
        val token = runCatching { FirebaseMessaging.getInstance().token.await() }.getOrNull() ?: return
        registerToken(token)
    }

    private suspend fun registerToken(token: String) =
        mutex.withLock {
            if (token == lastRegisteredToken) return@withLock
            notificationRepository.registerDevice(token).onSuccess { lastRegisteredToken = token }
        }
}
