package com.example.pickii.data.remote

import com.example.pickii.data.local.DeviceIdProvider
import com.example.pickii.data.local.TokenStore
import kotlinx.coroutines.flow.first

/** [AuthSession]을 기존 [TokenStore]/[DeviceIdProvider]로 구현한다. 둘 다 수정하지 않고 감싸기만 한다. */
class TokenStoreAuthSession(
    private val tokenStore: TokenStore,
    private val deviceIdProvider: DeviceIdProvider
) : AuthSession {
    override suspend fun currentAccessToken(): String? = tokenStore.accessTokenFlow.first()

    override suspend fun currentRefreshToken(): String? = tokenStore.refreshTokenFlow.first()

    override suspend fun deviceId(): String = deviceIdProvider.getDeviceId()

    override suspend fun onTokensRefreshed(
        accessToken: String,
        refreshToken: String
    ) {
        tokenStore.saveTokens(accessToken, refreshToken)
    }

    override suspend fun onRefreshFailed() {
        tokenStore.clear()
    }
}
