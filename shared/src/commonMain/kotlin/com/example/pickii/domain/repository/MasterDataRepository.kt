package com.example.pickii.domain.repository

import com.example.pickii.domain.model.ApplyKeywordCategory
import com.example.pickii.domain.model.LicenseOption
import com.example.pickii.domain.model.LinkCategory
import com.example.pickii.domain.model.RecruitCategory
import com.example.pickii.domain.model.RecruitTopic
import com.example.pickii.domain.model.TechStack
import com.example.pickii.domain.model.University

/** 공고 작성/필터, 회원가입/이력서 작성에서 쓰는 마스터 데이터를 조회한다. */
interface MasterDataRepository {
    /** 전체 카테고리 목록을 조회한다. 변경이 거의 없는 데이터라 최초 조회 후 캐싱한다. */
    suspend fun getCategories(): Result<List<RecruitCategory>>

    /** 전체 주제 목록을 조회한다. 변경이 거의 없는 데이터라 최초 조회 후 캐싱한다. */
    suspend fun getTopics(): Result<List<RecruitTopic>>

    /** [keyword]로 대학교를 검색한다. 미지정 시 전체 목록을 반환한다(캐싱하지 않는다 — 검색어별로 결과가 다름). */
    suspend fun getUniversities(keyword: String? = null): Result<List<University>>

    /** 전체 기술 스택 목록을 조회한다. 최초 조회 후 캐싱한다. */
    suspend fun getTechStacks(): Result<List<TechStack>>

    /** 전체 자격증 선택지를 조회한다. 최초 조회 후 캐싱한다. */
    suspend fun getLicenseOptions(): Result<List<LicenseOption>>

    /** 전체 외부 링크 플랫폼 목록을 조회한다. 최초 조회 후 캐싱한다. */
    suspend fun getLinkCategories(): Result<List<LinkCategory>>

    /** 지원 키워드 목록을 카테고리별로 조회한다(전체 카테고리 통틀어 최대 5개까지 선택 가능). 최초 조회 후 캐싱한다. */
    suspend fun getApplyKeywords(): Result<List<ApplyKeywordCategory>>
}
