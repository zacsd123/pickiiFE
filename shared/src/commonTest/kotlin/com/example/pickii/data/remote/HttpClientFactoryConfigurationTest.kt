package com.example.pickii.data.remote

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private const val BASE_URL = "https://api.test/base/"

@Serializable
private data class SampleRequestBody(
    val value: String
)

/**
 * [HttpClientFactory]가 만드는 클라이언트의 기본 설정(baseUrl 병합, 기본 Content-Type) 자체를
 * 검증한다 — 개별 기능(Auth 등)이 아니라 "이 두 설정이 실제로 적용되는가"만 본다.
 *
 * 이 테스트가 존재하는 이유: `defaultRequest { url(baseUrl) }`가 커밋을 나누는 과정에서 통째로
 * 빠진 적이 있다. MockEngine 기반 다른 테스트들은 전부 절대 URL을 직접 넣어서 호출했기 때문에
 * 아무도 이 누락을 못 잡았고, 실제 백엔드에 붙는 통합 테스트에서야 발견됐다(요청이
 * `http://localhost/...`로 나감). MockEngine으로도 `request.url`을 그대로 캡처할 수 있어서
 * 이런 설정 누락은 충분히 잡을 수 있다 — 이 테스트가 그 증거다.
 */
class HttpClientFactoryConfigurationTest {
    private fun MockRequestHandleScope.ok() =
        respond(
            content = ByteReadChannel("{}"),
            status = HttpStatusCode.OK,
            headers = headersOf("Content-Type", "application/json")
        )

    @Test
    fun `상대 경로가 baseUrl과 합쳐진다`() =
        runTest {
            var capturedUrl: String? = null
            val engine =
                MockEngine { request ->
                    capturedUrl = request.url.toString()
                    ok()
                }
            val client =
                HttpClientFactory(
                    engine = engine,
                    baseUrl = BASE_URL,
                    json = Json { ignoreUnknownKeys = true },
                    authSession = NoOpAuthSessionForConfigTest
                ).create()

            client.get("auth/login")

            assertEquals(
                "https://api.test/base/auth/login",
                capturedUrl,
                "상대 경로가 baseUrl과 안 합쳐짐 — defaultRequest { url(baseUrl) } 누락 의심"
            )
        }

    @Test
    fun `바디 있는 요청에 개별 contentType 호출 없이도 기본 Content-Type이 적용된다`() =
        runTest {
            var capturedContentType: String? = null
            val engine =
                MockEngine { request ->
                    capturedContentType = request.body.contentType?.toString()
                    ok()
                }
            val client =
                HttpClientFactory(
                    engine = engine,
                    baseUrl = BASE_URL,
                    json = Json { ignoreUnknownKeys = true },
                    authSession = NoOpAuthSessionForConfigTest
                ).create()

            // 호출부에서 일부러 contentType()을 안 부름 — KtorAuthApiService의 모든 메서드가
            // 실제로 이렇게 생겼다.
            client.post("auth/login") { setBody(SampleRequestBody("x")) }

            assertTrue(
                capturedContentType?.contains("application/json") == true,
                "기본 Content-Type이 적용 안 됨: $capturedContentType"
            )
        }

    @Test
    fun `바디 없는 GET에 기본 Content-Type이 실려도 요청은 정상 처리된다`() =
        runTest {
            val engine = MockEngine { ok() }
            val client =
                HttpClientFactory(
                    engine = engine,
                    baseUrl = BASE_URL,
                    json = Json { ignoreUnknownKeys = true },
                    authSession = NoOpAuthSessionForConfigTest
                ).create()

            val response = client.get("users/me/social-accounts")

            assertEquals(HttpStatusCode.OK, response.status)
        }

    @Test
    fun `멀티파트 바디는 기본 Content-Type에 덮이지 않는다`() =
        runTest {
            var capturedContentType: String? = null
            val engine =
                MockEngine { request ->
                    capturedContentType = request.body.contentType?.toString()
                    ok()
                }
            val client =
                HttpClientFactory(
                    engine = engine,
                    baseUrl = BASE_URL,
                    json = Json { ignoreUnknownKeys = true },
                    authSession = NoOpAuthSessionForConfigTest
                ).create()

            client.post("chatrooms/1/images") {
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append("image", ByteArray(0))
                        }
                    )
                )
            }

            assertTrue(
                capturedContentType?.contains("multipart/form-data") == true,
                "멀티파트인데 기본 Content-Type(application/json)에 덮임 — 나중에 이미지 업로드 전환 시 문제될 수 있음: $capturedContentType"
            )
        }
}

private object NoOpAuthSessionForConfigTest : AuthSession {
    override suspend fun currentAccessToken(): String? = null

    override suspend fun currentRefreshToken(): String? = null

    override suspend fun deviceId(): String = "config-test-device"

    override suspend fun onTokensRefreshed(
        accessToken: String,
        refreshToken: String
    ) = Unit

    override suspend fun onRefreshFailed() = Unit
}
