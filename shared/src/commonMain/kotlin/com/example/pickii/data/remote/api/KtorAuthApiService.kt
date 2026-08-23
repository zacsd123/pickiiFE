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
import io.ktor.http.ContentType
import io.ktor.http.contentType

/**
 * [AuthApiService]를 Ktor [HttpClient]로 구현한다. Retrofit→Ktor 전환 파일럿.
 *
 * 바디가 있는 요청은 전부 `contentType(ContentType.Application.Json)`을 명시한다 — 안 하면
 * `ContentNegotiation`이 어떤 컨버터로 직렬화할지 못 정해서
 * "Content-Type: null... please check that you have installed the corresponding plugin"
 * 예외가 난다(실측: `AuthApiServiceBackendIntegrationTest`가 실제 백엔드로 잡아냄).
 */
class KtorAuthApiService(
    private val client: HttpClient
) : AuthApiService {
    override suspend fun login(request: LoginRequest): HttpResponse =
        client.post("auth/login") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }

    override suspend fun refreshToken(request: TokenRefreshRequest): HttpResponse =
        client.post("auth/token/refresh") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }

    override suspend fun sendEmailCode(request: EmailSendRequest): HttpResponse =
        client.post("auth/email/send") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }

    override suspend fun verifyEmailCode(request: EmailVerifyRequest): HttpResponse =
        client.post("auth/email/verify") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }

    override suspend fun checkNickname(nickname: String): HttpResponse =
        client.get("auth/nickname/check") { parameter("nickname", nickname) }

    override suspend fun signUp(request: SignupRequest): HttpResponse =
        client.post("auth/signup") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }

    override suspend fun resetPassword(request: PasswordResetRequest): HttpResponse =
        client.post("auth/password/reset") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }

    override suspend fun logout(request: LogoutRequest): HttpResponse =
        client.post("auth/logout") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }

    override suspend fun changePassword(request: ChangePasswordRequest): HttpResponse =
        client.patch("auth/password") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }

    override suspend fun withdraw(request: WithdrawRequest): HttpResponse =
        client.delete("auth/withdraw") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }

    override suspend fun getSocialAccounts(): HttpResponse = client.get("users/me/social-accounts")

    override suspend fun unlinkSocialAccount(provider: String): HttpResponse =
        client.delete("auth/social/$provider/link")

    override suspend fun socialLogin(
        provider: String,
        request: SocialLoginRequest
    ): HttpResponse =
        client.post("auth/social/$provider/login") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }

    override suspend fun linkSocialAccount(
        provider: String,
        request: SocialLinkRequest
    ): HttpResponse =
        client.post("auth/social/$provider/link") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
}
