package com.example.pickii.data.remote.api

import com.example.pickii.data.remote.dto.ApplyStatusUpdateRequest
import io.ktor.client.statement.HttpResponse

/** 공고 지원자 조회(4-7) 및 수락/거절(4-8) API. */
interface ApplicantApiService {
    suspend fun getApplicants(
        recruitId: Long,
        page: Int,
        size: Int
    ): HttpResponse

    /** 4-8 지원자 수락/거절. */
    suspend fun updateApplyStatus(
        applyId: Long,
        request: ApplyStatusUpdateRequest
    ): HttpResponse
}
