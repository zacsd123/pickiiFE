package com.example.pickii.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import java.util.UUID

private val Context.deviceIdDataStore by preferencesDataStore(name = "pickii_device_store")

private val DEVICE_ID_KEY = stringPreferencesKey("device_id")

/** 로그인 API의 `deviceId`로 사용할, 앱 설치 단위로 고정된 UUID를 발급/조회한다. Koin 싱글턴(`di/InfraModule.kt`). */
class DeviceIdProvider(
    private val context: Context
) {
    suspend fun getDeviceId(): String {
        val stored = context.deviceIdDataStore.data.first()[DEVICE_ID_KEY]
        if (stored != null) return stored

        val generated = UUID.randomUUID().toString()
        context.deviceIdDataStore.edit { it[DEVICE_ID_KEY] = generated }
        return generated
    }
}
