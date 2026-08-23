package com.example.pickii.ui.mypage.myrecruits

import androidx.lifecycle.viewModelScope
import com.example.pickii.R
import com.example.pickii.domain.model.MyRecruitSummary
import com.example.pickii.domain.repository.MyPageActivityRepository
import com.example.pickii.domain.repository.RecruitRepository
import com.example.pickii.ui.common.PagedFetchResult
import com.example.pickii.ui.common.PagedListViewModel
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** 작성 공고 목록(4-5) 조회, 마감/추가모집/삭제(3-4, 3-5, 3-6)를 담당한다. */
class MyRecruitsViewModel
    constructor(
        private val myPageActivityRepository: MyPageActivityRepository,
        private val recruitRepository: RecruitRepository
    ) : PagedListViewModel<MyRecruitSummary, MyRecruitsUiState>(
            initialState = MyRecruitsUiState(),
            loadFailureMessageRes = R.string.myrecruits_toast_load_failed
        ) {
        override suspend fun fetchPage(
            page: Int,
            size: Int
        ): Result<PagedFetchResult<MyRecruitSummary>> =
            myPageActivityRepository
                .getMyRecruits(page = page, size = size)
                .map { PagedFetchResult(it.items, it.currentPage, it.totalPages) }

        override fun MyRecruitsUiState.withPagedState(
            isLoading: Boolean,
            items: List<MyRecruitSummary>,
            currentPage: Int,
            totalPages: Int,
            toastMessageRes: Int?
        ): MyRecruitsUiState =
            copy(
                isLoading = isLoading,
                items = items,
                currentPage = currentPage,
                totalPages = totalPages,
                toastMessageRes = toastMessageRes
            )

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
                _uiState.update {
                    it.copy(
                        pendingRecruitId = null,
                        pendingAction = null,
                        toastMessageRes = if (result.isFailure) R.string.myrecruits_toast_action_failed else null
                    )
                }
                if (result.isSuccess) refresh()
            }
        }
    }
