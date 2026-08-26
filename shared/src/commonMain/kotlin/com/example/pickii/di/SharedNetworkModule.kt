package com.example.pickii.di

import com.example.pickii.data.remote.AuthSession
import com.example.pickii.data.remote.HttpClientFactory
import com.example.pickii.data.remote.TokenStoreAuthSession
import com.example.pickii.data.remote.api.ApplicantApiService
import com.example.pickii.data.remote.api.AuthApiService
import com.example.pickii.data.remote.api.CalendarApiService
import com.example.pickii.data.remote.api.ChatApiService
import com.example.pickii.data.remote.api.FeedbackApiService
import com.example.pickii.data.remote.api.KtorApplicantApiService
import com.example.pickii.data.remote.api.KtorAuthApiService
import com.example.pickii.data.remote.api.KtorCalendarApiService
import com.example.pickii.data.remote.api.KtorChatApiService
import com.example.pickii.data.remote.api.KtorFeedbackApiService
import com.example.pickii.data.remote.api.KtorMasterDataApiService
import com.example.pickii.data.remote.api.KtorMeetingPollApiService
import com.example.pickii.data.remote.api.KtorMyPageActivityApiService
import com.example.pickii.data.remote.api.KtorNotificationApiService
import com.example.pickii.data.remote.api.KtorNotificationSettingsApiService
import com.example.pickii.data.remote.api.KtorProfileApiService
import com.example.pickii.data.remote.api.KtorProjectApiService
import com.example.pickii.data.remote.api.KtorRecruitApiService
import com.example.pickii.data.remote.api.MasterDataApiService
import com.example.pickii.data.remote.api.MeetingPollApiService
import com.example.pickii.data.remote.api.MyPageActivityApiService
import com.example.pickii.data.remote.api.NotificationApiService
import com.example.pickii.data.remote.api.NotificationSettingsApiService
import com.example.pickii.data.remote.api.ProfileApiService
import com.example.pickii.data.remote.api.ProjectApiService
import com.example.pickii.data.remote.api.RecruitApiService
import com.example.pickii.network.httpClientEngine
import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json
import org.koin.dsl.module

/** 백엔드 API 서버 기본 URL. app의 `BuildConfig.API_BASE_URL`과 같은 값이다(shared/commonMain은 BuildConfig를 모른다). */
internal const val API_BASE_URL = "https://pikiibackend-production.up.railway.app/api/v1/"

/**
 * Ktor 네트워킹 기반 인프라를 제공하는 Koin 모듈 — app의 `networkModule`에서 Chat 관련(OkHttpClient
 * 싱글턴, ChatApiService)을 뺀 13개 API 서비스만 옮겼다. Chat은 Krossbow WebSocket 엔진 교체가
 * 끝나야(Phase 5) 옮길 수 있다.
 *
 * 엔진은 Phase 1에서 만들어두고 안 쓰던 `network/HttpClientEngine.android.kt`/`.ios.kt`
 * expect/actual(`httpClientEngine()`)을 여기서 처음 실제로 연결한다 — app의 networkModule은
 * `OkHttp.create()`를 하드코딩해서 애초에 iOS로 옮길 수 없는 구조였다.
 */
val sharedNetworkModule =
    module {
        single {
            Json {
                ignoreUnknownKeys = true
                explicitNulls = false
                encodeDefaults = true
            }
        }

        single<AuthSession> { TokenStoreAuthSession(get(), get()) }

        single<HttpClient> {
            HttpClientFactory(
                engine = httpClientEngine().create(),
                baseUrl = API_BASE_URL,
                json = get(),
                authSession = get(),
                // TODO: app의 BuildConfig.DEBUG에 연동되던 값 — shared/commonMain은 빌드 타입을
                // 모른다. 안전한 기본값(꺼짐)으로 두고, 디버그 로깅이 다시 필요해지면 플랫폼별
                // "디버그 빌드인가" expect/actual을 추가해서 연결한다.
                enableBodyLogging = false
            ).create()
        }

        single<AuthApiService> { KtorAuthApiService(get()) }
        single<MasterDataApiService> { KtorMasterDataApiService(get()) }
        single<RecruitApiService> { KtorRecruitApiService(get()) }
        single<ProfileApiService> { KtorProfileApiService(get()) }
        single<MyPageActivityApiService> { KtorMyPageActivityApiService(get()) }
        single<NotificationSettingsApiService> { KtorNotificationSettingsApiService(get()) }
        single<FeedbackApiService> { KtorFeedbackApiService(get()) }
        single<ChatApiService> { KtorChatApiService(get()) }
        single<ApplicantApiService> { KtorApplicantApiService(get()) }
        single<NotificationApiService> { KtorNotificationApiService(get()) }
        single<CalendarApiService> { KtorCalendarApiService(get()) }
        single<MeetingPollApiService> { KtorMeetingPollApiService(get()) }
        single<ProjectApiService> { KtorProjectApiService(get()) }
    }
