package com.example.pickii.domain.model

/** `POST /users/create-resume` 요청에 필요한 입력값. 온보딩 8단계에서 누적한 값을 마지막 단계에서 한 번에 담는다. */
data class CreateProfileInput(
    val univId: Int,
    val major: String,
    val academicStatus: AcademicStatus,
    val hope: String?,
    val strength: String?,
    val topicIds: List<Int> = emptyList(),
    val skillTools: List<SkillToolEntry> = emptyList(),
    val licenses: List<LicenseEntry> = emptyList(),
    val experiences: List<ExperienceEntry> = emptyList(),
    val additionalLinks: List<AdditionalLinkEntry> = emptyList()
)
