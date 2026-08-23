package com.example.pickii.data.remote

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

private const val PROTECTED_PATH = "/protected"
private const val AUTH_REFRESH_PATH = "/auth/token/refresh"
private const val BASE_URL = "https://base"

/** 데드락이면 실제 시간 기준으로 이만큼만 기다리고 명확한 실패로 드러낸다. */
private const val REAL_TIMEOUT_MS = 3_000L

private class FakeAuthSession(
    initialAccessToken: String?,
    initialRefreshToken: String?
) : AuthSession {
    var accessToken: String? = initialAccessToken
    var refreshToken: String? = initialRefreshToken
    var onRefreshFailedCalls = 0
        private set
    var onTokensRefreshedCalls = 0
        private set

    override suspend fun currentAccessToken(): String? = accessToken

    override suspend fun currentRefreshToken(): String? = refreshToken

    override suspend fun deviceId(): String = "fake-device-id"

    override suspend fun onTokensRefreshed(
        accessToken: String,
        refreshToken: String
    ) {
        onTokensRefreshedCalls++
        this.accessToken = accessToken
        this.refreshToken = refreshToken
    }

    override suspend fun onRefreshFailed() {
        onRefreshFailedCalls++
        accessToken = null
        refreshToken = null
    }
}

/**
 * [HttpClientFactory]가 실제로 조립하는 [io.ktor.client.HttpClient]를 테스트한다 —
 * [BearerAuthRefreshSpikeTest]와 달리 이건 라이브러리 특성이 아니라 **우리 프로덕션 코드**를 검증한다.
 * `HttpClientFactory.create()`를 그대로 호출해서 만든 클라이언트를 쓰기 때문에, 누가
 * `HttpClientFactory.kt`의 `markAsRefreshTokenRequest()` 호출을 지워버리면 이 테스트가 실패(정확히는
 * 타임아웃으로 죽지 않고 실패)해야 한다. 그래서 데드락 가능성이 있는 케이스는 반드시 실제 시간
 * `withTimeout`으로 감싼다 — 안 그러면 CI가 그냥 멈춘다.
 */
class HttpClientFactoryAuthTest {
    private fun MockRequestHandleScope.jsonResponse(status: HttpStatusCode) =
        respond(
            content = ByteReadChannel("""{"data":{"accessToken":"new-access","refreshToken":"new-refresh"},"timestamp":"now"}"""),
            status = status,
            headers = headersOf("Content-Type", "application/json")
        )

    private fun MockRequestHandleScope.unauthorizedResponse() =
        respond(
            content = ByteReadChannel("{}"),
            status = HttpStatusCode.Unauthorized,
            headers = headersOf("Content-Type", "application/json")
        )

    @Test
    fun `HttpClientFactory로 만든 클라이언트는 401 후 refresh 성공하면 재시도해서 200을 받는다`() =
        runBlocking {
            var protectedCalls = 0
            var refreshCalls = 0

            val engine =
                MockEngine { request ->
                    when (request.url.encodedPath) {
                        PROTECTED_PATH -> {
                            protectedCalls++
                            if (protectedCalls == 1) unauthorizedResponse() else jsonResponse(HttpStatusCode.OK)
                        }
                        AUTH_REFRESH_PATH -> {
                            refreshCalls++
                            jsonResponse(HttpStatusCode.OK)
                        }
                        else -> error("unexpected path ${request.url.encodedPath}")
                    }
                }

            val authSession = FakeAuthSession(initialAccessToken = "old-access", initialRefreshToken = "old-refresh")
            val client =
                HttpClientFactory(
                    engine = engine,
                    baseUrl = BASE_URL,
                    json = Json { ignoreUnknownKeys = true },
                    authSession = authSession
                ).create()

            val result =
                try {
                    withTimeout(REAL_TIMEOUT_MS) { client.get("$BASE_URL$PROTECTED_PATH") }
                } catch (e: TimeoutCancellationException) {
                    fail("HttpClientFactory가 만든 클라이언트가 정상 갱신 경로에서도 hang됨 — markAsRefreshTokenRequest 등 Auth 설정 확인 필요")
                }

            assertEquals(HttpStatusCode.OK, result.status)
            assertEquals(2, protectedCalls)
            assertEquals(1, refreshCalls)
            assertEquals(1, authSession.onTokensRefreshedCalls)
            assertEquals("new-access", authSession.accessToken)
        }

    @Test
    fun `★ HttpClientFactory로 만든 클라이언트는 refresh가 401이어도 hang되지 않고 401을 반환한다`() =
        runBlocking {
            var protectedCalls = 0
            var refreshCalls = 0

            val engine =
                MockEngine { request ->
                    when (request.url.encodedPath) {
                        PROTECTED_PATH -> {
                            protectedCalls++
                            unauthorizedResponse()
                        }
                        AUTH_REFRESH_PATH -> {
                            refreshCalls++
                            // 리프레시 토큰 자체가 만료된 상황 재현.
                            unauthorizedResponse()
                        }
                        else -> error("unexpected path ${request.url.encodedPath}")
                    }
                }

            val authSession = FakeAuthSession(initialAccessToken = "old-access", initialRefreshToken = "expired-refresh")
            val client =
                HttpClientFactory(
                    engine = engine,
                    baseUrl = BASE_URL,
                    json = Json { ignoreUnknownKeys = true },
                    authSession = authSession
                ).create()

            // 이 테스트의 존재 이유: markAsRefreshTokenRequest()가 HttpClientFactory.kt에서
            // 지워지면 여기서 진짜로 hang된다 — CPU 스핀도 없이 그냥 멈춘다(BearerAuthRefreshSpikeTest에서
            // 실측 확인됨). withTimeout 없이 이 테스트를 짜면 CI가 통째로 멈추게 된다.
            val result =
                try {
                    withTimeout(REAL_TIMEOUT_MS) { client.get("$BASE_URL$PROTECTED_PATH") }
                } catch (e: TimeoutCancellationException) {
                    fail(
                        "실제 시간 ${REAL_TIMEOUT_MS}ms 안에 안 끝남 — HttpClientFactory의 refreshTokens{}에서 " +
                            "markAsRefreshTokenRequest() 호출이 빠졌을 가능성이 매우 높음. " +
                            "그 시점까지 protectedCalls=$protectedCalls, refreshCalls=$refreshCalls"
                    )
                }

            assertEquals(HttpStatusCode.Unauthorized, result.status, "리프레시 토큰 만료 시 401이 그대로 전파돼야 로그아웃 처리가 됨")
            assertTrue(refreshCalls in 1..2, "refresh 시도 횟수가 비정상적으로 많음: $refreshCalls 회")
            assertEquals(1, authSession.onRefreshFailedCalls, "갱신 실패 콜백이 정확히 한 번 불려야 세션이 정리됨")
        }
}
