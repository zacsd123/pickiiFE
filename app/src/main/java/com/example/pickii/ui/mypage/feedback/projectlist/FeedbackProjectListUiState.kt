package com.example.pickii.ui.mypage.feedback.projectlist

import com.example.pickii.domain.model.FeedbackProject

/** [FeedbackProjectListScreen]에 표시되는 상태. */
data class FeedbackProjectListUiState(
    val isLoading: Boolean = true,
    val items: List<FeedbackProject> = emptyList(),
    val currentPage: Int = 1,
    val totalPages: Int = 1
)
