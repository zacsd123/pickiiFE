package com.example.pickii.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class LoginResponseDto(
    val memberId: Long,
    val nickname: String,
    val accessToken: String,
    val refreshToken: String
)
