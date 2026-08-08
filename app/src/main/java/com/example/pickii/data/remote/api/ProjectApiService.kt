package com.example.pickii.data.remote.api

import com.example.pickii.data.remote.dto.ApiEnvelope
import com.example.pickii.data.remote.dto.ProjectDetailDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path

/** `6. Project` 중 클라이언트가 쓰는 API(6-2 상세, 6-4 종료). */
interface ProjectApiService {
    /** 6-2 프로젝트 상세 조회. */
    @GET("projects/{projectId}")
    suspend fun getProject(
        @Path("projectId") projectId: Long
    ): Response<ApiEnvelope<ProjectDetailDto>>

    /** 6-4 프로젝트 종료. */
    @PATCH("projects/{projectId}/close")
    suspend fun closeProject(
        @Path("projectId") projectId: Long
    ): Response<Unit>
}
