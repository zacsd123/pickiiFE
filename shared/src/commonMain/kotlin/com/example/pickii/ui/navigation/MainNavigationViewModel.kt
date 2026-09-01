package com.example.pickii.ui.navigation

import androidx.lifecycle.ViewModel
import com.example.pickii.domain.repository.SessionRepository
import kotlinx.coroutines.flow.StateFlow

/** [MainActivity][com.example.pickii.MainActivity]가 탭 레벨 로그인 게이팅(마이페이지 탭 등)에 사용하는 최소 ViewModel. */
class MainNavigationViewModel
    constructor(
        sessionRepository: SessionRepository
    ) : ViewModel() {
        val isLoggedIn: StateFlow<Boolean> = sessionRepository.isLoggedIn
    }
