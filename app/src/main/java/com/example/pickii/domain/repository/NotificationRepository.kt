package com.example.pickii.domain.repository

import com.example.pickii.domain.model.NotificationEntry

/** 알림 조회/읽음처리/삭제(9-1~9-4, 9-7) 및 FCM 디바이스 토큰 등록/삭제(9-8, 9-9)를 담당한다. */
interface NotificationRepository {
    suspend fun getNotifications(): Result<List<NotificationEntry>>

    suspend fun readNotification(notificationId: String): Result<Unit>

    suspend fun readAllNotifications(): Result<Unit>

    suspend fun deleteNotification(notificationId: String): Result<Unit>

    suspend fun getUnreadCount(): Result<Int>

    /** 이 기기의 FCM 토큰을 서버에 등록(upsert)한다(9-8). */
    suspend fun registerDevice(fcmToken: String): Result<Unit>

    /** 이 기기의 FCM 토큰을 서버에서 삭제한다(9-9, 로그아웃 시 호출). */
    suspend fun unregisterDevice(fcmToken: String): Result<Unit>
}
