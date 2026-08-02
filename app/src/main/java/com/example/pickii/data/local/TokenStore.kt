package com.example.pickii.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

private val Context.tokenDataStore by preferencesDataStore(name = "pickii_token_store")

private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")

/** 로그인 Access/Refresh Token을 DataStore에 저장/조회한다. */
@Singleton
class TokenStore
    @Inject
    constructor(
        @ApplicationContext private val context: Context
    ) {
        /** 현재 저장된 Access Token. 비로그인 상태면 null이다. */
        val accessTokenFlow: Flow<String?> = context.tokenDataStore.data.map { it[ACCESS_TOKEN_KEY] }

        suspend fun saveTokens(
            accessToken: String,
            refreshToken: String
        ) {
            context.tokenDataStore.edit { prefs ->
                prefs[ACCESS_TOKEN_KEY] = accessToken
                prefs[REFRESH_TOKEN_KEY] = refreshToken
            }
        }

        suspend fun clear() {
            context.tokenDataStore.edit { it.clear() }
        }

        /** [AuthInterceptor]처럼 코루틴 밖(OkHttp 백그라운드 스레드)에서 동기적으로 토큰을 읽을 때 사용한다. */
        fun currentAccessTokenBlocking(): String? = runBlocking { accessTokenFlow.first() }
    }
