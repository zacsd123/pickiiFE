package com.example.pickii.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

/** 데드락이면 실제 시간 기준으로 이만큼만 기다리고 명확한 실패로 드러낸다. */
private const val REAL_TIMEOUT_MS = 3_000L

private const val PROTECTED_PATH = "/protected"
private const val REFRESH_PATH = "/auth/refresh"

/** 무한루프면 실제로 이 횟수를 넘겨서 명확한 실패로 드러내기 위한 안전장치(하드 서킷브레이커). */
private const val CALL_COUNT_GUARD = 500

/**
 * Ktor 3.3.3의 `Auth { bearer { } }`가 Hilt→Koin 전환 때 `TokenAuthenticator`에서 실측했던 것과
 * 동일한 질문(순환/무한루프/재시도 상한/캐싱)에 대해 실제로 어떻게 동작하는지 MockEngine으로
 * 결정적으로 재현한다. 문서가 아니라 설치된 실물(3.3.3) 바이트코드 + 실행 결과 기준.
 *
 * 무한루프 가능성이 있는 시나리오(★)는 `kotlinx.coroutines.test.runTest`의 가상 시간 대신
 * `runBlocking`(실제 시간)을 쓰고, MockEngine 응답기 안에 하드 호출 횟수 가드([CALL_COUNT_GUARD])를
 * 심어서 "진짜 무한루프면 빠르고 명확하게 실패"하도록 만들었다 — `withTimeout` + 가상 시간 조합은
 * 가상 시간이 순식간에 다 소진돼버려서 실제로 몇 번 돌았는지 알려주지 못했다(1차 시도에서 확인).
 *
 * 이 클래스는 스파이크가 아니라 영구 회귀 테스트다 — 나중에 누가 Auth 설정(특히
 * `markAsRefreshTokenRequest()` 호출)을 건드렸을 때 무한 루프가 재발하면 여기서 잡혀야 한다.
 */
class BearerAuthRefreshSpikeTest {
    private fun MockRequestHandleScope.jsonResponse(status: HttpStatusCode) =
        respond(
            content = ByteReadChannel("{}"),
            status = status,
            headers = headersOf("Content-Type", "application/json")
        )

    private fun buildClient(
        engine: MockEngine,
        refreshTokens: suspend io.ktor.client.plugins.auth.providers.RefreshTokensParams.() -> BearerTokens?,
        loadTokensCounter: IntArray
    ) = HttpClient(engine) {
        install(Auth) {
            bearer {
                loadTokens {
                    loadTokensCounter[0]++
                    BearerTokens("initial-access", "initial-refresh")
                }
                refreshTokens { refreshTokens() }
            }
        }
    }

    // ── 시나리오 1: 정상 갱신 — 401 → refresh 200 → 재시도 200 ──────────────────────

    @Test
    fun `401 후 refresh 200이면 재시도해서 최종 200을 받는다`() =
        runTest {
            var protectedCalls = 0
            var refreshCalls = 0
            val loadTokensCalls = IntArray(1)

            val engine =
                MockEngine { request ->
                    when (request.url.encodedPath) {
                        PROTECTED_PATH -> {
                            protectedCalls++
                            if (protectedCalls == 1) jsonResponse(HttpStatusCode.Unauthorized) else jsonResponse(HttpStatusCode.OK)
                        }
                        REFRESH_PATH -> {
                            refreshCalls++
                            jsonResponse(HttpStatusCode.OK)
                        }
                        else -> error("unexpected path ${request.url.encodedPath}")
                    }
                }

            val client =
                buildClient(
                    engine = engine,
                    refreshTokens = {
                        val response = client.post(REFRESH_PATH) { markAsRefreshTokenRequest() }
                        if (response.status == HttpStatusCode.OK) BearerTokens("new-access", "new-refresh") else null
                    },
                    loadTokensCounter = loadTokensCalls
                )

            val result = client.get(PROTECTED_PATH)

            assertEquals(HttpStatusCode.OK, result.status, "401 -> refresh 200 -> 재시도까지 갔는데 최종 상태가 200이 아님")
            assertEquals(2, protectedCalls, "protected 엔드포인트가 정확히 두 번(최초 401 + 재시도 200) 불려야 함")
            assertEquals(1, refreshCalls, "refresh는 정확히 한 번만 불려야 함")
        }

    // ── 시나리오 2 (★): refresh 엔드포인트 자체가 401 → 무한 루프 확인 ──────────────────
    // markAsRefreshTokenRequest()를 "제대로" 호출한 경우.

