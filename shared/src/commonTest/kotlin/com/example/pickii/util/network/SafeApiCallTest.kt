package com.example.pickii.util.network

import com.example.pickii.data.remote.dto.ApiException
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

@Serializable
private data class SamplePayload(
    val value: String
)

class SafeApiCallTest {
    private fun buildClient(engine: MockEngine) =
        HttpClient(engine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

    @Test
    fun `성공 응답이면 바디를 그대로 담은 Result_success를 돌려준다`() =
        runTest {
            val engine =
                MockEngine {
                    respond(
                        content = ByteReadChannel("""{"value":"hello"}"""),
                        status = HttpStatusCode.OK,
                        headers = headersOf("Content-Type", "application/json")
                    )
                }
            val client = buildClient(engine)

            val result = safeApiCall<SamplePayload> { client.get("/x") }

            assertEquals(Result.success(SamplePayload("hello")), result)
        }

    @Test
    fun `실패 응답이면 에러 바디를 파싱해서 ApiException으로 감싼다`() =
        runTest {
            val engine =
                MockEngine {
                    respond(
                        content = """{"error":{"code":"NOT_FOUND","message":"없음"},"timestamp":"now"}""",
                        status = HttpStatusCode.NotFound,
                        headers = headersOf("Content-Type", "application/json")
                    )
                }
            val client = buildClient(engine)

            val result = safeApiCall<SamplePayload> { client.get("/x") }

            val exception = result.exceptionOrNull()
            assertIs<ApiException>(exception)
            assertEquals("NOT_FOUND", exception.code)
            assertEquals("없음", exception.message)
        }

    @Test
    fun `에러 바디가 파싱 안 되는 형태면 UNKNOWN_ERROR로 폴백한다`() =
        runTest {
            val engine =
                MockEngine {
                    respond(
                        content = "이건 JSON이 아님",
                        status = HttpStatusCode.InternalServerError,
                        headers = headersOf("Content-Type", "text/plain")
                    )
                }
            val client = buildClient(engine)

            val result = safeApiCall<SamplePayload> { client.get("/x") }

            val exception = result.exceptionOrNull()
            assertIs<ApiException>(exception)
            assertEquals("UNKNOWN_ERROR", exception.code)
            assertTrue(exception.message!!.contains("500"))
        }

    @Test
    fun `safeApiCallUnit은 성공이면 Unit을 실패면 ApiException을 담는다`() =
        runTest {
            val okEngine = MockEngine { respond(content = "", status = HttpStatusCode.NoContent) }
            val okClient = buildClient(okEngine)
            assertEquals(Result.success(Unit), safeApiCallUnit { okClient.get("/x") })

            val failEngine =
                MockEngine {
                    respond(
                        content = """{"error":{"code":"FORBIDDEN","message":"권한 없음"},"timestamp":"now"}""",
                        status = HttpStatusCode.Forbidden,
                        headers = headersOf("Content-Type", "application/json")
                    )
                }
            val failClient = buildClient(failEngine)
            val exception = safeApiCallUnit { failClient.get("/x") }.exceptionOrNull()
            assertIs<ApiException>(exception)
            assertEquals("FORBIDDEN", exception.code)
        }
}
