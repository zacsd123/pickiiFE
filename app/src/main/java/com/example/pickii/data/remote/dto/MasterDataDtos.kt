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
