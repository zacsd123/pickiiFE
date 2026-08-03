package com.example.pickii.ui.mypage.myrecruits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pickii.domain.repository.MyPageActivityRepository
import com.example.pickii.domain.repository.RecruitRepository
import com.example.pickii.util.visiblePageNumbers
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val PAGE_SIZE = 10

/** 작성 공고 목록(4-5) 조회, 마감/추가모집/삭제(3-4, 3-5, 3-6)를 담당한다. */
@HiltViewModel
class MyRecruitsViewModel
    @Inject
    constructor(
        private val myPageActivityRepository: MyPageActivityRepository,
        private val recruitRepository: RecruitRepository
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(MyRecruitsUiState())
        val uiState: StateFlow<MyRecruitsUiState> = _uiState.asStateFlow()

        init {
            refresh()
        }

        fun refresh() {
            load(_uiState.value.currentPage)
        }

        private fun load(page: Int) {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true) }
                myPageActivityRepository
                    .getMyRecruits(page = page - 1, size = PAGE_SIZE)
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

        fun onActionRequest(
            recruitId: String,
            action: MyRecruitPendingAction
        ) {
            _uiState.update { it.copy(pendingRecruitId = recruitId, pendingAction = action) }
        }

        fun onDismissActionDialog() {
            _uiState.update { it.copy(pendingRecruitId = null, pendingAction = null) }
        }

        fun onConfirmAction() {
            val state = _uiState.value
            val recruitId = state.pendingRecruitId ?: return
            val action = state.pendingAction ?: return
            viewModelScope.launch {
                val result =
                    when (action) {
                        MyRecruitPendingAction.CLOSE -> recruitRepository.closePost(recruitId)
                        MyRecruitPendingAction.REOPEN -> recruitRepository.reopenAdditionalRecruiting(recruitId)
                        MyRecruitPendingAction.DELETE -> recruitRepository.deletePost(recruitId)
                    }
                _uiState.update { it.copy(pendingRecruitId = null, pendingAction = null) }
                if (result.isSuccess) refresh()
            }
        }
    }
