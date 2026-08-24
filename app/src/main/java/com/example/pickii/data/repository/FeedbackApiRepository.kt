package com.example.pickii.data.repository

import com.example.pickii.data.remote.api.FeedbackApiService
import com.example.pickii.data.remote.dto.AiFeedbackDto
import com.example.pickii.data.remote.dto.ApiEnvelope
import com.example.pickii.data.remote.dto.FeedbackMemberDto
import com.example.pickii.data.remote.dto.FeedbackProjectDto
import com.example.pickii.data.remote.dto.FeedbackProjectMembersDto
import com.example.pickii.data.remote.dto.FeedbackScoresDto
import com.example.pickii.data.remote.dto.FeedbackSubmitRequest
import com.example.pickii.data.remote.dto.PageEnvelope
import com.example.pickii.domain.model.AiFeedback
import com.example.pickii.domain.model.FeedbackProject
import com.example.pickii.domain.model.FeedbackProjectMembers
import com.example.pickii.domain.model.FeedbackProjectPage
import com.example.pickii.domain.model.FeedbackScores
import com.example.pickii.domain.model.FeedbackTeamMember
import com.example.pickii.domain.repository.FeedbackRepository
import com.example.pickii.util.network.invalidIdException
import com.example.pickii.util.network.safeApiCall
import com.example.pickii.util.network.safeApiCallUnit
import com.example.pickii.util.parseIsoOffsetDateTime
import kotlinx.datetime.LocalDate

/** `4-9`~`4-12` API로 [FeedbackRepository]를 구현한다. */
class FeedbackApiRepository
    constructor(
        private val apiService: FeedbackApiService
    ) : FeedbackRepository {
        override suspend fun getMyFeedbackProjects(
            page: Int,
            size: Int
        ): Result<FeedbackProjectPage> =
            safeApiCall<ApiEnvelope<PageEnvelope<FeedbackProjectDto>>> {
                apiService.getMyFeedbackProjects(page, size)
            }.map { envelope ->
                FeedbackProjectPage(items = envelope.data.content.map { it.toDomain() })
            }

        override suspend fun getProjectMembers(projectId: String): Result<FeedbackProjectMembers> {
            val id = projectId.toLongOrNull() ?: return Result.failure(invalidIdException(projectId))
            return safeApiCall<ApiEnvelope<FeedbackProjectMembersDto>> {
                apiService.getProjectMembers(id)
            }.map { envelope ->
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
            return safeApiCallUnit {
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
            return safeApiCall<ApiEnvelope<AiFeedbackDto>> { apiService.getAiFeedback(id) }.map { envelope ->
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
                isAiFeedbackAvailable = isAiFeedbackAvailable
            )

        private fun FeedbackMemberDto.toDomain(): FeedbackTeamMember =
            FeedbackTeamMember(memberId = memberId.toString(), nickname = nickname, isEvaluated = isEvaluated)
    }
