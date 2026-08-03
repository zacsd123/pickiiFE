package com.example.pickii.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class EmailSendRequest(
    val email: String,
    val type: String
)

@Serializable
data class EmailVerifyRequest(
    val email: String,
    val code: String,
    val type: String
)

@Serializable
data class EmailVerifyResponseDto(
    val isVerified: Boolean,
    val emailVerificationToken: String
)

@Serializable
data class NicknameCheckResponseDto(
    val isAvailable: Boolean,
    val nicknameVerificationToken: String
)

@Serializable
data class SignupTermsDto(
    val ageAgreed: Boolean,
    val termsAgreed: Boolean,
    val privacyAgreed: Boolean,
    val profileShareAgreed: Boolean,
    val dataCollectionAgreed: Boolean,
    val pushNotiAgreed: Boolean
)

@Serializable
data class SignupRequest(
    val nickname: String,
    val email: String,
    val password: String,
    val passwordConfirm: String,
    val emailVerificationToken: String,
    val nicknameVerificationToken: String,
    val terms: SignupTermsDto
)

@Serializable
data class SignupResponseDto(
    val memberId: Long,
    val email: String,
    val nickname: String
)

/** `POST /auth/password/reset` 요청(비로그인 상태 비밀번호 재설정). */
@Serializable
data class PasswordResetRequest(
    val email: String,
    val emailVerificationToken: String,
    val newPassword: String,
    val newPasswordConfirm: String
)
