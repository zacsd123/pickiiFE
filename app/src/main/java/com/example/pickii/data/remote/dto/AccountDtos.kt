package com.example.pickii.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * 로그인 상태 비밀번호 변경 요청.
 *
 * 사진 목업 기준(현재 비밀번호 입력칸 포함)으로 만든 가정 엔드포인트(`PATCH users/me/password`)다.
 * 실제 1-12(`PATCH auth/password`, 이메일 인증코드 기반, 현재 비밀번호 불필요)와는 다른 별도 경로이며,
 * 백엔드에 아직 이 필드를 받는 엔드포인트가 없어 호출 시 실패할 수 있다(백엔드팀에 추가 요청 예정).
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
