package com.example.pickii.ui.mypage.mycomments

import com.example.pickii.domain.model.MyComment
import com.example.pickii.ui.common.PagedListUiState
import org.jetbrains.compose.resources.StringResource

/** [MyCommentsScreen]에 표시되는 상태. */
data class MyCommentsUiState(
    override val isLoading: Boolean = true,
    override val items: List<MyComment> = emptyList(),
    override val currentPage: Int = 1,
    override val totalPages: Int = 1,
    override val toastMessageRes: StringResource? = null
) : PagedListUiState<MyComment>
