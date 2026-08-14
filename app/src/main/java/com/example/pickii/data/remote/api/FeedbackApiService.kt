package com.example.pickii.data.remote.api

import com.example.pickii.data.remote.dto.AiFeedbackDto
import com.example.pickii.data.remote.dto.ApiEnvelope
import com.example.pickii.data.remote.dto.FeedbackProjectDto
import com.example.pickii.data.remote.dto.FeedbackProjectMembersDto
import com.example.pickii.data.remote.dto.FeedbackSubmitRequest
import com.example.pickii.data.remote.dto.PageEnvelope
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/** `4-9`~`4-12` 상호평가/피드백 API. */
interface FeedbackApiService {
    @GET("feedbacks")
    suspend fun getMyFeedbackProjects(
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Response<ApiEnvelope<PageEnvelope<FeedbackProjectDto>>>

    @GET("feedbacks/projects/{projectId}/members")
    suspend fun getProjectMembers(
        @Path("projectId") projectId: Long
    ): Response<ApiEnvelope<FeedbackProjectMembersDto>>

    @POST("feedbacks")
    suspend fun submitFeedback(
        @Body request: FeedbackSubmitRequest
    ): Response<Unit>

    @GET("feedbacks/ai/{projectId}")
    suspend fun getAiFeedback(
        @Path("projectId") projectId: Long
    ): Response<ApiEnvelope<AiFeedbackDto>>
}
