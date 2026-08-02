package com.example.pickii.di

import com.example.pickii.BuildConfig
import com.example.pickii.data.remote.AuthInterceptor
import com.example.pickii.data.remote.api.AuthApiService
import com.example.pickii.data.remote.api.FeedbackApiService
import com.example.pickii.data.remote.api.MasterDataApiService
import com.example.pickii.data.remote.api.MyPageActivityApiService
import com.example.pickii.data.remote.api.NotificationSettingsApiService
import com.example.pickii.data.remote.api.ProfileApiService
import com.example.pickii.data.remote.api.RecruitApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import javax.inject.Singleton

/** Retrofit/OkHttp 네트워킹 기반 인프라를 제공하는 Hilt 모듈. */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideJson(): Json =
        Json {
            ignoreUnknownKeys = true
            explicitNulls = false
            encodeDefaults = true
        }

    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient =
        OkHttpClient
            .Builder()
            .addInterceptor(authInterceptor)
            .apply {
                if (BuildConfig.DEBUG) {
                    addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
                }
            }.build()

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        json: Json
    ): Retrofit =
        Retrofit
            .Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

    @Provides
    @Singleton
    fun provideAuthApiService(retrofit: Retrofit): AuthApiService = retrofit.create(AuthApiService::class.java)

    @Provides
    @Singleton
    fun provideMasterDataApiService(retrofit: Retrofit): MasterDataApiService =
        retrofit.create(MasterDataApiService::class.java)

    @Provides
    @Singleton
    fun provideRecruitApiService(retrofit: Retrofit): RecruitApiService = retrofit.create(RecruitApiService::class.java)

    @Provides
    @Singleton
    fun provideProfileApiService(retrofit: Retrofit): ProfileApiService = retrofit.create(ProfileApiService::class.java)

    @Provides
    @Singleton
    fun provideMyPageActivityApiService(retrofit: Retrofit): MyPageActivityApiService =
        retrofit.create(MyPageActivityApiService::class.java)

    @Provides
    @Singleton
    fun provideNotificationSettingsApiService(retrofit: Retrofit): NotificationSettingsApiService =
        retrofit.create(NotificationSettingsApiService::class.java)

    @Provides
    @Singleton
    fun provideFeedbackApiService(retrofit: Retrofit): FeedbackApiService =
        retrofit.create(FeedbackApiService::class.java)
}
