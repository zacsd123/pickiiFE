package com.example.pickii.ui.passwordreset

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pickii.domain.model.EmailPurpose
import com.example.pickii.domain.repository.SignupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** 비밀번호 규칙: 8~20자, 영문 대/소문자·숫자 각 1자 이상 포함(0.7 공통 Validation 규칙). */
private val PASSWORD_REGEX = Regex("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).{8,20}$")

private const val DEFAULT_ERROR_MESSAGE = "잠시 후 다시 시도해주세요."

/** [PasswordResetScreen]에 표시되는 상태. */
data class PasswordResetUiState(
    val email: String = "",
    val isSendingEmailCode: Boolean = false,
    val isEmailCodeSent: Boolean = false,
    val emailCode: String = "",
    val isVerifyingEmailCode: Boolean = false,
    val isEmailVerified: Boolean = false,
    val emailVerificationToken: String? = null,
    val emailMessage: String? = null,
    val isEmailMessageError: Boolean = false,
    val newPassword: String = "",
    val isPasswordVisible: Boolean = false,
    val newPasswordConfirm: String = "",
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val isComplete: Boolean = false
) {
    /** 새 비밀번호가 8~20자, 영문 대/소문자·숫자를 각 1자 이상 포함하는지 여부. */
    val isPasswordValid: Boolean
        get() = PASSWORD_REGEX.matches(newPassword)

    /** 비밀번호 확인이 새 비밀번호와 일치하는지 여부. */
    val isPasswordMatching: Boolean
        get() = newPassword.isNotEmpty() && newPassword == newPasswordConfirm

    /** "비밀번호 재설정" 버튼 활성화 조건: 이메일 인증 완료, 새 비밀번호 유효+일치. */
    val isSubmitEnabled: Boolean
        get() = isEmailVerified && isPasswordValid && isPasswordMatching && !isSubmitting
}

/**
 * 비로그인 상태 비밀번호 재설정 화면의 상태를 보관한다.
 *
 * 이메일 인증(`purpose = PW_RESET`) 후 `SignupRepository.resetPassword`로 제출한다. 이메일 인증
 * 인프라는 회원가입 화면과 동일한 [SignupRepository]를 재사용한다.
 */
@HiltViewModel
class PasswordResetViewModel
    @Inject
    constructor(
        private val signupRepository: SignupRepository
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(PasswordResetUiState())
        val uiState: StateFlow<PasswordResetUiState> = _uiState.asStateFlow()

        fun onEmailChange(value: String) {
            _uiState.update {
                it.copy(
                    email = value,
                    isEmailCodeSent = false,
                    isEmailVerified = false,
                    emailVerificationToken = null,
                    emailCode = "",
                    emailMessage = null
                )
            }
        }

        fun onSendEmailCodeClick() {
            val email = _uiState.value.email
            if (email.isBlank()) return
            _uiState.update { it.copy(isSendingEmailCode = true) }
            viewModelScope.launch {
                signupRepository
                    .sendEmailCode(email, EmailPurpose.PW_RESET)
                    .onSuccess {
                        _uiState.update {
                            it.copy(
                                isSendingEmailCode = false,
                                isEmailCodeSent = true,
                                emailMessage = "인증코드를 발송했습니다.",
                                isEmailMessageError = false
                            )
                        }
                    }.onFailure { error ->
                        _uiState.update {
                            it.copy(
                                isSendingEmailCode = false,
                                emailMessage = error.message ?: DEFAULT_ERROR_MESSAGE,
                                isEmailMessageError = true
                            )
                        }
                    }
            }
        }

        fun onEmailCodeChange(value: String) {
            _uiState.update { it.copy(emailCode = value) }
        }

        fun onVerifyEmailCodeClick() {
            val state = _uiState.value
            if (state.emailCode.isBlank()) return
            _uiState.update { it.copy(isVerifyingEmailCode = true) }
            viewModelScope.launch {
                signupRepository
                    .verifyEmailCode(state.email, state.emailCode, EmailPurpose.PW_RESET)
                    .onSuccess { token ->
                        _uiState.update {
                            it.copy(
                                isVerifyingEmailCode = false,
                                isEmailVerified = true,
                                emailVerificationToken = token,
                                emailMessage = "인증되었습니다.",
                                isEmailMessageError = false
                            )
                        }
                    }.onFailure { error ->
                        _uiState.update {
                            it.copy(
                                isVerifyingEmailCode = false,
                                isEmailVerified = false,
                                emailMessage = error.message ?: DEFAULT_ERROR_MESSAGE,
                                isEmailMessageError = true
                            )
                        }
                    }
            }
        }

        fun onNewPasswordChange(value: String) {
            _uiState.update { it.copy(newPassword = value) }
        }

        fun onTogglePasswordVisibility() {
            _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
        }

        fun onNewPasswordConfirmChange(value: String) {
            _uiState.update { it.copy(newPasswordConfirm = value) }
        }

        fun onSubmitClick() {
            val state = _uiState.value
            val token = state.emailVerificationToken ?: return
            if (!state.isSubmitEnabled) return

            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
            viewModelScope.launch {
                signupRepository
                    .resetPassword(
                        email = state.email,
                        emailVerificationToken = token,
                        newPassword = state.newPassword,
                        newPasswordConfirm = state.newPasswordConfirm
                    ).onSuccess {
                        _uiState.update { it.copy(isSubmitting = false, isComplete = true) }
                    }.onFailure { error ->
                        _uiState.update {
                            it.copy(isSubmitting = false, errorMessage = error.message ?: DEFAULT_ERROR_MESSAGE)
                        }
                    }
            }
        }
    }
