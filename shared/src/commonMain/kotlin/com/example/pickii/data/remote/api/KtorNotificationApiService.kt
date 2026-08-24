package com.example.pickii.data.remote.api

import com.example.pickii.data.remote.dto.RegisterDeviceRequest
import com.example.pickii.data.remote.dto.UnregisterDeviceRequest
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse

/** [NotificationApiService]를 Ktor [HttpClient]로 구현한다. */
class KtorNotificationApiService(
    private val client: HttpClient
) : NotificationApiService {
    override suspend fun getNotifications(
        page: Int,
        size: Int
    ): HttpResponse =
        client.get("notifications") {
            parameter("page", page)
            parameter("size", size)
        }

    override suspend fun readNotification(notificationId: Long): HttpResponse = client.patch("notifications/$notificationId/read")

    override suspend fun readAllNotifications(): HttpResponse = client.patch("notifications/read-all")

    override suspend fun deleteNotification(notificationId: Long): HttpResponse = client.delete("notifications/$notificationId")

    override suspend fun getUnreadCount(): HttpResponse = client.get("notifications/unread-count")

    override suspend fun registerDevice(request: RegisterDeviceRequest): HttpResponse =
        client.post("devices") {
            setBody(request)
        }

    override suspend fun unregisterDevice(request: UnregisterDeviceRequest): HttpResponse =
        client.delete("devices") {
            setBody(request)
        }
}
