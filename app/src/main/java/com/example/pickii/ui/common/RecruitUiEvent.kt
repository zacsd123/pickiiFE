package com.example.pickii.ui.common

import org.jetbrains.compose.resources.StringResource

/** 화면에서 한 번만 소비해야 하는 1회성 UI 이벤트. ViewModel이 Context 없이 발행할 수 있도록 문자열 리소스로 전달한다. */
sealed interface RecruitUiEvent {
    /** 지정한 문자열 리소스를 토스트로 보여준다. */
    data class ShowToast(
        val messageRes: StringResource
    ) : RecruitUiEvent
}
