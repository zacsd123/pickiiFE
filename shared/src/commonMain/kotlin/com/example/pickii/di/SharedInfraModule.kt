package com.example.pickii.di

import com.example.pickii.data.local.DeviceIdProvider
import com.example.pickii.data.local.SavedMeetingScheduleStore
import com.example.pickii.data.local.TokenStore
import com.example.pickii.data.notification.ActiveChatRoomTracker
import org.koin.dsl.module

/**
 * 로컬 저장소 인프라(DataStore 기반) — commonMain으로 옮겨진 것만 여기 담는다. 채팅 WebSocket/FCM
 * 등록처럼 아직 플랫폼별 구현이 필요한 인프라(Krossbow OkHttp 엔진, Firebase)는 Phase 5 범위라
 * app의 `infraModule`에 그대로 남아 있다.
 */
val sharedInfraModule =
    module {
        single { TokenStore() }
        single { DeviceIdProvider() }
        single { SavedMeetingScheduleStore() }
        single { ActiveChatRoomTracker() }
    }
