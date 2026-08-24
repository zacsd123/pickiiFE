package com.example.pickii.data.remote.api

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpResponse

/** [MasterDataApiService]를 Ktor [HttpClient]로 구현한다. */
class KtorMasterDataApiService(
    private val client: HttpClient
) : MasterDataApiService {
    override suspend fun getCategories(): HttpResponse = client.get("categories")

    override suspend fun getTopics(): HttpResponse = client.get("topics")

    override suspend fun getUniversities(keyword: String?): HttpResponse =
        client.get("universities") {
            parameter("keyword", keyword)
        }

    override suspend fun getTechStacks(): HttpResponse = client.get("tech-stacks")

    override suspend fun getLicenses(): HttpResponse = client.get("licenses")

    override suspend fun getLinkCategories(): HttpResponse = client.get("link-categories")

    override suspend fun getApplyKeywords(): HttpResponse = client.get("apply-keywords")
}
