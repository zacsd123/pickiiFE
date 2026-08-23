package com.example.pickii

import android.app.Application
import android.util.Log
import com.example.pickii.data.local.TokenStore
import com.example.pickii.data.notification.FcmTokenRegistrar
import com.example.pickii.di.calendarRepositoryModule
import com.example.pickii.di.infraModule
import com.example.pickii.di.networkModule
import com.example.pickii.di.repositoryModule
import com.example.pickii.di.viewModelModule
import com.example.pickii.util.debugRemainingValiditySeconds
import com.kakao.sdk.common.KakaoSdk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

private const val TOKEN_DEBUG_TAG = "TokenDebug"

class PickiiApplication : Application() {
    private val fcmTokenRegistrar: FcmTokenRegistrar by inject()
    private val tokenStore: TokenStore by inject()

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@PickiiApplication)
            modules(infraModule, networkModule, repositoryModule, calendarRepositoryModule, viewModelModule)
        }
        KakaoSdk.init(this, BuildConfig.KAKAO_NATIVE_APP_KEY, loggingEnabled = BuildConfig.DEBUG)
        fcmTokenRegistrar.start()
        // TODO(디버그 검증용 임시 코드, 확인 끝나면 삭제): 액세스 토큰 남은 유효시간만 로그로 확인.
        // 토큰 값 자체는 절대 로그로 남기지 않는다.
        if (BuildConfig.DEBUG) {
            CoroutineScope(Dispatchers.IO).launch {
                val token = tokenStore.accessTokenFlow.first()
                when {
                    token == null -> Log.d(TOKEN_DEBUG_TAG, "저장된 Access Token 없음 (로그인 안 된 상태)")
                    else ->
                        when (val remaining = debugRemainingValiditySeconds(token)) {
                            null -> Log.d(TOKEN_DEBUG_TAG, "exp 클레임을 읽을 수 없음 (파싱 실패 또는 클레임 없음)")
                            else -> Log.d(TOKEN_DEBUG_TAG, "Access Token 남은 유효시간: 약 ${remaining / 60}분 (${remaining}초)")
                        }
                }
            }
        }
    }
}
