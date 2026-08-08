package com.example.pickii.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * 모집 글 상세 하나를 표현하는 모델(`GET /recruits/{id}` 1:1 대응).
 *
 * [categories]/[topics]은 서버가 배열로 주는 id 중 마스터 데이터에서 찾은 것만 담는다.
 */
data class RecruitPost(
    val id: String,
    val title: String,
    val authorId: String,
    val authorNickname: String,
    val authorExperience: Int,
    val onCampus: CampusScope,
    val categories: List<RecruitCategory>,
    val topics: List<RecruitTopic>,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val maxParticipants: Int,
    val currentParticipants: Int,
    val shortIntro: String,
    val detailContent: String,
    val status: RecruitStatus,
    val createdAt: LocalDateTime,
    val isScrapped: Boolean = false
)
