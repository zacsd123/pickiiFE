package com.example.pickii.debug

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.pickii.data.local.TokenStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

private const val TAG = "TokenCorruptionDebug"

/**
 * TODO(디버그 검증용 임시 코드, 확인 끝나면 삭제): 저장된 Access Token만 깨뜨려서 다음 API 호출이
 * 401을 받게 만들고, `HttpClientFactory`의 Ktor `Auth` 플러그인 갱신 경로를 실기기에서 토큰의
 * 실제 만료를 기다리지 않고 바로 확인하기 위한 트리거. `app/src/debug` 전용 소스셋이라 release
 * 빌드에는 이 파일 자체가 포함되지 않는다.
 *
 * 트리거 방법 (adb, 컴포넌트 직접 지정이라 exported 권한 불필요):
 *   adb shell am broadcast -n com.example.pickii/.debug.TokenCorruptionDebugReceiver \
 *     -a com.example.pickii.debug.CORRUPT_ACCESS_TOKEN
 *
 * 실행 후 앱에서 아무 화면이나 새로고침해서(예: 홈 pull-to-refresh) API 호출을 한 번 유도하면 된다.
 */
class TokenCorruptionDebugReceiver :
    BroadcastReceiver(),
    KoinComponent {
    private val tokenStore: TokenStore by inject()

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                tokenStore.debugCorruptAccessToken()
                Log.d(TAG, "Access Token을 의도적으로 깨뜨렸습니다 — 다음 API 호출에서 401 → 자동 갱신 경로를 확인하세요.")
            } finally {
                pendingResult.finish()
            }
        }
    }
}
