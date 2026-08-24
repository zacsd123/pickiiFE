package com.example.pickii.data.remote.api

import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpResponse

/** [MyPageActivityApiService]를 Ktor [HttpClient]로 구현한다. */
class KtorMyPageActivityApiService(
    private val client: HttpClient
) : MyPageActivityApiService {
    override suspend fun getMyApplies(
        page: Int,
        size: Int
    ): HttpResponse =
        client.get("users/me/applies") {
            parameter("page", page)
            parameter("size", size)
        }

    override suspend fun cancelApply(applyId: Long): HttpResponse = client.delete("applies/$applyId")

    override suspend fun getMyRecruits(
        page: Int,
        size: Int
    ): HttpResponse =
        client.get("users/me/recruits") {
            parameter("page", page)
            parameter("size", size)
        }

    override suspend fun getMyComments(
        page: Int,
        size: Int
    ): HttpResponse =
        client.get("users/me/comments") {
            parameter("page", page)
            parameter("size", size)
        }

    override suspend fun getMyScraps(
        page: Int,
        size: Int
    ): HttpResponse =
        client.get("users/me/scraps") {
            parameter("page", page)
            parameter("size", size)
        }
}
