package com.example.pickii.data.remote

import com.example.pickii.data.local.DeviceIdProvider
import com.example.pickii.data.local.TokenStore
import com.example.pickii.data.remote.api.AuthApiService
import com.example.pickii.data.remote.dto.TokenRefreshRequest
import com.example.pickii.util.network.safeApiCall
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Provider

private const val REFRESH_ENDPOINT_PATH = "auth/token/refresh"
private const val MAX_REFRESH_ATTEMPTS = 2

/**
 * Access Token이 만료되어 401이 오면 `POST /auth/token/refresh`(1-6)로 조용히 갱신하고 원래 요청을
 * 재시도한다. 갱신도 실패하면(Refresh Token 만료 등) 저장된 토큰을 지워, [TokenStore.accessTokenFlow]를
 * 구독하는 로그인 상태가 자동으로 로그아웃으로 반영되게 한다.
 *
 * [AuthApiService]는 이 Authenticator가 붙는 OkHttpClient로 만든 Retrofit이 제공하므로, 생성자에서
 * 바로 주입받으면 순환 의존이 생긴다. [Provider]로 실제 401이 나는 시점에 꺼내 순환을 끊는다.
 */
class TokenAuthenticator
    @Inject
    constructor(
        private val tokenStore: TokenStore,
        private val deviceIdProvider: DeviceIdProvider,
        private val authApiService: Provider<AuthApiService>,
        private val json: Json
    ) : Authenticator {
        private val refreshLock = Any()

        override fun authenticate(
            route: Route?,
            response: Response
        ): Request? {
            if (response.request.url.encodedPath.endsWith(REFRESH_ENDPOINT_PATH)) return null
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
                            authApiService
                                .get()
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
