package com.example.pickii.domain.model

/** 외부 링크 플랫폼 마스터 데이터(`GET /link-categories`) 항목. */
data class LinkCategory(
    val id: Int,
    val name: String,
    val iconUrl: String
)
