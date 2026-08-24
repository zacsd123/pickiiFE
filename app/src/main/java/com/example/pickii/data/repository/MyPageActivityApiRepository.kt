package com.example.pickii.data.repository

import com.example.pickii.data.remote.api.MyPageActivityApiService
import com.example.pickii.data.remote.dto.ApiEnvelope
import com.example.pickii.data.remote.dto.MyApplyDto
import com.example.pickii.data.remote.dto.MyCommentDto
import com.example.pickii.data.remote.dto.MyScrapDto
import com.example.pickii.data.remote.dto.PageEnvelope
import com.example.pickii.data.remote.dto.RecruitSummaryDto
import com.example.pickii.domain.model.ApplyStatus
import com.example.pickii.domain.model.MyApply
import com.example.pickii.domain.model.MyApplyPage
import com.example.pickii.domain.model.MyComment
import com.example.pickii.domain.model.MyCommentPage
import com.example.pickii.domain.model.MyRecruitPage
import com.example.pickii.domain.model.MyRecruitSummary
import com.example.pickii.domain.model.MyScrap
import com.example.pickii.domain.model.MyScrapPage
import com.example.pickii.domain.model.RecruitStatus
import com.example.pickii.domain.repository.MyPageActivityRepository
import com.example.pickii.util.network.invalidIdException
import com.example.pickii.util.network.safeApiCall
import com.example.pickii.util.network.safeApiCallUnit
import com.example.pickii.util.parseIsoOffsetDateTime

/** `3. Recruit`, `4. User & Feedback` 문서의 마이페이지 활동내역 API로 [MyPageActivityRepository]를 구현한다. */
class MyPageActivityApiRepository
    constructor(
        private val apiService: MyPageActivityApiService
    ) : MyPageActivityRepository {
        override suspend fun getMyApplies(
            page: Int,
            size: Int
        ): Result<MyApplyPage> =
            safeApiCall<ApiEnvelope<PageEnvelope<MyApplyDto>>> { apiService.getMyApplies(page, size) }.map { envelope ->
                val pageEnvelope = envelope.data
                MyApplyPage(
                    items = pageEnvelope.content.map { it.toDomain() },
                    currentPage = pageEnvelope.pageInfo.currentPage,
                    totalPages = pageEnvelope.pageInfo.totalPages,
                    totalElements = pageEnvelope.pageInfo.totalElements,
                    hasNext = pageEnvelope.pageInfo.hasNext
                )
            }

        override suspend fun cancelApply(applyId: String): Result<Unit> {
            val id = applyId.toLongOrNull() ?: return Result.failure(invalidIdException(applyId))
            return safeApiCallUnit { apiService.cancelApply(id) }
        }

        override suspend fun getMyRecruits(
            page: Int,
            size: Int
        ): Result<MyRecruitPage> =
            safeApiCall<ApiEnvelope<PageEnvelope<RecruitSummaryDto>>> {
                apiService.getMyRecruits(page, size)
            }.map { envelope ->
                val pageEnvelope = envelope.data
                MyRecruitPage(
                    items = pageEnvelope.content.map { it.toDomain() },
                    currentPage = pageEnvelope.pageInfo.currentPage,
                    totalPages = pageEnvelope.pageInfo.totalPages,
                    totalElements = pageEnvelope.pageInfo.totalElements,
                    hasNext = pageEnvelope.pageInfo.hasNext
                )
            }

        override suspend fun getMyComments(
            page: Int,
            size: Int
        ): Result<MyCommentPage> =
            safeApiCall<ApiEnvelope<PageEnvelope<MyCommentDto>>> {
                apiService.getMyComments(page, size)
            }.map { envelope ->
                val pageEnvelope = envelope.data
                MyCommentPage(
                    items = pageEnvelope.content.map { it.toDomain() },
                    currentPage = pageEnvelope.pageInfo.currentPage,
                    totalPages = pageEnvelope.pageInfo.totalPages,
                    totalElements = pageEnvelope.pageInfo.totalElements,
                    hasNext = pageEnvelope.pageInfo.hasNext
                )
            }

        override suspend fun getMyScraps(
            page: Int,
            size: Int
        ): Result<MyScrapPage> =
            safeApiCall<ApiEnvelope<PageEnvelope<MyScrapDto>>> { apiService.getMyScraps(page, size) }.map { envelope ->
                val pageEnvelope = envelope.data
                MyScrapPage(
                    items = pageEnvelope.content.map { it.toDomain() },
                    currentPage = pageEnvelope.pageInfo.currentPage,
                    totalPages = pageEnvelope.pageInfo.totalPages,
                    totalElements = pageEnvelope.pageInfo.totalElements,
                    hasNext = pageEnvelope.pageInfo.hasNext
                )
            }

        private fun MyApplyDto.toDomain(): MyApply =
            MyApply(
                id = applyId.toString(),
                recruitId = recruitId.toString(),
                recruitTitle = recruitTitle,
                status = status.toApplyStatus(),
                appliedAt = parseIsoOffsetDateTime(createdAt),
                chatRoomId = chatRoomId?.toString()
            )

        private fun RecruitSummaryDto.toDomain(): MyRecruitSummary =
            MyRecruitSummary(
                id = recruitId.toString(),
                title = title,
                status = status.toRecruitStatus(),
                createdAt = parseIsoOffsetDateTime(createdAt)
            )

        private fun MyCommentDto.toDomain(): MyComment =
            MyComment(
                id = commentId.toString(),
                recruitId = recruitId.toString(),
                recruitTitle = recruitTitle,
                recruitStatus = recruitStatus.toRecruitStatus(),
                content = content,
                createdAt = parseIsoOffsetDateTime(createdAt)
            )

        private fun MyScrapDto.toDomain(): MyScrap =
            MyScrap(
                recruitId = recruitId.toString(),
                title = title,
                authorNickname = authorNickname,
                status = status.toRecruitStatus(),
                scrapedAt = parseIsoOffsetDateTime(scrappedAt)
            )
    }

private fun String.toApplyStatus(): ApplyStatus =
    runCatching { ApplyStatus.valueOf(this) }.getOrDefault(ApplyStatus.WAITING)

private fun String.toRecruitStatus(): RecruitStatus =
    runCatching { RecruitStatus.valueOf(this) }.getOrDefault(RecruitStatus.OPEN)
