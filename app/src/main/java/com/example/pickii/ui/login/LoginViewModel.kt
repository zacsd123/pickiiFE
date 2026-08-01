package com.example.pickii.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pickii.domain.repository.ProfileRepository
import com.example.pickii.domain.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** [LoginScreen]에 표시되는 입력 값과 토글 상태. */
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isAutoLoginChecked: Boolean = false
)

/** 로그인 화면의 입력 상태를 보관하고, [SessionRepository]를 통해 실제 로그인/비회원 전환을 처리한다. */
@HiltViewModel
class LoginViewModel
    @Inject
    constructor(
        private val sessionRepository: SessionRepository,
        private val profileRepository: ProfileRepository
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(LoginUiState())
        val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

        fun onEmailChange(value: String) {
            _uiState.update { it.copy(email = value) }
        }

        fun onPasswordChange(value: String) {
            _uiState.update { it.copy(password = value) }
        }

        fun onTogglePasswordVisibility() {
            _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
        }

        fun onToggleAutoLogin() {
            _uiState.update { it.copy(isAutoLoginChecked = !it.isAutoLoginChecked) }
        }

        /**
         * 입력된 이메일/비밀번호로 로그인을 시도하고, 성공하면 프로필(이력서) 보유 여부에 따라
         * [onNavigateHome] 또는 [onNavigateOnboarding]을 호출한다.
         *
         * @param onNavigateHome 로그인 성공 + 프로필 보유 시 호출되는 콜백
         * @param onNavigateOnboarding 로그인 성공 + 프로필 미보유 시 호출되는 콜백(최초 로그인 온보딩)
         */
        fun onLoginClick(
            onNavigateHome: () -> Unit,
            onNavigateOnboarding: () -> Unit
        ) {
            viewModelScope.launch {
                val state = _uiState.value
                sessionRepository.login(state.email, state.password).onSuccess {
                    if (profileRepository.hasResume()) onNavigateHome() else onNavigateOnboarding()
                }
            }
        }

        /**
         * 비회원으로 둘러보기를 선택한다. 로그아웃 상태로 세션을 정리한 뒤 [onComplete]를 호출한다.
         *
         * @param onComplete 처리 완료 시 호출되는 콜백(화면 전환 등 내비게이션 용도)
         */
        fun onGuestClick(onComplete: () -> Unit) {
            viewModelScope.launch {
                sessionRepository.continueAsGuest()
                onComplete()
            }
        }
    }
