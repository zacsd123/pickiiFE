package com.example.pickii.domain.repository

import com.example.pickii.domain.model.MyApplyPage
import com.example.pickii.domain.model.MyCommentPage
import com.example.pickii.domain.model.MyRecruitPage
import com.example.pickii.domain.model.MyScrapPage

/** 마이페이지 활동내역(지원 현황/작성 공고/작성한 댓글/스크랩한 공고)을 조회/관리한다. */
interface MyPageActivityRepository {
    suspend fun getMyApplies(
        page: Int = 0,
        size: Int = 10
    ): Result<MyApplyPage>

    /** WAITING 상태인 지원서만 취소할 수 있다(3-13). */
    suspend fun cancelApply(applyId: String): Result<Unit>

    suspend fun getMyRecruits(
        page: Int = 0,
        size: Int = 10
    ): Result<MyRecruitPage>

    suspend fun getMyComments(
        page: Int = 0,
        size: Int = 10
    ): Result<MyCommentPage>

    suspend fun getMyScraps(
        page: Int = 0,
        size: Int = 10
    ): Result<MyScrapPage>
}
