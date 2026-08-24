package com.example.pickii.di

import com.example.pickii.data.local.DeviceIdProvider
import com.example.pickii.data.local.SavedMeetingScheduleStore
import com.example.pickii.data.local.TokenStore
import com.example.pickii.data.notification.ActiveChatRoomTracker
import com.example.pickii.data.notification.FcmTokenRegistrar
import com.example.pickii.data.remote.socket.ChatStompClient
import org.koin.dsl.module

/**
 * 로컬 저장소·채팅 소켓·FCM 등록 등, Hilt에서는 `@Inject constructor`만으로
 * 암묵적으로 제공되던 인프라 계층. Koin은 애노테이션 스캔이 없어서 전부 명시적으로 등록해야 한다.
 *
 * [FcmTokenRegistrar]가 요구하는 [com.example.pickii.domain.repository.NotificationRepository]/
 * [com.example.pickii.domain.repository.SessionRepository]는 아직 이 모듈에 없다(리포지토리 계층은
 * 다음 단계에서 별도 모듈로 등록) — `startKoin`으로 전체 모듈을 합치기 전까지는 정상이다.
 */
val infraModule =
    module {
        single { TokenStore(get()) }
        single { DeviceIdProvider(get()) }
        single { SavedMeetingScheduleStore(get()) }
        single { ActiveChatRoomTracker() }
        single { ChatStompClient(get(), get(), get()) }
        single { FcmTokenRegistrar(get(), get()) }
    }
