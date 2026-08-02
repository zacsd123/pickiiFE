package com.example.pickii.ui.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pickii.domain.model.SignupTerms
import com.example.pickii.domain.repository.ProfileRepository
import com.example.pickii.domain.repository.SessionRepository
import com.example.pickii.domain.repository.SignupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** 회원가입 화면이 어느 단계를 보여주는 중인지. */
enum class SignupPhase {
    /** 닉네임/이메일/비밀번호/약관을 입력받는 가입 폼. */
    FORM,

    /** 가입 완료 후 입력한 정보를 보여주고 로그인으로 이어주는 환영 화면. */
    WELCOME
}

/** 비밀번호 규칙: 8~20자, 영문 대/소문자·숫자 각 1자 이상 포함(0.7 공통 Validation 규칙). */
private val PASSWORD_REGEX = Regex("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).{8,20}$")

private const val DEFAULT_ERROR_MESSAGE = "잠시 후 다시 시도해주세요."

/** [SignupScreen]에 표시되는 상태. */
data class SignupUiState(
    val phase: SignupPhase = SignupPhase.FORM,
    val nickname: String = "",
    val isCheckingNickname: Boolean = false,
    val isNicknameVerified: Boolean = false,
    val nicknameVerificationToken: String? = null,
    val nicknameMessage: String? = null,
    val isNicknameMessageError: Boolean = false,
    val email: String = "",
    val isSendingEmailCode: Boolean = false,
    val isEmailCodeSent: Boolean = false,
    val emailCode: String = "",
    val isVerifyingEmailCode: Boolean = false,
    val isEmailVerified: Boolean = false,
    val emailVerificationToken: String? = null,
    val emailMessage: String? = null,
    val isEmailMessageError: Boolean = false,
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val passwordConfirm: String = "",
    val terms: SignupTerms = SignupTerms(),
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null
) {
    /** 비밀번호가 8~20자, 영문 대/소문자·숫자를 각 1자 이상 포함하는지 여부. */
    val isPasswordValid: Boolean
        get() = PASSWORD_REGEX.matches(password)

    /** 비밀번호 확인이 비밀번호와 일치하는지 여부. */
    val isPasswordMatching: Boolean
        get() = password.isNotEmpty() && password == passwordConfirm

    /** "회원가입하기" 버튼 활성화 조건: 닉네임/이메일 인증 완료, 비밀번호 유효+일치, 필수 약관 전부 동의. */
    val isSubmitEnabled: Boolean
        get() =
            isNicknameVerified &&
                isEmailVerified &&
                isPasswordValid &&
                isPasswordMatching &&
                terms.isAllRequiredAgreed &&
                !isSubmitting
}

/**
 * 회원가입 폼과 가입완료(환영) 화면의 상태를 함께 보관한다.
 *
 * 환영 화면의 "로그인하기" 버튼이 방금 입력한 비밀번호로 바로 로그인해야 해서, 비밀번호를 내비게이션
 * 인자로 넘기지 않고 두 화면을 한 ViewModel/한 화면으로 묶어 [phase]로 전환한다.
 */
