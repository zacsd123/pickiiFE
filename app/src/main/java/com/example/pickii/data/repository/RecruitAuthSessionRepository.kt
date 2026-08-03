package com.example.pickii.data.repository

import com.example.pickii.data.local.DeviceIdProvider
import com.example.pickii.data.local.TokenStore
import com.example.pickii.data.remote.api.AuthApiService
import com.example.pickii.data.remote.dto.LoginRequest
import com.example.pickii.data.remote.dto.LogoutRequest
import com.example.pickii.data.remote.dto.SocialLoginRequest
import com.example.pickii.domain.model.CurrentUser
import com.example.pickii.domain.repository.ProfileRepository
import com.example.pickii.domain.repository.SessionRepository
import com.example.pickii.util.network.safeApiCall
import com.example.pickii.util.network.safeApiCallUnit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * `POST /auth/login`으로 실제 로그인을 수행하는 Repository.
 *
 * 로그인 응답에는 경험치/프로필 보유 여부가 없다(Member Profile API는 연동 범위 밖). 지원 화면의
 * "프로필 필요" 안내가 로그인 직후에는 부정확할 수 있음을 감수하고 기본값으로 채운다.
 */
private const val DEFAULT_EXPERIENCE = 0
private const val DEFAULT_HAS_PROFILE = true
private const val SOCIAL_PROVIDER_KAKAO = "KAKAO"

@Singleton
class RecruitAuthSessionRepository
    @Inject
    constructor(
        private val authApiService: AuthApiService,
        private val tokenStore: TokenStore,
        private val deviceIdProvider: DeviceIdProvider,
        private val profileRepository: ProfileRepository,
        private val json: Json
    ) : SessionRepository {
        private val _currentUser = MutableStateFlow<CurrentUser?>(null)
        override val currentUser: StateFlow<CurrentUser?> = _currentUser.asStateFlow()

        private val _isLoggedIn = MutableStateFlow(false)
        override val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

        override suspend fun login(
            email: String,
            password: String,
            autoLogin: Boolean
        ): Result<CurrentUser> =
            safeApiCall(json) {
                val deviceId = deviceIdProvider.getDeviceId()
                authApiService.login(LoginRequest(email, password, autoLogin, deviceId))
            }.map { envelope ->
                val body = envelope.data
                tokenStore.saveTokens(body.accessToken, body.refreshToken)
                val user =
                    CurrentUser(
                        id = body.memberId.toString(),
                        nickname = body.nickname,
                        experience = DEFAULT_EXPERIENCE,
                        hasProfile = DEFAULT_HAS_PROFILE
                    )
                _currentUser.value = user
                _isLoggedIn.value = true
                user
            }

        override suspend fun loginWithKakao(
            kakaoAccessToken: String,
            autoLogin: Boolean
        ): Result<CurrentUser> =
            safeApiCall(json) {
                val deviceId = deviceIdProvider.getDeviceId()
                authApiService.socialLogin(
                    SOCIAL_PROVIDER_KAKAO,
                    SocialLoginRequest(kakaoAccessToken, autoLogin, deviceId)
                )
            }.map { envelope ->
                val body = envelope.data
                tokenStore.saveTokens(body.accessToken, body.refreshToken)
                // 소셜 로그인 응답(1-10)에는 memberId가 내려오지 않아 id는 비워둔다.
                // "내 글" 비교 등 id 기반 UI는 다음 프로필 갱신 전까지 부정확할 수 있다.
                val hasProfile = profileRepository.hasResume()
                val nickname =
                    if (hasProfile) {
                        profileRepository
                            .getMyProfile()
                            .getOrNull()
                            ?.nickname
                            .orEmpty()
                    } else {
                        ""
                    }
                val user =
                    CurrentUser(
                        id = "",
                        nickname = nickname,
                        experience = DEFAULT_EXPERIENCE,
                        hasProfile = hasProfile
                    )
                _currentUser.value = user
                _isLoggedIn.value = true
                user
            }

        override suspend fun continueAsGuest() {
            clearSession()
        }

        override suspend fun logout(): Result<Unit> {
            val result =
                safeApiCallUnit(json) { authApiService.logout(LogoutRequest(deviceIdProvider.getDeviceId())) }
            clearSession()
            return result
        }

        override suspend fun clearSession() {
            tokenStore.clear()
            _currentUser.value = null
            _isLoggedIn.value = false
        }
    }
