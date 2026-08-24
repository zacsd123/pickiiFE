package com.example.pickii.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import org.koin.core.context.GlobalContext

/**
 * shared 모듈에는 Composable이 아니라서 `LocalContext`를 쓸 수 없다 — 이미 Koin에
 * `androidContext()`로 등록돼 있는 애플리케이션 Context를 그대로 가져다 쓴다
 * ([PickiiApplication.onCreate] 참고).
 */
actual fun openUrl(url: String) {
    val context = GlobalContext.get().get<Context>()
    val intent =
        Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            // 애플리케이션 Context에서 startActivity를 호출하려면 반드시 필요하다(액티비티 스택이 없어서).
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    context.startActivity(intent)
}
