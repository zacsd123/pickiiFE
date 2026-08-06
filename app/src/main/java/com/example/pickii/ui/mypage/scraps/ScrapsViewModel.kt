package com.example.pickii.ui.mypage.scraps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pickii.R
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

/** 스크랩한 공고 목록(3-16) 조회 및 스크랩 해제(3-15)를 담당한다. */
@HiltViewModel
class ScrapsViewModel
    @Inject
    constructor(
        private val myPageActivityRepository: MyPageActivityRepository,
        private val recruitRepository: RecruitRepository
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(ScrapsUiState())
        val uiState: StateFlow<ScrapsUiState> = _uiState.asStateFlow()

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
                    .getMyScraps(page = page - 1, size = PAGE_SIZE)
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
                            it.copy(isLoading = false, toastMessageRes = R.string.scraps_toast_load_failed)
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

        /** 목록에서 바로 스크랩을 해제한다(확인 팝업 없이 즉시 해제 — 공고 상세 화면의 스크랩 토글과 동일한 정책). */
        fun onUnscrapClick(recruitId: String) {
            viewModelScope.launch {
                recruitRepository
                    .unscrapPost(recruitId)
                    .onSuccess {
                        _uiState.update { state ->
                            state.copy(items = state.items.filterNot { it.recruitId == recruitId })
                        }
                    }.onFailure {
                        _uiState.update { it.copy(toastMessageRes = R.string.scraps_toast_unscrap_failed) }
                    }
            }
        }

        fun onToastShown() {
            _uiState.update { it.copy(toastMessageRes = null) }
        }
    }
