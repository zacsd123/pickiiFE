package com.example.pickii.data.remote.api

import com.example.pickii.data.remote.dto.CreateResumeRequest
import com.example.pickii.data.remote.dto.UpdateResumeRequest
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse

/** [ProfileApiService]를 Ktor [HttpClient]로 구현한다. */
class KtorProfileApiService(
    private val client: HttpClient
) : ProfileApiService {
    override suspend fun getMyProfile(): HttpResponse = client.get("users/me")

    override suspend fun createResume(request: CreateResumeRequest): HttpResponse =
        client.post("users/create-resume") {
            setBody(request)
        }

    override suspend fun updateResume(request: UpdateResumeRequest): HttpResponse =
        client.patch("users/me") {
            setBody(request)
        }

    override suspend fun getMemberProfile(memberId: Long): HttpResponse = client.get("users/$memberId")
}
