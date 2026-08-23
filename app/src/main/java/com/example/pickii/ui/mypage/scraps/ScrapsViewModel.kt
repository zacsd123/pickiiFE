package com.example.pickii.ui.mypage.scraps

import androidx.lifecycle.viewModelScope
import com.example.pickii.R
import com.example.pickii.domain.model.MyScrap
import com.example.pickii.domain.repository.MyPageActivityRepository
import com.example.pickii.domain.repository.RecruitRepository
import com.example.pickii.ui.common.PagedFetchResult
import com.example.pickii.ui.common.PagedListViewModel
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** 스크랩한 공고 목록(3-16) 조회 및 스크랩 해제(3-15)를 담당한다. */
class ScrapsViewModel
    constructor(
        private val myPageActivityRepository: MyPageActivityRepository,
        private val recruitRepository: RecruitRepository
    ) : PagedListViewModel<MyScrap, ScrapsUiState>(
            initialState = ScrapsUiState(),
            loadFailureMessageRes = R.string.scraps_toast_load_failed
        ) {
        init {
            refresh()
        }

        override suspend fun fetchPage(
            page: Int,
            size: Int
        ): Result<PagedFetchResult<MyScrap>> =
            myPageActivityRepository
                .getMyScraps(page = page, size = size)
                .map { PagedFetchResult(it.items, it.currentPage, it.totalPages) }

        override fun ScrapsUiState.withPagedState(
            isLoading: Boolean,
            items: List<MyScrap>,
            currentPage: Int,
            totalPages: Int,
            toastMessageRes: Int?
        ): ScrapsUiState =
            copy(
                isLoading = isLoading,
                items = items,
                currentPage = currentPage,
                totalPages = totalPages,
                toastMessageRes = toastMessageRes
            )

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
    }
