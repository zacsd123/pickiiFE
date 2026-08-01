package com.example.pickii.ui.findid

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pickii.R
import com.example.pickii.ui.common.RecruitUiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** [FindIdScreen]에 표시되는 상태. */
data class FindIdUiState(
    val name: String = "",
    val email: String = "",
    val isEmailCodeSent: Boolean = false,
    val emailCode: String = "",
    val isEmailVerified: Boolean = false
) {
    /** "코드 발송" 버튼 활성화 조건: 이름/이메일을 모두 입력. */
    val isSendCodeEnabled: Boolean
        get() = name.isNotBlank() && email.isNotBlank()

    /** "인증하기" 버튼 활성화 조건: 인증코드를 입력. */
    val isVerifyEnabled: Boolean
        get() = emailCode.isNotBlank()

    /** "아이디 찾기" 버튼 활성화 조건: 이메일 인증 완료. */
    val isSubmitEnabled: Boolean
        get() = isEmailVerified
}

/**
 * 아이디 찾기 화면의 입력 상태를 보관한다.
 *
 * 이름+이메일로 계정을 조회해 아이디(닉네임)를 돌려주는 API가 아직 명세서에 없어, 이번 화면은
 * 참고 디자인과 입력 UI만 구현하고 버튼 동작은 안내 토스트만 띄운다. 백엔드 API가 정해지면
 * [onSendCodeClick]/[onVerifyClick]/[onFindIdClick]을 실제 리포지토리 호출로 바꾸면 된다.
 */
@HiltViewModel
class FindIdViewModel
    @Inject
    constructor() : ViewModel() {
        private val _uiState = MutableStateFlow(FindIdUiState())
        val uiState: StateFlow<FindIdUiState> = _uiState.asStateFlow()

        private val _events = Channel<RecruitUiEvent>(Channel.BUFFERED)
        val events: Flow<RecruitUiEvent> = _events.receiveAsFlow()

        fun onNameChange(value: String) {
            _uiState.update { it.copy(name = value) }
        }

        fun onEmailChange(value: String) {
            _uiState.update {
                it.copy(email = value, isEmailCodeSent = false, isEmailVerified = false, emailCode = "")
            }
        }

        fun onSendCodeClick() {
            if (!_uiState.value.isSendCodeEnabled) return
            emitUnavailableToast()
        }

        fun onEmailCodeChange(value: String) {
            _uiState.update { it.copy(emailCode = value) }
        }

        fun onVerifyClick() {
            if (!_uiState.value.isVerifyEnabled) return
            emitUnavailableToast()
        }

        fun onFindIdClick() {
            if (!_uiState.value.isSubmitEnabled) return
            emitUnavailableToast()
        }

        private fun emitUnavailableToast() {
            viewModelScope.launch {
                _events.send(RecruitUiEvent.ShowToast(R.string.account_recovery_toast_find_id_unavailable))
            }
        }
    }
