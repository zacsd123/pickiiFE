package com.example.pickii.ui.common

/** 번호 페이지네이션 목록 화면들이 공통으로 갖는 상태 필드. [PagedListViewModel]이 다룬다. */
interface PagedListUiState<T> {
    val isLoading: Boolean
    val items: List<T>
    val currentPage: Int
    val totalPages: Int
    val toastMessageRes: Int?
}
