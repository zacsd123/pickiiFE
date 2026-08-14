package com.example.pickii.ui.calendar.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pickii.domain.model.ScheduleColorType
import com.example.pickii.domain.repository.CalendarRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 태그 관리 화면의 상태와 동작을 관리한다.
 */
@HiltViewModel
class ScheduleCategoryViewModel
    @Inject
    constructor(
        private val calendarRepository: CalendarRepository
    ) : ViewModel() {
        private val _uiState =
            MutableStateFlow(
                ScheduleCategoryUiState()
            )

        val uiState: StateFlow<ScheduleCategoryUiState> =
            _uiState.asStateFlow()

        init {
            observeCategories()
        }

        private fun observeCategories() {
            viewModelScope.launch {
                calendarRepository.categories.collect { categories ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            categories = categories
                        )
                    }
                }
            }
        }

        fun updateNewCategoryName(name: String) {
            _uiState.update { currentState ->
                currentState.copy(
                    newCategoryName = name,
                    isSaved = false
                )
            }
        }

        fun selectColor(color: ScheduleColorType) {
            _uiState.update { currentState ->
                currentState.copy(
                    selectedColor = color,
                    isSaved = false
                )
            }
        }

        fun startEditingCategory(categoryId: Long) {
            val category =
                _uiState.value.categories.find { item ->
                    item.id == categoryId
                } ?: return

            _uiState.update { currentState ->
                currentState.copy(
                    editingCategoryId = category.id,
                    newCategoryName = category.name,
                    selectedColor = category.color,
                    isSaved = false
                )
            }
        }

        fun addOrUpdateCategory() {
            val currentState = _uiState.value
            val trimmedName = currentState.newCategoryName.trim()

            if (trimmedName.isBlank()) {
                return
            }

            val editingId = currentState.editingCategoryId

            viewModelScope.launch {
                val result =
                    if (editingId == null) {
                        calendarRepository.addCategory(trimmedName, currentState.selectedColor)
                    } else {
                        calendarRepository.updateCategory(editingId, trimmedName, currentState.selectedColor)
                    }

                result.onSuccess { resetEditor() }
            }
        }

        fun deleteCategory(categoryId: Long) {
            viewModelScope.launch {
                calendarRepository.deleteCategory(categoryId).onSuccess {
                    if (_uiState.value.editingCategoryId == categoryId) {
                        resetEditor()
                    }
                }
            }
        }

        fun saveCategories() {
            _uiState.update { currentState ->
                currentState.copy(
                    isSaved = true
                )
            }
        }

        fun consumeSavedState() {
            _uiState.update { currentState ->
                currentState.copy(
                    isSaved = false
                )
            }
        }

        private fun resetEditor() {
            _uiState.update { currentState ->
                currentState.copy(
                    newCategoryName = "",
                    selectedColor = ScheduleColorType.RED,
                    editingCategoryId = null,
                    isSaved = false
                )
            }
        }
    }
