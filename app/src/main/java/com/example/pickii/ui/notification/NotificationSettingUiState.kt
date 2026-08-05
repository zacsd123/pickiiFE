package com.example.pickii.ui.notification

data class NotificationSettingUiState(
    val chatNotificationEnabled: Boolean = true,
    val applicantNotificationEnabled: Boolean = true,
    val commentNotificationEnabled: Boolean = true,
    val scheduleNotificationEnabled: Boolean = true,
    val matchingNotificationEnabled: Boolean = true,
    val marketingNotificationEnabled: Boolean = false,
)
