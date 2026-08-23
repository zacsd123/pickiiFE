package com.example.pickii.data.remote.api

import com.example.pickii.data.remote.dto.ChangePasswordRequest
import com.example.pickii.data.remote.dto.EmailSendRequest
import com.example.pickii.data.remote.dto.EmailVerifyRequest
import com.example.pickii.data.remote.dto.LoginRequest
import com.example.pickii.data.remote.dto.LogoutRequest
import com.example.pickii.data.remote.dto.PasswordResetRequest
import com.example.pickii.data.remote.dto.SignupRequest
import com.example.pickii.data.remote.dto.SocialLinkRequest
import com.example.pickii.data.remote.dto.SocialLoginRequest
import com.example.pickii.data.remote.dto.TokenRefreshRequest
import com.example.pickii.data.remote.dto.WithdrawRequest
import io.ktor.client.statement.HttpResponse

/** `1. Authentication` 문서 중 로그인/회원가입/계정관리(로그아웃, 탈퇴, 비밀번호, 소셜연동)를 다룬다. */
interface AuthApiService {
    suspend fun login(request: LoginRequest): HttpResponse

    /**
     * 1-6 토큰 갱신(Silent Refresh). 실제로는 [com.example.pickii.data.remote.HttpClientFactory]의
     * `refreshTokens{}`가 같은 엔드포인트를 직접 호출한다 — 이 메서드는 인터페이스 완전성을 위해
     * 유지하며, 외부에서 직접 호출하는 곳은 없다.
     */
    suspend fun refreshToken(request: TokenRefreshRequest): HttpResponse

    suspend fun sendEmailCode(request: EmailSendRequest): HttpResponse

    suspend fun verifyEmailCode(request: EmailVerifyRequest): HttpResponse

    suspend fun checkNickname(nickname: String): HttpResponse

    suspend fun signUp(request: SignupRequest): HttpResponse

    suspend fun resetPassword(request: PasswordResetRequest): HttpResponse

    /** 1-7 로그아웃. */
    suspend fun logout(request: LogoutRequest): HttpResponse

    /** 1-12 비밀번호 변경(로그인 상태). */
    suspend fun changePassword(request: ChangePasswordRequest): HttpResponse

    /** 1-9 회원 탈퇴. */
    suspend fun withdraw(request: WithdrawRequest): HttpResponse

    /** 1-13 소셜 계정 연동 조회. */
    suspend fun getSocialAccounts(): HttpResponse

    /** 1-13 소셜 계정 연동 해제. */
    suspend fun unlinkSocialAccount(provider: String): HttpResponse

    /**
     * 1-10 소셜 로그인. 연동되지 않은 소셜 계정으로 시도하면 404 `NOT_LINKED_ACCOUNT`로 실패한다
     * (소셜은 가입 수단이 아니라, 이메일/비밀번호로 가입 후 연동한 계정의 로그인 편의 기능이다).
     */
    suspend fun socialLogin(
        provider: String,
        request: SocialLoginRequest
    ): HttpResponse

    /** 1-11 소셜 계정 연동. 로그인된 상태에서 카카오 등 소셜 계정을 현재 계정에 연결한다. */
    suspend fun linkSocialAccount(
        provider: String,
        request: SocialLinkRequest
    ): HttpResponse
}
