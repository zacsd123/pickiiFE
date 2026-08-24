package com.example.pickii.ui.mypage.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pickii.shared.generated.resources.Res
import com.example.pickii.shared.generated.resources.common_button_cancel
import com.example.pickii.shared.generated.resources.mypage_logout_body
import com.example.pickii.shared.generated.resources.mypage_logout_confirm
import com.example.pickii.shared.generated.resources.mypage_logout_title
import com.example.pickii.shared.generated.resources.mypage_settings_logout
import com.example.pickii.ui.common.BackHeader
import com.example.pickii.ui.theme.PickiiFieldBackground
import com.example.pickii.ui.theme.PickiiPaletteBaseWhite
import com.example.pickii.ui.theme.PickiiTextGray
import com.example.pickii.ui.theme.PickiiYellowLight
import org.jetbrains.compose.resources.stringResource
import org.koin.androidx.compose.koinViewModel

/** 로그아웃 확인 화면(설정 화면에서 진입). */
@Composable
fun LogoutScreen(
    onBackClick: () -> Unit,
    onLoggedOut: () -> Unit,
    viewModel: LogoutViewModel = koinViewModel()
) {
    LogoutScreenContent(
        onBackClick = onBackClick,
        onConfirmLogout = { viewModel.onConfirmLogout(onLoggedOut) }
    )
}

@Composable
private fun LogoutScreenContent(
    onBackClick: () -> Unit,
    onConfirmLogout: () -> Unit
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(PickiiPaletteBaseWhite)
                .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        BackHeader(title = stringResource(Res.string.mypage_settings_logout), onBackClick = onBackClick)

        Column(
            modifier = Modifier.fillMaxSize().weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.size(72.dp).clip(CircleShape).background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = stringResource(Res.string.mypage_logout_title),
                color = Color.Black,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(Res.string.mypage_logout_body),
                color = PickiiTextGray,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(28.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier =
                        Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(PickiiFieldBackground)
                            .clickable(onClick = onBackClick)
                            .padding(horizontal = 32.dp, vertical = 14.dp)
                ) {
                    Text(text = stringResource(Res.string.common_button_cancel), color = Color.Black, fontSize = 14.sp)
                }
                Box(
                    modifier =
                        Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.Black)
                            .clickable(onClick = onConfirmLogout)
                            .padding(horizontal = 32.dp, vertical = 14.dp)
                ) {
                    Text(
                        text = stringResource(Res.string.mypage_logout_confirm),
                        color = PickiiYellowLight,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
