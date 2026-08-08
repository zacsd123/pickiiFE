package com.example.pickii.data.remote.api

import com.example.pickii.data.remote.dto.ApiEnvelope
import com.example.pickii.data.remote.dto.NotificationDto
import com.example.pickii.data.remote.dto.PageEnvelope
import com.example.pickii.data.remote.dto.RegisterDeviceRequest
import com.example.pickii.data.remote.dto.UnreadCountDto
import com.example.pickii.data.remote.dto.UnregisterDeviceRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/** 알림 조회/읽음처리/삭제(9-1~9-4, 9-7) 및 FCM 디바이스 토큰 등록/삭제(9-8, 9-9) API. */
interface NotificationApiService {
    @GET("notifications")
    suspend fun getNotifications(
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Response<ApiEnvelope<PageEnvelope<NotificationDto>>>

    /** 9-2 알림 읽음 처리. */
    @PATCH("notifications/{notificationId}/read")
    suspend fun readNotification(
        @Path("notificationId") notificationId: Long
    ): Response<Unit>

    /** 9-3 전체 읽음 처리. */
    @PATCH("notifications/read-all")
    suspend fun readAllNotifications(): Response<Unit>

    /** 9-4 알림 삭제. */
    @DELETE("notifications/{notificationId}")
    suspend fun deleteNotification(
        @Path("notificationId") notificationId: Long
    ): Response<Unit>

    /** 9-7 안 읽은 알림 개수 조회. */
    @GET("notifications/unread-count")
    suspend fun getUnreadCount(): Response<ApiEnvelope<UnreadCountDto>>

    /** 9-8 디바이스 토큰 등록(upsert). */
    @POST("devices")
    suspend fun registerDevice(
        @Body request: RegisterDeviceRequest
    ): Response<Unit>

    /** 9-9 디바이스 토큰 삭제. */
    @HTTP(method = "DELETE", path = "devices", hasBody = true)
    suspend fun unregisterDevice(
        @Body request: UnregisterDeviceRequest
    ): Response<Unit>
}
