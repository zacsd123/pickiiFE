package com.example.pickii.data.remote.api

import com.example.pickii.data.remote.dto.ApiEnvelope
import com.example.pickii.data.remote.dto.NotificationSettingsDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH

/** `9. Notification` 문서 중 알림 설정 조회/수정(9-5, 9-6)만 다룬다. */
interface NotificationSettingsApiService {
    @GET("users/me/notification-settings")
    suspend fun getSettings(): Response<ApiEnvelope<NotificationSettingsDto>>

    @PATCH("users/me/notification-settings")
    suspend fun updateSettings(
        @Body request: NotificationSettingsDto
    ): Response<Unit>
}
