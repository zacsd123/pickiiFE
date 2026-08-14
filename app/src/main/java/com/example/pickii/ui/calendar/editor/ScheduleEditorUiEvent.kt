package com.example.pickii.ui.calendar.editor

import com.example.pickii.domain.model.ScheduleRepeatType
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

/**
 * 일정 등록 화면에서 발생하는 사용자 동작이다.
 */
sealed interface ScheduleEditorUiEvent {
    data class TitleChanged(
        val title: String
    ) : ScheduleEditorUiEvent

    data class CategorySelected(
        val categoryId: Long?
    ) : ScheduleEditorUiEvent

    data class StartDateChanged(
        val date: LocalDate
    ) : ScheduleEditorUiEvent

    data class EndDateChanged(
        val date: LocalDate
    ) : ScheduleEditorUiEvent

    data class DateRangeSelected(
        val startDate: LocalDate,
        val endDate: LocalDate
    ) : ScheduleEditorUiEvent

    data class StartTimeChanged(
        val time: LocalTime
    ) : ScheduleEditorUiEvent

    data class EndTimeChanged(
        val time: LocalTime
    ) : ScheduleEditorUiEvent

    data class AllDayChanged(
        val isAllDay: Boolean
    ) : ScheduleEditorUiEvent

    data class LocationChanged(
        val location: String
    ) : ScheduleEditorUiEvent

    data class RepeatTypeChanged(
        val repeatType: ScheduleRepeatType
    ) : ScheduleEditorUiEvent

    data class RepeatWeekdayToggled(
        val dayOfWeek: DayOfWeek
    ) : ScheduleEditorUiEvent

    data class MemoChanged(
        val memo: String
    ) : ScheduleEditorUiEvent

    data object SaveClicked : ScheduleEditorUiEvent

    data object SavedStateConsumed : ScheduleEditorUiEvent
}
