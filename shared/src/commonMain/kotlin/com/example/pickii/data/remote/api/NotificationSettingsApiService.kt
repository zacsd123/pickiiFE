package com.example.pickii.data.remote.api

import com.example.pickii.data.remote.dto.NotificationSettingsDto
import io.ktor.client.statement.HttpResponse

/** `9. Notification` 문서 중 알림 설정 조회/수정(9-5, 9-6)만 다룬다. */
interface NotificationSettingsApiService {
    suspend fun getSettings(): HttpResponse

    suspend fun updateSettings(request: NotificationSettingsDto): HttpResponse
}
