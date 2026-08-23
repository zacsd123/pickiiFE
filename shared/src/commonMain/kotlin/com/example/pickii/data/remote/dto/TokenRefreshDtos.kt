package com.example.pickii.data.remote.dto

import kotlinx.serialization.Serializable

/** `POST /auth/token/refresh`(1-6) 요청. */
@Serializable
data class TokenRefreshRequest(
    val deviceId: String,
    val refreshToken: String
)

/** `POST /auth/token/refresh`(1-6) 응답. Refresh Token Rotation으로 access/refresh 모두 새로 내려온다. */
@Serializable
data class TokenRefreshResponseDto(
    val accessToken: String,
    val refreshToken: String
)
