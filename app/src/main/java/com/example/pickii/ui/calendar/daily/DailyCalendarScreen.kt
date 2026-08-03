package com.example.pickii.ui.calendar.daily

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.pickii.ui.calendar.daily.component.DailyCalendarHeader
import com.example.pickii.ui.calendar.daily.component.DailyScheduleBlock
import com.example.pickii.ui.calendar.daily.component.DailyTimeLabel
import com.example.pickii.ui.calendar.daily.component.DailyTimeLine
import java.time.LocalDate
import java.time.LocalTime

private val ScreenBackgroundColor = Color(0xFFFFFFFF)

private val HourRowHeight = 72.dp
private val TimeLabelWidth = 56.dp
private val TimeLineStartSpacing = 10.dp
private val ScheduleHorizontalSpacing = 8.dp

/**
 * 일일 캘린더 화면이다.
 */
@Composable
fun DailyCalendarScreen(
    uiState: DailyCalendarUiState,
    onMenuClick: () -> Unit,
    onPreviousDateClick: () -> Unit,
    onNextDateClick: () -> Unit,
    onAddScheduleClick: () -> Unit,
    onScheduleClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(ScreenBackgroundColor)
    ) {
        DailyCalendarHeader(
            selectedDate = uiState.selectedDate,
            onMenuClick = onMenuClick,
            onPreviousDateClick = onPreviousDateClick,
            onNextDateClick = onNextDateClick,
            onAddScheduleClick = onAddScheduleClick,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 16.dp,
                        vertical = 12.dp
                    )
        )

        DailyCalendarTimeTable(
            selectedDate = uiState.selectedDate,
            schedules = uiState.schedules,
            onScheduleClick = onScheduleClick,
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(
                        start = 16.dp,
                        end = 16.dp
                    )
        )
    }
}

/**
 * 시간 라인과 일정 블록이 표시되는 영역이다.
 */
@Composable
private fun DailyCalendarTimeTable(
    selectedDate: LocalDate,
    schedules: List<DailyScheduleUiModel>,
    onScheduleClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    val selectedDateSchedules =
        schedules.filter { schedule ->
            schedule.includesDate(selectedDate)
        }

    Box(
        modifier =
            modifier
                .verticalScroll(scrollState)
    ) {
        TimeGrid()

        ScheduleLayer(
            schedules = selectedDateSchedules,
            onScheduleClick = onScheduleClick
        )
    }
}

/**
 * 00시부터 24시까지의 시간 구분선을 표시한다.
 */
@Composable
private fun TimeGrid(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        for (hour in 0..23) {
            HourRow(
                hour = hour
            )
        }

        LastTimeLine()
    }
}

/**
 * 시간 한 줄을 표시한다.
 */
@Composable
private fun HourRow(
    hour: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .height(HourRowHeight),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier.width(TimeLabelWidth),
            contentAlignment = Alignment.TopStart
        ) {
            DailyTimeLabel(
                hour = hour
            )
        }

        Spacer(
            modifier = Modifier.width(TimeLineStartSpacing)
        )

        DailyTimeLine(
            modifier =
                Modifier
                    .weight(1f)
                    .padding(top = 9.dp)
        )
    }
}

/**
 * 시간표 가장 아래쪽의 마지막 구분선을 표시한다.
 */
@Composable
private fun LastTimeLine(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(
            modifier =
                Modifier.width(
                    TimeLabelWidth + TimeLineStartSpacing
                )
        )

        DailyTimeLine(
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * 일정들을 시간에 맞는 위치에 배치한다.
 */
@Composable
private fun ScheduleLayer(
    schedules: List<DailyScheduleUiModel>,
    onScheduleClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(HourRowHeight * 24)
                .padding(
                    start = TimeLabelWidth + TimeLineStartSpacing
                )
    ) {
        schedules.forEach { schedule ->
            val topOffset =
                calculateScheduleTopOffset(
                    startTime = schedule.startTime
                )

            val blockHeight =
                calculateScheduleHeight(
                    startTime = schedule.startTime,
                    endTime = schedule.endTime
                )

            DailyScheduleBlock(
                schedule = schedule,
                onClick = onScheduleClick,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            start = ScheduleHorizontalSpacing,
                            end = ScheduleHorizontalSpacing
                        ).offset(y = topOffset)
                        .height(blockHeight)
                        .zIndex(1f)
            )
        }
    }
}

/**
 * 일정 시작 시간을 기준으로 위쪽 위치를 계산한다.
 */
private fun calculateScheduleTopOffset(startTime: LocalTime): Dp {
    val startMinutes = startTime.hour * 60 + startTime.minute
    val minuteHeight = HourRowHeight / 60f

    return minuteHeight * startMinutes
}

/**
 * 일정 시작 시간과 종료 시간을 기준으로 블록 높이를 계산한다.
 */
private fun calculateScheduleHeight(
    startTime: LocalTime,
    endTime: LocalTime
): Dp {
    val startMinutes = startTime.hour * 60 + startTime.minute
    val endMinutes = endTime.hour * 60 + endTime.minute

    val durationMinutes =
        (endMinutes - startMinutes)
            .coerceAtLeast(30)

    val minuteHeight = HourRowHeight / 60f

    return minuteHeight * durationMinutes
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFFFFFFFF
)
@Composable
private fun DailyCalendarScreenPreview() {
    DailyCalendarScreen(
        uiState =
            DailyCalendarUiState(
                selectedDate = LocalDate.of(2026, 7, 4),
                schedules =
                    listOf(
                        DailyScheduleUiModel(
                            id = 1L,
                            title = "일정1",
                            date = LocalDate.of(2026, 7, 4),
                            startTime = LocalTime.of(1, 0),
                            endTime = LocalTime.of(2, 0),
                            colorType = DailyScheduleColorType.RED
                        ),
                        DailyScheduleUiModel(
                            id = 2L,
                            title = "일정2",
                            date = LocalDate.of(2026, 7, 4),
                            startTime = LocalTime.of(6, 0),
                            endTime = LocalTime.of(8, 0),
                            colorType = DailyScheduleColorType.GREEN
                        ),
                        DailyScheduleUiModel(
                            id = 3L,
                            title = "팀 회의",
                            date = LocalDate.of(2026, 7, 4),
                            startTime = LocalTime.of(13, 30),
                            endTime = LocalTime.of(15, 0),
                            colorType = DailyScheduleColorType.BLUE
                        )
                    )
            ),
        onMenuClick = {},
        onPreviousDateClick = {},
        onNextDateClick = {},
        onAddScheduleClick = {},
        onScheduleClick = {}
    )
}
