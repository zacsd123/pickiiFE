package com.example.pickii.domain.repository

import com.example.pickii.domain.model.CreateProfileInput
import com.example.pickii.domain.model.MemberProfile

/** 내 프로필(이력서) 조회/생성을 담당한다. */
interface ProfileRepository {
    /** 내 프로필을 조회한다. 프로필이 없으면 [ApiException][com.example.pickii.data.remote.dto.ApiException]("RESUME_NOT_FOUND")로 실패한다. */
    suspend fun getMyProfile(): Result<MemberProfile>

    /** 로그인한 사용자가 이미 프로필(이력서)을 만들었는지 여부. 판단 불가능한 에러는 안전하게 "있음"으로 처리한다(fail-open). */
    suspend fun hasResume(): Boolean

    /** 새 프로필(이력서)을 생성한다. 서버가 응답 중 AboutMe를 자동 생성한다. */
    suspend fun createProfile(input: CreateProfileInput): Result<Unit>
}
