package com.example.pickii.data.remote.api

import com.example.pickii.data.remote.dto.FeedbackSubmitRequest
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse

/** [FeedbackApiService]를 Ktor [HttpClient]로 구현한다. */
class KtorFeedbackApiService(
    private val client: HttpClient
) : FeedbackApiService {
    override suspend fun getMyFeedbackProjects(
        page: Int,
        size: Int
    ): HttpResponse =
        client.get("feedbacks") {
            parameter("page", page)
            parameter("size", size)
        }

    override suspend fun getProjectMembers(projectId: Long): HttpResponse = client.get("feedbacks/projects/$projectId/members")

    override suspend fun submitFeedback(request: FeedbackSubmitRequest): HttpResponse =
        client.post("feedbacks") {
            setBody(request)
        }

    override suspend fun getAiFeedback(projectId: Long): HttpResponse = client.get("feedbacks/ai/$projectId")
}
