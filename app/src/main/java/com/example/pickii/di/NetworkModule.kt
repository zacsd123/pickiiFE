package com.example.pickii.di

import com.example.pickii.BuildConfig
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
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import org.koin.dsl.module

/** Ktor 네트워킹 기반 인프라를 제공하는 Koin 모듈 (Hilt `NetworkModule`을 대체). */
val networkModule =
    module {
        single {
            Json {
                ignoreUnknownKeys = true
                explicitNulls = false
                encodeDefaults = true
            }
        }

        // Retrofit은 걷어냈지만 OkHttpClient 자체는 아직 필요하다 — ChatStompClient(Krossbow
        // websocket-okhttp)가 물고 있다. Ktor 엔진(krossbow-websocket-ktor)으로 옮기면 이 싱글턴도
        // 정리할 수 있는데, 이번 작업 범위 밖이다.
        single { OkHttpClient.Builder().build() }

        single<AuthSession> { TokenStoreAuthSession(get(), get()) }

        single<HttpClient> {
            HttpClientFactory(
                engine = OkHttp.create(),
                baseUrl = BuildConfig.API_BASE_URL,
                json = get(),
                authSession = get(),
                // 기존 OkHttp HttpLoggingInterceptor(BODY, BuildConfig.DEBUG 가드)와 동일한 정책.
                enableBodyLogging = BuildConfig.DEBUG
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
