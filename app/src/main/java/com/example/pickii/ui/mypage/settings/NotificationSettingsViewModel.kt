package com.example.pickii.ui.mypage.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pickii.domain.model.NotificationSettings
import com.example.pickii.domain.repository.NotificationSettingsRepository
import com.example.pickii.shared.generated.resources.Res
import com.example.pickii.shared.generated.resources.notification_settings_toast_load_failed
import com.example.pickii.shared.generated.resources.notification_settings_toast_update_failed
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** 알림 설정 조회/수정을 담당한다(9-5, 9-6). 토글 하나를 바꿀 때마다 즉시 서버에 반영한다. */
class NotificationSettingsViewModel
    constructor(
        private val repository: NotificationSettingsRepository
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(NotificationSettingsUiState())
        val uiState: StateFlow<NotificationSettingsUiState> = _uiState.asStateFlow()

        init {
            viewModelScope.launch {
                repository
                    .getSettings()
                    .onSuccess { settings -> _uiState.update { it.copy(isLoading = false, settings = settings) } }
                    .onFailure {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                toastMessageRes = Res.string.notification_settings_toast_load_failed
                            )
                        }
                    }
            }
        }

        fun onToastShown() {
            _uiState.update { it.copy(toastMessageRes = null) }
        }

        private fun update(transform: (NotificationSettings) -> NotificationSettings) {
            val previousSettings = _uiState.value.settings
            val newSettings = transform(previousSettings)
            _uiState.update { it.copy(settings = newSettings) }
            viewModelScope.launch {
                repository.updateSettings(newSettings).onFailure {
                    _uiState.update {
                        it.copy(
                            settings = previousSettings,
                            toastMessageRes = Res.string.notification_settings_toast_update_failed
                        )
                    }
                }
            }
        }

        /** "전체 알림"은 서버 필드가 아니라, marketingNoti를 제외한 6개를 한 번에 켜고 끄는 클라이언트 편의 토글이다. */
        fun onToggleAllEssential(enabled: Boolean) =
            update {
                it.copy(
                    chatNoti = enabled,
                    applicantNoti = enabled,
                    commentNoti = enabled,
                    scheduleNoti = enabled,
                    matchNoti = enabled,
                    projectNoti = enabled
                )
            }

        fun onToggleChat(enabled: Boolean) = update { it.copy(chatNoti = enabled) }

        fun onToggleApplicant(enabled: Boolean) = update { it.copy(applicantNoti = enabled) }

        fun onToggleComment(enabled: Boolean) = update { it.copy(commentNoti = enabled) }

        fun onToggleSchedule(enabled: Boolean) = update { it.copy(scheduleNoti = enabled) }

        fun onToggleMatch(enabled: Boolean) = update { it.copy(matchNoti = enabled) }

        fun onToggleProject(enabled: Boolean) = update { it.copy(projectNoti = enabled) }

        fun onToggleMarketing(enabled: Boolean) = update { it.copy(marketingNoti = enabled) }
    }
