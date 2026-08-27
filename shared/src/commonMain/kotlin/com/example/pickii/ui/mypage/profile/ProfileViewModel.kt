package com.example.pickii.ui.mypage.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pickii.data.remote.dto.ApiException
import com.example.pickii.domain.repository.MasterDataRepository
import com.example.pickii.domain.repository.ProfileRepository
import com.example.pickii.shared.generated.resources.Res
import com.example.pickii.shared.generated.resources.profile_view_toast_load_failed
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val ERROR_CODE_RESUME_NOT_FOUND = "RESUME_NOT_FOUND"

/** 이력서 형태의 내 프로필 조회 화면. [ProfileRepository]/[MasterDataRepository]를 조합해서 보여준다. */
class ProfileViewModel
    constructor(
        private val profileRepository: ProfileRepository,
        private val masterDataRepository: MasterDataRepository
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
                    }.onFailure { error ->
                        val isNoResume = error is ApiException && error.code == ERROR_CODE_RESUME_NOT_FOUND
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                toastMessageRes = if (isNoResume) null else Res.string.profile_view_toast_load_failed
                            )
                        }
                    }
            }
        }

        fun onToastShown() {
            _uiState.update { it.copy(toastMessageRes = null) }
        }
    }
