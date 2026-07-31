package com.example.pickii.ui.calendar.monthly

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * 월간 캘린더의 ViewModel 상태와 화면을 연결한다.
 */
@Composable
fun MonthlyCalendarRoute(
    onScheduleEditClick: (Long) -> Unit,
    onAddScheduleClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MonthlyCalendarViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    MonthlyCalendarScreen(
        uiState = uiState,
        onPreviousMonthClick = viewModel::moveToPreviousMonth,
        onNextMonthClick = viewModel::moveToNextMonth,
        onDateClick = viewModel::selectDate,
        onScheduleClick = viewModel::toggleScheduleDetail,
        onScheduleDetailCloseClick = viewModel::closeScheduleDetail,
        onScheduleEditClick = onScheduleEditClick,
        onAddScheduleClick = onAddScheduleClick,
        modifier = modifier,
    )
}