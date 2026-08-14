package com.example.pickii.ui.calendar.category

import com.example.pickii.domain.model.ScheduleCategory
import com.example.pickii.domain.model.ScheduleColorType

/**
 * 태그 관리 화면의 상태다.
 */
data class ScheduleCategoryUiState(
    val categories: List<ScheduleCategory> = emptyList(),
    val newCategoryName: String = "",
    val selectedColor: ScheduleColorType = ScheduleColorType.RED,
    val editingCategoryId: Long? = null,
    val isSaved: Boolean = false
) {
    val canAddCategory: Boolean
        get() = newCategoryName.isNotBlank()
}
