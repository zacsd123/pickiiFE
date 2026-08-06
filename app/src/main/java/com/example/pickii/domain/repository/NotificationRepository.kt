package com.example.pickii.domain.repository

import com.example.pickii.domain.model.NotificationEntry

/** 알림 조회/읽음처리/삭제(9-1~9-4, 9-7)를 담당한다. */
interface NotificationRepository {
    suspend fun getNotifications(): Result<List<NotificationEntry>>

    suspend fun readNotification(notificationId: String): Result<Unit>

    suspend fun readAllNotifications(): Result<Unit>

    suspend fun deleteNotification(notificationId: String): Result<Unit>

    suspend fun getUnreadCount(): Result<Int>
}
