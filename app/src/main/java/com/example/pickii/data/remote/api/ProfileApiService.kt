package com.example.pickii.data.remote.api

import com.example.pickii.data.remote.dto.ApiEnvelope
import com.example.pickii.data.remote.dto.CreateResumeRequest
import com.example.pickii.data.remote.dto.CreateResumeResponseDto
import com.example.pickii.data.remote.dto.MemberProfileDto
import com.example.pickii.data.remote.dto.UpdateResumeRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST

/** `4-1 내 프로필 조회`, `4-2 프로필 생성`, `4-3 프로필 수정`을 다룬다. */
interface ProfileApiService {
    @GET("users/me")
    suspend fun getMyProfile(): Response<ApiEnvelope<MemberProfileDto>>

    @POST("users/create-resume")
    suspend fun createResume(
        @Body request: CreateResumeRequest
    ): Response<ApiEnvelope<CreateResumeResponseDto>>

    @PATCH("users/me")
    suspend fun updateResume(
        @Body request: UpdateResumeRequest
    ): Response<Unit>
}
