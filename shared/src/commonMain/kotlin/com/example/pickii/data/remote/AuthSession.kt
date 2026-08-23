package com.example.pickii.data.remote

/**
 * [HttpClientFactory]가 Bearer 인증 상태를 읽고 쓰기 위해 필요한 최소 인터페이스.
 * 실제 토큰 저장소(Android `TokenStore`/`DeviceIdProvider` 등)를 몰라도 되게 분리해서,
 * 테스트에서는 fake 구현체 하나로 [HttpClientFactory]의 Auth 설정을 검증할 수 있다.
 */
interface AuthSession {
    /** 현재 저장된 Access Token. 비로그인 상태면 null. */
    suspend fun currentAccessToken(): String?

    /** 현재 저장된 Refresh Token. 비로그인 상태면 null. */
    suspend fun currentRefreshToken(): String?

    /** 로그인 API의 `deviceId`로 쓸, 앱 설치 단위로 고정된 식별자. */
    suspend fun deviceId(): String

    /** 토큰 갱신에 성공하면 새 토큰 쌍을 저장한다. */
    suspend fun onTokensRefreshed(
        accessToken: String,
        refreshToken: String
    )

    /** 토큰 갱신에 실패하면(Refresh Token 만료 등) 저장된 토큰을 지운다. */
    suspend fun onRefreshFailed()
}
