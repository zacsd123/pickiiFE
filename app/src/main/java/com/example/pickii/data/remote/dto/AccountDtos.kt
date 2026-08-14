package com.example.pickii.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * 로그인 상태 비밀번호 변경 요청(1-12, `PATCH auth/password`).
 *
 * 실제 스펙 확인 결과 이메일 재입력/인증코드는 필요 없고(서버가 로그인 사용자의 이메일을 안다),
 * 본인 확인은 현재 비밀번호로 한다 — 이 DTO 형태가 스펙과 일치한다.
 */
@Serializable
data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String,
    val newPasswordConfirm: String
)

/** `DELETE /auth/withdraw`(1-9) 요청 바디의 `agreements`. */
@Serializable
data class WithdrawAgreementsDto(
    val dataDeletionAgreed: Boolean,
    val rejoinPolicyAgreed: Boolean
)

/** `DELETE /auth/withdraw`(1-9) 요청. */
@Serializable
data class WithdrawRequest(
    val password: String,
    val emailVerificationToken: String,
    val agreements: WithdrawAgreementsDto
)

/** `POST /auth/logout`(1-7) 요청. */
@Serializable
data class LogoutRequest(
    val deviceId: String
)

/** `GET /users/me/social-accounts`(1-13) 목록 항목. */
@Serializable
data class SocialAccountDto(
    val provider: String,
    val linked: Boolean
)
