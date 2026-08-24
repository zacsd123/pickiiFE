package com.example.pickii.data.remote.api

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.statement.HttpResponse

/** [ProjectApiService]를 Ktor [HttpClient]로 구현한다. */
class KtorProjectApiService(
    private val client: HttpClient
) : ProjectApiService {
    override suspend fun getProject(projectId: Long): HttpResponse = client.get("projects/$projectId")

    override suspend fun closeProject(projectId: Long): HttpResponse = client.patch("projects/$projectId/close")
}
