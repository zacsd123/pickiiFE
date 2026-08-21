package com.example.pickii.domain.model

import kotlinx.datetime.LocalDateTime

/** 지원 상태(4-4 지원 현황 조회, 4-8 지원자 수락/거절 기준). */
enum class ApplyStatus {
    WAITING,
    ACCEPTED,
    REJECTED
}

/** 내가 지원한 공고 한 건(`GET /users/me/applies`). */
data class MyApply(
    val id: String,
    val recruitId: String,
    val recruitTitle: String,
    val status: ApplyStatus,
    val appliedAt: LocalDateTime,
    val chatRoomId: String?
)

data class MyApplyPage(
    val items: List<MyApply>,
    val currentPage: Int,
    val totalPages: Int,
    val totalElements: Int,
    val hasNext: Boolean
)

/** 내가 작성한 공고 한 건(`GET /users/me/recruits`). */
data class MyRecruitSummary(
    val id: String,
    val title: String,
    val status: RecruitStatus,
    val createdAt: LocalDateTime
)

data class MyRecruitPage(
    val items: List<MyRecruitSummary>,
    val currentPage: Int,
    val totalPages: Int,
    val totalElements: Int,
    val hasNext: Boolean
)

/** 내가 작성한 댓글 한 건(`GET /users/me/comments`). */
data class MyComment(
    val id: String,
    val recruitId: String,
    val recruitTitle: String,
    val recruitStatus: RecruitStatus,
    val content: String,
    val createdAt: LocalDateTime
)

data class MyCommentPage(
    val items: List<MyComment>,
    val currentPage: Int,
    val totalPages: Int,
    val totalElements: Int,
    val hasNext: Boolean
)

/** 내가 스크랩한 공고 한 건(`GET /users/me/scraps`). */
data class MyScrap(
    val recruitId: String,
    val title: String,
    val authorNickname: String,
    val status: RecruitStatus,
    val scrapedAt: LocalDateTime
)

data class MyScrapPage(
    val items: List<MyScrap>,
    val currentPage: Int,
    val totalPages: Int,
    val totalElements: Int,
    val hasNext: Boolean
)
