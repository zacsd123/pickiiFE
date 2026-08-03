package com.example.pickii.ui.calendar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
    CATEGORY
}

/**
 * 캘린더 내부 화면 전환을 관리한다.
 *
 * @param onTopLevelScreenChange 월간 화면(최상위)인지 여부가 바뀔 때마다 호출된다. 바깥의 공유 바텀 내비게이션을
 * 언제 보여줄지 결정하는 데 쓰인다.
 */
@Composable
fun CalendarRoute(
    onScheduleClick: (Long) -> Unit,
    onTopLevelScreenChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentScreen by rememberSaveable {
        mutableStateOf(CalendarScreenType.MONTHLY.name)
    }

    var editingScheduleId by rememberSaveable {
        mutableStateOf<Long?>(null)
    }

    var selectedDateText by rememberSaveable {
        mutableStateOf(
            LocalDate.of(2026, 7, 4).toString()
        )
    }

    LaunchedEffect(currentScreen) {
        onTopLevelScreenChange(currentScreen == CalendarScreenType.MONTHLY.name)
    }

    when (
        CalendarScreenType.valueOf(currentScreen)
    ) {
        CalendarScreenType.MONTHLY -> {
            MonthlyCalendarRoute(
                onAddScheduleClick = {
                    editingScheduleId = null
                    currentScreen = CalendarScreenType.EDITOR.name
                },
                onDailyCalendarClick = { selectedDate ->
                    selectedDateText = selectedDate.toString()
                    currentScreen = CalendarScreenType.DAILY.name
                },
                onEditScheduleClick = { scheduleId ->
                    editingScheduleId = scheduleId
                    currentScreen = CalendarScreenType.EDITOR.name
                }
            )
        }

        CalendarScreenType.DAILY -> {
            DailyCalendarRoute(
                selectedDate = LocalDate.parse(selectedDateText),
                onMenuClick = {
                    currentScreen = CalendarScreenType.MONTHLY.name
                },
                onAddScheduleClick = {
                    editingScheduleId = null
                    currentScreen = CalendarScreenType.EDITOR.name
                },
                onScheduleClick = onScheduleClick,
                modifier = modifier
            )
        }

        CalendarScreenType.EDITOR -> {
            ScheduleEditorRoute(
                scheduleId = editingScheduleId,
                onBackClick = {
                    editingScheduleId = null
                    currentScreen = CalendarScreenType.MONTHLY.name
                },
                onManageCategoryClick = {
                    currentScreen = CalendarScreenType.CATEGORY.name
                },
                onScheduleSaved = {
                    editingScheduleId = null
                    currentScreen = CalendarScreenType.MONTHLY.name
                },
                modifier = modifier
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
                modifier = modifier
            )
        }
    }
}
