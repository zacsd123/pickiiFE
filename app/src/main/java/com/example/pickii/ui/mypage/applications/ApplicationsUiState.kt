package com.example.pickii.ui.mypage.applications

import com.example.pickii.domain.model.MyApply

/** [ApplicationsScreen]에 표시되는 상태. */
data class ApplicationsUiState(
    val isLoading: Boolean = true,
    val items: List<MyApply> = emptyList(),
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val pendingCancelApplyId: String? = null,
    val toastMessageRes: Int? = null
)
