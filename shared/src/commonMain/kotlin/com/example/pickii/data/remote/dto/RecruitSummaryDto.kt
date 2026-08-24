package com.example.pickii.data.remote.dto

import kotlinx.serialization.Serializable

/** `GET /recruits` 목록 항목. */
@Serializable
data class RecruitSummaryDto(
    val recruitId: Long,
    val title: String,
    val authorId: Long? = null,
    val authorNickname: String,
    /** 작성자 경험치(레벨 아바타 계산용). 목록 응답 필드명 미확정 — 상세(authorEXP) 관례를 따름. */
    val authorEXP: Int = 0,
    val maxMembers: Int,
    val availableSlots: Int,
    val status: String,
    val createdAt: String
)
