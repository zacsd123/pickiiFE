package com.example.pickii.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.flow.Flow

/**
 * [flow]에서 방출되는 1회성 이벤트를 구독해 [onEvent]로 전달한다.
 * ViewModel의 Channel/SharedFlow 기반 이벤트(토스트 등)를 화면에서 소비할 때 사용한다.
 *
 * [onEvent]가 suspend인 이유: 토스트 메시지가 [org.jetbrains.compose.resources.StringResource]로
 * 넘어오는데, 이걸 실제 문자열로 바꾸려면 CMP의 suspend `getString()`을 호출해야 한다.
 */
@Composable
fun <T> OneShotEventEffect(
    flow: Flow<T>,
    onEvent: suspend (T) -> Unit
) {
    LaunchedEffect(flow) {
        flow.collect { event -> onEvent(event) }
    }
}
