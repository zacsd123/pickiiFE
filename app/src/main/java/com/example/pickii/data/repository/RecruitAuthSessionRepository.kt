package com.example.pickii.data.repository

import com.example.pickii.data.local.DeviceIdProvider
import com.example.pickii.data.local.TokenStore
import com.example.pickii.data.remote.api.AuthApiService
import com.example.pickii.data.remote.dto.LoginRequest
import com.example.pickii.data.remote.dto.LogoutRequest
import com.example.pickii.data.remote.dto.SocialLoginRequest
import com.example.pickii.domain.model.CurrentUser
import com.example.pickii.domain.repository.NotificationRepository
import com.example.pickii.domain.repository.ProfileRepository
import com.example.pickii.domain.repository.SessionRepository
import com.example.pickii.util.decodeJwtSubject
import com.example.pickii.util.network.safeApiCall
import com.example.pickii.util.network.safeApiCallUnit
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * `POST /auth/login`으로 실제 로그인을 수행하는 Repository.
 *
 * 로그인 응답에는 프로필 보유 여부가 없다(Member Profile API는 연동 범위 밖). 지원 화면의
 * "프로필 필요" 안내가 로그인 직후에는 부정확할 수 있음을 감수하고 기본값으로 채운다.
 */
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
        private val notificationRepository: NotificationRepository,
        private val json: Json
    ) : SessionRepository {
        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

        private val _currentUser = MutableStateFlow<CurrentUser?>(null)
        override val currentUser: StateFlow<CurrentUser?> = _currentUser.asStateFlow()

        /**
         * 저장된 Access Token 유무로 로그인 상태를 판단한다. 별도 "세션 복원" 호출 없이 앱을 껐다 켜도
         * (자동 로그인) 로그인 상태가 그대로 유지되고, [com.example.pickii.data.remote.TokenAuthenticator]가
         * Refresh Token 만료로 토큰을 지우면 로그아웃 상태에도 자동으로 반영된다.
         */
        override val isLoggedIn: StateFlow<Boolean> =
            tokenStore.accessTokenFlow
                .map { !it.isNullOrBlank() }
                .stateIn(scope, SharingStarted.Eagerly, false)

        init {
            scope.launch {
                tokenStore.accessTokenFlow.collect { accessToken ->
                    if (accessToken.isNullOrBlank()) {
                        _currentUser.value = null
                    } else if (_currentUser.value == null) {
                        restoreCurrentUser(accessToken)
                    }
                }
            }
        }

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
                        hasProfile = DEFAULT_HAS_PROFILE
                    )
                _currentUser.value = user
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
                // 소셜 로그인 응답(1-10)에는 memberId가 내려오지 않는다. [resolveCurrentUserFromToken]이
                // 정상 로그인과 같은 인증 서버가 발급한 토큰의 subject 클레임에서 회원 id를 대신 읽는다.
                val user = resolveCurrentUserFromToken(body.accessToken)
                _currentUser.value = user
                user
            }

        override suspend fun continueAsGuest() {
            clearSession()
        }

        override suspend fun logout(): Result<Unit> {
            unregisterDeviceToken()
            val result =
                safeApiCallUnit(json) { authApiService.logout(LogoutRequest(deviceIdProvider.getDeviceId())) }
            clearSession()
            return result
        }

        /**
         * `clearSession()`이 Access Token을 지우기 전에 호출해야 한다 — 토큰이 사라진 뒤에는
         * `DELETE /devices` 요청 자체가 401로 거부된다.
         */
        private suspend fun unregisterDeviceToken() {
            val token = runCatching { FirebaseMessaging.getInstance().token.await() }.getOrNull() ?: return
            notificationRepository.unregisterDevice(token)
        }

        override suspend fun clearSession() {
            tokenStore.clear()
            _currentUser.value = null
        }

        /** 앱 재시작 등으로 토큰은 있는데 메모리의 사용자 정보가 없을 때(자동 로그인) 복원한다. */
        private suspend fun restoreCurrentUser(accessToken: String) {
            _currentUser.value = resolveCurrentUserFromToken(accessToken)
        }

        /**
         * sub가 숫자가 아니면(백엔드가 다른 값을 넣은 경우) id는 빈 값으로 남고, "내 글" 비교 등
         * id 기반 UI는 다음 프로필 갱신 전까지 부정확할 수 있다 — 이 경우 백엔드가 소셜 로그인
         * 응답에 memberId를 직접 내려주는 것이 근본적인 해결책이다.
         */
        private suspend fun resolveCurrentUserFromToken(accessToken: String): CurrentUser {
            val memberId = decodeJwtSubject(accessToken)?.toLongOrNull()
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
            return CurrentUser(
                id = memberId?.toString().orEmpty(),
                nickname = nickname,
                hasProfile = hasProfile
            )
        }
    }
