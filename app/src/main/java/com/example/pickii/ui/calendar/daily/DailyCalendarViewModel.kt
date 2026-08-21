package com.example.pickii.ui.calendar.daily

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pickii.domain.model.ScheduleColorType
import com.example.pickii.domain.repository.CalendarRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.pickii.util.today
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.YearMonth
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import javax.inject.Inject

private val ALL_DAY_START = LocalTime(0, 0)
private val ALL_DAY_END = LocalTime(23, 59)

/**
 * 일일 캘린더 화면의 상태를 관리한다. [CalendarRepository]의 일정 캐시에서 선택한 날짜에 해당하는 것만 걸러 보여준다.
 */
@HiltViewModel
class DailyCalendarViewModel
    @Inject
    constructor(
        private val calendarRepository: CalendarRepository
    ) : ViewModel() {
        private val _uiState =
            MutableStateFlow(
                DailyCalendarUiState(
                    selectedDate = today()
                )
            )

        val uiState: StateFlow<DailyCalendarUiState> = _uiState.asStateFlow()

        init {
            observeSchedules()
            loadMonth(_uiState.value.selectedDate)
        }

        private fun observeSchedules() {
            combine(
                calendarRepository.schedules,
                calendarRepository.categories,
                _uiState
            ) { schedules, categories, state ->
                schedules
                    .filter { it.includesDate(state.selectedDate) }
                    .map { schedule ->
                        val category = categories.find { it.id == schedule.categoryId }
                        DailyScheduleUiModel(
                            id = schedule.id,
                            title = schedule.title,
                            date = state.selectedDate,
                            startTime = schedule.startTime ?: ALL_DAY_START,
                            endTime = schedule.endTime ?: ALL_DAY_END,
                            colorType = category?.color ?: ScheduleColorType.BLUE
                        )
                    }
            }.onEach { schedules ->
                _uiState.update { it.copy(schedules = schedules) }
            }.launchIn(viewModelScope)
        }

        private fun loadMonth(date: LocalDate) {
            viewModelScope.launch { calendarRepository.loadSchedules(YearMonth(date.year, date.month)) }
        }

        /**
         * 이전 날짜로 이동한다.
         */
        fun moveToPreviousDate() {
            selectDate(_uiState.value.selectedDate.minus(1, DateTimeUnit.DAY))
        }

        /**
         * 다음 날짜로 이동한다.
         */
        fun moveToNextDate() {
            selectDate(_uiState.value.selectedDate.plus(1, DateTimeUnit.DAY))
        }

        /**
         * 특정 날짜를 선택한다.
         */
        fun selectDate(date: LocalDate) {
            val previousMonth = YearMonth(_uiState.value.selectedDate.year, _uiState.value.selectedDate.month)
            _uiState.update { it.copy(selectedDate = date) }
            if (YearMonth(date.year, date.month) != previousMonth) {
                loadMonth(date)
            }
        }

        fun addSchedule() = Unit

        fun selectSchedule(scheduleId: Long) = Unit
    }
