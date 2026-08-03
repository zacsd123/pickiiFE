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
     */
    suspend fun login(
        email: String,
        password: String
    ): Result<CurrentUser>

    /** 로그인하지 않고 비회원으로 둘러본다. */
    suspend fun continueAsGuest()
}
