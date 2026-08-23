package com.example.pickii.data.remote

import com.example.pickii.data.local.DeviceIdProvider
import com.example.pickii.data.local.TokenStore
import com.example.pickii.data.remote.api.RetrofitAuthRefreshService
import com.example.pickii.data.remote.dto.TokenRefreshRequest
import com.example.pickii.util.network.safeApiCall
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

private const val REFRESH_ENDPOINT_PATH = "auth/token/refresh"
private const val MAX_REFRESH_ATTEMPTS = 2

/**
 * Access Token이 만료되어 401이 오면 `POST /auth/token/refresh`(1-6)로 조용히 갱신하고 원래 요청을
 * 재시도한다. 갱신도 실패하면(Refresh Token 만료 등) 저장된 토큰을 지워, [TokenStore.accessTokenFlow]를
 * 구독하는 로그인 상태가 자동으로 로그아웃으로 반영되게 한다.
 *
 * Koin 싱글턴(`di/InfraModule.kt`).
 *
 * ⚠️ [authRefreshService] 순환 의존성 경고 — 절대 `Lazy` 벗기고 즉시 주입으로 바꾸지 말 것:
 * 이 클래스가 붙는 OkHttpClient로 Retrofit이 만들어지고, 그 Retrofit이 [RetrofitAuthRefreshService]를
 * 만든다. 즉 `OkHttpClient → TokenAuthenticator → RetrofitAuthRefreshService → Retrofit →
 * OkHttpClient`로 순환한다(원래는 `AuthApiService`가 이 역할이었는데, `AuthApiService`가 Ktor로
 * 전환되면서 이 전용 인터페이스로 갈아탔다 — 순환 구조 자체는 그대로다).
 * `di/InfraModule.kt`에서 `lazy { get<RetrofitAuthRefreshService>() }`로 감싸지 않고 그냥 `get()`으로
 * 즉시 해석하면, OkHttpClient를 만드는 도중에 같은 OkHttpClient를 다시 만들려고 해서
 * **`StackOverflowError`가 첫 네트워크 호출(로그인) 시점에 터진다** — 빌드는 멀쩡히 통과하고
 * 런타임에만 죽어서 원인 찾기 아주 어렵다(`CircularDependencyKoinTest`에서 실측 재현해뒀다).
 * Hilt에서는 `Provider<AuthApiService>`가 하던 역할을 [Lazy]가 대신한다 — `.value`를 처음 건드리는
 * 시점(=실제 401이 나서 갱신할 때)에야 그래프를 타므로 그땐 이미 OkHttpClient가 싱글턴으로
 * 만들어져 있어 재귀가 없다.
 *
 * `lazy { }`의 기본 스레드 세이프티 모드(`SYNCHRONIZED`)도 그대로 둘 것 — OkHttp는 여러 스레드에서
 * 동시에 `authenticate()`를 부를 수 있어서 `LazyThreadSafetyMode.NONE`으로 바꾸면 레이스가 생긴다.
 */
class TokenAuthenticator(
    private val tokenStore: TokenStore,
    private val deviceIdProvider: DeviceIdProvider,
    private val authRefreshService: Lazy<RetrofitAuthRefreshService>,
    private val json: Json
) : Authenticator {
    private val refreshLock = Any()

    override fun authenticate(
        route: Route?,
        response: Response
    ): Request? {
        if (response.request.url.encodedPath
                .endsWith(REFRESH_ENDPOINT_PATH)
        ) {
            return null
        }
        if (responseCount(response) >= MAX_REFRESH_ATTEMPTS) return null

        val failedToken = response.request.header("Authorization")?.removePrefix("Bearer ")

        synchronized(refreshLock) {
            val latestToken = tokenStore.currentAccessTokenBlocking()
            if (!latestToken.isNullOrBlank() && latestToken != failedToken) {
                // 다른 요청이 이 락을 기다리는 동안 이미 갱신을 마쳤다 — 새 토큰으로 재시도만 한다.
                return response.request.withBearerToken(latestToken)
            }

            val refreshToken = tokenStore.currentRefreshTokenBlocking()
            if (refreshToken.isNullOrBlank()) {
                runBlocking { tokenStore.clear() }
                return null
            }

            val newAccessToken =
                runBlocking {
                    safeApiCall(json) {
                        authRefreshService.value
                            .refreshToken(TokenRefreshRequest(deviceIdProvider.getDeviceId(), refreshToken))
                    }.map { it.data }
                        .onSuccess { tokenStore.saveTokens(it.accessToken, it.refreshToken) }
                        .getOrNull()
                }?.accessToken

            if (newAccessToken == null) {
                runBlocking { tokenStore.clear() }
                return null
            }

            return response.request.withBearerToken(newAccessToken)
        }
    }

    private fun Request.withBearerToken(token: String): Request =
        newBuilder().header("Authorization", "Bearer $token").build()

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}
