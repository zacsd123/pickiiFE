package com.example.pickii.spike

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.pickii.spike.kakao.KakaoAuthBridgeHolder

@Composable
fun SpikeScreen() {
    var count by remember { mutableIntStateOf(0) }
    var kakaoResultText by remember { mutableStateOf("아직 로그인 안 함") }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text("Hello Pickii")
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { count++ }) {
                    Text("Count: $count")
                }

                Spacer(modifier = Modifier.height(48.dp))

                Button(onClick = {
                    kakaoResultText = "로그인 중..."
                    val bridge = KakaoAuthBridgeHolder.bridge
                    if (bridge == null) {
                        kakaoResultText = "브릿지 미연결 (iosApp에서 KakaoAuthBridgeHolder.bridge를 안 채웠음)"
                    } else {
                        bridge.login { accessToken, errorMessage ->
                            kakaoResultText =
                                when {
                                    accessToken != null -> "토큰: ${accessToken.take(12)}..."
                                    else -> "실패: $errorMessage"
                                }
                        }
                    }
                }) {
                    Text("카카오 로그인")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = kakaoResultText,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                )
            }
        }
    }
}
