package com.example.pickii.ui.mypage.scraps

import com.example.pickii.domain.model.MyScrap
import com.example.pickii.ui.common.PagedListUiState
import org.jetbrains.compose.resources.StringResource

/** [ScrapsScreen]에 표시되는 상태. */
data class ScrapsUiState(
    override val isLoading: Boolean = true,
    override val items: List<MyScrap> = emptyList(),
    override val currentPage: Int = 1,
    override val totalPages: Int = 1,
    override val toastMessageRes: StringResource? = null
) : PagedListUiState<MyScrap>
