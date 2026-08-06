package com.example.pickii.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
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
import com.example.pickii.ui.theme.PickiiFieldBackground

/**
 * 화면 상단 공용 헤더: Pickii 로고 + 가운데 슬롯([centerContent]) + 오른쪽 슬롯([trailingContent]).
 *
 * @param centerContent 로고 오른쪽, [trailingContent] 왼쪽에 들어가는 내용(학교명 텍스트, 화면 제목 등). 생략하면 로고만 남고 [trailingContent]가 오른쪽 끝에 붙는다.
 * @param trailingContent 오른쪽 끝에 들어가는 내용. 생략하면 알림 개수 뱃지가 붙은 종 아이콘([NotificationBellButton])이 뜬다.
 */
@Composable
fun PickiiTopBar(
    notificationCount: Int,
    onNotificationClick: () -> Unit,
    modifier: Modifier = Modifier,
    centerContent: @Composable RowScope.() -> Unit = {},
    trailingContent: @Composable RowScope.() -> Unit = {
        NotificationBellButton(count = notificationCount, onClick = onNotificationClick)
    }
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier =
                Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "P", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(text = "Pickii", color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.width(10.dp))

        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            centerContent()
        }

        trailingContent()
    }
}

/** 알림 개수 뱃지가 붙은 종 모양 아이콘 버튼. */
@Composable
fun NotificationBellButton(
    count: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(36.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier =
                Modifier
                    .matchParentSize()
                    .clip(CircleShape)
                    .background(PickiiFieldBackground)
                    .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Notifications,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(18.dp)
            )
        }

        if (count > 0) {
            Box(
                modifier =
                    Modifier
                        .align(Alignment.TopEnd)
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(Color.Red),
                contentAlignment = Alignment.Center
            ) {
                Text(text = count.toString(), color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
