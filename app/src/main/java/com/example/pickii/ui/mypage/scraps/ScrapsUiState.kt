package com.example.pickii.ui.mypage.scraps

import com.example.pickii.domain.model.MyScrap

/** [ScrapsScreen]에 표시되는 상태. */
data class ScrapsUiState(
    val isLoading: Boolean = true,
    val items: List<MyScrap> = emptyList(),
    val currentPage: Int = 1,
    val totalPages: Int = 1
)
