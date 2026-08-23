package com.example.pickii.domain.model

/**
 * `PATCH /users/me` 요청에 필요한 입력값(4-3 프로필 수정).
 *
 * [CreateProfileInput]과 달리 최초 생성 이후에만 존재하는 [aboutMe]/[contactEmail]을 사용자가 직접 수정할 수 있다.
 */
data class UpdateProfileInput(
    val univId: Int,
    val major: String,
    val academicStatus: AcademicStatus,
    val hope: String?,
    val strength: String?,
    val aboutMe: String?,
    val contactEmail: String,
    val topicIds: List<Int> = emptyList(),
    val skillTools: List<SkillToolEntry> = emptyList(),
    val licenses: List<LicenseEntry> = emptyList(),
    val experiences: List<ExperienceEntry> = emptyList(),
    val additionalLinks: List<AdditionalLinkEntry> = emptyList()
)
