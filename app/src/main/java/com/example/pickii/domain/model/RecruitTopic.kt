package com.example.pickii.domain.model

/** 모집 글 주제. 서버 Master Data(`GET /topics`)에서 받아온다. */
data class RecruitTopic(
    val id: Int,
    val label: String
)
