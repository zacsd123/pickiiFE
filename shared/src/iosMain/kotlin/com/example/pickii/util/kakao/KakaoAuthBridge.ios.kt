package com.example.pickii.util.kakao

import androidx.compose.runtime.Composable

/**
 * iOS는 Compose 트리보다 먼저(`iOSApp.swift`의 `init()`) Swift가 실제 구현체를 만들어
 * [KakaoAuthBridgeHolder]에 넣어둔다. 여기서는 그 값을 그대로 반환한다.
 */
@Composable
actual fun rememberKakaoAuthBridge(): KakaoAuthBridge =
    KakaoAuthBridgeHolder.bridge
        ?: error(
            "KakaoAuthBridge가 주입되지 않았습니다 — iOSApp.swift의 init()에서 " +
                "KakaoAuthBridgeHolder.shared.bridge를 설정했는지 확인하세요."
        )
