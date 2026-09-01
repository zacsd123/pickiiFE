package com.example.pickii.ui.mypage.applications

import com.example.pickii.domain.model.MyApply
import com.example.pickii.ui.common.PagedListUiState
import org.jetbrains.compose.resources.StringResource

/** [ApplicationsScreen]에 표시되는 상태. */
data class ApplicationsUiState(
    override val isLoading: Boolean = true,
    override val items: List<MyApply> = emptyList(),
    override val currentPage: Int = 1,
    override val totalPages: Int = 1,
    val pendingCancelApplyId: String? = null,
    override val toastMessageRes: StringResource? = null
) : PagedListUiState<MyApply>
