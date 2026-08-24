package com.example.pickii.data.remote.api

import com.example.pickii.data.remote.dto.NotificationSettingsDto
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse

/** [NotificationSettingsApiService]를 Ktor [HttpClient]로 구현한다. */
class KtorNotificationSettingsApiService(
    private val client: HttpClient
) : NotificationSettingsApiService {
    override suspend fun getSettings(): HttpResponse = client.get("users/me/notification-settings")

    override suspend fun updateSettings(request: NotificationSettingsDto): HttpResponse =
        client.patch("users/me/notification-settings") { setBody(request) }
}
