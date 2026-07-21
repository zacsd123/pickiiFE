package com.example.pickii.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val SPLASH_DURATION_MS = 1500L

/** 스플래시 화면 노출 시간을 관리하고, 다음 화면으로 넘어갈 시점을 알린다. */
@HiltViewModel
class SplashViewModel @Inject constructor() : ViewModel() {
    private val _isReadyToProceed = MutableStateFlow(false)
    val isReadyToProceed: StateFlow<Boolean> = _isReadyToProceed.asStateFlow()

    init {
        viewModelScope.launch {
            delay(SPLASH_DURATION_MS)
            _isReadyToProceed.value = true
        }
    }
}
