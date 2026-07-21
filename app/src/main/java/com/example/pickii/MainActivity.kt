package com.example.pickii

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.pickii.ui.login.LoginScreen
import com.example.pickii.ui.splash.SplashScreen
import com.example.pickii.ui.theme.PickiiTheme
import dagger.hilt.android.AndroidEntryPoint

/** 앱이 현재 보여주고 있는 최상위 화면. */
private enum class PickiiScreen {
    Splash,
    Login
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    /**
     * 스플래시 화면을 먼저 띄우고, 대기 시간이 끝나면 로그인 화면으로 전환하는 콘텐츠를 구성한다.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PickiiTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    var currentScreen by remember { mutableStateOf(PickiiScreen.Splash) }

                    when (currentScreen) {
                        PickiiScreen.Splash -> SplashScreen(
                            onTimeout = { currentScreen = PickiiScreen.Login }
                        )

                        PickiiScreen.Login -> LoginScreen()
                    }
                }
            }
        }
    }
}
