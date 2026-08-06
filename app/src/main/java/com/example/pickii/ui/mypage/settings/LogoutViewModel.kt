package com.example.pickii.ui.mypage.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pickii.domain.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/** 로그아웃 확인 화면의 동작을 담당한다. */
@HiltViewModel
class LogoutViewModel
    @Inject
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
