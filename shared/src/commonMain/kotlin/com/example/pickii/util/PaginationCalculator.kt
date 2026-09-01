package com.example.pickii.util

private const val DEFAULT_PAGE_WINDOW_SIZE = 5

/** 페이지네이션에 한 번에 표시할 페이지 번호 목록을 계산한다. 전체 페이지가 적으면 그만큼만 보여준다. */
fun visiblePageNumbers(
    currentPage: Int,
    totalPages: Int,
    windowSize: Int = DEFAULT_PAGE_WINDOW_SIZE
): List<Int> {
    if (totalPages <= windowSize) return (1..totalPages).toList()
    val start = (currentPage - windowSize / 2).coerceIn(1, totalPages - windowSize + 1)
    return (start until start + windowSize).toList()
}
