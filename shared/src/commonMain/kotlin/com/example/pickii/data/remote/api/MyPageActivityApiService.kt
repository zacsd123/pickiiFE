package com.example.pickii.data.remote.api

import io.ktor.client.statement.HttpResponse

/** 마이페이지 활동내역(지원 현황/작성 공고/작성한 댓글/스크랩한 공고) API. `3. Recruit`, `4. User & Feedback` 문서. */
interface MyPageActivityApiService {
    suspend fun getMyApplies(
        page: Int,
        size: Int
    ): HttpResponse

    /** 3-13 지원 취소. WAITING 상태인 지원서만 취소 가능하다. */
    suspend fun cancelApply(applyId: Long): HttpResponse

    suspend fun getMyRecruits(
        page: Int,
        size: Int
    ): HttpResponse

    suspend fun getMyComments(
        page: Int,
        size: Int
    ): HttpResponse

    /** 3-16 스크랩한 공고 목록 조회. */
    suspend fun getMyScraps(
        page: Int,
        size: Int
    ): HttpResponse
}
