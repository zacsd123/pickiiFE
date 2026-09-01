package com.example.pickii.ui.calendar.editor

import com.example.pickii.domain.model.ScheduleCategory
import com.example.pickii.domain.model.ScheduleRepeatType
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

/**
 * 일정 등록 화면의 상태다.
 */
data class ScheduleEditorUiState(
    val title: String = "",
    val categories: List<ScheduleCategory> = emptyList(),
    val selectedCategoryId: Long? = null,
    val startDate: LocalDate = LocalDate(2026, 7, 4),
    val endDate: LocalDate = LocalDate(2026, 7, 4),
    val startTime: LocalTime = LocalTime(9, 0),
    val endTime: LocalTime = LocalTime(10, 0),
    val isAllDay: Boolean = false,
    val location: String = "",
    val repeatType: ScheduleRepeatType = ScheduleRepeatType.NONE,
    val repeatWeekdays: Set<DayOfWeek> = emptySet(),
    val memo: String = "",
    val editingScheduleId: Long? = null,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val errorMessage: String? = null
) {
    val isEditMode: Boolean
        get() = editingScheduleId != null

    val canSave: Boolean
        get() {
            if (title.isBlank()) {
                return false
            }

            if (endDate < startDate) {
                return false
            }

            if (
                !isAllDay &&
                startDate == endDate &&
                endTime <= startTime
            ) {
                return false
            }

            return true
        }
}
