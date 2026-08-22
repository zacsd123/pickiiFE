package com.example.pickii.domain.repository

import com.example.pickii.domain.model.ApplicantEntry

/** 공고 지원자 조회(4-7) 및 수락/거절(4-8)을 담당한다. */
interface ApplicantRepository {
    suspend fun getApplicants(recruitId: String): Result<List<ApplicantEntry>>

    /** 지원자를 수락(accept=true) 또는 거절(accept=false)한다(4-8). WAITING 상태인 지원서만 처리할 수 있다. */
    suspend fun updateApplyStatus(
        applyId: String,
        accept: Boolean
    ): Result<Unit>
}
