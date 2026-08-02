package com.example.pickii.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

/** 내가 참여한 종료 프로젝트 하나의 상호평가 진행 현황(4-11). */
data class FeedbackProject(
    val projectId: String,
    val name: String,
    val periodStart: LocalDate,
    val periodEnd: LocalDate,
    val remainingDays: Int,
    val memberCount: Int,
    val evaluatedCount: Int,
    val requiredCount: Int,
    val isAiFeedbackAvailable: Boolean
)

data class FeedbackProjectPage(
    val items: List<FeedbackProject>,
    val currentPage: Int,
    val totalPages: Int,
    val totalElements: Int,
    val hasNext: Boolean
)

/** 평가 대상 팀원 한 명(4-9). */
data class FeedbackTeamMember(
    val memberId: String,
    val nickname: String,
    val isEvaluated: Boolean
)

/** 프로젝트의 평가 대상 팀원 전체(4-9). */
data class FeedbackProjectMembers(
    val projectId: String,
    val evaluationDeadline: LocalDateTime,
    val members: List<FeedbackTeamMember>
)

/** 상호평가 점수 5개 항목, 각 1~5점(4-10). */
data class FeedbackScores(
    val responsibility: Int,
    val communication: Int,
    val deadline: Int,
    val cooperation: Int,
    val contribution: Int
)

/** AI가 요약한 상호평가 결과(4-12). */
data class AiFeedback(
    val projectId: String,
    val keywords: List<String>,
    val strengthSummary: String,
    val weaknessSummary: String
)
