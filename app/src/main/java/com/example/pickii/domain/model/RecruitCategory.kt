package com.example.pickii.domain.model

/** 모집 글 카테고리(단일 선택). */
enum class RecruitCategory(
    val label: String
) {
    COMPETITION("공모전 및 대회"),
    PROJECT("프로젝트"),
    STUDY("스터디")
}
