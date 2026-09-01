package com.example.pickii.ui.calendar.monthly

import com.example.pickii.domain.model.ScheduleColorType
import com.example.pickii.domain.model.ScheduleRepeatType
import com.example.pickii.domain.model.scheduleRecurrenceIncludesDate
import com.example.pickii.util.today
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth

private fun currentYearMonth(): YearMonth = today().let { YearMonth(it.year, it.month) }

/**
 * 월간 캘린더 화면에 표시할 전체 상태다.
 *
 * @property displayedYearMonth 현재 달력에 표시 중인 연도와 월
 * @property selectedDate 사용자가 선택한 날짜
 * @property schedules 캘린더에 표시할 일정 목록
 * @property expandedScheduleId 상세 정보가 펼쳐진 일정 ID
 * @property notificationCount 상단 헤더에 표시할 미읽음 알림 수
 */
data class MonthlyCalendarUiState(
    val displayedYearMonth: YearMonth = currentYearMonth(),
    val selectedDate: LocalDate = today(),
    val schedules: List<MonthlyScheduleUiModel> = emptyList(),
    val expandedScheduleId: Long? = null,
    val notificationCount: Int = 0
) {
    /**
     * 현재 선택한 날짜에 포함되는 일정 목록이다.
     */
    val selectedDateSchedules: List<MonthlyScheduleUiModel>
        get() =
            schedules.filter { schedule ->
                schedule.includesDate(selectedDate)
            }
}

/**
 * 월간 캘린더에 표시할 일정 UI 모델이다.
 *
 * 백엔드 연동 전까지 월간 캘린더 화면 내부에서 사용한다.
 *
 * @property id 일정 고유 ID
 * @property title 일정 제목
 * @property categoryName 태그 이름
 * @property categoryColor 태그 색상
 * @property startDate 일정 시작 날짜
 * @property endDate 일정 종료 날짜
 * @property startTime 시작 시간
 * @property endTime 종료 시간
 * @property location 일정 장소
 * @property repeatText 반복 정보
 * @property memo 일정 메모
 */
data class MonthlyScheduleUiModel(
    val id: Long,
    val title: String,
    val categoryName: String,
    val categoryColor: ScheduleColorType,
    val startDate: LocalDate,
    val endDate: LocalDate = startDate,
    val startTime: String = "",
    val endTime: String = "",
    val location: String = "",
    val repeatType: ScheduleRepeatType = ScheduleRepeatType.NONE,
    val repeatWeekdays: Set<DayOfWeek> = emptySet(),
    val repeatText: String = "없음",
    val memo: String = "",
    val isAllDay: Boolean
) {
    /**
     * 전달받은 날짜가 일정 기간(반복 포함) 안에 포함되는지 확인한다.
     */
    fun includesDate(date: LocalDate): Boolean =
        scheduleRecurrenceIncludesDate(
            date = date,
            startDate = startDate,
            endDate = endDate,
            repeatType = repeatType,
            repeatWeekdays = repeatWeekdays
        )

    /**
     * 여러 날짜에 걸친 일정인지 반환한다.
     */
    val isMultiDay: Boolean
        get() = startDate != endDate
}
