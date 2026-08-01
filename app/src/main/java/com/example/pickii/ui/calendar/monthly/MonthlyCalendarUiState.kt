package com.example.pickii.ui.calendar.monthly

import java.time.LocalDate
import java.time.YearMonth
import java.time.LocalTime

/**
 * 월간 캘린더 화면에 표시할 전체 상태다.
 *
 * @property displayedYearMonth 현재 달력에 표시 중인 연도와 월
 * @property selectedDate 사용자가 선택한 날짜
 * @property schedules 캘린더에 표시할 일정 목록
 * @property expandedScheduleId 상세 정보가 펼쳐진 일정 ID
 */
data class MonthlyCalendarUiState(
    val displayedYearMonth: YearMonth = YearMonth.of(2026, 7),
    val selectedDate: LocalDate = LocalDate.of(2026, 7, 4),
    val schedules: List<MonthlyScheduleUiModel> = createMockSchedules(),
    val expandedScheduleId: Long? = null,
) {

    /**
     * 현재 선택한 날짜에 포함되는 일정 목록이다.
     */
    val selectedDateSchedules: List<MonthlyScheduleUiModel>
        get() = schedules.filter { schedule ->
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
    val repeatText: String = "없음",
    val memo: String = "",
    val isAllDay: Boolean,
) {

    /**
     * 전달받은 날짜가 일정 기간 안에 포함되는지 확인한다.
     */
    fun includesDate(date: LocalDate): Boolean {
        return !date.isBefore(startDate) && !date.isAfter(endDate)
    }

    /**
     * 여러 날짜에 걸친 일정인지 반환한다.
     */
    val isMultiDay: Boolean
        get() = startDate != endDate
}

/**
 * 일정 태그 색상 종류다.
 *
 * 실제 Compose Color 변환은 UI 컴포넌트에서 처리한다.
 */
enum class ScheduleColorType {
    RED,
    ORANGE,
    YELLOW,
    GREEN,
    BLUE,
    PURPLE,
    PINK,
    GRAY,
    BLACK,
}

/**
 * 화면 확인을 위한 임시 일정 데이터다.
 */
private fun createMockSchedules(): List<MonthlyScheduleUiModel> {
    return listOf(
        MonthlyScheduleUiModel(
            id = 1L,
            title = "아르바이트",
            categoryName = "알바",
            categoryColor = ScheduleColorType.RED,
            startDate = LocalDate.of(2026, 7, 4),
            startTime = "09:00",
            endTime = "18:00",
            location = "스타벅스 강남점",
            repeatText = "매주 토, 일",
            memo = "머리끈 꼭 챙기기. 오픈 담당이라 8:50까지 도착해야 함.",
            isAllDay = false,
        ),
        MonthlyScheduleUiModel(
            id = 2L,
            title = "아르바이트",
            categoryName = "알바",
            categoryColor = ScheduleColorType.RED,
            startDate = LocalDate.of(2026, 7, 4),
            startTime = "19:00",
            endTime = "22:00",
            location = "카페",
            repeatText = "없음",
            memo = "",
            isAllDay = false,
        ),
        MonthlyScheduleUiModel(
            id = 3L,
            title = "프로젝트 진행 기간",
            categoryName = "약속",
            categoryColor = ScheduleColorType.GREEN,
            startDate = LocalDate.of(2026, 7, 6),
            endDate = LocalDate.of(2026, 7, 10),
            isAllDay = false,
        ),
        MonthlyScheduleUiModel(
            id = 4L,
            title = "개인 일정",
            categoryName = "개인",
            categoryColor = ScheduleColorType.PURPLE,
            startDate = LocalDate.of(2026, 7, 7),
            isAllDay = false,
        ),
        MonthlyScheduleUiModel(
            id = 5L,
            title = "모임",
            categoryName = "약속",
            categoryColor = ScheduleColorType.RED,
            startDate = LocalDate.of(2026, 7, 14),
            isAllDay = false,
        ),
        MonthlyScheduleUiModel(
            id = 6L,
            title = "개인 일정",
            categoryName = "개인",
            categoryColor = ScheduleColorType.PURPLE,
            startDate = LocalDate.of(2026, 7, 18),
            isAllDay = true,
        ),
        MonthlyScheduleUiModel(
            id = 7L,
            title = "일정",
            categoryName = "없음",
            categoryColor = ScheduleColorType.GRAY,
            startDate = LocalDate.of(2026, 7, 21),
            isAllDay = false,
        ),
        MonthlyScheduleUiModel(
            id = 8L,
            title = "일정",
            categoryName = "없음",
            categoryColor = ScheduleColorType.BLACK,
            startDate = LocalDate.of(2026, 7, 25),
            isAllDay = false,
        ),
        MonthlyScheduleUiModel(
            id = 9L,
            title = "개인 일정",
            categoryName = "개인",
            categoryColor = ScheduleColorType.PURPLE,
            startDate = LocalDate.of(2026, 7, 28),
            isAllDay = false,
        ),
    )
}