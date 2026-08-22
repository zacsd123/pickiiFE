package com.example.pickii.ui.memberprofile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pickii.R
import com.example.pickii.data.remote.dto.ApiException
import com.example.pickii.domain.repository.MasterDataRepository
import com.example.pickii.domain.repository.ProfileRepository
import com.example.pickii.ui.mypage.profile.ProfileViewUiState
import com.example.pickii.ui.navigation.ARG_MEMBER_ID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val ERROR_CODE_RESUME_NOT_FOUND = "RESUME_NOT_FOUND"

/** 다른 회원의 프로필(10-1)을 조회한다. 공고 작성자 닉네임 클릭 등 앱 여러 곳에서 재사용한다. */
class MemberProfileViewModel
    constructor(
        savedStateHandle: SavedStateHandle,
        private val profileRepository: ProfileRepository,
        private val masterDataRepository: MasterDataRepository
    ) : ViewModel() {
        private val memberId: String = requireNotNull(savedStateHandle[ARG_MEMBER_ID]) { "memberId가 필요합니다." }

        private val _uiState = MutableStateFlow(ProfileViewUiState())
        val uiState: StateFlow<ProfileViewUiState> = _uiState.asStateFlow()

        init {
            refresh()
        }

        private fun refresh() {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true) }
                profileRepository
                    .getMemberProfile(memberId)
                    .onSuccess { profile ->
                        val topics = masterDataRepository.getTopics().getOrDefault(emptyList())
                        val labels = profile.topicIds.mapNotNull { id -> topics.find { it.id == id }?.label }
                        _uiState.update { it.copy(isLoading = false, profile = profile, topicLabels = labels) }
                    }.onFailure { error ->
                        val isNoResume = error is ApiException && error.code == ERROR_CODE_RESUME_NOT_FOUND
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                profile = null,
                                toastMessageRes = if (isNoResume) null else R.string.member_profile_toast_load_failed
                            )
                        }
                    }
            }
        }

        fun onToastShown() {
            _uiState.update { it.copy(toastMessageRes = null) }
        }
    }
