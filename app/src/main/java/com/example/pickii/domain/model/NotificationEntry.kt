package com.example.pickii.domain.model

import java.time.LocalDateTime

/** 알림 한 건(9-1). */
data class NotificationEntry(
    val id: String,
    val type: String,
    val title: String,
    val content: String,
    val referenceType: String?,
    val referenceId: String?,
    val isRead: Boolean,
    val sentAt: LocalDateTime
)
