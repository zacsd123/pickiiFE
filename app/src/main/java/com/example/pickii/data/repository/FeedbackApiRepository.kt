package com.example.pickii.data.repository

import com.example.pickii.data.remote.api.FeedbackApiService
import com.example.pickii.data.remote.dto.FeedbackMemberDto
import com.example.pickii.data.remote.dto.FeedbackProjectDto
import com.example.pickii.data.remote.dto.FeedbackScoresDto
import com.example.pickii.data.remote.dto.FeedbackSubmitRequest
import com.example.pickii.domain.model.AiFeedback
import com.example.pickii.domain.model.FeedbackProject
import com.example.pickii.domain.model.FeedbackProjectMembers
import com.example.pickii.domain.model.FeedbackProjectPage
import com.example.pickii.domain.model.FeedbackScores
import com.example.pickii.domain.model.FeedbackTeamMember
import com.example.pickii.domain.repository.FeedbackRepository
import com.example.pickii.util.network.safeApiCall
import com.example.pickii.util.network.safeApiCallUnit
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import javax.inject.Inject
import javax.inject.Singleton

/** `4-9`~`4-12` API로 [FeedbackRepository]를 구현한다. */
@Singleton
class FeedbackApiRepository
    @Inject
    constructor(
        private val apiService: FeedbackApiService,
        private val json: Json
    ) : FeedbackRepository {
        override suspend fun getMyFeedbackProjects(
            page: Int,
            size: Int
        ): Result<FeedbackProjectPage> =
            safeApiCall(json) { apiService.getMyFeedbackProjects(page, size) }.map { envelope ->
                val pageEnvelope = envelope.data
                FeedbackProjectPage(
                    items = pageEnvelope.content.map { it.toDomain() },
                    currentPage = pageEnvelope.pageInfo.currentPage,
                    totalPages = pageEnvelope.pageInfo.totalPages,
                    totalElements = pageEnvelope.pageInfo.totalElements,
                    hasNext = pageEnvelope.pageInfo.hasNext
                )
            }

        override suspend fun getProjectMembers(projectId: String): Result<FeedbackProjectMembers> {
            val id = projectId.toLongOrNull() ?: return Result.failure(invalidIdException(projectId))
            return safeApiCall(json) { apiService.getProjectMembers(id) }.map { envelope ->
                val dto = envelope.data
                FeedbackProjectMembers(
                    projectId = dto.projectId.toString(),
                    evaluationDeadline = parseIsoOffsetDateTime(dto.evaluationDeadline),
                    members = dto.members.map { it.toDomain() }
                )
            }
        }

        override suspend fun submitFeedback(
            projectId: String,
            revieweeId: String,
            scores: FeedbackScores,
            strength: String,
            weakness: String
        ): Result<Unit> {
            val pid = projectId.toLongOrNull() ?: return Result.failure(invalidIdException(projectId))
            val rid = revieweeId.toLongOrNull() ?: return Result.failure(invalidIdException(revieweeId))
            return safeApiCallUnit(json) {
                apiService.submitFeedback(
                    FeedbackSubmitRequest(
                        projectId = pid,
                        revieweeId = rid,
                        scores =
                            FeedbackScoresDto(
                                responsibility = scores.responsibility,
                                communication = scores.communication,
                                deadline = scores.deadline,
                                cooperation = scores.cooperation,
                                contribution = scores.contribution
                            ),
                        strength = strength,
                        weakness = weakness
                    )
                )
            }
        }

        override suspend fun getAiFeedback(projectId: String): Result<AiFeedback> {
            val id = projectId.toLongOrNull() ?: return Result.failure(invalidIdException(projectId))
            return safeApiCall(json) { apiService.getAiFeedback(id) }.map { envelope ->
                val dto = envelope.data
                AiFeedback(
                    projectId = dto.projectId.toString(),
                    keywords = dto.keywords,
                    strengthSummary = dto.strengthSummary,
                    weaknessSummary = dto.weaknessSummary
                )
            }
        }

        private fun FeedbackProjectDto.toDomain(): FeedbackProject =
            FeedbackProject(
                projectId = projectId.toString(),
                name = name,
                periodStart = LocalDate.parse(evaluationPeriod.start),
                periodEnd = LocalDate.parse(evaluationPeriod.end),
                remainingDays = remainingDays,
                memberCount = memberCount,
                evaluatedCount = evaluatedCount,
                requiredCount = requiredCount,
                isAiFeedbackAvailable = isAiFeedbackAvailable
            )

        private fun FeedbackMemberDto.toDomain(): FeedbackTeamMember =
            FeedbackTeamMember(memberId = memberId.toString(), nickname = nickname, isEvaluated = isEvaluated)
    }

private fun invalidIdException(id: String) = IllegalArgumentException("잘못된 id: $id")

private fun parseIsoOffsetDateTime(value: String): LocalDateTime = OffsetDateTime.parse(value).toLocalDateTime()
