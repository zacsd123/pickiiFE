package com.example.pickii.domain.model

import java.time.LocalDateTime

/** 모집 글에 대한 지원 내역 하나. 이름이 안드로이드의 `Application`과 겹치지 않도록 구분한다. */
data class RecruitApplication(
    val id: String,
    val postId: String,
    val applicantId: String,
    val applicantNickname: String,
    val message: String,
    val appliedAt: LocalDateTime
)
