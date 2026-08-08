package com.example.pickii.ui.mypage.myrecruits

import com.example.pickii.domain.model.MyRecruitSummary
import com.example.pickii.ui.common.PagedListUiState

/** 작성 공고 카드에서 확인 팝업을 띄울 동작 종류. */
enum class MyRecruitPendingAction {
    CLOSE,
    REOPEN,
    DELETE
}

/** [MyRecruitsScreen]에 표시되는 상태. */
data class MyRecruitsUiState(
    override val isLoading: Boolean = true,
    override val items: List<MyRecruitSummary> = emptyList(),
    override val currentPage: Int = 1,
    override val totalPages: Int = 1,
    val pendingRecruitId: String? = null,
    val pendingAction: MyRecruitPendingAction? = null,
    override val toastMessageRes: Int? = null
) : PagedListUiState<MyRecruitSummary>
