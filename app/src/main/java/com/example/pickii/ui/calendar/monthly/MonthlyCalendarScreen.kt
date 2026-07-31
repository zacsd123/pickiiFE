package com.example.pickii.ui.calendar.monthly

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pickii.ui.calendar.monthly.component.CalendarMonthGrid
import com.example.pickii.ui.calendar.monthly.component.CalendarMonthHeader
import com.example.pickii.ui.calendar.monthly.component.ScheduleDetailCard
import com.example.pickii.ui.calendar.monthly.component.ScheduleSummaryCard
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val MonthlyCalendarBackgroundColor = Color(0xFFF8FFB8)
private val SelectedDateTitleColor = Color(0xFF1B1B1B)
private val AddScheduleButtonColor = Color(0xFF1B1B1B)
private val AddScheduleButtonTextColor = Color(0xFFFFFFFF)

private val SelectedDateFormatter = DateTimeFormatter.ofPattern(
    "M월 d일 E요일",
    Locale.KOREAN,
)

/**
 * 월간 캘린더 화면을 표시한다.
 */
@Composable
fun MonthlyCalendarScreen(
    uiState: MonthlyCalendarUiState,
    onPreviousMonthClick: () -> Unit,
    onNextMonthClick: () -> Unit,
    onDateClick: (LocalDate) -> Unit,
    onScheduleClick: (Long) -> Unit,
    onScheduleDetailCloseClick: () -> Unit,
    onScheduleEditClick: (Long) -> Unit,
    onAddScheduleClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedDateSchedules = uiState.schedules
        .filter { schedule ->
            schedule.includesDate(uiState.selectedDate)
        }

    val expandedSchedule = uiState.expandedScheduleId?.let { scheduleId ->
        uiState.schedules.find { schedule ->
            schedule.id == scheduleId
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MonthlyCalendarBackgroundColor),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = 24.dp,
                bottom = 120.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                CalendarMonthHeader(
                    displayedYearMonth = uiState.displayedYearMonth,
                    onPreviousMonthClick = onPreviousMonthClick,
                    onNextMonthClick = onNextMonthClick,
                )
            }

            item {
                Spacer(
                    modifier = Modifier.height(4.dp),
                )
            }

            item {
                CalendarMonthGrid(
                    displayedYearMonth = uiState.displayedYearMonth,
                    selectedDate = uiState.selectedDate,
                    schedules = uiState.schedules,
                    onDateClick = onDateClick,
                )
            }

            item {
                Spacer(
                    modifier = Modifier.height(8.dp),
                )
            }

            item {
                Text(
                    text = uiState.selectedDate.format(
                        SelectedDateFormatter,
                    ),
                    color = SelectedDateTitleColor,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            if (selectedDateSchedules.isEmpty()) {
                item {
                    EmptyScheduleContent()
                }
            } else {
                items(
                    items = selectedDateSchedules,
                    key = { schedule ->
                        schedule.id
                    },
                ) { schedule ->
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        ScheduleSummaryCard(
                            schedule = schedule,
                            onClick = {
                                onScheduleClick(schedule.id)
                            },
                        )

                        if (expandedSchedule?.id == schedule.id) {
                            ScheduleDetailCard(
                                schedule = schedule,
                                onCloseClick = onScheduleDetailCloseClick,
                                onEditClick = {
                                    onScheduleEditClick(schedule.id)
                                },
                            )
                        }
                    }
                }
            }
        }

        AddScheduleButton(
            onClick = onAddScheduleClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(
                    end = 24.dp,
                    bottom = 28.dp,
                ),
        )
    }
}

/**
 * 선택한 날짜에 일정이 없을 때 안내 문구를 표시한다.
 */
@Composable
private fun EmptyScheduleContent(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 28.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "등록된 일정이 없어요.",
            color = Color(0xFF77776E),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

/**
 * 일정 추가 버튼을 표시한다.
 *
 * 현재 + 문자는 이후 SVG 리소스로 교체할 임시 표시다.
 */
@Composable
private fun AddScheduleButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    androidx.compose.foundation.layout.Row(
        modifier = modifier
            .background(
                color = AddScheduleButtonColor,
                shape = CircleShape,
            )
            .clickable(
                onClick = onClick,
            )
            .padding(
                horizontal = 20.dp,
                vertical = 14.dp,
            ),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "+",
            color = AddScheduleButtonTextColor,
            fontSize = 22.sp,
            fontWeight = FontWeight.Medium,
        )

        Text(
            text = "일정 추가",
            color = AddScheduleButtonTextColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}