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
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.encodedPath
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

private const val REFRESH_ENDPOINT_PATH = "auth/token/refresh"

/** 요청/응답 바디에 비밀번호·인증코드 등이 그대로 들어있는 인증 관련 엔드포인트 — 로깅에서 제외한다. */
private const val AUTH_PATH_PREFIX = "auth/"

private val printlnLogger =
    object : Logger {
        override fun log(message: String) {
            println(message)
        }
    }

/**
 * Ktor `HttpClient`를 조립한다. 엔진을 생성자로 주입받아 프로덕션(OkHttp/Darwin)과 테스트(MockEngine)가
 * 완전히 같은 Auth 설정 코드를 타게 만든다 — 그래야 `markAsRefreshTokenRequest()` 같은 걸 실수로
 * 지워도 프로덕션 테스트가 진짜로 실패한다(`BearerAuthRefreshSpikeTest`는 라이브러리 자체 특성만
 * 검증해서 이 클래스가 바뀌어도 항상 통과한다).
 *
 * [enableBodyLogging] — 기존 OkHttp `HttpLoggingInterceptor.Level.BODY`가 `BuildConfig.DEBUG`로
 * 가드돼 있던 것과 동일한 역할. `shared`의 commonMain은 `BuildConfig`를 모르기 때문에(안드로이드
 * 전용) 호출부(app의 Koin 모듈)에서 `BuildConfig.DEBUG`를 그대로 넘겨받는다. 켜져 있어도
 * [AUTH_PATH_PREFIX]로 시작하는 요청(로그인/회원가입/비밀번호/토큰갱신 등)은 바디를 절대 찍지
 * 않고, `Authorization` 헤더는 항상 마스킹한다 — 기존 OkHttp 로깅은 이 두 가지를 전혀 하지
 * 않아서 디버그 빌드 로그캣에 비밀번호와 Bearer 토큰이 그대로 남았다(별도 이슈로 기록).
 */
class HttpClientFactory(
    private val engine: HttpClientEngine,
    private val baseUrl: String,
    private val json: Json,
    private val authSession: AuthSession,
    private val enableBodyLogging: Boolean = false
) {
    fun create(): HttpClient =
        HttpClient(engine) {
            install(ContentNegotiation) {
                json(json)
            }
            if (enableBodyLogging) {
                install(Logging) {
                    level = LogLevel.BODY
                    logger = printlnLogger
                    filter { request -> !request.url.encodedPath.contains(AUTH_PATH_PREFIX) }
                    sanitizeHeader("Authorization") { true }
                }
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
