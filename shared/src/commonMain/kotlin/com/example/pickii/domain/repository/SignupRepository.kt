package com.example.pickii.domain.repository

import com.example.pickii.domain.model.EmailPurpose
import com.example.pickii.domain.model.SignupTerms

/** 회원가입/비밀번호 재설정에 필요한 이메일 인증, 닉네임 중복확인, 가입/재설정 요청을 담당한다. */
interface SignupRepository {
    /** [purpose] 목적의 이메일 인증코드를 발송한다. */
    suspend fun sendEmailCode(
        email: String,
        purpose: EmailPurpose = EmailPurpose.SIGNUP
    ): Result<Unit>

    /** 인증코드를 확인하고, 성공하면 후속 요청에 쓸 emailVerificationToken을 반환한다. */
    suspend fun verifyEmailCode(
        email: String,
        code: String,
        purpose: EmailPurpose = EmailPurpose.SIGNUP
    ): Result<String>

    /** 닉네임 중복 여부를 확인하고, 사용 가능하면 가입 요청에 쓸 nicknameVerificationToken을 반환한다. */
    suspend fun checkNickname(nickname: String): Result<String>

    /** 회원가입을 완료한다. */
    suspend fun signUp(
        nickname: String,
        email: String,
        password: String,
        passwordConfirm: String,
        emailVerificationToken: String,
        nicknameVerificationToken: String,
        terms: SignupTerms
    ): Result<Unit>

    /** 비로그인 상태에서 이메일 인증을 마친 뒤 비밀번호를 재설정한다. */
    suspend fun resetPassword(
        email: String,
        emailVerificationToken: String,
        newPassword: String,
        newPasswordConfirm: String
    ): Result<Unit>
}
