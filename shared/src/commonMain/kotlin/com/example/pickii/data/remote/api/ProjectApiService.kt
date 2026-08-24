package com.example.pickii.data.remote.api

import io.ktor.client.statement.HttpResponse

/** `6. Project` 중 클라이언트가 쓰는 API(6-2 상세, 6-4 종료). */
interface ProjectApiService {
    /** 6-2 프로젝트 상세 조회. */
    suspend fun getProject(projectId: Long): HttpResponse

    /** 6-4 프로젝트 종료. */
    suspend fun closeProject(projectId: Long): HttpResponse
}
