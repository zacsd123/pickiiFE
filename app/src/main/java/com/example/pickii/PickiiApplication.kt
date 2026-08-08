package com.example.pickii

import android.app.Application
import com.example.pickii.data.notification.FcmTokenRegistrar
import com.kakao.sdk.common.KakaoSdk
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class PickiiApplication : Application() {
    @Inject
    lateinit var fcmTokenRegistrar: FcmTokenRegistrar

    override fun onCreate() {
        super.onCreate()
        KakaoSdk.init(this, BuildConfig.KAKAO_NATIVE_APP_KEY, loggingEnabled = BuildConfig.DEBUG)
        fcmTokenRegistrar.start()
    }
}
