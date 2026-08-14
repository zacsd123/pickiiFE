package com.example.pickii.ui.calendar.monthly.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pickii.ui.calendar.monthly.MonthlyScheduleUiModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

private val WeekdayTextColor = Color(0xFFA8A8A8)
private val WeekendTextColor = Color(0xFFB99CFF)

/**
 * 월간 달력의 요일 헤더와 날짜 영역을 표시한다.
 */
@Composable
fun CalendarMonthGrid(
    displayedYearMonth: YearMonth,
    selectedDate: LocalDate,
    schedules: List<MonthlyScheduleUiModel>,
    onDateClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val calendarDates =
        createCalendarDates(
            yearMonth = displayedYearMonth
        )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        CalendarWeekdayHeader()

        calendarDates
            .chunked(DAYS_PER_WEEK)
            .forEach { weekDates ->
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(62.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    weekDates.forEach { date ->
                        val scheduleColors =
                            schedules
                                .filter { schedule ->
                                    date != null &&
                                        schedule.includesDate(date) &&
                                        !schedule.isMultiDay
                                }.map { schedule ->
                                    schedule.categoryColor
                                }

                        CalendarDayCell(
                            date = date,
                            isSelected = date == selectedDate,
                            scheduleColors = scheduleColors,
                            onDateClick = onDateClick
                        )
                    }
                }

                CalendarScheduleBar(
                    weekDates = weekDates,
                    schedules = schedules
                )
            }
    }
}

/**
 * 일요일부터 토요일까지 요일 이름을 표시한다.
 */
@Composable
private fun CalendarWeekdayHeader(modifier: Modifier = Modifier) {
    val weekdayLabels =
        listOf(
            "S",
            "M",
            "T",
            "W",
            "T",
            "F",
            "S"
        )

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        weekdayLabels.forEachIndexed { index, label ->
            val isWeekend = index in WEEKEND_INDICES

            Text(
                text = label,
                color =
                    if (isWeekend) {
                        WeekendTextColor
                    } else {
                        WeekdayTextColor
                    },
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(42.dp)
            )
        }
    }
}

/**
 * 전달받은 월을 7열 달력에 맞게 날짜 목록으로 변환한다.
 *
 * 이번 달에 포함되지 않는 앞뒤 칸은 null로 채운다.
 */
private fun createCalendarDates(yearMonth: YearMonth): List<LocalDate?> {
    val firstDate = yearMonth.atDay(1)
    val lastDate = yearMonth.atEndOfMonth()

    val leadingEmptyCount =
        calculateSundayBasedIndex(firstDate.dayOfWeek)

    val dates =
        buildList<LocalDate?> {
            repeat(leadingEmptyCount) {
                add(null)
            }

            for (dayOfMonth in 1..lastDate.dayOfMonth) {
                add(yearMonth.atDay(dayOfMonth))
            }
        }

    val trailingEmptyCount =
        (DAYS_PER_WEEK - dates.size % DAYS_PER_WEEK) % DAYS_PER_WEEK

    return buildList {
        addAll(dates)

        repeat(trailingEmptyCount) {
            add(null)
        }
    }
}

/**
 * DayOfWeek 값을 일요일 시작 기준 0부터 6까지로 변환한다.
 */
private fun calculateSundayBasedIndex(dayOfWeek: DayOfWeek): Int = dayOfWeek.value % 7

private const val DAYS_PER_WEEK = 7
private val WEEKEND_INDICES = setOf(0, 6)
