package com.example.pickii.data.remote.socket

import com.example.pickii.BuildConfig
import com.example.pickii.data.local.TokenStore
import com.example.pickii.data.remote.dto.ApiError
import com.example.pickii.data.remote.dto.ChatMessageDto
import com.example.pickii.data.remote.dto.PublishChatMessage
import com.example.pickii.data.remote.dto.PublishChatRead
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import org.hildan.krossbow.stomp.StompClient
import org.hildan.krossbow.stomp.conversions.kxserialization.StompSessionWithKxSerialization
import org.hildan.krossbow.stomp.conversions.kxserialization.convertAndSend
import org.hildan.krossbow.stomp.conversions.kxserialization.json.withJsonConversions
import org.hildan.krossbow.stomp.conversions.kxserialization.subscribe
import org.hildan.krossbow.websocket.okhttp.OkHttpWebSocketClient
import javax.inject.Inject
import javax.inject.Singleton

enum class ChatConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    FAILED
}

/**
 * 채팅방 하나에 대한 STOMP 연결(구독/발행)을 관리한다. 현재 채팅 화면 구조상(ChatRoute) 한 번에 하나의
 * 채팅방만 열려 있으므로 동시에 하나의 세션만 유지한다고 가정한다. 방을 옮길 때는 [connect]를 다시 호출하면
 * 기존 세션을 정리하고 새로 연결한다.
 *
 * SockJS 주의: 문서상 엔드포인트는 "ws(s)://{host}/api/v1/ws (SockJS)"이지만 Krossbow는 원시
 * STOMP-over-WebSocket만 지원하고 SockJS 핸드셰이크 프로토콜은 구현하지 않는다. 우선 원시 웹소켓으로
 * `{baseUrl}/ws`에 직접 접속을 시도한다. 만약 실제 백엔드가 이 경로에서 원시 업그레이드를 거부한다면(SockJS
 * 핸드셰이크를 강제하는 경우) Spring STOMP+SockJS의 관례적 폴백 경로인 `{baseUrl}/ws/websocket`으로 바꿔야
 * 한다 — 이는 실제 서버로 실증 확인이 필요하다(코드로 자동 폴백하지 않고, 확인 후 아래 [webSocketUrl]을 고친다).
 */
@Singleton
class ChatStompClient
    @Inject
    constructor(
        okHttpClient: OkHttpClient,
        private val tokenStore: TokenStore,
        private val json: Json
    ) {
        private val scope = CoroutineScope(SupervisorJob())
        private val stompClient = StompClient(OkHttpWebSocketClient(okHttpClient))

        private var session: StompSessionWithKxSerialization? = null
        private var subscriptionJob: Job? = null

        private val _connectionState = MutableStateFlow(ChatConnectionState.DISCONNECTED)
        val connectionState: StateFlow<ChatConnectionState> = _connectionState

        private val _incomingMessages = MutableSharedFlow<ChatMessageDto>(extraBufferCapacity = 64)
        val incomingMessages: SharedFlow<ChatMessageDto> = _incomingMessages

        private val _incomingErrors = MutableSharedFlow<ApiError>(extraBufferCapacity = 16)
        val incomingErrors: SharedFlow<ApiError> = _incomingErrors

        /** [roomId] 방에 연결하고 `/sub/chatrooms/{roomId}`, `/user/queue/errors`를 구독한다. */
        suspend fun connect(roomId: Long) {
            disconnect()
            _connectionState.value = ChatConnectionState.CONNECTING

            val accessToken = tokenStore.accessTokenFlow.first()
            val connectHeaders = accessToken?.let { mapOf("Authorization" to "Bearer $it") }.orEmpty()

            runCatching {
                stompClient
                    .connect(url = webSocketUrl(), customStompConnectHeaders = connectHeaders)
                    .withJsonConversions(json)
            }.onSuccess { newSession ->
                session = newSession
                _connectionState.value = ChatConnectionState.CONNECTED
                subscriptionJob =
                    scope.launch {
                        launch {
                            newSession
                                .subscribe("/sub/chatrooms/$roomId", ChatMessageDto.serializer())
                                .collect { _incomingMessages.emit(it) }
                        }
                        launch {
                            newSession
                                .subscribe("/user/queue/errors", ApiError.serializer())
                                .collect { _incomingErrors.emit(it) }
                        }
                    }
            }.onFailure {
                _connectionState.value = ChatConnectionState.FAILED
            }
        }

        /** `/pub/chatrooms/{roomId}/messages`로 메시지를 발행한다. 연결돼 있지 않으면 조용히 무시한다. */
        suspend fun sendMessage(
            roomId: Long,
            body: PublishChatMessage
        ) {
            session?.convertAndSend("/pub/chatrooms/$roomId/messages", body, PublishChatMessage.serializer())
        }

        /** `/pub/chatrooms/{roomId}/read`로 읽음 처리를 발행한다. */
        suspend fun sendRead(
            roomId: Long,
            lastReadMessageId: String
        ) {
            session?.convertAndSend(
                "/pub/chatrooms/$roomId/read",
                PublishChatRead(lastReadMessageId),
                PublishChatRead.serializer()
            )
        }

        suspend fun disconnect() {
            subscriptionJob?.cancel()
            subscriptionJob = null
            runCatching { session?.disconnect() }
            session = null
            _connectionState.value = ChatConnectionState.DISCONNECTED
        }

        /** [ViewModel.onCleared]처럼 suspend 함수를 호출할 수 없는 곳에서 쓰는 fire-and-forget 해제. */
        fun disconnectAsync() {
            scope.launch { disconnect() }
        }

        private fun webSocketUrl(): String {
            val httpBaseUrl = BuildConfig.API_BASE_URL.trimEnd('/')
            val webSocketBaseUrl = httpBaseUrl.replaceFirst("http", "ws")
            return "$webSocketBaseUrl/ws"
        }
    }
