package com.example.pickii.data.repository

import com.example.pickii.domain.model.CurrentUser
import com.example.pickii.domain.repository.SessionRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/** 로그인 흉내를 위해 인위적으로 대기시키는 시간(ms). */
private const val MOCK_LOGIN_DELAY_MS = 500L

/** 로그인한 사용자의 경험치 목업 값. */
private const val MOCK_USER_EXPERIENCE = 87

/** 이메일에 이 문자열이 포함되면 프로필(Resume)이 없는 사용자로 로그인된다. 지원 화면의 프로필 안내 분기를 확인하기 위한 테스트용 훅. */
private const val NO_PROFILE_EMAIL_HINT = "noprofile"

/**
 * 백엔드 없이 로그인 상태를 흉내 내는 목업 Repository.
 *
 * 자격 증명 형식은 검증하지 않고, 이메일에 [NO_PROFILE_EMAIL_HINT]가 포함되어 있는지로만
 * 프로필 보유 여부를 갈라서 지원 화면의 "프로필이 필요해요" 분기를 데모에서 재현할 수 있게 한다.
 */
@Singleton
class MockSessionRepository
    @Inject
    constructor() : SessionRepository {
        private val _currentUser = MutableStateFlow<CurrentUser?>(null)
        override val currentUser: StateFlow<CurrentUser?> = _currentUser.asStateFlow()

        private val _isLoggedIn = MutableStateFlow(false)
        override val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

        override suspend fun login(
            email: String,
            password: String
        ): Result<CurrentUser> {
            delay(MOCK_LOGIN_DELAY_MS)
            val user =
                CurrentUser(
                    id = MockUserIds.CURRENT_USER_ID,
                    nickname = "김기획",
                    experience = MOCK_USER_EXPERIENCE,
                    hasProfile = !email.contains(NO_PROFILE_EMAIL_HINT, ignoreCase = true)
                )
            _currentUser.value = user
            _isLoggedIn.value = true
            return Result.success(user)
        }

        override suspend fun continueAsGuest() {
            _currentUser.value = null
            _isLoggedIn.value = false
        }
    }
