package com.example.pickii.domain.repository

import com.example.pickii.domain.model.NotificationSettings

/** 알림 설정 조회/수정을 담당한다(9-5, 9-6). */
interface NotificationSettingsRepository {
    suspend fun getSettings(): Result<NotificationSettings>

    suspend fun updateSettings(settings: NotificationSettings): Result<Unit>
}