    @Test
    fun `refresh 엔드포인트가 401을 줘도 markAsRefreshTokenRequest를 호출하면 무한루프에 빠지지 않는다`() =
        runBlocking {
            var protectedCalls = 0
            var refreshCalls = 0
            val loadTokensCalls = IntArray(1)

            val engine =
                MockEngine { request ->
                    val total = protectedCalls + refreshCalls
                    if (total >= CALL_COUNT_GUARD) {
                        fail(
                            "호출 횟수가 $CALL_COUNT_GUARD 회를 넘음 — 무한루프 확정. " +
                                "protectedCalls=$protectedCalls, refreshCalls=$refreshCalls"
                        )
                    }
                    when (request.url.encodedPath) {
                        PROTECTED_PATH -> {
                            protectedCalls++
                            jsonResponse(HttpStatusCode.Unauthorized)
                        }
                        REFRESH_PATH -> {
                            refreshCalls++
                            // 리프레시 토큰 자체가 만료된 상황 재현 — refresh 엔드포인트도 401.
                            jsonResponse(HttpStatusCode.Unauthorized)
                        }
                        else -> error("unexpected path ${request.url.encodedPath}")
                    }
                }

            val client =
                buildClient(
                    engine = engine,
                    refreshTokens = {
                        val response = client.post(REFRESH_PATH) { markAsRefreshTokenRequest() }
                        if (response.status == HttpStatusCode.OK) BearerTokens("new-access", "new-refresh") else null
                    },
                    loadTokensCounter = loadTokensCalls
                )

            val result =
                try {
                    withTimeout(REAL_TIMEOUT_MS) { client.get(PROTECTED_PATH) }
                } catch (e: TimeoutCancellationException) {
                    fail(
                        "실제 시간 ${REAL_TIMEOUT_MS}ms 안에 안 끝남 — 데드락 확정(CPU 스핀이 아니라 뭔가에 " +
                            "block된 채 멈춤). 그 시점까지 protectedCalls=$protectedCalls, refreshCalls=$refreshCalls"
                    )
                }

            println(
                "[refresh 401 + mark 있음] 실측: protectedCalls=$protectedCalls, refreshCalls=$refreshCalls, " +
                    "최종 status=${result.status}"
            )
            assertEquals(HttpStatusCode.Unauthorized, result.status, "리프레시 토큰이 만료됐으면 최종적으로 401이 그대로 전파돼야 함(로그아웃 트리거)")
            assertTrue(refreshCalls in 1..2, "refresh 시도 횟수가 비정상적으로 많음(무한루프 의심): $refreshCalls 회")
            assertTrue(protectedCalls in 1..2, "protected 재시도 횟수가 비정상적으로 많음(무한루프 의심): $protectedCalls 회")
        }

    // ── 시나리오 2-보조 (대조군): markAsRefreshTokenRequest()를 "빼먹으면" 어떻게 되는가 ──
    // 실서비스에 넣을 코드가 아니라, 가드를 빼먹었을 때의 실제 위험도를 보여주기 위한 대조군.

    @Test
    fun `markAsRefreshTokenRequest를 빼먹으면 refresh 요청 자체도 Auth 파이프라인에 다시 걸려 무한루프가 된다`() =
        runBlocking {
            var protectedCalls = 0
            var refreshCalls = 0
            val loadTokensCalls = IntArray(1)
            var guardTripped = false

            val engine =
                MockEngine { request ->
                    val total = protectedCalls + refreshCalls
                    if (total >= CALL_COUNT_GUARD) {
                        guardTripped = true
                        fail(
                            "호출 횟수가 $CALL_COUNT_GUARD 회를 넘음 — 마크를 빼먹으면 무한루프라는 게 확정됨. " +
                                "protectedCalls=$protectedCalls, refreshCalls=$refreshCalls"
                        )
                    }
                    when (request.url.encodedPath) {
                        PROTECTED_PATH -> {
                            protectedCalls++
                            jsonResponse(HttpStatusCode.Unauthorized)
                        }
                        REFRESH_PATH -> {
                            refreshCalls++
                            jsonResponse(HttpStatusCode.Unauthorized)
                        }
                        else -> error("unexpected path ${request.url.encodedPath}")
                    }
                }

            val client =
                buildClient(
                    engine = engine,
                    refreshTokens = {
                        // 의도적으로 markAsRefreshTokenRequest() 호출을 뺐다.
                        val response = client.post(REFRESH_PATH)
                        if (response.status == HttpStatusCode.OK) BearerTokens("new-access", "new-refresh") else null
                    },
                    loadTokensCounter = loadTokensCalls
                )

            try {
                val result =
                    withTimeout(REAL_TIMEOUT_MS) { client.get(PROTECTED_PATH) }
                println(
                    "[대조군: 마크 없음] 무한루프 안 걸리고 종료됨 — protectedCalls=$protectedCalls, " +
                        "refreshCalls=$refreshCalls, status=${result.status}"
                )
            } catch (e: AssertionError) {
                if (!guardTripped) throw e
                println("[대조군: 마크 없음] 하드 가드($CALL_COUNT_GUARD 회)에 걸림(빠른 무한루프) — 마크를 빼먹으면 실제로 위험함을 확인")
            } catch (e: TimeoutCancellationException) {
                println(
                    "[대조군: 마크 없음] 데드락으로 확정(실제 ${REAL_TIMEOUT_MS}ms 동안 진행 없음) — " +
                        "그 시점까지 protectedCalls=$protectedCalls, refreshCalls=$refreshCalls. " +
                        "마크를 빼먹으면 위험함을 확인"
                )
            }
        }

