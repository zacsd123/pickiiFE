package com.example.pickii.data.remote.api

import io.ktor.client.statement.HttpResponse

/** `5. Master Data` 문서 중 공고 작성/필터, 회원가입/이력서 작성에 필요한 항목을 다룬다. */
interface MasterDataApiService {
    suspend fun getCategories(): HttpResponse

    suspend fun getTopics(): HttpResponse

    suspend fun getUniversities(keyword: String?): HttpResponse

    suspend fun getTechStacks(): HttpResponse

    suspend fun getLicenses(): HttpResponse

    suspend fun getLinkCategories(): HttpResponse

    /** `GET /apply-keywords`(5-7). 인증 불필요, 카테고리별로 중첩된 지원 키워드 목록. */
    suspend fun getApplyKeywords(): HttpResponse
}
