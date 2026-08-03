package com.example.pickii.ui.mypage.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pickii.domain.repository.MasterDataRepository
import com.example.pickii.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** 이력서 형태의 내 프로필 조회 화면. [ProfileRepository]/[MasterDataRepository]를 조합해서 보여준다. */
@HiltViewModel
class ProfileViewModel
    @Inject
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
                    }.onFailure {
                        _uiState.update { it.copy(isLoading = false) }
                    }
            }
        }
    }