    // ── 시나리오 3: 재시도 횟수 상한 실측 — protected가 갱신 후에도 계속 401 ──────────────

    @Test
    fun `refresh는 성공하는데 protected가 계속 401이면 재시도 횟수에 상한이 있다`() =
        runBlocking {
            var protectedCalls = 0
            var refreshCalls = 0
            val loadTokensCalls = IntArray(1)

            val engine =
                MockEngine { request ->
                    val total = protectedCalls + refreshCalls
                    if (total >= CALL_COUNT_GUARD) {
                        fail(
                            "호출 횟수가 $CALL_COUNT_GUARD 회를 넘음 — refresh가 매번 성공해도 무한 재시도됨. " +
                                "protectedCalls=$protectedCalls, refreshCalls=$refreshCalls"
                        )
                    }
                    when (request.url.encodedPath) {
                        PROTECTED_PATH -> {
                            protectedCalls++
                            jsonResponse(HttpStatusCode.Unauthorized) // 새 토큰으로도 계속 401
                        }
                        REFRESH_PATH -> {
                            refreshCalls++
                            jsonResponse(HttpStatusCode.OK) // refresh 자체는 매번 "성공"
                        }
                        else -> error("unexpected path ${request.url.encodedPath}")
                    }
                }

            val client =
                buildClient(
                    engine = engine,
                    refreshTokens = {
                        val response = client.post(REFRESH_PATH) { markAsRefreshTokenRequest() }
                        refreshCalls.let {
                            if (response.status == HttpStatusCode.OK) BearerTokens("new-access-$it", "new-refresh-$it") else null
                        }
                    },
                    loadTokensCounter = loadTokensCalls
                )

            val result =
                try {
                    withTimeout(REAL_TIMEOUT_MS) { client.get(PROTECTED_PATH) }
                } catch (e: TimeoutCancellationException) {
                    fail(
                        "실제 시간 ${REAL_TIMEOUT_MS}ms 안에 안 끝남 — 데드락 확정. 그 시점까지 " +
                            "protectedCalls=$protectedCalls, refreshCalls=$refreshCalls"
                    )
                }

            println(
                "[refresh 성공, protected 계속 401] 실측 재시도 상한: protectedCalls=$protectedCalls, " +
                    "refreshCalls=$refreshCalls, 최종 status=${result.status}"
            )
            assertEquals(HttpStatusCode.Unauthorized, result.status, "결국 401로 끝나야 함(무한 재시도 X)")
            assertTrue(protectedCalls <= 5, "protected 재시도 상한 실측값 초과 의심 — 실제 관측치: $protectedCalls 회")
            assertTrue(refreshCalls <= 5, "refresh 재시도 상한 실측값 초과 의심 — 실제 관측치: $refreshCalls 회")
        }

    // ── 시나리오 4: loadTokens가 매 요청마다 불리는지, 캐시되는지 ─────────────────────

    @Test
    fun `loadTokens는 401이 한 번도 없으면 요청마다 다시 불리지 않고 캐시된다`() =
        runTest {
            val loadTokensCalls = IntArray(1)

            val engine =
                MockEngine { request ->
                    when (request.url.encodedPath) {
                        PROTECTED_PATH -> jsonResponse(HttpStatusCode.OK)
                        else -> error("unexpected path ${request.url.encodedPath}")
                    }
                }

            val client =
                buildClient(
                    engine = engine,
                    refreshTokens = { null },
                    loadTokensCounter = loadTokensCalls
                )

            client.get(PROTECTED_PATH)
            client.get(PROTECTED_PATH)
            client.get(PROTECTED_PATH)

            assertEquals(1, loadTokensCalls[0], "loadTokens가 요청마다 다시 불리면 매번 DataStore를 읽게 돼 성능 문제")
        }
}
