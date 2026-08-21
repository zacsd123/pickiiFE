package com.example.pickii.domain.model

/** 교내/교외 모집 글 범위. */
enum class CampusScope(
    val label: String
) {
    INTERNAL("교내"),
    EXTERNAL("교외")
}
