package com.example.pickii.data.remote.api

import com.example.pickii.data.remote.dto.ApiEnvelope
import com.example.pickii.data.remote.dto.ApplyKeywordCategoryDto
import com.example.pickii.data.remote.dto.CategoryDto
import com.example.pickii.data.remote.dto.LicenseOptionDto
import com.example.pickii.data.remote.dto.LinkCategoryDto
import com.example.pickii.data.remote.dto.TechStackDto
import com.example.pickii.data.remote.dto.TopicDto
import com.example.pickii.data.remote.dto.UniversityDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/** `5. Master Data` 문서 중 공고 작성/필터, 회원가입/이력서 작성에 필요한 항목을 다룬다. */
interface MasterDataApiService {
    @GET("categories")
    suspend fun getCategories(): Response<ApiEnvelope<List<CategoryDto>>>

    @GET("topics")
    suspend fun getTopics(): Response<ApiEnvelope<List<TopicDto>>>

    @GET("universities")
    suspend fun getUniversities(
        @Query("keyword") keyword: String?
    ): Response<ApiEnvelope<List<UniversityDto>>>

    @GET("tech-stacks")
    suspend fun getTechStacks(): Response<ApiEnvelope<List<TechStackDto>>>

    @GET("licenses")
    suspend fun getLicenses(): Response<ApiEnvelope<List<LicenseOptionDto>>>

    @GET("link-categories")
    suspend fun getLinkCategories(): Response<ApiEnvelope<List<LinkCategoryDto>>>

    /** `GET /apply-keywords`(5-7). 인증 불필요, 카테고리별로 중첩된 지원 키워드 목록. */
    @GET("apply-keywords")
    suspend fun getApplyKeywords(): Response<ApiEnvelope<List<ApplyKeywordCategoryDto>>>
}
