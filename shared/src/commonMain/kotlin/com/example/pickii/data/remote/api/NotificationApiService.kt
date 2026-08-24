package com.example.pickii.data.remote.api

import com.example.pickii.data.remote.dto.RegisterDeviceRequest
import com.example.pickii.data.remote.dto.UnregisterDeviceRequest
import io.ktor.client.statement.HttpResponse

/** 알림 조회/읽음처리/삭제(9-1~9-4, 9-7) 및 FCM 디바이스 토큰 등록/삭제(9-8, 9-9) API. */
interface NotificationApiService {
    suspend fun getNotifications(
        page: Int,
        size: Int
    ): HttpResponse

    /** 9-2 알림 읽음 처리. */
    suspend fun readNotification(notificationId: Long): HttpResponse

    /** 9-3 전체 읽음 처리. */
    suspend fun readAllNotifications(): HttpResponse

    /** 9-4 알림 삭제. */
    suspend fun deleteNotification(notificationId: Long): HttpResponse

    /** 9-7 안 읽은 알림 개수 조회. */
    suspend fun getUnreadCount(): HttpResponse

    /** 9-8 디바이스 토큰 등록(upsert). */
    suspend fun registerDevice(request: RegisterDeviceRequest): HttpResponse

    /** 9-9 디바이스 토큰 삭제. */
    suspend fun unregisterDevice(request: UnregisterDeviceRequest): HttpResponse
}
