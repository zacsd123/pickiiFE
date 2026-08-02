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

/** 설정 화면의 계정 설정 섹션(카카오 연동 상태 표시, 로그아웃)을 담당한다. */
@HiltViewModel
class SettingsViewModel
    @Inject
    constructor(
        private val accountRepository: AccountRepository,
        private val sessionRepository: SessionRepository
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(SettingsUiState())
        val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

        init {
            viewModelScope.launch {
                accountRepository.getSocialAccounts().onSuccess { accounts ->
                    _uiState.update {
                        it.copy(
                            isKakaoLinked =
                                accounts.find { a -> a.provider == "KAKAO" }?.linked == true
                        )
                    }
                }
            }
        }

        fun onLogoutRequest() {
            _uiState.update { it.copy(isLogoutConfirmVisible = true) }
        }

        fun onDismissLogoutDialog() {
            _uiState.update { it.copy(isLogoutConfirmVisible = false) }
        }

        fun onConfirmLogout(onLoggedOut: () -> Unit) {
            viewModelScope.launch {
                sessionRepository.logout()
                _uiState.update { it.copy(isLogoutConfirmVisible = false) }
                onLoggedOut()
            }
        }
    }