@HiltViewModel
class SignupViewModel
    @Inject
    constructor(
        private val signupRepository: SignupRepository,
        private val sessionRepository: SessionRepository,
        private val profileRepository: ProfileRepository
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(SignupUiState())
        val uiState: StateFlow<SignupUiState> = _uiState.asStateFlow()

        fun onNicknameChange(value: String) {
            _uiState.update {
                it.copy(
                    nickname = value,
                    isNicknameVerified = false,
                    nicknameVerificationToken = null,
                    nicknameMessage = null
                )
            }
        }

        fun onCheckNicknameClick() {
            val nickname = _uiState.value.nickname
            if (nickname.isBlank()) return
            _uiState.update { it.copy(isCheckingNickname = true) }
            viewModelScope.launch {
                signupRepository
                    .checkNickname(nickname)
                    .onSuccess { token ->
                        _uiState.update {
                            it.copy(
                                isCheckingNickname = false,
                                isNicknameVerified = true,
                                nicknameVerificationToken = token,
                                nicknameMessage = "사용 가능한 닉네임입니다.",
                                isNicknameMessageError = false
                            )
                        }
                    }.onFailure { error ->
                        _uiState.update {
                            it.copy(
                                isCheckingNickname = false,
                                isNicknameVerified = false,
                                nicknameMessage = error.message ?: DEFAULT_ERROR_MESSAGE,
                                isNicknameMessageError = true
                            )
                        }
                    }
            }
        }

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
                    .sendEmailCode(email)
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
                    .verifyEmailCode(state.email, state.emailCode)
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

        fun onPasswordConfirmChange(value: String) {
            _uiState.update { it.copy(passwordConfirm = value) }
        }

        fun onAgeAgreedChange(checked: Boolean) = updateTerms { it.copy(ageAgreed = checked) }

        fun onTermsAgreedChange(checked: Boolean) = updateTerms { it.copy(termsAgreed = checked) }

        fun onPrivacyAgreedChange(checked: Boolean) = updateTerms { it.copy(privacyAgreed = checked) }

        fun onProfileShareAgreedChange(checked: Boolean) = updateTerms { it.copy(profileShareAgreed = checked) }

        fun onDataCollectionAgreedChange(checked: Boolean) = updateTerms { it.copy(dataCollectionAgreed = checked) }

        fun onPushNotiAgreedChange(checked: Boolean) = updateTerms { it.copy(pushNotiAgreed = checked) }

        /** "전체 선택" 체크박스. 이미 전부 동의된 상태면 전부 해제하고, 아니면 전부 동의한다. */
        fun onToggleAllTerms() {
            val allAgreed = !_uiState.value.terms.isAllAgreed
            updateTerms {
                SignupTerms(
                    ageAgreed = allAgreed,
                    termsAgreed = allAgreed,
                    privacyAgreed = allAgreed,
                    profileShareAgreed = allAgreed,
                    dataCollectionAgreed = allAgreed,
                    pushNotiAgreed = allAgreed
                )
            }
        }

        private fun updateTerms(transform: (SignupTerms) -> SignupTerms) {
            _uiState.update { it.copy(terms = transform(it.terms)) }
        }

        /** 회원가입을 확정한다. 성공하면 [SignupPhase.WELCOME] 단계로 전환한다. */
        fun onSubmitClick() {
            val state = _uiState.value
            val emailToken = state.emailVerificationToken ?: return
            val nicknameToken = state.nicknameVerificationToken ?: return
            if (!state.isSubmitEnabled) return

            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
            viewModelScope.launch {
                signupRepository
                    .signUp(
                        nickname = state.nickname,
                        email = state.email,
                        password = state.password,
                        passwordConfirm = state.passwordConfirm,
                        emailVerificationToken = emailToken,
                        nicknameVerificationToken = nicknameToken,
                        terms = state.terms
                    ).onSuccess {
                        _uiState.update { it.copy(isSubmitting = false, phase = SignupPhase.WELCOME) }
                    }.onFailure { error ->
                        _uiState.update {
                            it.copy(isSubmitting = false, errorMessage = error.message ?: DEFAULT_ERROR_MESSAGE)
                        }
                    }
            }
        }

        /**
         * 환영 화면의 "로그인하기" 버튼. 방금 가입한 이메일/비밀번호로 로그인한 뒤, 프로필 보유 여부에 따라
         * 온보딩 또는 홈으로 이동시킨다.
         */
        fun onLoginClick(
            onNavigateHome: () -> Unit,
            onNavigateOnboarding: () -> Unit
        ) {
            val state = _uiState.value
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
            viewModelScope.launch {
                sessionRepository
                    .login(state.email, state.password)
                    .onSuccess {
                        _uiState.update { it.copy(isSubmitting = false) }
                        if (profileRepository.hasResume()) onNavigateHome() else onNavigateOnboarding()
                    }.onFailure { error ->
                        _uiState.update {
                            it.copy(isSubmitting = false, errorMessage = error.message ?: DEFAULT_ERROR_MESSAGE)
                        }
                    }
            }
        }
    }
