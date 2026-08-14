package com.example.pickii.data.remote.api

import com.example.pickii.data.remote.dto.ApiEnvelope
import com.example.pickii.data.remote.dto.ApplicantDto
import com.example.pickii.data.remote.dto.ApplyStatusUpdateRequest
import com.example.pickii.data.remote.dto.PageEnvelope
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.Query

/** 공고 지원자 조회(4-7) 및 수락/거절(4-8) API. */
interface ApplicantApiService {
    @GET("recruits/{recruitId}/applicants")
    suspend fun getApplicants(
        @Path("recruitId") recruitId: Long,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Response<ApiEnvelope<PageEnvelope<ApplicantDto>>>

    /** 4-8 지원자 수락/거절. */
    @PATCH("applies/{applyId}/status")
    suspend fun updateApplyStatus(
        @Path("applyId") applyId: Long,
        @Body request: ApplyStatusUpdateRequest
    ): Response<Unit>
}
