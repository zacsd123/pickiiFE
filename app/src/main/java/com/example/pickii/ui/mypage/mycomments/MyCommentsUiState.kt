package com.example.pickii.ui.mypage.mycomments

import com.example.pickii.domain.model.MyComment

/** [MyCommentsScreen]에 표시되는 상태. */
data class MyCommentsUiState(
    val isLoading: Boolean = true,
    val items: List<MyComment> = emptyList(),
    val currentPage: Int = 1,
    val totalPages: Int = 1
)
