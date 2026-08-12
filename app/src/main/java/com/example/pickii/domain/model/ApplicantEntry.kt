package com.example.pickii.domain.model

import java.time.LocalDateTime

/** 공고 지원자 한 명(4-7). */
data class ApplicantEntry(
    val applyId: String,
    val memberId: String,
    val nickname: String,
    val message: String,
    val keywords: List<String>,
    val status: ApplyStatus,
    val appliedAt: LocalDateTime,
    val exp: Int = 0
)
