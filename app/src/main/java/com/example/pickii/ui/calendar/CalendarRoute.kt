package com.example.pickii.ui.calendar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.pickii.ui.calendar.category.ScheduleCategoryRoute
import com.example.pickii.ui.calendar.daily.DailyCalendarRoute
import com.example.pickii.ui.calendar.editor.ScheduleEditorRoute
import com.example.pickii.ui.calendar.monthly.MonthlyCalendarRoute
import java.time.LocalDate

private enum class CalendarScreenType {
    MONTHLY,
    DAILY,
    EDITOR,
    CATEGORY,
}

/**
 * 캘린더 내부 화면 전환을 관리한다.
 */
@Composable
fun CalendarRoute(
    onScheduleClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    var currentScreen by rememberSaveable {
        mutableStateOf(CalendarScreenType.MONTHLY.name)
    }

    var selectedDateText by rememberSaveable {
        mutableStateOf(
            LocalDate.of(2026, 7, 4).toString(),
        )
    }

    when (
        CalendarScreenType.valueOf(currentScreen)
    ) {
        CalendarScreenType.MONTHLY -> {
            MonthlyCalendarRoute(
                onAddScheduleClick = {
                    currentScreen = CalendarScreenType.EDITOR.name
                },
                onDailyCalendarClick = { selectedDate ->
                    selectedDateText = selectedDate.toString()
                    currentScreen = CalendarScreenType.DAILY.name
                },
            )
        }

        CalendarScreenType.DAILY -> {
            DailyCalendarRoute(
                selectedDate = LocalDate.parse(selectedDateText),
                onMenuClick = {
                    currentScreen = CalendarScreenType.MONTHLY.name
                },
                onAddScheduleClick = {
                    currentScreen = CalendarScreenType.EDITOR.name
                },
                onScheduleClick = onScheduleClick,
                modifier = modifier,
            )
        }

        CalendarScreenType.EDITOR -> {
            ScheduleEditorRoute(
                onBackClick = {
                    currentScreen = CalendarScreenType.MONTHLY.name
                },
                onManageCategoryClick = {
                    currentScreen = CalendarScreenType.CATEGORY.name
                },
                onScheduleSaved = {
                    currentScreen = CalendarScreenType.MONTHLY.name
                },
                modifier = modifier,
            )
        }

        CalendarScreenType.CATEGORY -> {
            ScheduleCategoryRoute(
                onCloseClick = {
                    currentScreen = CalendarScreenType.EDITOR.name
                },
                onSaved = {
                    currentScreen = CalendarScreenType.EDITOR.name
                },
                modifier = modifier,
            )
        }
    }
}