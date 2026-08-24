package com.example.pickii.data.remote

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private const val BASE_URL = "https://api.test/base/"
private const val FAKE_TOKEN = "super-secret-token-abc123xyz"

/**
 * [HttpClientFactory]의 `enableBodyLogging = true` 로깅이 실제로 헤더를 찍고, `Authorization`만
 * 마스킹하는지 검증한다.
 *
 * 이 테스트가 만들어진 이유: `LogLevel.BODY`는 바이트코드 기준 `headers=false`라서 헤더 자체를
 * 안 찍는다 — 그래서 `sanitizeHeader`가 한 번도 안 불려도 "헤더가 로그에 없다"는 겉보기 결과가
 * 똑같이 나왔다("마스킹됨"이 아니라 "안 찍힘"). `LogLevel.ALL`로 바꾼 뒤에는 실제로
 * `sanitizeHeader(placeholder, predicate)`의 `predicate`가 헤더 **값**이 아니라 헤더 **이름**을
 * 받는다는 것도 이 테스트를 만들다가 실측으로 알게 됐다 — `sanitizeHeader("Authorization") { true }`
 * 처럼 짜면 "Authorization"이 대체 텍스트로 쓰이고 predicate가 항상 true라 모든 헤더 값이
 * "Authorization"이라는 문자열로 뒤바뀌는(!) 사고가 났었다. 그래서 이 테스트는 단순히 "토큰이
 * 안 보인다"만이 아니라 "다른 헤더는 안 건드렸다"까지 같이 확인한다.
 */
class HttpClientFactoryLoggingTest {
    private fun MockRequestHandleScope.ok() =
        respond(
            content = ByteReadChannel("{}"),
            status = HttpStatusCode.OK,
            headers = headersOf("Content-Type", "application/json")
        )

    private fun buildClient(engine: MockEngine) =
        HttpClientFactory(
            engine = engine,
            baseUrl = BASE_URL,
            json = Json { ignoreUnknownKeys = true },
            authSession = StaticAuthSessionForLoggingTest,
            enableBodyLogging = true
        ).create()

    private suspend fun captureStdout(block: suspend () -> Unit): String {
        val originalOut = System.out
        val captured = java.io.ByteArrayOutputStream()
        System.setOut(java.io.PrintStream(captured))
        try {
            block()
        } finally {
            System.setOut(originalOut)
        }
        return captured.toString()
    }

    @Test
    fun `Authorization 헤더는 로그에서 마스킹되고 다른 헤더는 그대로 찍힌다`() =
        runTest {
            val engine = MockEngine { ok() }
            val client = buildClient(engine)

            val output = captureStdout { client.get("users/me/social-accounts") }

            assertFalse(output.contains(FAKE_TOKEN), "원본 토큰이 로그에 그대로 찍힘 — sanitizeHeader가 안 먹음")
            assertTrue(output.contains("Authorization"), "Authorization 헤더 라인 자체가 로그에 없음 — LogLevel이 헤더를 안 찍는 상태로 되돌아간 것 아닌지 확인 필요")
            assertTrue(
                output.contains("Content-Type") && output.contains("application/json"),
                "다른 헤더(Content-Type)까지 마스킹 텍스트로 뒤바뀜 — sanitizeHeader의 predicate가 헤더 이름이 아니라 " +
                    "다른 걸 보고 있을 가능성"
            )
        }

    @Test
    fun `auth 경로 요청은 여전히 로그에 아예 안 찍힌다`() =
        runTest {
            val engine = MockEngine { ok() }
            val client = buildClient(engine)

            val output = captureStdout { client.get("auth/login") }

            assertTrue(output.isBlank(), "auth/ 경로 요청이 로깅됨 — filter가 깨졌을 수 있음. 출력: $output")
        }
}

private object StaticAuthSessionForLoggingTest : AuthSession {
    override suspend fun currentAccessToken(): String? = FAKE_TOKEN

    override suspend fun currentRefreshToken(): String? = "refresh-$FAKE_TOKEN"

    override suspend fun deviceId(): String = "logging-test-device"

    override suspend fun onTokensRefreshed(
        accessToken: String,
        refreshToken: String
    ) = Unit

    override suspend fun onRefreshFailed() = Unit
}
