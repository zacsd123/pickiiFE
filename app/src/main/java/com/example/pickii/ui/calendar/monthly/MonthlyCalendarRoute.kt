package com.example.pickii.ui.calendar.monthly


import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import java.time.LocalDate

@Composable
fun MonthlyCalendarRoute(
    onAddScheduleClick: () -> Unit,
    onDailyCalendarClick: (LocalDate) -> Unit,
    viewModel: MonthlyCalendarViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    MonthlyCalendarScreen(
        uiState = uiState,
        onPreviousMonthClick = viewModel::moveToPreviousMonth,
        onNextMonthClick = viewModel::moveToNextMonth,
        onDateClick = viewModel::selectDate,
        onScheduleClick = viewModel::toggleSchedule,
        onAddScheduleClick = onAddScheduleClick,
        onDailyCalendarClick = onDailyCalendarClick,
    )
}