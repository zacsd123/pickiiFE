package com.example.pickii.data.repository

import com.example.pickii.data.remote.api.NotificationApiService
import com.example.pickii.data.remote.dto.NotificationDto
import com.example.pickii.domain.model.NotificationEntry
import com.example.pickii.domain.repository.NotificationRepository
import com.example.pickii.util.network.safeApiCall
import com.example.pickii.util.network.safeApiCallUnit
import kotlinx.serialization.json.Json
import java.time.LocalDateTime
import java.time.OffsetDateTime
import javax.inject.Inject
import javax.inject.Singleton

private const val NOTIFICATIONS_PAGE_SIZE = 50

/** `9-1`~`9-4`, `9-7` API로 [NotificationRepository]를 구현한다. */
@Singleton
class NotificationApiRepository
    @Inject
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

private fun invalidIdException(id: String) = IllegalArgumentException("잘못된 id: $id")

private fun parseIsoOffsetDateTime(value: String): LocalDateTime = OffsetDateTime.parse(value).toLocalDateTime()
