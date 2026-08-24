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
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
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
 * [AUTH_PATH_PREFIX]로 시작하는 요청(로그인/회원가입/비밀번호/토큰갱신 등)은 **요청 전체**(바디+헤더)를
 * 로깅에서 제외하고, 그 외 요청의 `Authorization` 헤더는 항상 마스킹한다 — 기존 OkHttp 로깅은 이
 * 두 가지를 전혀 하지 않아서 디버그 빌드 로그캣에 비밀번호와 Bearer 토큰이 그대로 남았다(별도
 * 이슈로 기록).
 *
 * `LogLevel.ALL`을 쓴다(`BODY`가 아니라) — `LogLevel.BODY`는 바이트코드로 확인해보면
 * `headers=false`라서 헤더를 아예 안 찍는다. 그러면 `sanitizeHeader("Authorization")`가 한 번도
 * 실행될 일이 없는 죽은 설정이 되고("헤더가 마스킹됐다"가 아니라 "헤더가 안 찍혔다"), 90개
 * 엔드포인트를 옮기면서 Content-Type/인증 헤더 유무 같은 걸 디버그 로그로 확인할 수도 없다.
 * `ALL`로 올리는 대신 [AUTH_PATH_PREFIX] `filter`가 더 중요해진다 — 헤더까지 찍히는 만큼, 인증
 * 관련 요청을 통째로 빼는 역할이 커진다.
 *
 * `defaultRequest { }`에 `contentType(ContentType.Application.Json)`을 기본값으로 깔아둔다 —
 * 개별 `Ktor*ApiService` 메서드가 바디를 넣으면서 `contentType()`을 빠뜨려도(실제로 이 사고가
 * 났었다 — `KtorAuthApiService`의 모든 바디 있는 메서드가 처음엔 이걸 빠뜨렸다) 여전히
 * `ContentNegotiation`이 직렬화 컨버터를 찾을 수 있다. 앞으로 90개 엔드포인트를 옮기면서 매번
 * 사람이 기억해야 하는 구조로 두면 반드시 또 빠뜨리기 때문에 구조적으로 막았다. 바디 없는
 * GET에도 이 헤더가 실리지만 서버는 통상 무시하고([HttpClientFactoryConfigurationTest]로 실측),
 * `MultiPartFormDataContent` 같은 멀티파트 바디는 자기 자신의 Content-Type(예:
 * `multipart/form-data; boundary=...`)으로 이 기본값을 덮어써서 나중에 이미지 업로드를 옮길 때도
 * 문제가 없다(마찬가지로 실측 확인됨).
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
            defaultRequest {
                url(baseUrl)
                contentType(ContentType.Application.Json)
            }
            install(ContentNegotiation) {
                json(json)
            }
            if (enableBodyLogging) {
                install(Logging) {
                    level = LogLevel.ALL
                    logger = printlnLogger
                    filter { request -> !request.url.encodedPath.contains(AUTH_PATH_PREFIX) }
                    // ⚠️ sanitizeHeader(placeholder, predicate) — 첫 인자는 마스킹 후 대체할 텍스트,
                    // predicate는 "헤더 값"이 아니라 "헤더 이름"을 받는다. sanitizeHeader("Authorization")
                    // { true }처럼 잘못 쓰면 첫 인자가 대체 텍스트로 쓰이고 predicate가 항상 true라
                    // 모든 헤더 값이 전부 "Authorization"이라는 문자열로 바뀌어버린다(실측 확인 — 처음엔
                    // 이렇게 잘못 짰었다). 아래처럼 헤더 이름을 predicate에서 비교해야 한다.
                    sanitizeHeader("[REDACTED]") { headerName -> headerName.equals("Authorization", ignoreCase = true) }
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
                            client.post(REFRESH_ENDPOINT_PATH) {
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
