package com.example.pickii.ui.notification

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val SettingBackgroundColor = Color(0xFFF8F8F5)
private val SettingPointColor = Color(0xFF7A5AF8)
private val SettingTextColor = Color(0xFF1D1D1B)
private val SettingDescriptionColor = Color(0xFF85857E)

@Composable
fun NotificationSettingScreen(
    settings: NotificationSettingUiState,
    onBackClick: () -> Unit,
    onChatNotificationChanged: (Boolean) -> Unit,
    onApplicantNotificationChanged: (Boolean) -> Unit,
    onCommentNotificationChanged: (Boolean) -> Unit,
    onScheduleNotificationChanged: (Boolean) -> Unit,
    onMatchingNotificationChanged: (Boolean) -> Unit,
    onMarketingNotificationChanged: (Boolean) -> Unit
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(SettingBackgroundColor)
    ) {
        NotificationSettingHeader(
            onBackClick = onBackClick
        )

        Text(
            text = "서비스 알림",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = SettingDescriptionColor,
            modifier =
                Modifier.padding(
                    start = 20.dp,
                    top = 24.dp,
                    bottom = 10.dp
                )
        )

        NotificationSettingItem(
            title = "채팅 알림",
            description = "새로운 채팅 메시지를 알려드려요.",
            checked = settings.chatNotificationEnabled,
            onCheckedChange = onChatNotificationChanged
        )

        NotificationSettingItem(
            title = "새 지원자 알림",
            description = "내 공고에 새로운 지원자가 생기면 알려드려요.",
            checked = settings.applicantNotificationEnabled,
            onCheckedChange = onApplicantNotificationChanged
        )

        NotificationSettingItem(
            title = "댓글 알림",
            description = "공고에 댓글과 답글이 작성되면 알려드려요.",
            checked = settings.commentNotificationEnabled,
            onCheckedChange = onCommentNotificationChanged
        )

        NotificationSettingItem(
            title = "팀 일정 알림",
            description = "등록된 팀 일정과 시작 시간을 알려드려요.",
            checked = settings.scheduleNotificationEnabled,
            onCheckedChange = onScheduleNotificationChanged
        )

        NotificationSettingItem(
            title = "매칭 결과 알림",
            description = "지원 수락이나 거절 결과를 알려드려요.",
            checked = settings.matchingNotificationEnabled,
            onCheckedChange = onMatchingNotificationChanged
        )

        Text(
            text = "마케팅 정보",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = SettingDescriptionColor,
            modifier =
                Modifier.padding(
                    start = 20.dp,
                    top = 28.dp,
                    bottom = 10.dp
                )
        )

        NotificationSettingItem(
            title = "마케팅 알림",
            description = "Pickii의 새로운 소식과 이벤트를 받아보세요.",
            checked = settings.marketingNotificationEnabled,
            onCheckedChange = onMarketingNotificationChanged
        )
    }
}

@Composable
private fun NotificationSettingHeader(onBackClick: () -> Unit) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(
                    horizontal = 20.dp,
                    vertical = 18.dp
                ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = null,
            tint = Color.Black,
            modifier = Modifier.clickable(onClick = onBackClick)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = "알림 설정",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = SettingTextColor
        )
    }
}

@Composable
private fun NotificationSettingItem(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(
                    horizontal = 20.dp,
                    vertical = 17.dp
                ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = SettingTextColor
            )

            Text(
                text = description,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = SettingDescriptionColor,
                modifier = Modifier.padding(top = 5.dp)
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors =
                SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = SettingPointColor,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color(0xFFD2D2CC),
                    uncheckedBorderColor = Color.Transparent
                )
        )
    }

    HorizontalDivider(
        modifier = Modifier.padding(start = 20.dp),
        thickness = 1.dp,
        color = Color(0xFFEDEDE8)
    )
}
