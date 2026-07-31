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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pickii.ui.calendar.monthly.MonthlyScheduleUiModel
import com.example.pickii.ui.calendar.monthly.ScheduleColorType
import java.time.format.DateTimeFormatter
import java.util.Locale

// 요약 카드를 눌렀을 때 ScheduleDetailCard가 펼쳐짐
// 카테고리 색상 원
// 일정 제목
// 시작·종료 시간
// 하루 종일 표시
// 오른쪽 상세 이동 화살표
// 카드 전체 클릭 처리

private val SummaryCardBackgroundColor = Color(0xFFF4F4EE)
private val SummaryTitleColor = Color(0xFF1B1B1B)
private val SummaryDescriptionColor = Color(0xFF77776E)
private val SummaryArrowBackgroundColor = Color(0xFFFFFFFF)

private val ScheduleTimeFormatter = DateTimeFormatter.ofPattern(
    "a h:mm",
    Locale.KOREAN,
)

/**
 * 선택한 날짜에 포함된 일정의 요약 카드를 표시한다.
 */
@Composable
fun ScheduleSummaryCard(
    schedule: MonthlyScheduleUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = SummaryCardBackgroundColor,
                shape = RoundedCornerShape(20.dp),
            )
            .clickable(onClick = onClick)
            .padding(
                horizontal = 18.dp,
                vertical = 16.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(
                    color = schedule.categoryColor.toComposeColor(),
                    shape = CircleShape,
                ),
        )

        Spacer(
            modifier = Modifier.width(14.dp),
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Text(
                text = schedule.title,
                color = SummaryTitleColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Text(
                text = createScheduleSummary(schedule),
                color = SummaryDescriptionColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Spacer(
            modifier = Modifier.width(12.dp),
        )

        Box(
            modifier = Modifier
                .size(34.dp)
                .background(
                    color = SummaryArrowBackgroundColor,
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "›",
                color = SummaryTitleColor,
                fontSize = 26.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 26.sp,
            )
        }
    }
}

/**
 * 일정의 시간 또는 기간을 요약 문자열로 만든다.
 */
private fun createScheduleSummary(
    schedule: MonthlyScheduleUiModel,
): String {
    if (schedule.isAllDay) {
        return "하루 종일"
    }

    val startTimeText = schedule.startTime?.format(
        ScheduleTimeFormatter,
    )

    val endTimeText = schedule.endTime?.format(
        ScheduleTimeFormatter,
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