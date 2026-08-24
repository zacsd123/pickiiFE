package com.example.pickii.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Phase 1 스켈레톤 단계에서 만든 플랫폼별 Ktor 엔진(Android=OkHttp, iOS=Darwin) 선택 진입점.
 * 실제 Phase 2 데이터 레이어 전환은 이 파일이 아니라 `data.remote.HttpClientFactory`
 * (인증/토큰 갱신을 포함한 Auth 플러그인 설정까지 갖춘 버전)로 진행됐다 — 이 파일은 어떤
 * API 서비스도 쓰지 않는 미사용 스캐폴딩으로 남아 있다.
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
