package com.example.pickii.data.local

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")

/** 로그인 Access/Refresh Token을 DataStore에 저장/조회한다. Koin 싱글턴(`di/InfraModule.kt`). */
class TokenStore {
    private val dataStore = preferencesDataStore("pickii_token_store")

    /** 현재 저장된 Access Token. 비로그인 상태면 null이다. */
    val accessTokenFlow: Flow<String?> = dataStore.data.map { it[ACCESS_TOKEN_KEY] }

    /** 현재 저장된 Refresh Token. 비로그인 상태면 null이다. */
    val refreshTokenFlow: Flow<String?> = dataStore.data.map { it[REFRESH_TOKEN_KEY] }

    suspend fun saveTokens(
        accessToken: String,
        refreshToken: String
    ) {
        dataStore.edit { prefs ->
            prefs[ACCESS_TOKEN_KEY] = accessToken
            prefs[REFRESH_TOKEN_KEY] = refreshToken
        }
    }

    suspend fun clear() {
        dataStore.edit { it.clear() }
    }
}
