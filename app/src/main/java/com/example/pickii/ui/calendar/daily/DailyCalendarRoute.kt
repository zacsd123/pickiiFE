package com.example.pickii.ui.calendar.daily

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.LocalDate

/**
 * 일일 캘린더 화면과 ViewModel을 연결한다.
 */
@Composable
fun DailyCalendarRoute(
    selectedDate: LocalDate,
    onMenuClick: () -> Unit,
    onAddScheduleClick: () -> Unit,
    onScheduleClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DailyCalendarViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(selectedDate) {
        viewModel.selectDate(selectedDate)
    }

    DailyCalendarScreen(
        uiState = uiState,
        onMenuClick = onMenuClick,
        onPreviousDateClick = viewModel::moveToPreviousDate,
        onNextDateClick = viewModel::moveToNextDate,
        onAddScheduleClick = {
            viewModel.addSchedule()
            onAddScheduleClick()
        },
        onScheduleClick = { scheduleId ->
            viewModel.selectSchedule(scheduleId)
            onScheduleClick(scheduleId)
        },
        modifier = modifier,
    )
}