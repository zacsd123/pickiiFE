package com.example.pickii.ui.calendar.monthly

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pickii.domain.model.ScheduleColorType
import com.example.pickii.domain.model.ScheduleRepeatType
import com.example.pickii.domain.repository.CalendarRepository
import com.example.pickii.domain.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import javax.inject.Inject

private val ScheduleTimeFormatter =
    DateTimeFormatter.ofPattern(
        "HH:mm"
    )

/**
 * 월간 캘린더 화면의 상태와 사용자 동작을 관리한다.
 */
@HiltViewModel
class MonthlyCalendarViewModel
    @Inject
    constructor(
        private val calendarRepository: CalendarRepository,
        private val notificationRepository: NotificationRepository
    ) : ViewModel() {
        private val _uiState =
            MutableStateFlow(
                MonthlyCalendarUiState(
                    schedules = emptyList()
                )
            )

        val uiState = _uiState.asStateFlow()

        init {
            observeCalendarData()
            viewModelScope.launch { calendarRepository.loadCategories() }
            loadMonth(_uiState.value.displayedYearMonth)
            loadNotificationCount()
        }

        /** 상단 헤더에 표시할 미읽음 알림 수를 불러온다(예: 알림 화면을 봤다 돌아왔을 때). */
        fun loadNotificationCount() {
            viewModelScope.launch {
                notificationRepository.getUnreadCount().onSuccess { count ->
                    _uiState.update { it.copy(notificationCount = count) }
                }
            }
        }

        private fun loadMonth(yearMonth: YearMonth) {
            viewModelScope.launch { calendarRepository.loadSchedules(yearMonth) }
        }

        /**
         * 저장소의 일정과 태그 목록을 함께 관찰한다.
         */
        private fun observeCalendarData() {
            combine(
                calendarRepository.schedules,
                calendarRepository.categories
            ) { schedules, categories ->
                schedules.map { schedule ->
                    val category =
                        categories.find { category ->
                            category.id == schedule.categoryId
                        }

                    MonthlyScheduleUiModel(
                        id = schedule.id,
                        title = schedule.title,
                        categoryName = category?.name ?: "없음",
                        categoryColor = category?.color ?: ScheduleColorType.GRAY,
                        startDate = schedule.startDate,
                        endDate = schedule.endDate,
                        startTime =
                            schedule.startTime
                                ?.format(ScheduleTimeFormatter)
                                .orEmpty(),
                        endTime =
                            schedule.endTime
                                ?.format(ScheduleTimeFormatter)
                                .orEmpty(),
                        location = schedule.location,
                        repeatType = schedule.repeatType,
                        repeatWeekdays = schedule.repeatWeekdays,
                        repeatText =
                            createRepeatText(
                                repeatType = schedule.repeatType,
                                repeatWeekdays = schedule.repeatWeekdays
                            ),
                        memo = schedule.memo,
                        isAllDay = schedule.isAllDay
                    )
                }
            }.onEach { schedules ->
                _uiState.update { currentState ->
                    currentState.copy(
                        schedules = schedules
                    )
                }
            }.launchIn(viewModelScope)
        }

        /**
         * 이전 달로 이동한다.
         */
        fun moveToPreviousMonth() {
            val previousMonth = _uiState.value.displayedYearMonth.minusMonths(1)
            _uiState.update { currentState ->
                currentState.copy(
                    displayedYearMonth = previousMonth,
                    selectedDate = previousMonth.atDay(1),
                    expandedScheduleId = null
                )
            }
            loadMonth(previousMonth)
        }

        /**
         * 다음 달로 이동한다.
         */
        fun moveToNextMonth() {
            val nextMonth = _uiState.value.displayedYearMonth.plusMonths(1)
            _uiState.update { currentState ->
                currentState.copy(
                    displayedYearMonth = nextMonth,
                    selectedDate = nextMonth.atDay(1),
                    expandedScheduleId = null
                )
            }
            loadMonth(nextMonth)
        }

        /**
         * 사용자가 선택한 날짜로 상태를 변경한다.
         */
        fun selectDate(date: LocalDate) {
            _uiState.update { currentState ->
                currentState.copy(
                    selectedDate = date,
                    expandedScheduleId = null
                )
            }
        }

        /**
         * 일정 상세 카드의 펼침 상태를 변경한다.
         */
        private fun toggleScheduleDetail(scheduleId: Long) {
            _uiState.update { currentState ->
                currentState.copy(
                    expandedScheduleId =
                        if (
                            currentState.expandedScheduleId == scheduleId
                        ) {
                            null
                        } else {
                            scheduleId
                        }
                )
            }
        }

        /**
         * 현재 펼쳐진 일정 상세 영역을 닫는다.
         */
        private fun closeScheduleDetail() {
            _uiState.update { currentState ->
                currentState.copy(
                    expandedScheduleId = null
                )
            }
        }

        /**
         * 일정 카드 펼침 상태를 변경한다.
         */
        fun toggleSchedule(scheduleId: Long) {
            toggleScheduleDetail(scheduleId)
        }

        /**
         * 일정을 삭제한다.
         */
        fun deleteSchedule(scheduleId: Long) {
            viewModelScope.launch {
                calendarRepository.deleteSchedule(scheduleId).onSuccess {
                    if (_uiState.value.expandedScheduleId == scheduleId) {
                        closeScheduleDetail()
                    }
                }
            }
        }
    }

/**
 * 반복 정보를 화면에 표시할 문자열로 변환한다.
 */
private fun createRepeatText(
    repeatType: ScheduleRepeatType,
    repeatWeekdays: Set<DayOfWeek>
): String =
    when (repeatType) {
        ScheduleRepeatType.NONE -> "없음"

        ScheduleRepeatType.DAILY -> "매일"

        ScheduleRepeatType.WEEKLY -> {
            if (repeatWeekdays.isEmpty()) {
                "매주"
            } else {
                val weekdayText =
                    repeatWeekdays
                        .sortedBy { dayOfWeek ->
                            dayOfWeek.value
                        }.joinToString(", ") { dayOfWeek ->
                            dayOfWeek.toKoreanShortName()
                        }

                "매주 $weekdayText"
            }
        }

        ScheduleRepeatType.MONTHLY -> "매월"

        ScheduleRepeatType.YEARLY -> "매년"
    }

/**
 * 요일을 짧은 한글 문자열로 변환한다.
 */
private fun DayOfWeek.toKoreanShortName(): String =
    when (this) {
        DayOfWeek.MONDAY -> "월"
        DayOfWeek.TUESDAY -> "화"
        DayOfWeek.WEDNESDAY -> "수"
        DayOfWeek.THURSDAY -> "목"
        DayOfWeek.FRIDAY -> "금"
        DayOfWeek.SATURDAY -> "토"
        DayOfWeek.SUNDAY -> "일"
    }

