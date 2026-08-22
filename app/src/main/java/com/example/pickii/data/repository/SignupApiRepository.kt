package com.example.pickii.data.repository

import com.example.pickii.data.remote.api.AuthApiService
import com.example.pickii.data.remote.dto.ApiException
import com.example.pickii.data.remote.dto.EmailSendRequest
import com.example.pickii.data.remote.dto.EmailVerifyRequest
import com.example.pickii.data.remote.dto.PasswordResetRequest
import com.example.pickii.data.remote.dto.SignupRequest
import com.example.pickii.data.remote.dto.SignupTermsDto
import com.example.pickii.domain.model.EmailPurpose
import com.example.pickii.domain.model.SignupTerms
import com.example.pickii.domain.repository.SignupRepository
import com.example.pickii.util.network.safeApiCall
import com.example.pickii.util.network.safeApiCallUnit
import kotlinx.serialization.json.Json

/** `1. Authentication` 문서의 이메일 인증/닉네임 중복확인/회원가입/비밀번호 재설정으로 [SignupRepository]를 구현한다. */
class SignupApiRepository
    constructor(
        private val authApiService: AuthApiService,
        private val json: Json
    ) : SignupRepository {
        override suspend fun sendEmailCode(
            email: String,
            purpose: EmailPurpose
        ): Result<Unit> =
            safeApiCallUnit(json) {
                authApiService.sendEmailCode(EmailSendRequest(email = email, type = purpose.name))
            }

        override suspend fun verifyEmailCode(
            email: String,
            code: String,
            purpose: EmailPurpose
        ): Result<String> =
            safeApiCall(json) {
                authApiService.verifyEmailCode(EmailVerifyRequest(email = email, code = code, type = purpose.name))
            }.mapCatching { envelope ->
                val body = envelope.data
                if (!body.isVerified) throw ApiException("INVALID_VERIFICATION_CODE", "인증코드가 올바르지 않습니다.")
                body.emailVerificationToken
            }

        override suspend fun checkNickname(nickname: String): Result<String> =
            safeApiCall(json) { authApiService.checkNickname(nickname) }
                .mapCatching { envelope ->
                    val body = envelope.data
                    if (!body.isAvailable) throw ApiException("NICKNAME_ALREADY_EXISTS", "이미 사용 중인 닉네임입니다.")
                    body.nicknameVerificationToken
                }

        override suspend fun signUp(
            nickname: String,
            email: String,
            password: String,
            passwordConfirm: String,
            emailVerificationToken: String,
            nicknameVerificationToken: String,
            terms: SignupTerms
        ): Result<Unit> =
            safeApiCall(json) {
                authApiService.signUp(
                    SignupRequest(
                        nickname = nickname,
                        email = email,
                        password = password,
                        passwordConfirm = passwordConfirm,
                        emailVerificationToken = emailVerificationToken,
                        nicknameVerificationToken = nicknameVerificationToken,
                        terms =
                            SignupTermsDto(
                                ageAgreed = terms.ageAgreed,
                                termsAgreed = terms.termsAgreed,
                                privacyAgreed = terms.privacyAgreed,
                                profileShareAgreed = terms.profileShareAgreed,
                                dataCollectionAgreed = terms.dataCollectionAgreed,
                                pushNotiAgreed = terms.pushNotiAgreed
                            )
                    )
                )
            }.map { }

        override suspend fun resetPassword(
            email: String,
            emailVerificationToken: String,
            newPassword: String,
            newPasswordConfirm: String
        ): Result<Unit> =
            safeApiCallUnit(json) {
                authApiService.resetPassword(
                    PasswordResetRequest(
                        email = email,
                        emailVerificationToken = emailVerificationToken,
                        newPassword = newPassword,
                        newPasswordConfirm = newPasswordConfirm
                    )
                )
            }
    }
