package com.example.pickii.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * 플랫폼별 Ktor 엔진(Android=OkHttp, iOS=Darwin)을 고르는 진입점. Phase 2에서
 * [com.example.pickii.data.remote.AuthInterceptor]/`TokenAuthenticator` 로직을 Ktor `Auth` 플러그인으로
 * 옮길 때 이 팩토리 위에 인증/토큰 갱신을 얹는다. 아직 어떤 API 서비스도 이 클라이언트를 쓰지 않는다.
 */
internal expect fun httpClientEngine(): HttpClientEngineFactory<*>

fun createHttpClient(
    json: Json,
    enableLogging: Boolean
): HttpClient =
    HttpClient(httpClientEngine()) {
        install(ContentNegotiation) { json(json) }
        if (enableLogging) {
            install(Logging) { level = LogLevel.BODY }
        }
    }
