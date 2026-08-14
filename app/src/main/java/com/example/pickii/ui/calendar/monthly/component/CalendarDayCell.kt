package com.example.pickii.ui.calendar.monthly.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pickii.domain.model.ScheduleColorType
import com.example.pickii.ui.common.toComposeColor
import java.time.DayOfWeek
import java.time.LocalDate

private val DefaultDateTextColor = Color(0xFF1C1C1C)
private val WeekendDateTextColor = Color(0xFFB99CFF)
private val SelectedDateBackgroundColor = Color(0xFFE5F63D)
private val EmptyDateColor = Color.Transparent

/**
 * 월간 달력 안의 날짜 한 칸을 표시한다.
 */
@Composable
fun CalendarDayCell(
    date: LocalDate?,
    isSelected: Boolean,
    scheduleColors: List<ScheduleColorType>,
    onDateClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    if (date == null) {
        Box(
            modifier =
                modifier.size(
                    width = 42.dp,
                    height = 58.dp
                )
        )

        return
    }

    val dateTextColor =
        if (
            date.dayOfWeek == DayOfWeek.SATURDAY ||
            date.dayOfWeek == DayOfWeek.SUNDAY
        ) {
            WeekendDateTextColor
        } else {
            DefaultDateTextColor
        }

    Column(
        modifier =
            modifier
                .size(
                    width = 42.dp,
                    height = 58.dp
                ).clickable {
                    onDateClick(date)
                },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Box(
            modifier =
                Modifier
                    .size(42.dp)
                    .background(
                        color =
                            if (isSelected) {
                                SelectedDateBackgroundColor
                            } else {
                                EmptyDateColor
                            },
                        shape = CircleShape
                    ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                color = dateTextColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
        }

        ScheduleColorDots(
            scheduleColors = scheduleColors
        )
    }
}

/**
 * 날짜 아래에 일정 색상 점을 표시한다.
 *
 * 한 날짜에 일정이 여러 개 있어도 최대 3개까지만 표시한다.
 */
@Composable
private fun ScheduleColorDots(
    scheduleColors: List<ScheduleColorType>,
    modifier: Modifier = Modifier
) {
    val visibleColors =
        scheduleColors
            .distinct()
            .take(MAX_VISIBLE_DOT_COUNT)

    androidx.compose.foundation.layout.Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        visibleColors.forEach { colorType ->
            Box(
                modifier =
                    Modifier
                        .size(5.dp)
                        .background(
                            color = colorType.toComposeColor(),
                            shape = CircleShape
                        )
            )
        }
    }
}

/**
 * 일정 색상 종류를 Compose 색상으로 변환한다.
 */

private const val MAX_VISIBLE_DOT_COUNT = 3
