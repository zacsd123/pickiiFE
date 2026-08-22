package com.example.pickii.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.pickii.BuildConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

private val Context.tokenDataStore by preferencesDataStore(name = "pickii_token_store")

private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")

/** 로그인 Access/Refresh Token을 DataStore에 저장/조회한다. Koin 싱글턴(`di/InfraModule.kt`). */
class TokenStore(
    private val context: Context
) {
    /** 현재 저장된 Access Token. 비로그인 상태면 null이다. */
    val accessTokenFlow: Flow<String?> = context.tokenDataStore.data.map { it[ACCESS_TOKEN_KEY] }

    /** 현재 저장된 Refresh Token. 비로그인 상태면 null이다. */
    val refreshTokenFlow: Flow<String?> = context.tokenDataStore.data.map { it[REFRESH_TOKEN_KEY] }

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

    /** [com.example.pickii.data.remote.TokenAuthenticator]가 코루틴 밖에서 동기적으로 Refresh Token을 읽을 때 사용한다. */
    fun currentRefreshTokenBlocking(): String? = runBlocking { refreshTokenFlow.first() }

    /**
     * TODO(디버그 검증용 임시 코드, 확인 끝나면 삭제): 저장된 Access Token만 의도적으로 깨뜨리고
     * Refresh Token은 그대로 둔다. 다음 API 호출이 401을 받아 [com.example.pickii.data.remote.TokenAuthenticator]의
     * 갱신 경로(Provider→Lazy로 바꾼 부분)를 실기기에서 토큰 실제 만료를 기다리지 않고 바로 태울 때 쓴다.
     */
    suspend fun debugCorruptAccessToken() {
        if (!BuildConfig.DEBUG) return
        context.tokenDataStore.edit { prefs ->
            prefs[ACCESS_TOKEN_KEY] = "debug-corrupted-${System.currentTimeMillis()}"
        }
    }
}
