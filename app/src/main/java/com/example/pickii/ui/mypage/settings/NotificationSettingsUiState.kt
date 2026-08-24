package com.example.pickii.ui.mypage.settings

import com.example.pickii.domain.model.NotificationSettings
import org.jetbrains.compose.resources.StringResource

/** [SettingsScreen]의 알림 설정 섹션에 표시되는 상태. */
data class NotificationSettingsUiState(
    val isLoading: Boolean = true,
    val settings: NotificationSettings =
        NotificationSettings(
            chatNoti = false,
            applicantNoti = false,
            commentNoti = false,
            scheduleNoti = false,
            matchNoti = false,
            projectNoti = false,
            marketingNoti = false
        ),
    val toastMessageRes: StringResource? = null
)
