package com.example.pickii.domain.repository

import com.example.pickii.domain.model.CurrentUser
import kotlinx.coroutines.flow.StateFlow

/** 로그인 여부와 현재 사용자 정보를 관리한다. */
interface SessionRepository {
    /** 로그인 상태 여부. */
    val isLoggedIn: StateFlow<Boolean>

    /** 로그인한 사용자. 비로그인 상태면 null이다. */
    val currentUser: StateFlow<CurrentUser?>

    /**
     * 이메일/비밀번호로 로그인한다.
     *
     * @param email 사용자 이메일
     * @param password 사용자 비밀번호
     * @param autoLogin 자동 로그인 여부(Refresh Token 만료 기간에 반영됨)
     */
    suspend fun login(
        email: String,
        password: String,
        autoLogin: Boolean = false
    ): Result<CurrentUser>

    /** 로그인하지 않고 비회원으로 둘러본다. */
    suspend fun continueAsGuest()

    /** 로그아웃한다. 서버 호출이 실패해도 로컬 세션은 항상 정리된다. */
    suspend fun logout(): Result<Unit>

    /** 저장된 토큰/로그인 상태를 초기화한다(로그아웃, 비회원 전환, 회원 탈퇴 등에서 공용으로 사용). */
    suspend fun clearSession()
}
