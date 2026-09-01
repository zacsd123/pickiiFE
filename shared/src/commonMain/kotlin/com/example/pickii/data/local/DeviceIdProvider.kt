package com.example.pickii.data.local

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

private val DEVICE_ID_KEY = stringPreferencesKey("device_id")

/** 로그인 API의 `deviceId`로 사용할, 앱 설치 단위로 고정된 UUID를 발급/조회한다. Koin 싱글턴(`di/InfraModule.kt`). */
class DeviceIdProvider {
    private val dataStore = preferencesDataStore("pickii_device_store")

    @OptIn(ExperimentalUuidApi::class)
    suspend fun getDeviceId(): String {
        val stored = dataStore.data.first()[DEVICE_ID_KEY]
        if (stored != null) return stored

        val generated = Uuid.random().toString()
        dataStore.edit { it[DEVICE_ID_KEY] = generated }
        return generated
    }
}
