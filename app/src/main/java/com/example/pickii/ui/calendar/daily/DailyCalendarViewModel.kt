package com.example.pickii.ui.calendar.daily

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.LocalTime

/**
 * 일일 캘린더 화면의 상태를 관리한다.
 */
class DailyCalendarViewModel : ViewModel() {
    private val initialDate = LocalDate.of(2026, 7, 4)

    private val _uiState =
        MutableStateFlow(
            DailyCalendarUiState(
                selectedDate = initialDate,
                schedules = createSampleSchedules()
            )
        )

    val uiState: StateFlow<DailyCalendarUiState> = _uiState.asStateFlow()

    /**
     * 이전 날짜로 이동한다.
     */
    fun moveToPreviousDate() {
        _uiState.update { currentState ->
            currentState.copy(
                selectedDate = currentState.selectedDate.minusDays(1)
            )
        }
    }

    /**
     * 다음 날짜로 이동한다.
     */
    fun moveToNextDate() {
        _uiState.update { currentState ->
            currentState.copy(
                selectedDate = currentState.selectedDate.plusDays(1)
            )
        }
    }

    /**
     * 특정 날짜를 선택한다.
     */
    fun selectDate(date: LocalDate) {
        _uiState.update { currentState ->
            currentState.copy(
                selectedDate = date
            )
        }
    }

    fun addSchedule() {
        // TODO 일정 등록 화면으로 이동
    }

    fun selectSchedule(scheduleId: Long) {
        // TODO 일정 상세 화면으로 이동
    }

    /**
     * 피그마 확인용 샘플 일정을 만든다.
     */
    private fun createSampleSchedules(): List<DailyScheduleUiModel> =
        listOf(
            DailyScheduleUiModel(
                id = 1L,
                title = "일정1",
                date = initialDate,
                startTime = LocalTime.of(1, 0),
                endTime = LocalTime.of(2, 0),
                colorType = DailyScheduleColorType.RED
            ),
            DailyScheduleUiModel(
                id = 2L,
                title = "일정2",
                date = initialDate,
                startTime = LocalTime.of(6, 0),
                endTime = LocalTime.of(8, 0),
                colorType = DailyScheduleColorType.GREEN
            )
        )
}
