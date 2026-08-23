package com.example.pickii.ui.mypage.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pickii.domain.repository.SessionRepository
import kotlinx.coroutines.launch

/** 로그아웃 확인 화면의 동작을 담당한다. */
class LogoutViewModel
    constructor(
        private val sessionRepository: SessionRepository
    ) : ViewModel() {
        fun onConfirmLogout(onLoggedOut: () -> Unit) {
            viewModelScope.launch {
                sessionRepository.logout()
                onLoggedOut()
            }
        }
    }
