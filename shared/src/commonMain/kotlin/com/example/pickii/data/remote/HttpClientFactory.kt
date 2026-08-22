package com.example.pickii.data.remote

import com.example.pickii.data.remote.dto.ApiEnvelope
import com.example.pickii.data.remote.dto.TokenRefreshRequest
import com.example.pickii.data.remote.dto.TokenRefreshResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

private const val REFRESH_ENDPOINT_PATH = "auth/token/refresh"

/**
 * Ktor `HttpClient`를 조립한다. 엔진을 생성자로 주입받아 프로덕션(OkHttp/Darwin)과 테스트(MockEngine)가
 * 완전히 같은 Auth 설정 코드를 타게 만든다 — 그래야 `markAsRefreshTokenRequest()` 같은 걸 실수로
 * 지워도 프로덕션 테스트가 진짜로 실패한다(`BearerAuthRefreshSpikeTest`는 라이브러리 자체 특성만
 * 검증해서 이 클래스가 바뀌어도 항상 통과한다).
 */
class HttpClientFactory(
    private val engine: HttpClientEngine,
    private val baseUrl: String,
    private val json: Json,
    private val authSession: AuthSession
) {
    fun create(): HttpClient =
        HttpClient(engine) {
            install(ContentNegotiation) {
                json(json)
            }
            install(Auth) {
                bearer {
                    loadTokens {
                        val accessToken = authSession.currentAccessToken() ?: return@loadTokens null
                        BearerTokens(accessToken, authSession.currentRefreshToken().orEmpty())
                    }
                    refreshTokens {
                        val refreshToken = authSession.currentRefreshToken()
                        if (refreshToken.isNullOrBlank()) {
                            authSession.onRefreshFailed()
                            return@refreshTokens null
                        }

                        val response =
                            client.post("$baseUrl/$REFRESH_ENDPOINT_PATH") {
                                // ⚠️ 순환/무한루프 주의 — 이 markAsRefreshTokenRequest() 호출을 절대
                                // 빼지 말 것: 이 요청은 방금 위에서 쓴 `client`(=이 HttpClient 자기 자신)로
                                // 나가는데, 마크가 없으면 이 refresh 요청 자체도 같은 Auth 파이프라인을
                                // 다시 타게 된다. 리프레시 토큰이 만료돼서 이 refresh 엔드포인트가 401을
                                // 주는 순간 — 무한루프가 아니라 완전한 hang이 된다(CPU도 안 쓰고 그냥
                                // 멈춤, StackOverflowError처럼 로그도 안 남는다). 실측:
                                // BearerAuthRefreshSpikeTest의 "markAsRefreshTokenRequest를 빼먹으면..."
                                // 테스트 참고. Hilt→Koin 전환 때 순환 의존성을 lazy{}로 끊었던 것과
                                // 같은 종류의 "안 보이지만 지우면 조용히 죽는" 지점이다.
                                markAsRefreshTokenRequest()
                                contentType(ContentType.Application.Json)
                                setBody(TokenRefreshRequest(authSession.deviceId(), refreshToken))
                            }

                        if (response.status.isSuccess()) {
                            val body = response.body<ApiEnvelope<TokenRefreshResponseDto>>()
                            authSession.onTokensRefreshed(body.data.accessToken, body.data.refreshToken)
                            BearerTokens(body.data.accessToken, body.data.refreshToken)
                        } else {
                            authSession.onRefreshFailed()
                            null
                        }
                    }
                }
            }
        }
}
