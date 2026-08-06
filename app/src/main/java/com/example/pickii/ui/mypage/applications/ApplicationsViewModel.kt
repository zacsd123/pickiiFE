package com.example.pickii.ui.mypage.applications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pickii.R
import com.example.pickii.domain.repository.MyPageActivityRepository
import com.example.pickii.util.visiblePageNumbers
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val PAGE_SIZE = 10

/** 지원 현황 목록(4-4) 조회 및 지원 취소(3-13)를 담당한다. */
@HiltViewModel
class ApplicationsViewModel
    @Inject
    constructor(
        private val repository: MyPageActivityRepository
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(ApplicationsUiState())
        val uiState: StateFlow<ApplicationsUiState> = _uiState.asStateFlow()

        init {
            refresh()
        }

        fun refresh() {
            load(_uiState.value.currentPage)
        }

        private fun load(page: Int) {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true) }
                repository
                    .getMyApplies(page = page - 1, size = PAGE_SIZE)
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
                        _uiState.update {
                            it.copy(isLoading = false, toastMessageRes = R.string.applications_toast_load_failed)
                        }
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

        fun onToastShown() {
            _uiState.update { it.copy(toastMessageRes = null) }
        }
    }
