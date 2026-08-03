package com.example.pickii.ui.mypage.myrecruits

import com.example.pickii.domain.model.MyRecruitSummary

/** 작성 공고 카드에서 확인 팝업을 띄울 동작 종류. */
enum class MyRecruitPendingAction {
    CLOSE,
    REOPEN,
    DELETE
}

/** [MyRecruitsScreen]에 표시되는 상태. */
data class MyRecruitsUiState(
    val isLoading: Boolean = true,
    val items: List<MyRecruitSummary> = emptyList(),
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val pendingRecruitId: String? = null,
    val pendingAction: MyRecruitPendingAction? = null
)
