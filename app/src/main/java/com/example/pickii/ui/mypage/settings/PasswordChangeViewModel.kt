package com.example.pickii.ui.mypage.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pickii.domain.repository.AccountRepository
import com.example.pickii.domain.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 로그인 상태 비밀번호 변경 화면(사진 목업 기준: 현재 비밀번호 입력 포함).
 *
 * [AccountRepository.changePassword]는 가정 엔드포인트라 백엔드 준비 전까지 실패할 수 있다. 성공 시에는 보안을 위해
 * 실제 1-12 정책과 동일하게 모든 기기에서 로그아웃시키고 로그인 화면으로 보낸다.
 */
@HiltViewModel
class PasswordChangeViewModel
    @Inject
    constructor(
        private val accountRepository: AccountRepository,
        private val sessionRepository: SessionRepository
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(PasswordChangeUiState())
        val uiState: StateFlow<PasswordChangeUiState> = _uiState.asStateFlow()

        fun onCurrentPasswordChange(value: String) {
            _uiState.update { it.copy(currentPassword = value) }
        }

        fun onNewPasswordChange(value: String) {
            _uiState.update { it.copy(newPassword = value) }
        }

        fun onNewPasswordConfirmChange(value: String) {
            _uiState.update { it.copy(newPasswordConfirm = value) }
        }

        fun onToggleCurrentPasswordVisibility() {
            _uiState.update { it.copy(isCurrentPasswordVisible = !it.isCurrentPasswordVisible) }
        }

        fun onToggleNewPasswordVisibility() {
            _uiState.update { it.copy(isNewPasswordVisible = !it.isNewPasswordVisible) }
        }

        fun onSubmitClick(onSuccess: () -> Unit) {
            val state = _uiState.value
            if (!state.isValid) return

            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
            viewModelScope.launch {
                accountRepository
                    .changePassword(
                        currentPassword = state.currentPassword,
                        newPassword = state.newPassword,
                        newPasswordConfirm = state.newPasswordConfirm
                    ).onSuccess {
                        sessionRepository.clearSession()
                        _uiState.update { it.copy(isSubmitting = false) }
                        onSuccess()
                    }.onFailure { error ->
                        _uiState.update { it.copy(isSubmitting = false, errorMessage = error.message) }
                    }
            }
        }
    }
