package com.example.pickii.data.remote.api

import com.example.pickii.data.remote.dto.ApiEnvelope
import com.example.pickii.data.remote.dto.ChangePasswordRequest
import com.example.pickii.data.remote.dto.EmailSendRequest
import com.example.pickii.data.remote.dto.EmailVerifyRequest
import com.example.pickii.data.remote.dto.EmailVerifyResponseDto
import com.example.pickii.data.remote.dto.LoginRequest
import com.example.pickii.data.remote.dto.LoginResponseDto
import com.example.pickii.data.remote.dto.LogoutRequest
import com.example.pickii.data.remote.dto.NicknameCheckResponseDto
import com.example.pickii.data.remote.dto.PasswordResetRequest
import com.example.pickii.data.remote.dto.SignupRequest
import com.example.pickii.data.remote.dto.SignupResponseDto
import com.example.pickii.data.remote.dto.SocialAccountDto
import com.example.pickii.data.remote.dto.WithdrawRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/** `1. Authentication` 문서 중 로그인/회원가입/계정관리(로그아웃, 탈퇴, 비밀번호, 소셜연동)를 다룬다. */
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

    /** 1-7 로그아웃. */
    @POST("auth/logout")
    suspend fun logout(
        @Body request: LogoutRequest
    ): Response<Unit>

    /**
     * 로그인 상태 비밀번호 변경. 가정 엔드포인트([ChangePasswordRequest] 참고) — 백엔드 준비 전까지 실패할 수 있다.
     */
    @PATCH("users/me/password")
    suspend fun changePassword(
        @Body request: ChangePasswordRequest
    ): Response<Unit>

    /** 1-9 회원 탈퇴. */
    @HTTP(method = "DELETE", path = "auth/withdraw", hasBody = true)
    suspend fun withdraw(
        @Body request: WithdrawRequest
    ): Response<Unit>

    /** 1-13 소셜 계정 연동 조회. */
    @GET("users/me/social-accounts")
    suspend fun getSocialAccounts(): Response<ApiEnvelope<List<SocialAccountDto>>>

    /** 1-13 소셜 계정 연동 해제. */
    @DELETE("auth/social/{provider}/link")
    suspend fun unlinkSocialAccount(
        @Path("provider") provider: String
    ): Response<Unit>
}
