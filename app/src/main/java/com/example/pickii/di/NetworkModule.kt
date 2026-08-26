package com.example.pickii.di

import okhttp3.OkHttpClient
import org.koin.dsl.module

/**
 * Ktor API 서비스 13개는 shared의 `sharedNetworkModule`로 옮겨갔다. 여기 남은 건 Chat 전용
 * OkHttpClient 하나뿐 — `ChatStompClient`(Krossbow `websocket-okhttp`)가 아직 물고 있다.
 * Ktor 엔진(krossbow-websocket-ktor)으로 옮기면 이 모듈 자체를 지울 수 있는데, Phase 5 범위다.
 */
val networkModule =
    module {
        single { OkHttpClient.Builder().build() }
    }
