package com.example.pickii.ui.mypage.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pickii.domain.repository.AccountRepository
import com.example.pickii.domain.repository.MasterDataRepository
import com.example.pickii.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** 이력서 형태의 내 프로필 조회 화면. [ProfileRepository]/[MasterDataRepository]/[AccountRepository]를 조합해서 보여준다. */
@HiltViewModel
class ProfileViewModel
    @Inject
    constructor(
        private val profileRepository: ProfileRepository,
        private val masterDataRepository: MasterDataRepository,
        private val accountRepository: AccountRepository
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(ProfileViewUiState())
        val uiState: StateFlow<ProfileViewUiState> = _uiState.asStateFlow()

        init {
            refresh()
        }

        fun refresh() {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true) }
                profileRepository
                    .getMyProfile()
                    .onSuccess { profile ->
                        val topics = masterDataRepository.getTopics().getOrDefault(emptyList())
                        val labels = profile.topicIds.mapNotNull { id -> topics.find { it.id == id }?.label }
                        _uiState.update { it.copy(isLoading = false, profile = profile, topicLabels = labels) }
                    }.onFailure {
                        _uiState.update { it.copy(isLoading = false) }
                    }
            }
            viewModelScope.launch {
                accountRepository.getSocialAccounts().onSuccess { accounts ->
                    _uiState.update { it.copy(socialAccounts = accounts) }
                }
            }
        }

        fun onLinkClick() {
            _uiState.update { it.copy(isSocialLinkComingSoonVisible = true) }
        }

        fun onSocialLinkComingSoonShown() {
            _uiState.update { it.copy(isSocialLinkComingSoonVisible = false) }
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
                refresh()
            }
        }
    }
