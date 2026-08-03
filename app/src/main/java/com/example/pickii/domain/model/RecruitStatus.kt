package com.example.pickii.domain.model

/** 모집 글의 모집 상태. */
enum class RecruitStatus(
    val label: String
) {
    OPEN("모집 중"),
    CLOSED("모집 마감")
}
