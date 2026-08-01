package com.example.pickii.ui.calendar.daily

import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime

/**
 * 일일 캘린더 화면의 상태를 나타낸다.
 */
data class DailyCalendarUiState(
    val selectedDate: LocalDate = LocalDate.of(2026, 7, 4),
    val schedules: List<DailyScheduleUiModel> = emptyList(),
)

/**
 * 일일 캘린더에 표시되는 일정 데이터다.
 */
data class DailyScheduleUiModel(
    val id: Long,
    val title: String,
    val date: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val colorType: DailyScheduleColorType,
) {
    /**
     * 일정 진행 시간을 분 단위로 반환한다.
     */
    val durationMinutes: Long
        get() = Duration.between(
            startTime,
            endTime,
        ).toMinutes()

    /**
     * 일정이 선택한 날짜에 포함되는지 확인한다.
     */
    fun includesDate(targetDate: LocalDate): Boolean {
        return date == targetDate
    }
}

/**
 * 일정 블록의 색상 종류다.
 */
enum class DailyScheduleColorType {
    RED,
    GREEN,
    PURPLE,
    BLUE,
    YELLOW,
}