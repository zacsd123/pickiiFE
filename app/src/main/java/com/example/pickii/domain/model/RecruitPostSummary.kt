package com.example.pickii.domain.model

import java.time.LocalDateTime

/** 홈 화면 목록 카드에 표시되는 모집 글 요약(`GET /recruits` 목록 항목과 1:1 대응). */
data class RecruitPostSummary(
    val id: String,
    val title: String,
    val authorId: String,
    val authorNickname: String,
    val authorExperience: Int = 0,
    val maxParticipants: Int,
    val availableSlots: Int,
    val status: RecruitStatus,
    val createdAt: LocalDateTime
) {
    /** 현재까지 모집된 인원. */
    val currentParticipants: Int
        get() = maxParticipants - availableSlots
}
