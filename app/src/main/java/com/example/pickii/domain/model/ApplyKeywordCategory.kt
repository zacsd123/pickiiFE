package com.example.pickii.domain.model

/** 지원 키워드 마스터 데이터(`GET /apply-keywords`)의 카테고리 하나. 카테고리 안에 키워드가 중첩된다. */
data class ApplyKeywordCategory(
    val categoryId: Int,
    val category: String,
    val keywords: List<ApplyKeywordItem>
)

/** [ApplyKeywordCategory]에 속한 키워드 하나. */
data class ApplyKeywordItem(
    val keywordId: Long,
    val content: String
)
