package com.example.pickii.ui.notification

data class NotificationUiState(
    val notifications: List<NotificationItemUiModel> = emptyList(),
)

data class NotificationItemUiModel(
    val id: Long,
    val title: String,
    val description: String,
    val timeText: String,
    val type: NotificationType,
    val isRead: Boolean,
)

enum class NotificationType {
    CHAT,
    ACCEPT,
    APPLY,
    CLOSED,
}
