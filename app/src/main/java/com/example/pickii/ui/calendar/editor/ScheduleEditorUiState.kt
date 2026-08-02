package com.example.pickii.ui.calendar.editor

import com.example.pickii.domain.model.ScheduleCategory
import com.example.pickii.domain.model.ScheduleRepeatType
import java.time.LocalDate
import java.time.LocalTime

/**
 * 일정 등록 화면의 상태다.
 */
data class ScheduleEditorUiState(
    val title: String = "",
    val categories: List<ScheduleCategory> = emptyList(),
    val selectedCategoryId: Long? = null,
    val startDate: LocalDate = LocalDate.of(2026, 7, 4),
    val endDate: LocalDate = LocalDate.of(2026, 7, 4),
    val startTime: LocalTime = LocalTime.of(9, 0),
    val endTime: LocalTime = LocalTime.of(10, 0),
    val isAllDay: Boolean = false,
    val location: String = "",
    val repeatType: ScheduleRepeatType = ScheduleRepeatType.NONE,
    val memo: String = "",
    val isSaved: Boolean = false
) {
    val canSave: Boolean
        get() {
            if (title.isBlank()) {
                return false
            }

            if (endDate.isBefore(startDate)) {
                return false
            }

            if (
                !isAllDay &&
                startDate == endDate &&
                !endTime.isAfter(startTime)
            ) {
                return false
            }

            return true
        }
}
