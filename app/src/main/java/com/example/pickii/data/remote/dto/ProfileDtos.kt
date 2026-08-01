package com.example.pickii.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class SkillToolDto(
    val techStackName: String,
    val level: Int
)

@Serializable
data class LicenseDto(
    val licenseName: String,
    val date: String
)

@Serializable
data class ExperienceDto(
    val startDate: String,
    val endDate: String? = null,
    val title: String,
    val organization: String,
    val description: String
)

@Serializable
data class AdditionalLinkDto(
    val linkName: String,
    val url: String
)

/** `POST /users/create-resume` 요청. `4-3 프로필 수정`도 같은 바디를 쓰지만, 이번 범위는 생성만 다룬다. */
@Serializable
data class CreateResumeRequest(
    val univId: Long,
    val major: String,
    val academicStatus: String,
    val hope: String? = null,
    val strength: String? = null,
    val topic: List<Int> = emptyList(),
    val skillTool: List<SkillToolDto> = emptyList(),
    val license: List<LicenseDto> = emptyList(),
    val experience: List<ExperienceDto> = emptyList(),
    val additionalLink: List<AdditionalLinkDto> = emptyList()
)

@Serializable
data class CreateResumeResponseDto(
    val message: String
)

/** `GET /users/me` 응답. */
@Serializable
data class MemberProfileDto(
    val nickname: String,
    val univId: Long,
    val univ: String,
    val major: String,
    val academicStatus: String,
    val hope: String? = null,
    val strength: String? = null,
    val aboutMe: String? = null,
    val exp: Int = 0,
    val topic: List<Int> = emptyList(),
    val skillTool: List<SkillToolDto> = emptyList(),
    val license: List<LicenseDto> = emptyList(),
    val experience: List<ExperienceDto> = emptyList(),
    val additionalLink: List<AdditionalLinkDto> = emptyList()
)
