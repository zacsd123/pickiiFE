package com.example.pickii.data.remote.api

import com.example.pickii.data.remote.dto.CreateResumeRequest
import com.example.pickii.data.remote.dto.UpdateResumeRequest
import io.ktor.client.statement.HttpResponse

/** `4-1 내 프로필 조회`, `4-2 프로필 생성`, `4-3 프로필 수정`, `10-1 회원 프로필 조회`를 다룬다. */
interface ProfileApiService {
    suspend fun getMyProfile(): HttpResponse

    suspend fun createResume(request: CreateResumeRequest): HttpResponse

    suspend fun updateResume(request: UpdateResumeRequest): HttpResponse

    /** 10-1 다른 회원의 프로필 조회. */
    suspend fun getMemberProfile(memberId: Long): HttpResponse
}
