package com.example.pickii.ui.calendar.monthly

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import kotlinx.datetime.LocalDate
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MonthlyCalendarRoute(
    onAddScheduleClick: () -> Unit,
    onDailyCalendarClick: (LocalDate) -> Unit,
    onEditScheduleClick: (Long) -> Unit,
    onNotificationClick: () -> Unit,
    viewModel: MonthlyCalendarViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    MonthlyCalendarScreen(
        uiState = uiState,
        onPreviousMonthClick = viewModel::moveToPreviousMonth,
        onNextMonthClick = viewModel::moveToNextMonth,
        onDateClick = viewModel::selectDate,
        onScheduleClick = viewModel::toggleSchedule,
        onEditScheduleClick = onEditScheduleClick,
        onDeleteScheduleClick = viewModel::deleteSchedule,
        onAddScheduleClick = onAddScheduleClick,
        onDailyCalendarClick = onDailyCalendarClick,
        onNotificationClick = onNotificationClick
    )
}
