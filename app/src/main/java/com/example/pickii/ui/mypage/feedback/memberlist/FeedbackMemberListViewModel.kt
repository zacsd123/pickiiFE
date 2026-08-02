package com.example.pickii.ui.mypage.feedback.memberlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pickii.domain.repository.FeedbackRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** 평가 대상 팀원 목록(4-9)을 조회한다. [projectId]는 호출부에서 직접 전달한다(마이페이지 내부 상태 네비게이션). */
@HiltViewModel
class FeedbackMemberListViewModel
    @Inject
    constructor(
        private val repository: FeedbackRepository
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(FeedbackMemberListUiState())
        val uiState: StateFlow<FeedbackMemberListUiState> = _uiState.asStateFlow()

        fun load(projectId: String) {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true) }
                repository
                    .getProjectMembers(projectId)
                    .onSuccess { result ->
                        _uiState.update { it.copy(isLoading = false, members = result.members) }
                    }.onFailure {
                        _uiState.update { it.copy(isLoading = false) }
                    }
            }
        }
    }
