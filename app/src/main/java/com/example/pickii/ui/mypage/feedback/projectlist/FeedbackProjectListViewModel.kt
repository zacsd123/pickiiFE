package com.example.pickii.ui.mypage.feedback.projectlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pickii.domain.repository.FeedbackRepository
import com.example.pickii.util.visiblePageNumbers
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val PAGE_SIZE = 10

/** 내가 참여한 종료 프로젝트의 상호평가 진행 현황 목록(4-11)을 조회한다. */
@HiltViewModel
class FeedbackProjectListViewModel
    @Inject
    constructor(
        private val repository: FeedbackRepository
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(FeedbackProjectListUiState())
        val uiState: StateFlow<FeedbackProjectListUiState> = _uiState.asStateFlow()

        init {
            load(1)
        }

        private fun load(page: Int) {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true) }
                repository
                    .getMyFeedbackProjects(page = page - 1, size = PAGE_SIZE)
                    .onSuccess { result ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                items = result.items,
                                currentPage = result.currentPage + 1,
                                totalPages = result.totalPages.coerceAtLeast(1)
                            )
                        }
                    }.onFailure {
                        _uiState.update { it.copy(isLoading = false) }
                    }
            }
        }

        val visiblePageNumbers: List<Int>
            get() = visiblePageNumbers(_uiState.value.currentPage, _uiState.value.totalPages)

        fun onPageClick(page: Int) = load(page)

        fun onPreviousPage() {
            if (_uiState.value.currentPage <= 1) return
            load(_uiState.value.currentPage - 1)
        }

        fun onNextPage() {
            if (_uiState.value.currentPage >= _uiState.value.totalPages) return
            load(_uiState.value.currentPage + 1)
        }
    }
