package com.example.pickii.di

import com.example.pickii.BuildConfig
import com.example.pickii.data.remote.AuthInterceptor
import com.example.pickii.data.remote.TokenAuthenticator
import com.example.pickii.data.remote.api.ApplicantApiService
import com.example.pickii.data.remote.api.AuthApiService
import com.example.pickii.data.remote.api.CalendarApiService
import com.example.pickii.data.remote.api.ChatApiService
import com.example.pickii.data.remote.api.FeedbackApiService
import com.example.pickii.data.remote.api.MasterDataApiService
import com.example.pickii.data.remote.api.MeetingPollApiService
import com.example.pickii.data.remote.api.MyPageActivityApiService
import com.example.pickii.data.remote.api.NotificationApiService
import com.example.pickii.data.remote.api.NotificationSettingsApiService
import com.example.pickii.data.remote.api.ProfileApiService
import com.example.pickii.data.remote.api.ProjectApiService
import com.example.pickii.data.remote.api.RecruitApiService
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/** Retrofit/OkHttp 네트워킹 기반 인프라를 제공하는 Koin 모듈 (Hilt `NetworkModule`을 대체). */
val networkModule =
    module {
        single {
            Json {
                ignoreUnknownKeys = true
                explicitNulls = false
                encodeDefaults = true
            }
        }

        single {
            OkHttpClient
                .Builder()
                .addInterceptor(get<AuthInterceptor>())
                .authenticator(get<TokenAuthenticator>())
                .apply {
                    if (BuildConfig.DEBUG) {
                        addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
                    }
                }.build()
        }

        single {
            Retrofit
                .Builder()
                .baseUrl(BuildConfig.API_BASE_URL)
                .client(get())
                .addConverterFactory(get<Json>().asConverterFactory("application/json".toMediaType()))
                .build()
        }

        single<AuthApiService> { get<Retrofit>().create(AuthApiService::class.java) }
        single<MasterDataApiService> { get<Retrofit>().create(MasterDataApiService::class.java) }
        single<RecruitApiService> { get<Retrofit>().create(RecruitApiService::class.java) }
        single<ProfileApiService> { get<Retrofit>().create(ProfileApiService::class.java) }
        single<MyPageActivityApiService> { get<Retrofit>().create(MyPageActivityApiService::class.java) }
        single<NotificationSettingsApiService> { get<Retrofit>().create(NotificationSettingsApiService::class.java) }
        single<FeedbackApiService> { get<Retrofit>().create(FeedbackApiService::class.java) }
        single<ChatApiService> { get<Retrofit>().create(ChatApiService::class.java) }
        single<ApplicantApiService> { get<Retrofit>().create(ApplicantApiService::class.java) }
        single<NotificationApiService> { get<Retrofit>().create(NotificationApiService::class.java) }
        single<CalendarApiService> { get<Retrofit>().create(CalendarApiService::class.java) }
        single<MeetingPollApiService> { get<Retrofit>().create(MeetingPollApiService::class.java) }
        single<ProjectApiService> { get<Retrofit>().create(ProjectApiService::class.java) }
    }
