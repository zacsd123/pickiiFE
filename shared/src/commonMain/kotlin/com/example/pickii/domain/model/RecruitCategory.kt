package com.example.pickii.domain.model

/** 모집 글 카테고리. 서버 Master Data(`GET /categories`)에서 받아온다. */
data class RecruitCategory(
    val id: Int,
    val label: String
)
