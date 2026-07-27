package com.example.pickii.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.flow.Flow

/**
 * [flow]에서 방출되는 1회성 이벤트를 구독해 [onEvent]로 전달한다.
 * ViewModel의 Channel/SharedFlow 기반 이벤트(토스트 등)를 화면에서 소비할 때 사용한다.
 */
@Composable
fun <T> OneShotEventEffect(
    flow: Flow<T>,
    onEvent: (T) -> Unit
) {
    LaunchedEffect(flow) {
        flow.collect { event -> onEvent(event) }
    }
}
