package com.example.pickii.data.remote.api

import com.example.pickii.data.remote.dto.ApiEnvelope
import com.example.pickii.data.remote.dto.EmailSendRequest
import com.example.pickii.data.remote.dto.EmailVerifyRequest
import com.example.pickii.data.remote.dto.EmailVerifyResponseDto
import com.example.pickii.data.remote.dto.LoginRequest
import com.example.pickii.data.remote.dto.LoginResponseDto
import com.example.pickii.data.remote.dto.NicknameCheckResponseDto
import com.example.pickii.data.remote.dto.PasswordResetRequest
import com.example.pickii.data.remote.dto.SignupRequest
import com.example.pickii.data.remote.dto.SignupResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/** `1. Authentication` 문서 중 로그인 + 회원가입(이메일 인증/닉네임 중복확인 포함)만 다룬다. */
interface AuthApiService {
    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<ApiEnvelope<LoginResponseDto>>

    @POST("auth/email/send")
    suspend fun sendEmailCode(
        @Body request: EmailSendRequest
    ): Response<Unit>

    @POST("auth/email/verify")
    suspend fun verifyEmailCode(
        @Body request: EmailVerifyRequest
    ): Response<ApiEnvelope<EmailVerifyResponseDto>>

    @GET("auth/nickname/check")
    suspend fun checkNickname(
        @Query("nickname") nickname: String
    ): Response<ApiEnvelope<NicknameCheckResponseDto>>

    @POST("auth/signup")
    suspend fun signUp(
        @Body request: SignupRequest
    ): Response<ApiEnvelope<SignupResponseDto>>

    @POST("auth/password/reset")
    suspend fun resetPassword(
        @Body request: PasswordResetRequest
    ): Response<Unit>
}
