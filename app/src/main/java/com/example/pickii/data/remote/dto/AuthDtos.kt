package com.example.pickii.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val email: String,
    val password: String,
    val autoLogin: Boolean,
    val deviceId: String
)

@Serializable
data class LoginResponseDto(
    val memberId: Long,
    val nickname: String,
    val accessToken: String,
    val refreshToken: String
)

/** `POST /auth/social/{provider}/login`(1-10) 요청. */
@Serializable
data class SocialLoginRequest(
    val socialAccessToken: String,
    val autoLogin: Boolean,
    val deviceId: String
)

/** `POST /auth/social/{provider}/login`(1-10) 응답. memberId/nickname은 내려주지 않는다. */
@Serializable
data class SocialLoginResponseDto(
    val accessToken: String,
    val refreshToken: String
)

/** `POST /auth/social/{provider}/link`(1-11) 요청. */
@Serializable
data class SocialLinkRequest(
    val providerId: String
)

/** `POST /auth/social/{provider}/link`(1-11) 응답. */
@Serializable
data class SocialLinkResponseDto(
    val message: String
)
