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

        private var lastInitializedEntryKey: Int? = null

        init {
            observeCategories()
        }

        /**
         * 등록 화면을 새 일정 작성 상태로, 또는 기존 일정을 불러와 수정 상태로 초기화한다.
         *
         * [entryKey]가 이전과 같으면(태그 설정 화면을 다녀오는 등 같은 편집 세션 안에서
         * 재진입한 경우) 무시한다 — 그렇지 않으면 화면이 재구성될 때마다 입력 중이던 값이 초기화된다.
         */
        fun initialize(scheduleId: Long?, entryKey: Int) {
            if (lastInitializedEntryKey == entryKey) {
                return
            }
            lastInitializedEntryKey = entryKey

            if (scheduleId == null) {
                _uiState.update { currentState ->
                    ScheduleEditorUiState(categories = currentState.categories)
                }
                return
            }

            val schedule =
                calendarRepository.schedules.value.find { existingSchedule ->
                    existingSchedule.id == scheduleId
                } ?: return

            _uiState.update { currentState ->
                currentState.copy(
                    editingScheduleId = schedule.id,
                    title = schedule.title,
                    selectedCategoryId = schedule.categoryId,
                    startDate = schedule.startDate,
                    endDate = schedule.endDate,
                    startTime = schedule.startTime ?: currentState.startTime,
                    endTime = schedule.endTime ?: currentState.endTime,
                    isAllDay = schedule.isAllDay,
                    location = schedule.location,
                    repeatType = schedule.repeatType,
                    repeatWeekdays = schedule.repeatWeekdays,
                    memo = schedule.memo,
                    isSaved = false
                )
            }
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
                    // 신규 등록 시 id는 서버가 발급하므로 실제로 쓰이지 않는 자리표시자다.
                    id = currentState.editingScheduleId ?: 0L,
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

            viewModelScope.launch {
                _uiState.update { it.copy(isSaving = true) }
                val result =
                    if (currentState.editingScheduleId != null) {
                        calendarRepository.updateSchedule(schedule)
                    } else {
                        calendarRepository.addSchedule(schedule)
                    }

                result
                    .onSuccess {
                        _uiState.update { it.copy(isSaving = false, isSaved = true) }
                    }.onFailure { error ->
                        _uiState.update { it.copy(isSaving = false, errorMessage = error.message) }
                    }
            }
        }
    }
