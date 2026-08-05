package com.example.pickii.ui.chat

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pickii.R

private val NotificationPanelBackgroundColor = Color.White
private val NotificationPanelScrimColor = Color.Black.copy(alpha = 0.35f)
private val NotificationPrimaryTextColor = Color(0xFF20283A)
private val NotificationSecondaryTextColor = Color(0xFF9CA3AF)
private val NotificationDisabledTextColor = Color(0xFFB6BAC4)
private val NotificationDividerColor = Color(0xFFF0F1F4)
private val NotificationCardColor = Color(0xFFFAFAFB)
private val NotificationCardBorderColor = Color(0xFFE4E6EB)
private val NotificationSwitchColor = Color(0xFFB7C85B)

/**
 * 알림을 모아서 받을 시간을 나타낸다.
 */
private enum class NotificationMuteTime(
    val displayName: String
) {
    NONE("없음"),
    ONE_HOUR("1시간"),
    TODAY("오늘 하루"),
    PERMANENT("영구 무음")
}

/**
 * 채팅방 알람 설정 패널을 표시한다.
 */
@Composable
fun ChatNotificationSettingPanel(
    initialNotificationEnabled: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    onBackClick: () -> Unit
) {
    var isNotificationEnabled by rememberSaveable {
        mutableStateOf(initialNotificationEnabled)
    }

    var isKeywordNotificationEnabled by rememberSaveable {
        mutableStateOf(false)
    }

    var selectedMuteTimeName by rememberSaveable {
        mutableStateOf(NotificationMuteTime.NONE.name)
    }

    val selectedMuteTime =
        NotificationMuteTime.valueOf(
            selectedMuteTimeName
        )

    BackHandler(
        enabled = true,
        onBack = onBackClick
    )

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(NotificationPanelScrimColor)
                    .clickable(onClick = onBackClick)
        )

        Column(
            modifier =
                Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.79f)
                    .align(Alignment.CenterEnd)
                    .background(NotificationPanelBackgroundColor)
        ) {
            NotificationSettingHeader(
                onBackClick = onBackClick
            )

            HorizontalDivider(
                color = NotificationDividerColor
            )

            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 20.dp,
                            vertical = 24.dp
                        ),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                NotificationToggleCard(
                    title = "알림 받기",
                    description = "채팅 및 공지 알림",
                    isChecked = isNotificationEnabled,
                    isEnabled = true,
                    onCheckedChange = { isChecked ->
                        isNotificationEnabled = isChecked
                        onEnabledChange(isChecked)

                        if (!isChecked) {
                            isKeywordNotificationEnabled = false
                        }
                    }
                )

                // 키워드 알림/알림 모음 시간은 8-8 API에 대응 필드가 없어 로컬 상태로만 둔다(의도적 — 이번 연동 범위 밖).
                NotificationToggleCard(
                    title = "키워드 알림",
                    description = "내 이름이 언급될 때만",
                    isChecked = isKeywordNotificationEnabled,
                    isEnabled = isNotificationEnabled,
                    onCheckedChange = { isChecked ->
                        isKeywordNotificationEnabled = isChecked
                    }
                )

                Text(
                    text = "알림 모음 시간",
                    modifier =
                        Modifier
                            .padding(
                                start = 4.dp,
                                top = 8.dp
                            ).alpha(
                                if (isNotificationEnabled) {
                                    1f
                                } else {
                                    0.38f
                                }
                            ),
                    color = NotificationSecondaryTextColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )

                NotificationMuteTime.entries.forEach { muteTime ->
                    NotificationMuteTimeItem(
                        title = muteTime.displayName,
                        isSelected = selectedMuteTime == muteTime,
                        isEnabled = isNotificationEnabled,
                        onClick = {
                            selectedMuteTimeName = muteTime.name
                        }
                    )
                }
            }
        }
    }
}

/**
 * 알람 설정 화면의 상단 헤더를 표시한다.
 */
@Composable
private fun NotificationSettingHeader(onBackClick: () -> Unit) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 20.dp,
                    vertical = 20.dp
                ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier =
                Modifier
                    .size(40.dp)
                    .clickable(onClick = onBackClick),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter =
                    painterResource(
                        id = R.drawable.ic_back
                    ),
                contentDescription = "뒤로가기",
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(
            modifier = Modifier.width(8.dp)
        )

        Text(
            text = "알람 설정",
            color = NotificationPrimaryTextColor,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * 알림 설정 스위치 카드를 표시한다.
 */
@Composable
private fun NotificationToggleCard(
    title: String,
    description: String,
    isChecked: Boolean,
    isEnabled: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val contentAlpha =
        if (isEnabled) {
            1f
        } else {
            0.38f
        }

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .alpha(contentAlpha)
                .background(
                    color = NotificationCardColor,
                    shape = RoundedCornerShape(18.dp)
                ).padding(
                    horizontal = 18.dp,
                    vertical = 22.dp
                ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                color =
                    if (isEnabled) {
                        NotificationPrimaryTextColor
                    } else {
                        NotificationDisabledTextColor
                    },
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.size(6.dp)
            )

            Text(
                text = description,
                color = NotificationSecondaryTextColor,
                fontSize = 13.sp
            )
        }

        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            enabled = isEnabled,
            colors =
                SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = NotificationSwitchColor,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color(0xFFD8DAE0),
                    uncheckedBorderColor = Color.Transparent,
                    disabledCheckedThumbColor = Color.White,
                    disabledCheckedTrackColor = Color(0xFFD8DAE0),
                    disabledUncheckedThumbColor = Color.White,
                    disabledUncheckedTrackColor = Color(0xFFD8DAE0),
                    disabledUncheckedBorderColor = Color.Transparent
                )
        )
    }
}

/**
 * 알림 모음 시간 선택 항목을 표시한다.
 */
@Composable
private fun NotificationMuteTimeItem(
    title: String,
    isSelected: Boolean,
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    val contentAlpha =
        if (isEnabled) {
            1f
        } else {
            0.38f
        }

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .alpha(contentAlpha)
                .background(
                    color = NotificationCardColor,
                    shape = RoundedCornerShape(16.dp)
                ).clickable(
                    enabled = isEnabled,
                    onClick = onClick
                ).padding(
                    horizontal = 18.dp,
                    vertical = 20.dp
                ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            color =
                if (isEnabled) {
                    NotificationSecondaryTextColor
                } else {
                    NotificationDisabledTextColor
                },
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )

        if (isSelected) {
            Text(
                text = "✓",
                color = Color(0xFF9CA3AF),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
