package com.example.pickii.ui.mypage.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pickii.domain.repository.NotificationRepository
import com.example.pickii.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** 마이페이지 홈의 프로필 요약(닉네임/레벨/경험치)을 [ProfileRepository]에서 불러온다. */
@HiltViewModel
class MyPageHomeViewModel
    @Inject
    constructor(
        private val profileRepository: ProfileRepository,
        private val notificationRepository: NotificationRepository
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(MyPageHomeUiState())
        val uiState: StateFlow<MyPageHomeUiState> = _uiState.asStateFlow()

        init {
            refresh()
        }

        private fun refresh() {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true) }
                profileRepository
                    .getMyProfile()
                    .onSuccess { profile ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                hasProfile = true,
                                nickname = profile.nickname,
                                exp = profile.exp
                            )
                        }
                    }.onFailure {
                        _uiState.update { it.copy(isLoading = false, hasProfile = false) }
                    }
            }
            viewModelScope.launch {
                notificationRepository.getUnreadCount().onSuccess { count ->
                    _uiState.update { it.copy(unreadNotificationCount = count) }
                }
            }
        }
    }
