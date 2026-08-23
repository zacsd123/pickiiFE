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
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse

/**
 * [AuthApiService]를 Ktor [HttpClient]로 구현한다. Retrofit→Ktor 전환 파일럿.
 *
 * 바디가 있는 요청도 개별적으로 `contentType()`을 부르지 않는다 —
 * [com.example.pickii.data.remote.HttpClientFactory]의 `defaultRequest { }`가
 * `application/json`을 기본값으로 깔아둔다. 원래는 메서드마다 직접 불렀었는데,
 * 사람이 매번 기억해야 하는 구조라 실제로 전부 빠뜨린 적이 있다("Content-Type: null"
 * 예외, `AuthApiServiceBackendIntegrationTest`가 실제 백엔드로 잡아냄) — 그래서
 * `HttpClientFactory` 쪽에 구조적으로 옮겼다.
 */
class KtorAuthApiService(
    private val client: HttpClient
) : AuthApiService {
    override suspend fun login(request: LoginRequest): HttpResponse = client.post("auth/login") { setBody(request) }

    override suspend fun refreshToken(request: TokenRefreshRequest): HttpResponse =
        client.post("auth/token/refresh") { setBody(request) }

    override suspend fun sendEmailCode(request: EmailSendRequest): HttpResponse =
        client.post("auth/email/send") { setBody(request) }

    override suspend fun verifyEmailCode(request: EmailVerifyRequest): HttpResponse =
        client.post("auth/email/verify") { setBody(request) }

    override suspend fun checkNickname(nickname: String): HttpResponse =
        client.get("auth/nickname/check") { parameter("nickname", nickname) }

    override suspend fun signUp(request: SignupRequest): HttpResponse = client.post("auth/signup") { setBody(request) }

    override suspend fun resetPassword(request: PasswordResetRequest): HttpResponse =
        client.post("auth/password/reset") { setBody(request) }

    override suspend fun logout(request: LogoutRequest): HttpResponse = client.post("auth/logout") { setBody(request) }

    override suspend fun changePassword(request: ChangePasswordRequest): HttpResponse =
        client.patch("auth/password") { setBody(request) }

    override suspend fun withdraw(request: WithdrawRequest): HttpResponse =
        client.delete("auth/withdraw") { setBody(request) }

    override suspend fun getSocialAccounts(): HttpResponse = client.get("users/me/social-accounts")

    override suspend fun unlinkSocialAccount(provider: String): HttpResponse =
        client.delete("auth/social/$provider/link")

    override suspend fun socialLogin(
        provider: String,
        request: SocialLoginRequest
    ): HttpResponse = client.post("auth/social/$provider/login") { setBody(request) }

    override suspend fun linkSocialAccount(
        provider: String,
        request: SocialLinkRequest
    ): HttpResponse = client.post("auth/social/$provider/link") { setBody(request) }
}
