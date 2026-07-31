package com.example.pickii.ui.calendar.monthly.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pickii.ui.calendar.monthly.MonthlyScheduleUiModel
import com.example.pickii.ui.calendar.monthly.ScheduleColorType
import java.time.format.DateTimeFormatter
import java.util.Locale

private val DetailCardBackgroundColor = Color(0xFF1D1D1B)
private val DetailTitleColor = Color(0xFFFFFFFF)
private val DetailDescriptionColor = Color(0xFFC8C8C1)
private val DetailButtonBackgroundColor = Color(0xFFF2F2EC)
private val DetailButtonTextColor = Color(0xFF1D1D1B)

private val DetailDateFormatter = DateTimeFormatter.ofPattern(
    "M월 d일 E요일",
    Locale.KOREAN,
)

private val DetailTimeFormatter = DateTimeFormatter.ofPattern(
    "a h:mm",
    Locale.KOREAN,
)

/**
 * 선택한 일정의 자세한 정보를 표시한다.
 */
@Composable
fun ScheduleDetailCard(
    schedule: MonthlyScheduleUiModel,
    onCloseClick: () -> Unit,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = DetailCardBackgroundColor,
                shape = RoundedCornerShape(24.dp),
            )
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        ScheduleDetailHeader(
            schedule = schedule,
            onCloseClick = onCloseClick,
        )

        ScheduleDetailInformation(
            emoji = "📅",
            label = createDateText(schedule),
        )

        ScheduleDetailInformation(
            emoji = "⏰",
            label = createTimeText(schedule),
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = DetailButtonBackgroundColor,
                    shape = RoundedCornerShape(16.dp),
                )
                .clickable(onClick = onEditClick)
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "일정 수정하기",
                color = DetailButtonTextColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

/**
 * 일정 제목과 닫기 버튼을 표시한다.
 */
@Composable
private fun ScheduleDetailHeader(
    schedule: MonthlyScheduleUiModel,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .padding(top = 5.dp)
                .size(12.dp)
                .background(
                    color = schedule.categoryColor.toComposeColor(),
                    shape = CircleShape,
                ),
        )

        Spacer(
            modifier = Modifier.width(12.dp),
        )

        Text(
            text = schedule.title,
            modifier = Modifier.weight(1f),
            color = DetailTitleColor,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 26.sp,
        )

        Text(
            text = "✕",
            modifier = Modifier
                .clickable(onClick = onCloseClick)
                .padding(
                    start = 12.dp,
                    top = 2.dp,
                    bottom = 8.dp,
                ),
            color = DetailDescriptionColor,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

/**
 * 상세 정보 한 줄을 표시한다.
 *
 * 현재 이모티콘은 이후 SVG 리소스로 교체할 임시 표시다.
 */
@Composable
private fun ScheduleDetailInformation(
    emoji: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = emoji,
            fontSize = 18.sp,
        )

        Spacer(
            modifier = Modifier.width(12.dp),
        )

        Text(
            text = label,
            color = DetailDescriptionColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = 20.sp,
        )
    }
}

/**
 * 일정 시작일과 종료일을 화면에 표시할 문자열로 만든다.
 */
private fun createDateText(
    schedule: MonthlyScheduleUiModel,
): String {
    val startDateText = schedule.startDate.format(
        DetailDateFormatter,
    )

    if (schedule.startDate == schedule.endDate) {
        return startDateText
    }

    val endDateText = schedule.endDate.format(
        DetailDateFormatter,
    )

    return "$startDateText - $endDateText"
}

/**
 * 일정 시간을 화면에 표시할 문자열로 만든다.
 */
private fun createTimeText(
    schedule: MonthlyScheduleUiModel,
): String {
    if (schedule.isAllDay) {
        return "하루 종일"
    }

    val startTimeText = schedule.startTime?.format(
        DetailTimeFormatter,
    )

    val endTimeText = schedule.endTime?.format(
        DetailTimeFormatter,
    )

    return when {
        startTimeText != null && endTimeText != null -> {
            "$startTimeText - $endTimeText"
        }

        startTimeText != null -> startTimeText

        else -> "시간 미정"
    }
}

/**
 * 일정 카테고리 색상을 Compose 색상으로 변환한다.
 */
private fun ScheduleColorType.toComposeColor(): Color {
    return when (this) {
        ScheduleColorType.RED -> Color(0xFFE86F73)
        ScheduleColorType.ORANGE -> Color(0xFFED9A53)
        ScheduleColorType.YELLOW -> Color(0xFFF1D354)
        ScheduleColorType.GREEN -> Color(0xFF84C976)
        ScheduleColorType.BLUE -> Color(0xFF6D9EEB)
        ScheduleColorType.PURPLE -> Color(0xFFAC8AFA)
        ScheduleColorType.PINK -> Color(0xFFE38AB0)
        ScheduleColorType.GRAY -> Color(0xFFBDBDBD)
        ScheduleColorType.BLACK -> Color(0xFF1C1C1C)
    }
}