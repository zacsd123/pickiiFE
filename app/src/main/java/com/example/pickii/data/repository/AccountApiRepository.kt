package com.example.pickii.data.repository

import com.example.pickii.data.remote.api.AuthApiService
import com.example.pickii.data.remote.dto.ApiEnvelope
import com.example.pickii.data.remote.dto.ChangePasswordRequest
import com.example.pickii.data.remote.dto.SocialAccountDto
import com.example.pickii.data.remote.dto.SocialLinkRequest
import com.example.pickii.data.remote.dto.SocialLinkResponseDto
import com.example.pickii.data.remote.dto.WithdrawAgreementsDto
import com.example.pickii.data.remote.dto.WithdrawRequest
import com.example.pickii.domain.model.SocialAccount
import com.example.pickii.domain.repository.AccountRepository
import com.example.pickii.util.network.safeApiCall
import com.example.pickii.util.network.safeApiCallUnit

/** `1. Authentication` 문서의 계정관리 API로 [AccountRepository]를 구현한다. */
class AccountApiRepository
    constructor(
        private val authApiService: AuthApiService
    ) : AccountRepository {
        override suspend fun changePassword(
            currentPassword: String,
            newPassword: String,
            newPasswordConfirm: String
        ): Result<Unit> =
            safeApiCallUnit {
                authApiService.changePassword(
                    ChangePasswordRequest(
                        currentPassword = currentPassword,
                        newPassword = newPassword,
                        newPasswordConfirm = newPasswordConfirm
                    )
                )
            }

        override suspend fun withdraw(
            password: String,
            emailVerificationToken: String,
            dataDeletionAgreed: Boolean,
            rejoinPolicyAgreed: Boolean
        ): Result<Unit> =
            safeApiCallUnit {
                authApiService.withdraw(
                    WithdrawRequest(
                        password = password,
                        emailVerificationToken = emailVerificationToken,
                        agreements =
                            WithdrawAgreementsDto(
                                dataDeletionAgreed = dataDeletionAgreed,
                                rejoinPolicyAgreed = rejoinPolicyAgreed
                            )
                    )
                )
            }

        override suspend fun getSocialAccounts(): Result<List<SocialAccount>> =
            safeApiCall<ApiEnvelope<List<SocialAccountDto>>> { authApiService.getSocialAccounts() }.map { envelope ->
                envelope.data.map { it.toDomain() }
            }

        override suspend fun unlinkSocialAccount(provider: String): Result<Unit> =
            safeApiCallUnit { authApiService.unlinkSocialAccount(provider) }

        override suspend fun linkSocialAccount(
            provider: String,
            providerId: String
        ): Result<Unit> =
            safeApiCall<ApiEnvelope<SocialLinkResponseDto>> {
                authApiService.linkSocialAccount(provider, SocialLinkRequest(providerId))
            }.map { }

        private fun SocialAccountDto.toDomain(): SocialAccount = SocialAccount(provider = provider, linked = linked)
    }
