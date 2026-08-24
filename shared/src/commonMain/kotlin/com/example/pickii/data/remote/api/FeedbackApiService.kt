package com.example.pickii.data.remote.api

import com.example.pickii.data.remote.dto.FeedbackSubmitRequest
import io.ktor.client.statement.HttpResponse

/** `4-9`~`4-12` 상호평가/피드백 API. */
interface FeedbackApiService {
    suspend fun getMyFeedbackProjects(
        page: Int,
        size: Int
    ): HttpResponse

    suspend fun getProjectMembers(projectId: Long): HttpResponse

    suspend fun submitFeedback(request: FeedbackSubmitRequest): HttpResponse

    suspend fun getAiFeedback(projectId: Long): HttpResponse
}
