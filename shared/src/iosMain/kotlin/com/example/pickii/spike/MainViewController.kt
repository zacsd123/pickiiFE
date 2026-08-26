package com.example.pickii.spike

import androidx.compose.ui.window.ComposeUIViewController
import com.example.pickii.ui.splash.SplashScreen
import com.example.pickii.ui.theme.PickiiTheme
import platform.UIKit.UIViewController

/**
 * iOS 진입점. 아직 iOS 쪽 NavHost가 없어서, 이식 중인 화면을 눈으로 확인할 카나리아를 여기 직접
 * 넣는다 — 화면이 늘어나면 이 함수가 임시로 보여주는 대상만 바뀐다.
 */
fun MainViewController(): UIViewController =
    ComposeUIViewController {
        PickiiTheme {
            SplashScreen()
        }
    }
