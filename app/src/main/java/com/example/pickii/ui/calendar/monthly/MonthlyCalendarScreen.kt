package com.example.pickii.ui.calendar.monthly

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.pickii.ui.calendar.monthly.component.ScheduleSummaryCard
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val MonthlyCalendarBackgroundColor = Color(0xFFF9FCA8)
private val CalendarContainerColor = Color(0xFFFFFFFF)

private val SelectedDateTitleColor = Color(0xFF1B1B1B)

private val ActionButtonColor = Color(0xFF1B1B1B)
private val ActionButtonTextColor = Color(0xFFFFFFFF)

private val SelectedDateFormatter = DateTimeFormatter.ofPattern(
    "M월 d일",
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
    onAddScheduleClick: () -> Unit,
    onDailyCalendarClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedDateSchedules = uiState.schedules
        .filter { schedule ->
            schedule.includesDate(uiState.selectedDate)
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
                CalendarContainer(
                    content = {
                        CalendarMonthGrid(
                            displayedYearMonth = uiState.displayedYearMonth,
                            selectedDate = uiState.selectedDate,
                            schedules = uiState.schedules,
                            onDateClick = onDateClick,
                        )
                    },
                )
            }

            item {
                SelectedDateActionRow(
                    selectedDate = uiState.selectedDate,
                    onDetailClick = {
                        onDailyCalendarClick(uiState.selectedDate)
                    },
                    onAddScheduleClick = onAddScheduleClick,
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
                    ScheduleSummaryCard(
                        schedule = schedule,
                        isExpanded = uiState.expandedScheduleId == schedule.id,
                        onClick = {
                            onScheduleClick(schedule.id)
                        },
                    )
                }
            }
        }
    }
}

/**
 * 날짜 캘린더를 흰색 둥근 박스로 감싼다.
 */
@Composable
private fun CalendarContainer(
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = CalendarContainerColor,
                shape = RoundedCornerShape(30.dp),
            )
            .padding(
                horizontal = 18.dp,
                vertical = 22.dp,
            ),
    ) {
        content()
    }
}

/**
 * 선택된 날짜와 자세히, 일정 추가 버튼을 표시한다.
 */
@Composable
private fun SelectedDateActionRow(
    selectedDate: LocalDate,
    onDetailClick: () -> Unit,
    onAddScheduleClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = selectedDate.format(
                    SelectedDateFormatter,
                ),
                color = SelectedDateTitleColor,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            )

            CalendarActionButton(
                text = "자세히",
                onClick = onDetailClick,
            )
        }

        CalendarActionButton(
            text = "+ 추가",
            onClick = onAddScheduleClick,
        )
    }
}

/**
 * 캘린더 하단의 검은색 둥근 버튼을 표시한다.
 */
@Composable
private fun CalendarActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .background(
                color = ActionButtonColor,
                shape = RoundedCornerShape(50),
            )
            .clickable(
                onClick = onClick,
            )
            .padding(
                horizontal = 16.dp,
                vertical = 10.dp,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = ActionButtonTextColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
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