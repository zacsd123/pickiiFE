package com.example.pickii.ui.common.entrydraft

import kotlinx.datetime.YearMonth
import java.util.UUID

/** "사용 가능 Skill & Tool" 항목 하나의 입력 중 상태. [level]은 1(하)~3(상). */
data class SkillToolDraft(
    val id: String = UUID.randomUUID().toString(),
    val techStackName: String = "",
    val level: Int = 2,
    val isSelected: Boolean = false
)

/** "자격증" 항목 하나의 입력 중 상태. */
data class LicenseDraft(
    val id: String = UUID.randomUUID().toString(),
    val licenseName: String = "",
    val acquiredDate: YearMonth? = null
)

/** "수상 및 경험" 항목 하나의 입력 중 상태. */
data class ExperienceDraft(
    val id: String = UUID.randomUUID().toString(),
    val startDate: YearMonth? = null,
    val endDate: YearMonth? = null,
    val title: String = "",
    val organization: String = "",
    val description: String = ""
)

/** "외부 링크" 항목 하나의 입력 중 상태. */
data class LinkDraft(
    val id: String = UUID.randomUUID().toString(),
    val linkName: String = "",
    val url: String = ""
)
