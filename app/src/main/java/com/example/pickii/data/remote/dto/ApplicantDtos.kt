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
    val createdAt: String,
    /** 지원자 경험치(레벨 아바타 계산용). 필드명 미확정 — 백엔드 확정 전 추정치. */
    val exp: Int = 0
)

/** `PATCH /applies/{applyId}/status`(4-8) 요청. */
@Serializable
data class ApplyStatusUpdateRequest(
    val status: String
)
