package com.example.pickii.ui.mypage.applications

import androidx.lifecycle.viewModelScope
import com.example.pickii.R
import com.example.pickii.domain.model.MyApply
import com.example.pickii.domain.repository.MyPageActivityRepository
import com.example.pickii.ui.common.PagedFetchResult
import com.example.pickii.ui.common.PagedListViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** 지원 현황 목록(4-4) 조회 및 지원 취소(3-13)를 담당한다. */
@HiltViewModel
class ApplicationsViewModel
    @Inject
    constructor(
        private val repository: MyPageActivityRepository
    ) : PagedListViewModel<MyApply, ApplicationsUiState>(
            initialState = ApplicationsUiState(),
            loadFailureMessageRes = R.string.applications_toast_load_failed
        ) {
        init {
            refresh()
        }

        override suspend fun fetchPage(
            page: Int,
            size: Int
        ): Result<PagedFetchResult<MyApply>> =
            repository.getMyApplies(page = page, size = size).map { PagedFetchResult(it.items, it.currentPage, it.totalPages) }

        override fun ApplicationsUiState.withPagedState(
            isLoading: Boolean,
            items: List<MyApply>,
            currentPage: Int,
            totalPages: Int,
            toastMessageRes: Int?
        ): ApplicationsUiState =
            copy(
                isLoading = isLoading,
                items = items,
                currentPage = currentPage,
                totalPages = totalPages,
                toastMessageRes = toastMessageRes
            )

        fun onCancelRequest(applyId: String) {
            _uiState.update { it.copy(pendingCancelApplyId = applyId) }
        }

        fun onDismissCancelDialog() {
            _uiState.update { it.copy(pendingCancelApplyId = null) }
        }

        fun onConfirmCancel() {
            val applyId = _uiState.value.pendingCancelApplyId ?: return
            viewModelScope.launch {
                repository
                    .cancelApply(applyId)
                    .onSuccess {
                        _uiState.update {
                            it.copy(
                                pendingCancelApplyId = null,
                                toastMessageRes = R.string.mypage_applications_cancel_toast
                            )
                        }
                        refresh()
                    }.onFailure {
                        _uiState.update {
                            it.copy(
                                pendingCancelApplyId = null,
                                toastMessageRes = R.string.applications_toast_cancel_failed
                            )
                        }
                    }
            }
        }
    }
