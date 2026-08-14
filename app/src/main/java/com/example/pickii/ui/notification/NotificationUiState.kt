package com.example.pickii.ui.notification

data class NotificationUiState(
    val notifications: List<NotificationItemUiModel> = emptyList()
)

data class NotificationItemUiModel(
    val id: Long,
    val title: String,
    val description: String,
    val timeText: String,
    val type: NotificationType,
    /** 서버가 내려준 원본 type 문자열([NotificationType]이 못 담는 값도 포함). 알림 클릭 이동 분기에 쓴다. */
    val typeRaw: String = "",
    val isRead: Boolean,
    val referenceType: String?,
    val referenceId: String?
)

enum class NotificationType {
    CHAT,
    ACCEPT,
    APPLY,
    CLOSED,
    OTHER
}
