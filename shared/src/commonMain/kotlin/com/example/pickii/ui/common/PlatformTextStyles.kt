package com.example.pickii.ui.common

import androidx.compose.ui.text.PlatformTextStyle

/**
 * Android의 폰트 패딩(글자 위아래 여백) 보정을 끈 [PlatformTextStyle]. `includeFontPadding`은 Android
 * 텍스트 렌더링에만 있는 레거시 개념이라 iOS엔 대응 생성자가 없다 — iOS는 애초에 이 보정이 필요한
 * 문제 자체가 없어서(폰트 패딩 여백이 없음) null을 돌려준다.
 */
expect fun noFontPaddingTextStyle(): PlatformTextStyle?
