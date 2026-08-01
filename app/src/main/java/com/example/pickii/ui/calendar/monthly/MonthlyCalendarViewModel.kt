package com.example.pickii.ui.calendar.monthly

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/**
 * 월간 캘린더 화면의 상태와 사용자 동작을 관리한다.
 */
@HiltViewModel
class MonthlyCalendarViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(
        MonthlyCalendarUiState(),
    )

    val uiState = _uiState.asStateFlow()

    /**
     * 이전 달로 이동한다.
     */
    fun moveToPreviousMonth() {
        _uiState.update { currentState ->
            val previousMonth = currentState.displayedYearMonth.minusMonths(1)

            currentState.copy(
                displayedYearMonth = previousMonth,
                selectedDate = previousMonth.atDay(1),
                expandedScheduleId = null,
            )
        }
    }

    /**
     * 다음 달로 이동한다.
     */
    fun moveToNextMonth() {
        _uiState.update { currentState ->
            val nextMonth = currentState.displayedYearMonth.plusMonths(1)

            currentState.copy(
                displayedYearMonth = nextMonth,
                selectedDate = nextMonth.atDay(1),
                expandedScheduleId = null,
            )
        }
    }

    /**
     * 전달받은 연도와 월로 이동한다.
     *
     * @param yearMonth 이동할 연도와 월
     */
    fun moveToMonth(yearMonth: YearMonth) {
        _uiState.update { currentState ->
            currentState.copy(
                displayedYearMonth = yearMonth,
                selectedDate = yearMonth.atDay(1),
                expandedScheduleId = null,
            )
        }
    }

    /**
     * 사용자가 선택한 날짜로 상태를 변경한다.
     *
     * @param date 새로 선택한 날짜
     */
    fun selectDate(date: LocalDate) {
        _uiState.update { currentState ->
            currentState.copy(
                selectedDate = date,
                expandedScheduleId = null,
            )
        }
    }

    /**
     * 일정 상세 카드의 펼침 상태를 변경한다.
     *
     * 이미 펼쳐진 일정을 다시 누르면 상세 영역을 닫는다.
     *
     * @param scheduleId 펼침 상태를 변경할 일정 식별자
     */
    fun toggleScheduleDetail(scheduleId: Long) {
        _uiState.update { currentState ->
            val newExpandedScheduleId =
                if (currentState.expandedScheduleId == scheduleId) {
                    null
                } else {
                    scheduleId
                }

            currentState.copy(
                expandedScheduleId = newExpandedScheduleId,
            )
        }
    }

    /**
     * 현재 펼쳐진 일정 상세 영역을 닫는다.
     */
    fun closeScheduleDetail() {
        _uiState.update { currentState ->
            currentState.copy(
                expandedScheduleId = null,
            )
        }
    }
}