package com.example.pickii.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import kotlinx.serialization.json.Json
import java.io.File
import java.util.Properties

/**
 * 실제 백엔드(Railway 프로덕션)에 붙는 수동 통합 테스트(`*BackendIntegrationTest`) 전용 공용 헬퍼.
 *
 * 남은 90개 엔드포인트를 Ktor로 옮길 때도 이 헬퍼로 [HttpClient]만 만들고, 그 서비스의
 * `Ktor*ApiService` 구현체에 넘겨 스팟 체크하면 된다 — 실기기 없이 JVM에서 바로 확인 가능.
 *
 * `HttpClientFactory(enableBodyLogging = true)`를 그대로 쓴다 — 별도로 로깅을 얹지 않는다.
 * 프로덕션과 똑같은 안전장치(auth/ 경로 바디 제외, Authorization 헤더 마스킹)를 통합 테스트에서도
 * 그대로 검증하기 위함이다.
 */
internal const val BACKEND_INTEGRATION_TEST_BASE_URL = "https://pikiibackend-production.up.railway.app/api/v1/"

/**
 * 자격증명을 환경변수가 아니라 gitignore된 `local.properties`에서 읽는다 — 커맨드라인에 비밀번호가
 * 남아 셸 히스토리에 노출되는 걸 피하기 위함. 경로는 `shared/build.gradle.kts`의
 * `backendIntegrationTest` 태스크가 시스템 프로퍼티로 넘겨준다. 파일/키가 없으면 빈 문자열을
 * 반환하고, 호출부(테스트)가 `assumeTrue`로 스킵 처리한다.
 */
internal fun readLocalTestCredential(key: String): String? {
    val path = System.getProperty("pickii.localPropertiesPath") ?: return null
    val file = File(path)
    if (!file.exists()) return null
    val properties = Properties().apply { file.inputStream().use { load(it) } }
    return properties.getProperty(key)?.takeIf { it.isNotBlank() }
}

internal fun backendIntegrationTestHttpClient(authSession: AuthSession = NoOpAuthSession()): HttpClient =
    HttpClientFactory(
        engine = OkHttp.create(),
        baseUrl = BACKEND_INTEGRATION_TEST_BASE_URL,
        json =
            Json {
                ignoreUnknownKeys = true
                explicitNulls = false
                encodeDefaults = true
            },
        authSession = authSession,
        enableBodyLogging = true
    ).create()

/** 토큰이 필요 없는 엔드포인트(로그인 등)를 찍어볼 때 쓰는 빈 [AuthSession]. */
internal class NoOpAuthSession : AuthSession {
    override suspend fun currentAccessToken(): String? = null

    override suspend fun currentRefreshToken(): String? = null

    override suspend fun deviceId(): String = "backend-integration-test-device"

    override suspend fun onTokensRefreshed(
        accessToken: String,
        refreshToken: String
    ) = Unit

    override suspend fun onRefreshFailed() = Unit
}

/**
 * 로그인으로 실제로 받은 토큰을 그대로 들고 있는 [AuthSession] — 인증이 필요한 엔드포인트를
 * 찍어볼 때 쓴다. `currentAccessToken()`이 non-null을 반환하는 순간부터 `HttpClientFactory`의
 * Bearer Auth가 `Authorization` 헤더를 자동으로 붙인다.
 */
internal class StaticAuthSession(
    private val accessToken: String,
    private val refreshToken: String
) : AuthSession {
    override suspend fun currentAccessToken(): String? = accessToken

    override suspend fun currentRefreshToken(): String? = refreshToken

    override suspend fun deviceId(): String = "backend-integration-test-device"

    override suspend fun onTokensRefreshed(
        accessToken: String,
        refreshToken: String
    ) = Unit

    override suspend fun onRefreshFailed() = Unit
}
