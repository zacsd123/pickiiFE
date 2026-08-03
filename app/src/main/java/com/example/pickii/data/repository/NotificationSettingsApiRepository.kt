package com.example.pickii.data.repository

import com.example.pickii.data.remote.api.NotificationSettingsApiService
import com.example.pickii.data.remote.dto.NotificationSettingsDto
import com.example.pickii.domain.model.NotificationSettings
import com.example.pickii.domain.repository.NotificationSettingsRepository
import com.example.pickii.util.network.safeApiCall
import com.example.pickii.util.network.safeApiCallUnit
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/** `9-5`, `9-6` API로 [NotificationSettingsRepository]를 구현한다. */
@Singleton
class NotificationSettingsApiRepository
    @Inject
    constructor(
        private val apiService: NotificationSettingsApiService,
        private val json: Json
    ) : NotificationSettingsRepository {
        override suspend fun getSettings(): Result<NotificationSettings> =
            safeApiCall(json) { apiService.getSettings() }.map { it.data.toDomain() }

        override suspend fun updateSettings(settings: NotificationSettings): Result<Unit> =
            safeApiCallUnit(json) { apiService.updateSettings(settings.toRequest()) }

        private fun NotificationSettingsDto.toDomain() =
            NotificationSettings(
                chatNoti = chatNoti,
                applicantNoti = applicantNoti,
                commentNoti = commentNoti,
                scheduleNoti = scheduleNoti,
                matchNoti = matchNoti,
                projectNoti = projectNoti,
                marketingNoti = marketingNoti
            )

        private fun NotificationSettings.toRequest() =
            NotificationSettingsDto(
                chatNoti = chatNoti,
                applicantNoti = applicantNoti,
                commentNoti = commentNoti,
                scheduleNoti = scheduleNoti,
                matchNoti = matchNoti,
                projectNoti = projectNoti,
                marketingNoti = marketingNoti
            )
    }
