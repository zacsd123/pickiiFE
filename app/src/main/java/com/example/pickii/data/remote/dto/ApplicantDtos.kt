package com.example.pickii.data.remote.dto

import kotlinx.serialization.Serializable

/** `GET /recruits/{recruitId}/applicants`(4-7) 응답에 포함된 지원 키워드 항목. */
@Serializable
data class ApplicantKeywordDto(
    val keywordId: Long,
    val content: String
)

/** `GET /recruits/{recruitId}/applicants`(4-7) 응답. */
@Serializable
data class ApplicantDto(
    val applyId: Long,
    val memberId: Long,
    val nickname: String,
    val message: String,
    val keywords: List<ApplicantKeywordDto> = emptyList(),
    val status: String,
    val createdAt: String
)

/** `PATCH /applies/{applyId}/status`(4-8) 요청. */
@Serializable
data class ApplyStatusUpdateRequest(
    val status: String
)
