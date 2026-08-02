package com.example.pickii.data.remote.dto

import kotlinx.serialization.Serializable

/** `GET/PATCH /users/me/notification-settings`(9-5, 9-6) 요청/응답 공용 바디. */
@Serializable
data class NotificationSettingsDto(
    val chatNoti: Boolean,
    val applicantNoti: Boolean,
    val commentNoti: Boolean,
    val scheduleNoti: Boolean,
    val matchNoti: Boolean,
    val projectNoti: Boolean,
    val marketingNoti: Boolean
)
