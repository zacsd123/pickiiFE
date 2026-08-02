package com.example.pickii.ui.calendar.monthly.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.pickii.ui.calendar.monthly.MonthlyScheduleUiModel
import com.example.pickii.ui.calendar.monthly.ScheduleColorType
import java.time.LocalDate

/**
 * 한 주에 포함된 여러 날짜 일정 막대를 표시한다.
 *
 * 일정이 월요일부터 금요일까지라면 해당 날짜 칸 아래를
 * 하나의 연결된 막대처럼 표시한다.
 */
@Composable
fun CalendarScheduleBar(
    weekDates: List<LocalDate?>,
    schedules: List<MonthlyScheduleUiModel>,
    modifier: Modifier = Modifier
) {
    val multiDaySchedules =
        schedules.filter { schedule ->
            schedule.isMultiDay &&
                weekDates.any { date ->
                    date != null && schedule.includesDate(date)
                }
        }

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(SCHEDULE_BAR_AREA_HEIGHT)
    ) {
        multiDaySchedules
            .take(MAX_VISIBLE_SCHEDULE_BAR_COUNT)
            .forEachIndexed { index, schedule ->
                ScheduleBarRow(
                    weekDates = weekDates,
                    schedule = schedule,
                    verticalIndex = index
                )
            }
    }
}

/**
 * 일정 하나를 7개의 날짜 칸에 맞춰 막대로 표시한다.
 */
@Composable
private fun ScheduleBarRow(
    weekDates: List<LocalDate?>,
    schedule: MonthlyScheduleUiModel,
    verticalIndex: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .height(SCHEDULE_BAR_HEIGHT)
    ) {
        weekDates.forEachIndexed { index, date ->
            val isIncluded =
                date != null && schedule.includesDate(date)

            val previousDate = weekDates.getOrNull(index - 1)
            val nextDate = weekDates.getOrNull(index + 1)

            val isPreviousIncluded =
                previousDate != null &&
                    schedule.includesDate(previousDate)

            val isNextIncluded =
                nextDate != null &&
                    schedule.includesDate(nextDate)

            Box(
                modifier =
                    Modifier
                        .weight(1f)
                        .height(SCHEDULE_BAR_HEIGHT)
            ) {
                if (isIncluded) {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(SCHEDULE_BAR_HEIGHT)
                                .background(
                                    color = schedule.categoryColor.toComposeColor(),
                                    shape =
                                        RoundedCornerShape(
                                            topStart =
                                                if (isPreviousIncluded) {
                                                    0.dp
                                                } else {
                                                    SCHEDULE_BAR_CORNER_RADIUS
                                                },
                                            bottomStart =
                                                if (isPreviousIncluded) {
                                                    0.dp
                                                } else {
                                                    SCHEDULE_BAR_CORNER_RADIUS
                                                },
                                            topEnd =
                                                if (isNextIncluded) {
                                                    0.dp
                                                } else {
                                                    SCHEDULE_BAR_CORNER_RADIUS
                                                },
                                            bottomEnd =
                                                if (isNextIncluded) {
                                                    0.dp
                                                } else {
                                                    SCHEDULE_BAR_CORNER_RADIUS
                                                }
                                        )
                                )
                    )
                }
            }
        }
    }
}

/**
 * 일정 색상 종류를 Compose 색상으로 변환한다.
 */
private fun ScheduleColorType.toComposeColor(): Color =
    when (this) {
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

private val SCHEDULE_BAR_AREA_HEIGHT = 14.dp
private val SCHEDULE_BAR_HEIGHT = 5.dp
private val SCHEDULE_BAR_CORNER_RADIUS = 3.dp

private const val MAX_VISIBLE_SCHEDULE_BAR_COUNT = 2
