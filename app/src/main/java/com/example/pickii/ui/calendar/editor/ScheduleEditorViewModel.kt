package com.example.pickii.ui.calendar.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pickii.domain.model.CalendarSchedule
import com.example.pickii.domain.model.ScheduleRepeatType
import com.example.pickii.domain.repository.CalendarRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 일정 등록 화면의 상태를 관리한다.
 */
@HiltViewModel
class ScheduleEditorViewModel
    @Inject
    constructor(
        private val calendarRepository: CalendarRepository
    ) : ViewModel() {
        private val _uiState =
            MutableStateFlow(
                ScheduleEditorUiState()
            )

        val uiState: StateFlow<ScheduleEditorUiState> =
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

        fun onEvent(event: ScheduleEditorUiEvent) {
            when (event) {
                is ScheduleEditorUiEvent.TitleChanged -> {
                    updateTitle(event.title)
                }

                is ScheduleEditorUiEvent.CategorySelected -> {
                    selectCategory(event.categoryId)
                }

                is ScheduleEditorUiEvent.StartDateChanged -> {
                    updateStartDate(event.date)
                }

                is ScheduleEditorUiEvent.EndDateChanged -> {
                    updateEndDate(event.date)
                }

                is ScheduleEditorUiEvent.DateRangeSelected -> {
                    _uiState.update { currentState ->
                        currentState.copy(
                            startDate = event.startDate,
                            endDate = event.endDate,
                            isSaved = false
                        )
                    }
                }

                is ScheduleEditorUiEvent.StartTimeChanged -> {
                    _uiState.update { currentState ->
                        currentState.copy(
                            startTime = event.time,
                            isSaved = false
                        )
                    }
                }

                is ScheduleEditorUiEvent.EndTimeChanged -> {
                    _uiState.update { currentState ->
                        currentState.copy(
                            endTime = event.time,
                            isSaved = false
                        )
                    }
                }

                is ScheduleEditorUiEvent.AllDayChanged -> {
                    _uiState.update { currentState ->
                        currentState.copy(
                            isAllDay = event.isAllDay,
                            isSaved = false
                        )
                    }
                }

                is ScheduleEditorUiEvent.LocationChanged -> {
                    _uiState.update { currentState ->
                        currentState.copy(
                            location = event.location,
                            isSaved = false
                        )
                    }
                }

                is ScheduleEditorUiEvent.RepeatTypeChanged -> {
                    _uiState.update { currentState ->
                        currentState.copy(
                            repeatType = event.repeatType,
                            repeatWeekdays =
                                if (event.repeatType == ScheduleRepeatType.WEEKLY) {
                                    currentState.repeatWeekdays
                                } else {
                                    emptySet()
                                },
                            isSaved = false
                        )
                    }
                }

                is ScheduleEditorUiEvent.RepeatWeekdayToggled -> {
                    _uiState.update { currentState ->
                        val weekdays = currentState.repeatWeekdays
                        currentState.copy(
                            repeatWeekdays =
                                if (event.dayOfWeek in weekdays) {
                                    weekdays - event.dayOfWeek
                                } else {
                                    weekdays + event.dayOfWeek
                                },
                            isSaved = false
                        )
                    }
                }

                is ScheduleEditorUiEvent.MemoChanged -> {
                    _uiState.update { currentState ->
                        currentState.copy(
                            memo = event.memo,
                            isSaved = false
                        )
                    }
                }

                ScheduleEditorUiEvent.SaveClicked -> {
                    saveSchedule()
                }

                ScheduleEditorUiEvent.SavedStateConsumed -> {
                    _uiState.update { currentState ->
                        currentState.copy(
                            isSaved = false
                        )
                    }
                }
            }
        }

        private fun updateTitle(title: String) {
            _uiState.update { currentState ->
                currentState.copy(
                    title = title,
                    isSaved = false
                )
            }
        }

        private fun selectCategory(categoryId: Long?) {
            _uiState.update { currentState ->
                currentState.copy(
                    selectedCategoryId = categoryId,
                    isSaved = false
                )
            }
        }

        private fun updateStartDate(date: java.time.LocalDate) {
            _uiState.update { currentState ->
                val adjustedEndDate =
                    if (
                        currentState.endDate.isBefore(date)
                    ) {
                        date
                    } else {
                        currentState.endDate
                    }

                currentState.copy(
                    startDate = date,
                    endDate = adjustedEndDate,
                    isSaved = false
                )
            }
        }

        private fun updateEndDate(date: java.time.LocalDate) {
            _uiState.update { currentState ->
                currentState.copy(
                    endDate = date,
                    isSaved = false
                )
            }
        }

        private fun saveSchedule() {
            val currentState = _uiState.value

            if (!currentState.canSave) {
                return
            }

            val schedule =
                CalendarSchedule(
                    id = calendarRepository.createScheduleId(),
                    title = currentState.title.trim(),
                    categoryId = currentState.selectedCategoryId,
                    startDate = currentState.startDate,
                    endDate = currentState.endDate,
                    startTime =
                        if (currentState.isAllDay) {
                            null
                        } else {
                            currentState.startTime
                        },
                    endTime =
                        if (currentState.isAllDay) {
                            null
                        } else {
                            currentState.endTime
                        },
                    location = currentState.location.trim(),
                    repeatType = currentState.repeatType,
                    repeatWeekdays = currentState.repeatWeekdays,
                    memo = currentState.memo.trim(),
                    isAllDay = currentState.isAllDay
                )

            calendarRepository.addSchedule(schedule)

            _uiState.update { currentState ->
                currentState.copy(
                    isSaved = true
                )
            }
        }
    }
