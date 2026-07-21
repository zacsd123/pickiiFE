package com.example.pickii.ui.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pickii.R
import com.example.pickii.ui.theme.PickiiTextGray
import com.example.pickii.ui.theme.PickiiYellow
import com.example.pickii.ui.theme.PickiiYellowLight

/**
 * 앱 진입 시 표시되는 스플래시 화면.
 *
 * [SplashViewModel]이 대기 시간이 끝났음을 알리면 [onTimeout]을 호출해 다음 화면으로 전환한다.
 *
 * @param onTimeout 대기 시간이 끝났을 때 호출되는 콜백
 */
@Composable
fun SplashScreen(
    onTimeout: () -> Unit = {},
    viewModel: SplashViewModel = hiltViewModel()
) {
    val isReadyToProceed by viewModel.isReadyToProceed.collectAsStateWithLifecycle()

    LaunchedEffect(isReadyToProceed) {
        if (isReadyToProceed) {
            onTimeout()
        }
    }

    SplashScreenContent()
}

/** [SplashScreen]의 실제 UI. ViewModel 없이도 미리보기가 가능하도록 분리했다. */
@Composable
private fun SplashScreenContent() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(PickiiYellow, PickiiYellowLight)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            androidx.compose.material3.Text(
                text = stringResource(R.string.splash_title),
                color = Color.Black,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            androidx.compose.material3.Text(
                text = stringResource(R.string.splash_subtitle),
                color = PickiiTextGray,
                fontSize = 14.sp
            )
        }
    }
}

/** [SplashScreen]의 프리뷰. */
@Preview(showBackground = true)
@Composable
private fun SplashScreenPreview() {
    SplashScreenContent()
}
