package com.example.pickii.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

/**
 * Material You 다이나믹 컬러 스킴(Android 12+ 전용). `dynamicDarkColorScheme`/`dynamicLightColorScheme`가
 * `android.content.Context`(`LocalContext`)를 요구해 commonMain에서 직접 못 불러 expect/actual로 감쌌다.
 * 호출부는 [supportsDynamicColor]로 먼저 걸러지므로 iOS의 null 반환은 실제로는 도달하지 않는다.
 */
@Composable
expect fun dynamicColorScheme(darkTheme: Boolean): ColorScheme?
