package com.example.pickii.domain.model

/** 내 프로필(이력서) 전체(`GET /users/me` 1:1 대응). */
data class MemberProfile(
    val nickname: String,
    val univId: Int,
    val univ: String,
    val major: String,
    val academicStatus: AcademicStatus,
    val hope: String?,
    val strength: String?,
    val aboutMe: String?,
    val contactEmail: String?,
    val exp: Int,
    val topicIds: List<Int>,
    val skillTools: List<SkillToolEntry>,
    val licenses: List<LicenseEntry>,
    val experiences: List<ExperienceEntry>,
    val additionalLinks: List<AdditionalLinkEntry>
)
