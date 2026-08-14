package com.example.pickii.data.remote.api

import com.example.pickii.data.remote.dto.AiDraftRequest
import com.example.pickii.data.remote.dto.AiDraftResponseDto
import com.example.pickii.data.remote.dto.ApiEnvelope
import com.example.pickii.data.remote.dto.ApplyAiDraftRequest
import com.example.pickii.data.remote.dto.ApplyAiDraftResponseDto
import com.example.pickii.data.remote.dto.ApplyRequest
import com.example.pickii.data.remote.dto.CommentCreateRequest
import com.example.pickii.data.remote.dto.CommentCreateResponseDto
import com.example.pickii.data.remote.dto.CommentsResponseDto
import com.example.pickii.data.remote.dto.PageEnvelope
import com.example.pickii.data.remote.dto.ProjectCreateRequest
import com.example.pickii.data.remote.dto.ProjectCreateResponseDto
import com.example.pickii.data.remote.dto.RecruitCreateResponseDto
import com.example.pickii.data.remote.dto.RecruitDetailDto
import com.example.pickii.data.remote.dto.RecruitSummaryDto
import com.example.pickii.data.remote.dto.RecruitWriteRequest
import com.example.pickii.data.remote.dto.ScrapResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/** `2. Main (Home)`, `3. Recruit` 문서의 엔드포인트. */
interface RecruitApiService {
    @GET("recruits")
    suspend fun getRecruits(
        @Query("keyword") keyword: String?,
        @Query("onCampus") onCampus: Boolean?,
        @Query("categoryIds") categoryIds: List<Int>,
        @Query("topicIds") topicIds: List<Int>,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Response<ApiEnvelope<PageEnvelope<RecruitSummaryDto>>>

    @GET("recruits/{recruitId}")
    suspend fun getRecruit(
        @Path("recruitId") recruitId: Long
    ): Response<ApiEnvelope<RecruitDetailDto>>

    @POST("recruits")
    suspend fun createRecruit(
        @Body request: RecruitWriteRequest
    ): Response<ApiEnvelope<RecruitCreateResponseDto>>

    @PATCH("recruits/{recruitId}")
    suspend fun updateRecruit(
        @Path("recruitId") recruitId: Long,
        @Body request: RecruitWriteRequest
    ): Response<Unit>

    @POST("recruits/ai-draft")
    suspend fun generateRecruitAiDraft(
        @Body request: AiDraftRequest
    ): Response<ApiEnvelope<AiDraftResponseDto>>

    @PATCH("recruits/{recruitId}/close")
    suspend fun closeRecruit(
        @Path("recruitId") recruitId: Long
    ): Response<Unit>

    @PATCH("recruits/{recruitId}/additional")
    suspend fun reopenAdditionalRecruit(
        @Path("recruitId") recruitId: Long
    ): Response<Unit>

    @DELETE("recruits/{recruitId}")
    suspend fun deleteRecruit(
        @Path("recruitId") recruitId: Long
    ): Response<Unit>

    @GET("recruits/{recruitId}/comments")
    suspend fun getComments(
        @Path("recruitId") recruitId: Long
    ): Response<ApiEnvelope<CommentsResponseDto>>

    @POST("recruits/{recruitId}/comments")
    suspend fun createComment(
        @Path("recruitId") recruitId: Long,
        @Body request: CommentCreateRequest
    ): Response<ApiEnvelope<CommentCreateResponseDto>>

    @DELETE("comments/{commentId}")
    suspend fun deleteComment(
        @Path("commentId") commentId: Long
    ): Response<Unit>

    @POST("recruits/{recruitId}/applies/ai-draft")
    suspend fun generateApplyAiDraft(
        @Path("recruitId") recruitId: Long,
        @Body request: ApplyAiDraftRequest
    ): Response<ApiEnvelope<ApplyAiDraftResponseDto>>

    @POST("recruits/{recruitId}/applies")
    suspend fun submitApplication(
        @Path("recruitId") recruitId: Long,
        @Body request: ApplyRequest
    ): Response<Unit>

    @POST("recruits/{recruitId}/scrap")
    suspend fun scrapRecruit(
        @Path("recruitId") recruitId: Long
    ): Response<ApiEnvelope<ScrapResponseDto>>

    @DELETE("recruits/{recruitId}/scrap")
    suspend fun unscrapRecruit(
        @Path("recruitId") recruitId: Long
    ): Response<Unit>

    /** 6-1 프로젝트 생성(그룹 채팅 생성). */
    @POST("recruits/{recruitId}/project")
    suspend fun createProject(
        @Path("recruitId") recruitId: Long,
        @Body request: ProjectCreateRequest
    ): Response<ApiEnvelope<ProjectCreateResponseDto>>
}
