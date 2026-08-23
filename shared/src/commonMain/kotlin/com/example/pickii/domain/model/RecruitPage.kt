package com.example.pickii.domain.model

/** `GET /recruits` 목록 조회 결과 한 페이지. */
data class RecruitPage(
    val posts: List<RecruitPostSummary>,
    val currentPage: Int,
    val totalPages: Int,
    val totalElements: Int,
    val hasNext: Boolean
)
