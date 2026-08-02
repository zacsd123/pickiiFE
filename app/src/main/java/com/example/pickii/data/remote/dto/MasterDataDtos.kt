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
    val techStackId: Int,
    val name: String,
    val type: String? = null
)

@Serializable
data class LicenseOptionDto(
    val licenseId: Int,
    val name: String
)

@Serializable
data class LinkCategoryDto(
    val linkCategoryId: Int,
    val name: String,
    val picUrl: String? = null
)
