package com.example.pickii.data.repository

import com.example.pickii.data.remote.api.ApplicantApiService
import com.example.pickii.data.remote.dto.ApiEnvelope
import com.example.pickii.data.remote.dto.ApplicantDto
import com.example.pickii.data.remote.dto.ApplyStatusUpdateRequest
import com.example.pickii.data.remote.dto.PageEnvelope
import com.example.pickii.domain.model.ApplicantEntry
import com.example.pickii.domain.model.ApplyStatus
import com.example.pickii.domain.repository.ApplicantRepository
import com.example.pickii.util.network.invalidIdException
import com.example.pickii.util.network.safeApiCall
import com.example.pickii.util.network.safeApiCallUnit
import com.example.pickii.util.parseIsoOffsetDateTime

private const val APPLICANTS_PAGE_SIZE = 100

/** `4-7`, `4-8` API로 [ApplicantRepository]를 구현한다. */
class ApplicantApiRepository
    constructor(
        private val apiService: ApplicantApiService
    ) : ApplicantRepository {
        override suspend fun getApplicants(recruitId: String): Result<List<ApplicantEntry>> {
            val id = recruitId.toLongOrNull() ?: return Result.failure(invalidIdException(recruitId))
            return safeApiCall<ApiEnvelope<PageEnvelope<ApplicantDto>>> {
                apiService.getApplicants(id, page = 0, size = APPLICANTS_PAGE_SIZE)
            }.map { envelope -> envelope.data.content.map { it.toDomain() } }
        }

        override suspend fun updateApplyStatus(
            applyId: String,
            accept: Boolean
        ): Result<Unit> {
            val id = applyId.toLongOrNull() ?: return Result.failure(invalidIdException(applyId))
            val status = if (accept) "ACCEPTED" else "REJECTED"
            return safeApiCallUnit { apiService.updateApplyStatus(id, ApplyStatusUpdateRequest(status)) }
        }

        private fun ApplicantDto.toDomain(): ApplicantEntry =
            ApplicantEntry(
                applyId = applyId.toString(),
                memberId = memberId.toString(),
                nickname = nickname,
                message = message,
                keywords = keywords.map { it.content },
                status = status.toApplyStatus(),
                appliedAt = parseIsoOffsetDateTime(createdAt),
                exp = exp
            )
    }

private fun String.toApplyStatus(): ApplyStatus =
    runCatching { ApplyStatus.valueOf(this) }.getOrDefault(ApplyStatus.WAITING)
