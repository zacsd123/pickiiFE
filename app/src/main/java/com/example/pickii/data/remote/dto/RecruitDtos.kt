package com.example.pickii.data.remote.dto

import kotlinx.serialization.Serializable

/** `GET /recruits` 목록 항목. */
@Serializable
data class RecruitSummaryDto(
    val recruitId: Long,
    val title: String,
    val authorNickname: String,
    val maxMembers: Int,
    val availableSlots: Int,
    val status: String,
    val createdAt: String
)

/** `GET /recruits/{id}` 상세. */
@Serializable
data class RecruitDetailDto(
    val recruitId: Long,
    val title: String,
    val authorId: Long,
    val authorNickname: String,
    val authorEXP: Int,
    val createdAt: String,
    val startDate: String,
    val endDate: String,
    val category: List<Int> = emptyList(),
    val topics: List<Int> = emptyList(),
    val onCampus: Boolean,
    val simpleDesc: String,
    val content: String,
    val status: String,
    val maxMembers: Int,
    val availableSlots: Int,
    val isScrapped: Boolean = false
)

/** 공고 작성(`POST /recruits`)/수정(`PATCH /recruits/{id}`) 공용 요청 바디. */
@Serializable
data class RecruitWriteRequest(
    val title: String,
    val maxMembers: Int,
    val onCampus: Boolean,
    val categoryIds: List<Int>,
    val topicIds: List<Int>,
    val startDate: String,
    val endDate: String,
    val simpleDesc: String,
    val content: String
)

@Serializable
data class RecruitCreateResponseDto(
    val recruitId: Long
)

/** `POST /recruits/ai-draft` 요청/응답. */
@Serializable
data class AiDraftRequest(
    val simpleDesc: String? = null,
    val content: String
)

@Serializable
data class AiDraftResponseDto(
    val simpleDesc: String,
    val content: String
)

@Serializable
data class CommentsResponseDto(
    val comments: List<CommentDto> = emptyList()
)

@Serializable
data class CommentDto(
    val commentId: Long,
    val authorId: Long? = null,
    val authorNickname: String? = null,
    val content: String,
    val createdAt: String,
    val isAuthor: Boolean = false,
    val replies: List<CommentDto> = emptyList()
)

@Serializable
data class CommentCreateRequest(
    val content: String,
    val parentCommentId: Long? = null
)

@Serializable
data class CommentCreateResponseDto(
    val commentId: Long
)

/** `POST /recruits/{id}/applies` 요청. */
@Serializable
data class ApplyRequest(
    val message: String,
    val keywordIds: List<Int>? = null
)

/** `POST /recruits/{id}/applies/ai-draft` 요청/응답. */
@Serializable
data class ApplyAiDraftRequest(
    val message: String
)

@Serializable
data class ApplyAiDraftResponseDto(
    val convertedText: String
)

/** `POST /recruits/{id}/scrap` 응답. */
@Serializable
data class ScrapResponseDto(
    val recruitId: Long,
    val isScrapped: Boolean
)
