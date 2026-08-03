package com.example.pickii.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

/** 모집 글 하나를 표현하는 모델. */
data class RecruitPost(
    val id: String,
    val title: String,
    val authorId: String,
    val authorNickname: String,
    val authorExperience: Int,
    val onCampus: CampusScope,
    val category: RecruitCategory,
    val topic: RecruitTopic,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val maxParticipants: Int,
    val currentParticipants: Int,
    val shortIntro: String,
    val detailContent: String,
    val status: RecruitStatus,
    val createdAt: LocalDateTime
)
