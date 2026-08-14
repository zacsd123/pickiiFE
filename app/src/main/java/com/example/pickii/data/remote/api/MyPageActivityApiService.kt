package com.example.pickii.data.remote.api

import com.example.pickii.data.remote.dto.ApiEnvelope
import com.example.pickii.data.remote.dto.MyApplyDto
import com.example.pickii.data.remote.dto.MyCommentDto
import com.example.pickii.data.remote.dto.MyScrapDto
import com.example.pickii.data.remote.dto.PageEnvelope
import com.example.pickii.data.remote.dto.RecruitSummaryDto
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/** 마이페이지 활동내역(지원 현황/작성 공고/작성한 댓글/스크랩한 공고) API. `3. Recruit`, `4. User & Feedback` 문서. */
interface MyPageActivityApiService {
    @GET("users/me/applies")
    suspend fun getMyApplies(
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Response<ApiEnvelope<PageEnvelope<MyApplyDto>>>

    /** 3-13 지원 취소. WAITING 상태인 지원서만 취소 가능하다. */
    @DELETE("applies/{applyId}")
    suspend fun cancelApply(
        @Path("applyId") applyId: Long
    ): Response<Unit>

    @GET("users/me/recruits")
    suspend fun getMyRecruits(
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Response<ApiEnvelope<PageEnvelope<RecruitSummaryDto>>>

    @GET("users/me/comments")
    suspend fun getMyComments(
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Response<ApiEnvelope<PageEnvelope<MyCommentDto>>>

    /** 3-16 스크랩한 공고 목록 조회. */
    @GET("users/me/scraps")
    suspend fun getMyScraps(
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Response<ApiEnvelope<PageEnvelope<MyScrapDto>>>
}
