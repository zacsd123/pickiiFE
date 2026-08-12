package com.example.pickii.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CategoryDto(
    val categoryId: Int,
    val name: String
)

@Serializable
data class TopicDto(
    val topicId: Int,
    val name: String
)

@Serializable
data class UniversityDto(
    val univId: Int,
    val name: String
)

@Serializable
data class TechStackDto(
    val name: String
)

@Serializable
data class LicenseOptionDto(
    val name: String
)

@Serializable
data class LinkCategoryDto(
    val name: String
)

/** `GET /apply-keywords`(5-7) 응답의 카테고리 하나. 카테고리 안에 키워드가 중첩된 Nested 구조다. */
@Serializable
data class ApplyKeywordCategoryDto(
    val categoryId: Int,
    val category: String,
    val keywords: List<ApplicantKeywordDto>
)
