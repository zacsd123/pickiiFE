package com.example.pickii.util.kakao

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * [KakaoAuthBridge]의 Android 구현. [KakaoAuthClient](suspend 기반)를 그대로 감싸기만 한다 — 카카오
 * SDK 호출 로직은 여기서 새로 만들지 않는다. 인터페이스가 콜백 형태(iOS 제약 때문)라, Compose의
 * [CoroutineScope]로 suspend 호출을 콜백에 이어 붙인다.
 */
private class AndroidKakaoAuthBridge(
    private val context: Context,
    private val scope: CoroutineScope
) : KakaoAuthBridge {
    override fun login(onResult: (accessToken: String?, errorMessage: String?) -> Unit) {
        scope.launch {
            KakaoAuthClient
                .login(context)
                .onSuccess { onResult(it.accessToken, null) }
                .onFailure { onResult(null, it.message) }
        }
    }

    override fun getUserId(onResult: (userId: Long?, errorMessage: String?) -> Unit) {
        scope.launch {
            KakaoAuthClient
                .getUserId()
                .onSuccess { onResult(it, null) }
                .onFailure { onResult(null, it.message) }
        }
    }
}

/**
 * Android SDK는 카카오톡 앱 전환에 Activity Context가 필요해서 [LocalContext.current]를 그대로
 * 구현체에 넘긴다. 별도 저장소(전역 var) 없이 컴포지션마다 얻는다.
 */
@Composable
actual fun rememberKakaoAuthBridge(): KakaoAuthBridge {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    return remember(context, scope) { AndroidKakaoAuthBridge(context, scope) }
}
