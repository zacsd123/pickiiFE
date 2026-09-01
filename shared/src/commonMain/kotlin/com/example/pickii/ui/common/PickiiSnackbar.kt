package com.example.pickii.ui.common

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * 앱 전역에서 하나만 쓰는 [SnackbarHostState]. `MainActivity`의 `PickiiNavHost` 루트에서 만들어
 * `CompositionLocalProvider`로 내려준다 — 화면마다 따로 호스트를 두지 않고 다 같이 쓴다(Toast를
 * 대체하는 자리라 Toast처럼 화면 전환에 영향받지 않고 어디서든 하나로 큐잉되길 원해서).
 */
val LocalSnackbarHostState =
    staticCompositionLocalOf<SnackbarHostState> {
        error("LocalSnackbarHostState가 제공되지 않았다 — PickiiNavHost 루트가 CompositionLocalProvider로 감싸야 한다.")
    }
