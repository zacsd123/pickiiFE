package com.example.pickii.ui.mypage.withdrawal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pickii.domain.model.EmailPurpose
import com.example.pickii.domain.repository.AccountRepository
import com.example.pickii.domain.repository.SessionRepository
import com.example.pickii.domain.repository.SignupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val DEFAULT_ERROR_MESSAGE = "잠시 후 다시 시도해주세요."

/**
 * 회원 탈퇴 화면(15번, 1-9). 이메일 인증(`purpose = WITHDRAWAL`) → 비밀번호 → 동의 2건 → [AccountRepository.withdraw].
 *
 * 이메일 인증 인프라는 회원가입/비밀번호 재설정과 동일한 [SignupRepository]를 재사용한다.
 */
@HiltViewModel
class WithdrawalViewModel
    @Inject
    constructor(
        private val signupRepository: SignupRepository,
        private val accountRepository: AccountRepository,
        private val sessionRepository: SessionRepository
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(WithdrawalUiState())
        val uiState: StateFlow<WithdrawalUiState> = _uiState.asStateFlow()

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
                    .sendEmailCode(email, EmailPurpose.WITHDRAWAL)
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
                    .verifyEmailCode(state.email, state.emailCode, EmailPurpose.WITHDRAWAL)
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

        fun onPasswordChange(value: String) {
            _uiState.update { it.copy(password = value) }
        }

        fun onTogglePasswordVisibility() {
            _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
        }

        fun onToggleDataDeletionAgreed() {
            _uiState.update { it.copy(isDataDeletionAgreed = !it.isDataDeletionAgreed) }
        }

        fun onToggleRejoinPolicyAgreed() {
            _uiState.update { it.copy(isRejoinPolicyAgreed = !it.isRejoinPolicyAgreed) }
        }

        fun onWithdrawClick() {
            val state = _uiState.value
            val token = state.emailVerificationToken ?: return
            if (!state.isSubmitEnabled) return

            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
            viewModelScope.launch {
                accountRepository
                    .withdraw(
                        password = state.password,
                        emailVerificationToken = token,
                        dataDeletionAgreed = state.isDataDeletionAgreed,
                        rejoinPolicyAgreed = state.isRejoinPolicyAgreed
                    ).onSuccess {
                        sessionRepository.clearSession()
                        _uiState.update { it.copy(isSubmitting = false, isComplete = true) }
                    }.onFailure { error ->
                        _uiState.update {
                            it.copy(isSubmitting = false, errorMessage = error.message ?: DEFAULT_ERROR_MESSAGE)
                        }
                    }
            }
        }
    }
