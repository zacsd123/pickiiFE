package com.example.pickii.data.repository

import com.example.pickii.data.remote.api.NotificationApiService
import com.example.pickii.data.remote.dto.NotificationDto
import com.example.pickii.data.remote.dto.RegisterDeviceRequest
import com.example.pickii.data.remote.dto.UnregisterDeviceRequest
import com.example.pickii.domain.model.NotificationEntry
import com.example.pickii.domain.repository.NotificationRepository
import com.example.pickii.util.network.invalidIdException
import com.example.pickii.util.network.safeApiCall
import com.example.pickii.util.network.safeApiCallUnit
import com.example.pickii.util.parseIsoOffsetDateTime
import kotlinx.serialization.json.Json

private const val NOTIFICATIONS_PAGE_SIZE = 50
private const val DEVICE_PLATFORM = "ANDROID"

/** `9-1`~`9-4`, `9-7` API로 [NotificationRepository]를 구현한다. */
class NotificationApiRepository
    constructor(
        private val apiService: NotificationApiService,
        private val json: Json
    ) : NotificationRepository {
        override suspend fun getNotifications(): Result<List<NotificationEntry>> =
            safeApiCall(json) {
                apiService.getNotifications(page = 0, size = NOTIFICATIONS_PAGE_SIZE)
            }.map { envelope -> envelope.data.content.map { it.toDomain() } }

        override suspend fun readNotification(notificationId: String): Result<Unit> {
            val id = notificationId.toLongOrNull() ?: return Result.failure(invalidIdException(notificationId))
            return safeApiCallUnit(json) { apiService.readNotification(id) }
        }

        override suspend fun readAllNotifications(): Result<Unit> =
            safeApiCallUnit(json) { apiService.readAllNotifications() }

        override suspend fun deleteNotification(notificationId: String): Result<Unit> {
            val id = notificationId.toLongOrNull() ?: return Result.failure(invalidIdException(notificationId))
            return safeApiCallUnit(json) { apiService.deleteNotification(id) }
        }

        override suspend fun getUnreadCount(): Result<Int> =
            safeApiCall(json) { apiService.getUnreadCount() }.map { it.data.unreadCount }

        override suspend fun registerDevice(fcmToken: String): Result<Unit> =
            safeApiCallUnit(json) {
                apiService.registerDevice(RegisterDeviceRequest(fcmToken = fcmToken, platform = DEVICE_PLATFORM))
            }

        override suspend fun unregisterDevice(fcmToken: String): Result<Unit> =
            safeApiCallUnit(json) { apiService.unregisterDevice(UnregisterDeviceRequest(fcmToken = fcmToken)) }

        private fun NotificationDto.toDomain(): NotificationEntry =
            NotificationEntry(
                id = notificationId.toString(),
                type = type,
                title = title,
                content = content,
                referenceType = referenceType,
                referenceId = referenceId?.toString(),
                isRead = isRead,
                sentAt = parseIsoOffsetDateTime(sentAt)
            )
    }
