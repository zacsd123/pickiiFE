package com.example.pickii.data.remote.dto

import kotlinx.serialization.Serializable

/** `GET /feedbacks`(4-11) 목록 항목: 내가 참여한 종료 프로젝트의 상호평가 진행률. */
@Serializable
data class FeedbackProjectDto(
    val projectId: Long,
    val name: String,
    val evaluationPeriod: FeedbackEvaluationPeriodDto,
    val remainingDays: Int,
    val memberCount: Int,
    val evaluatedCount: Int,
    val isAiFeedbackAvailable: Boolean
)

@Serializable
data class FeedbackEvaluationPeriodDto(
    val start: String,
    val end: String
)

/** `GET /feedbacks/projects/{projectId}/members`(4-9) 응답. */
@Serializable
data class FeedbackProjectMembersDto(
    val projectId: Long,
    val evaluationDeadline: String,
    val members: List<FeedbackMemberDto>
)

@Serializable
data class FeedbackMemberDto(
    val memberId: Long,
    val nickname: String,
    val isEvaluated: Boolean
)

/** `POST /feedbacks`(4-10) 요청. */
@Serializable
data class FeedbackSubmitRequest(
    val projectId: Long,
    val revieweeId: Long,
    val scores: FeedbackScoresDto,
    val strength: String,
    val weakness: String
)

@Serializable
data class FeedbackScoresDto(
    val responsibility: Int,
    val communication: Int,
    val deadline: Int,
    val cooperation: Int,
    val contribution: Int
)

/** `GET /feedbacks/ai/{projectId}`(4-12) 응답. */
@Serializable
data class AiFeedbackDto(
    val projectId: Long,
    val keywords: List<String>,
    val strengthSummary: String,
    val weaknessSummary: String
)
