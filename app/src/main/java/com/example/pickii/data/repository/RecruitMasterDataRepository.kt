package com.example.pickii.data.repository

import com.example.pickii.data.remote.api.MasterDataApiService
import com.example.pickii.domain.model.ApplyKeywordCategory
import com.example.pickii.domain.model.ApplyKeywordItem
import com.example.pickii.domain.model.LicenseOption
import com.example.pickii.domain.model.LinkCategory
import com.example.pickii.domain.model.RecruitCategory
import com.example.pickii.domain.model.RecruitTopic
import com.example.pickii.domain.model.TechStack
import com.example.pickii.domain.model.University
import com.example.pickii.domain.repository.MasterDataRepository
import com.example.pickii.util.network.safeApiCall
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json

/**
 * `5. Master Data` 문서로 마스터 데이터를 조회하고, 변경이 거의 없는 데이터 특성상
 * 최초 성공 응답을 메모리에 캐싱해 재요청을 피한다(검색어에 따라 결과가 달라지는 대학교 검색은 예외).
 */
class RecruitMasterDataRepository
    constructor(
        private val masterDataApiService: MasterDataApiService,
        private val json: Json
    ) : MasterDataRepository {
        private val categoriesMutex = Mutex()
        private var cachedCategories: List<RecruitCategory>? = null

        private val topicsMutex = Mutex()
        private var cachedTopics: List<RecruitTopic>? = null

        private val techStacksMutex = Mutex()
        private var cachedTechStacks: List<TechStack>? = null

        private val licenseOptionsMutex = Mutex()
        private var cachedLicenseOptions: List<LicenseOption>? = null

        private val linkCategoriesMutex = Mutex()
        private var cachedLinkCategories: List<LinkCategory>? = null

        private val applyKeywordsMutex = Mutex()
        private var cachedApplyKeywords: List<ApplyKeywordCategory>? = null

        override suspend fun getCategories(): Result<List<RecruitCategory>> =
            categoriesMutex.withLock {
                cachedCategories?.let { return@withLock Result.success(it) }
                safeApiCall(json) { masterDataApiService.getCategories() }
                    .map { envelope -> envelope.data.map { RecruitCategory(id = it.categoryId, label = it.name) } }
                    .onSuccess { cachedCategories = it }
            }

        override suspend fun getTopics(): Result<List<RecruitTopic>> =
            topicsMutex.withLock {
                cachedTopics?.let { return@withLock Result.success(it) }
                safeApiCall(json) { masterDataApiService.getTopics() }
                    .map { envelope -> envelope.data.map { RecruitTopic(id = it.topicId, label = it.name) } }
                    .onSuccess { cachedTopics = it }
            }

        override suspend fun getUniversities(keyword: String?): Result<List<University>> =
            safeApiCall(json) { masterDataApiService.getUniversities(keyword) }
                .map { envelope -> envelope.data.map { University(id = it.univId, name = it.name) } }

        override suspend fun getTechStacks(): Result<List<TechStack>> =
            techStacksMutex.withLock {
                cachedTechStacks?.let { return@withLock Result.success(it) }
                safeApiCall(json) { masterDataApiService.getTechStacks() }
                    .map { envelope -> envelope.data.map { TechStack(name = it.name) } }
                    .onSuccess { cachedTechStacks = it }
            }

        override suspend fun getLicenseOptions(): Result<List<LicenseOption>> =
            licenseOptionsMutex.withLock {
                cachedLicenseOptions?.let { return@withLock Result.success(it) }
                safeApiCall(json) { masterDataApiService.getLicenses() }
                    .map { envelope -> envelope.data.map { LicenseOption(name = it.name) } }
                    .onSuccess { cachedLicenseOptions = it }
            }

        override suspend fun getLinkCategories(): Result<List<LinkCategory>> =
            linkCategoriesMutex.withLock {
                cachedLinkCategories?.let { return@withLock Result.success(it) }
                safeApiCall(json) { masterDataApiService.getLinkCategories() }
                    .map { envelope -> envelope.data.map { LinkCategory(name = it.name) } }
                    .onSuccess { cachedLinkCategories = it }
            }

        override suspend fun getApplyKeywords(): Result<List<ApplyKeywordCategory>> =
            applyKeywordsMutex.withLock {
                cachedApplyKeywords?.let { return@withLock Result.success(it) }
                safeApiCall(json) { masterDataApiService.getApplyKeywords() }
                    .map { envelope ->
                        envelope.data.map { categoryDto ->
                            ApplyKeywordCategory(
                                categoryId = categoryDto.categoryId,
                                category = categoryDto.category,
                                keywords =
                                    categoryDto.keywords.map { keywordDto ->
                                        ApplyKeywordItem(keywordId = keywordDto.keywordId, content = keywordDto.content)
                                    }
                            )
                        }
                    }.onSuccess { cachedApplyKeywords = it }
            }
    }
