package com.example.pickii.di

import com.example.pickii.data.notification.FcmTokenRegistrar
import com.example.pickii.data.remote.socket.ChatStompClient
import org.koin.dsl.module

/**
 * 채팅 WebSocket·FCM 등록처럼 아직 iOS로 옮길 수 없는(Krossbow OkHttp 엔진 교체/Firebase 교체가
 * Phase 5 범위) 인프라만 남은 모듈. 로컬 저장소(TokenStore 등)는 shared의 sharedModule로 옮겨졌다.
 */
val infraModule =
    module {
        single { ChatStompClient(get(), get(), get()) }
        single { FcmTokenRegistrar(get(), get()) }
    }
