package com.example.pickii.ui.chat

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pickii.R

private val LeavePanelBackgroundColor = Color.White
private val LeavePanelScrimColor = Color.Black.copy(alpha = 0.35f)
private val LeavePrimaryTextColor = Color(0xFF20283A)
private val LeaveSecondaryTextColor = Color(0xFF7E8594)
private val LeaveDividerColor = Color(0xFFF0F1F4)
private val LeaveWarningBackgroundColor = Color(0xFFF9E1E1)
private val LeaveButtonColor = Color(0xFFD23B32)
private val LeaveCancelButtonColor = Color(0xFFF3F4F6)

/**
 * 채팅방 나가기 확인 패널을 표시한다.
 */
@Composable
fun ChatRoomLeavePanel(
    isCurrentUserLeader: Boolean,
    onBackClick: () -> Unit,
    onLeaveClick: () -> Unit
) {
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
                    .background(LeavePanelScrimColor)
                    .clickable(onClick = onBackClick)
        )

        Column(
            modifier =
                Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.79f)
                    .align(Alignment.CenterEnd)
                    .background(LeavePanelBackgroundColor)
        ) {
            ChatRoomLeaveHeader(
                onBackClick = onBackClick
            )

            HorizontalDivider(
                color = LeaveDividerColor
            )

            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(
                    modifier = Modifier.weight(1f)
                )

                Image(
                    painter =
                        painterResource(
                            id = R.drawable.ic_warning
                        ),
                    contentDescription = null,
                    modifier = Modifier.size(100.dp)
                )

                Spacer(
                    modifier = Modifier.height(26.dp)
                )

                Text(
                    text = "채팅방을 나가시겠어요?",
                    color = LeavePrimaryTextColor,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                Text(
                    text =
                        if (isCurrentUserLeader) {
                            "팀장이 나가면 팀원 중 1명이 자동으로 팀장이 됩니다."
                        } else {
                            "채팅방을 나가면 더 이상 메시지를 확인할 수 없습니다."
                        },
                    color = LeaveSecondaryTextColor,
                    fontSize = 14.sp,
                    lineHeight = 21.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(
                    modifier = Modifier.weight(1f)
                )

                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(bottom = 18.dp)
                ) {
                    Box(
                        modifier =
                            Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(18.dp))
                                .background(LeaveCancelButtonColor)
                                .clickable(onClick = onBackClick)
                                .padding(vertical = 18.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "취소",
                            color = LeaveSecondaryTextColor,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(
                        modifier = Modifier.width(14.dp)
                    )

                    Box(
                        modifier =
                            Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(18.dp))
                                .background(LeaveButtonColor)
                                .clickable(onClick = onLeaveClick)
                                .padding(vertical = 18.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "나가기",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * 채팅방 나가기 화면의 헤더를 표시한다.
 */
@Composable
private fun ChatRoomLeaveHeader(onBackClick: () -> Unit) {
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
            text = "채팅방 나가기",
            color = LeavePrimaryTextColor,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
