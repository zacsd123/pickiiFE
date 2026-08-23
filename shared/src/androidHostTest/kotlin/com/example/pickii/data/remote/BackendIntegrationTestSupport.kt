package com.example.pickii.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import kotlinx.serialization.json.Json

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
