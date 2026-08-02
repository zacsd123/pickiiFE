package com.example.pickii.data.repository

import com.example.pickii.data.local.TokenStore
import com.example.pickii.data.remote.api.AuthApiService
import com.example.pickii.data.remote.dto.LoginRequest
import com.example.pickii.domain.model.CurrentUser
import com.example.pickii.domain.repository.SessionRepository
import com.example.pickii.util.network.safeApiCall
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

@Singleton
class RecruitAuthSessionRepository
    @Inject
    constructor(
        private val authApiService: AuthApiService,
        private val tokenStore: TokenStore,
        private val json: Json
    ) : SessionRepository {
        private val _currentUser = MutableStateFlow<CurrentUser?>(null)
        override val currentUser: StateFlow<CurrentUser?> = _currentUser.asStateFlow()

        private val _isLoggedIn = MutableStateFlow(false)
        override val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

        override suspend fun login(
            email: String,
            password: String
        ): Result<CurrentUser> =
            safeApiCall(json) { authApiService.login(LoginRequest(email, password)) }
                .map { envelope ->
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

        override suspend fun continueAsGuest() {
            tokenStore.clear()
            _currentUser.value = null
            _isLoggedIn.value = false
        }
    }
