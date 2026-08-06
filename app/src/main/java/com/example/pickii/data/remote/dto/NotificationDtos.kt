package com.example.pickii.data.remote.dto

import kotlinx.serialization.Serializable

/** `GET /notifications`(9-1) 응답 항목. */
@Serializable
data class NotificationDto(
    val notificationId: Long,
    val type: String,
    val title: String,
    val content: String,
    val referenceType: String? = null,
    val referenceId: Long? = null,
    val isRead: Boolean,
    val sentAt: String,
    val readAt: String? = null
)

/** `GET /notifications/unread-count`(9-7) 응답. */
@Serializable
data class UnreadCountDto(
    val unreadCount: Int
)
