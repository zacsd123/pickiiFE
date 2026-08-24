package com.example.pickii.data.remote.api

import com.example.pickii.data.remote.dto.ApplyStatusUpdateRequest
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse

/** [ApplicantApiService]를 Ktor [HttpClient]로 구현한다. */
class KtorApplicantApiService(
    private val client: HttpClient
) : ApplicantApiService {
    override suspend fun getApplicants(
        recruitId: Long,
        page: Int,
        size: Int
    ): HttpResponse =
        client.get("recruits/$recruitId/applicants") {
            parameter("page", page)
            parameter("size", size)
        }

    override suspend fun updateApplyStatus(
        applyId: Long,
        request: ApplyStatusUpdateRequest
    ): HttpResponse =
        client.patch("applies/$applyId/status") {
            setBody(request)
        }
}
