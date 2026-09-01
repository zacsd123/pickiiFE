package com.example.pickii.ui.mypage.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pickii.domain.repository.AccountRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** 설정 화면의 계정 설정 섹션(카카오 연동/해제, 로그아웃)을 담당한다. */
class SettingsViewModel
    constructor(
        private val accountRepository: AccountRepository
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(SettingsUiState())
        val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

        init {
            refreshSocialAccounts()
        }

        private fun refreshSocialAccounts() {
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

        /**
         * 카카오 SDK로 얻은 사용자 id를 현재 계정에 연동한다(1-11).
         *
         * @param providerId 카카오 SDK `UserApiClient.me()`로 얻은 사용자 고유 id
         * @param onError 연동 요청이 실패했을 때 호출되는 콜백(이미 연동된 계정 등)
         */
        fun onKakaoLinkConfirmed(
            providerId: String,
            onError: () -> Unit
        ) {
            viewModelScope.launch {
                accountRepository
                    .linkSocialAccount("KAKAO", providerId)
                    .onSuccess { refreshSocialAccounts() }
                    .onFailure { error ->
                        println("SettingsViewModel: linkSocialAccount failed: $error")
                        onError()
                    }
            }
        }

        fun onUnlinkRequest(provider: String) {
            _uiState.update { it.copy(isUnlinkConfirmVisible = true, pendingUnlinkProvider = provider) }
        }

        fun onDismissUnlinkDialog() {
            _uiState.update { it.copy(isUnlinkConfirmVisible = false, pendingUnlinkProvider = null) }
        }

        fun onConfirmUnlink() {
            val provider = _uiState.value.pendingUnlinkProvider ?: return
            viewModelScope.launch {
                accountRepository.unlinkSocialAccount(provider)
                _uiState.update { it.copy(isUnlinkConfirmVisible = false, pendingUnlinkProvider = null) }
                refreshSocialAccounts()
            }
        }
    }
