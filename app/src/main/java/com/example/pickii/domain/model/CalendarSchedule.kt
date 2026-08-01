package com.example.pickii.domain.model

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

/**
 * 캘린더에서 공통으로 사용하는 일정 모델이다.
 *
 * 월간 캘린더, 일간 캘린더, 일정 등록 화면에서
 * 같은 일정 데이터를 사용하기 위한 모델이다.
 *
 * @property id 일정 고유 ID
 * @property title 일정 제목
 * @property categoryId 일정에 연결된 태그 ID
 * @property startDate 일정 시작 날짜
 * @property endDate 일정 종료 날짜
 * @property startTime 일정 시작 시간
 * @property endTime 일정 종료 시간
 * @property location 일정 위치
 * @property repeatType 반복 종류
 * @property repeatWeekdays 매주 반복할 요일
 * @property memo 일정 메모
 * @property isAllDay 하루 종일 일정인지 여부
 */
data class CalendarSchedule(
    val id: Long,
    val title: String,
    val categoryId: Long? = null,
    val startDate: LocalDate,
    val endDate: LocalDate = startDate,
    val startTime: LocalTime? = null,
    val endTime: LocalTime? = null,
    val location: String = "",
    val repeatType: ScheduleRepeatType = ScheduleRepeatType.NONE,
    val repeatWeekdays: Set<DayOfWeek> = emptySet(),
    val memo: String = "",
    val isAllDay: Boolean = false,
) {

    /**
     * 전달받은 날짜가 일정 기간 안에 포함되는지 확인한다.
     */
    fun includesDate(
        date: LocalDate,
    ): Boolean {
        return !date.isBefore(startDate) &&
                !date.isAfter(endDate)
    }

    /**
     * 여러 날짜에 걸쳐 진행되는 일정인지 확인한다.
     */
    val isMultiDay: Boolean
        get() = startDate != endDate

    /**
     * 일정에 시작 시간과 종료 시간이 모두 등록되어 있는지 확인한다.
     */
    val hasTime: Boolean
        get() = startTime != null && endTime != null
}