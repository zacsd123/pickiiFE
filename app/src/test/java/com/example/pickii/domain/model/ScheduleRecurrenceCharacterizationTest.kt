package com.example.pickii.domain.model

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * kotlinx-datetime 전환 전 java.time 기준 [scheduleRecurrenceIncludesDate]의 현재 동작을 고정한다.
 * 전환 후에도 이 테스트가 값 변경 없이 그대로 통과해야 한다.
 *
 * 여기 값 중 일부는 "의도"가 아니라 "현재 구현이 실제로 하는 동작"이다 — 예를 들어 DAILY는 단일
 * 일정(durationDays=0)이든 여러 날짜짜리 일정이든 startDate 이후로 기한 없이 매일 포함되고,
 * MONTHLY는 dayOfMonth 숫자만 비교해서 그 날이 없는 달은 그냥 안 걸린다. 버그처럼 보여도 이번
 * 전환에서는 고치지 않고 동작을 그대로 이식한다.
 */
class ScheduleRecurrenceCharacterizationTest {
    @Test
    fun `NONE only includes the original start-end range`() {
        val start = LocalDate(2025, 3, 10)
        val end = LocalDate(2025, 3, 12)

        assertFalse(includes(LocalDate(2025, 3, 9), start, end, ScheduleRepeatType.NONE))
        assertTrue(includes(LocalDate(2025, 3, 10), start, end, ScheduleRepeatType.NONE))
        assertTrue(includes(LocalDate(2025, 3, 11), start, end, ScheduleRepeatType.NONE))
        assertTrue(includes(LocalDate(2025, 3, 12), start, end, ScheduleRepeatType.NONE))
        assertFalse(includes(LocalDate(2025, 3, 13), start, end, ScheduleRepeatType.NONE))
    }

    @Test
    fun `DAILY includes every day from startDate onward with no upper bound`() {
        val start = LocalDate(2025, 3, 10)
        val end = LocalDate(2025, 3, 10)

        assertFalse(includes(LocalDate(2025, 3, 9), start, end, ScheduleRepeatType.DAILY))
        assertTrue(includes(LocalDate(2025, 3, 10), start, end, ScheduleRepeatType.DAILY))
        assertTrue(includes(LocalDate(2030, 1, 1), start, end, ScheduleRepeatType.DAILY))
    }

    @Test
    fun `WEEKLY matches only the specified weekdays on or after startDate`() {
        val start = LocalDate(2025, 3, 10) // 월요일
        val end = start
        val weekdays = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY)

        assertTrue(includes(LocalDate(2025, 3, 10), start, end, ScheduleRepeatType.WEEKLY, weekdays)) // 월(시작일)
        assertTrue(includes(LocalDate(2025, 3, 12), start, end, ScheduleRepeatType.WEEKLY, weekdays)) // 수
        assertFalse(includes(LocalDate(2025, 3, 11), start, end, ScheduleRepeatType.WEEKLY, weekdays)) // 화
        assertTrue(includes(LocalDate(2025, 3, 17), start, end, ScheduleRepeatType.WEEKLY, weekdays)) // 다음주 월
        assertFalse(includes(LocalDate(2025, 3, 9), start, end, ScheduleRepeatType.WEEKLY, weekdays)) // 시작일 이전
    }

    @Test
    fun `WEEKLY with empty weekday set defaults to startDate's weekday`() {
        val start = LocalDate(2025, 3, 10) // 월요일
        val end = start

        assertTrue(includes(LocalDate(2025, 3, 10), start, end, ScheduleRepeatType.WEEKLY, emptySet()))
        assertFalse(includes(LocalDate(2025, 3, 12), start, end, ScheduleRepeatType.WEEKLY, emptySet()))
        assertTrue(includes(LocalDate(2025, 3, 17), start, end, ScheduleRepeatType.WEEKLY, emptySet()))
    }

    @Test
    fun `WEEKLY over a multi-day original event covers the whole span from each matching weekday`() {
        val start = LocalDate(2025, 3, 10) // 월요일
        val end = LocalDate(2025, 3, 11) // 화요일(2일짜리 일정)
        val weekdays = setOf(DayOfWeek.MONDAY)

        // 3/17(월)~3/18(화) 발생 구간
        assertTrue(includes(LocalDate(2025, 3, 17), start, end, ScheduleRepeatType.WEEKLY, weekdays))
        assertTrue(includes(LocalDate(2025, 3, 18), start, end, ScheduleRepeatType.WEEKLY, weekdays))
        assertFalse(includes(LocalDate(2025, 3, 19), start, end, ScheduleRepeatType.WEEKLY, weekdays))
    }

    @Test
    fun `MONTHLY matches same day-of-month, silently skipping months without that day`() {
        val start = LocalDate(2025, 1, 15)
        val end = start

        assertTrue(includes(LocalDate(2025, 1, 15), start, end, ScheduleRepeatType.MONTHLY))
        assertTrue(includes(LocalDate(2025, 2, 15), start, end, ScheduleRepeatType.MONTHLY))
        assertFalse(includes(LocalDate(2025, 2, 20), start, end, ScheduleRepeatType.MONTHLY))
        assertFalse(includes(LocalDate(2025, 1, 14), start, end, ScheduleRepeatType.MONTHLY))
    }

    @Test
    fun `MONTHLY on day 31 never matches in a month without a 31st`() {
        val start = LocalDate(2025, 1, 31)
        val end = start

        assertTrue(includes(LocalDate(2025, 1, 31), start, end, ScheduleRepeatType.MONTHLY))
        assertTrue(includes(LocalDate(2025, 3, 31), start, end, ScheduleRepeatType.MONTHLY))
        assertFalse(includes(LocalDate(2025, 2, 28), start, end, ScheduleRepeatType.MONTHLY))
    }

    @Test
    fun `YEARLY matches same month and day-of-month regardless of year`() {
        val start = LocalDate(2025, 6, 15)
        val end = start

        assertTrue(includes(LocalDate(2025, 6, 15), start, end, ScheduleRepeatType.YEARLY))
        assertTrue(includes(LocalDate(2026, 6, 15), start, end, ScheduleRepeatType.YEARLY))
        assertFalse(includes(LocalDate(2026, 6, 16), start, end, ScheduleRepeatType.YEARLY))
        assertFalse(includes(LocalDate(2026, 7, 15), start, end, ScheduleRepeatType.YEARLY))
        assertFalse(includes(LocalDate(2024, 6, 15), start, end, ScheduleRepeatType.YEARLY)) // 시작일 이전 해
    }

    private fun includes(
        date: LocalDate,
        start: LocalDate,
        end: LocalDate,
        repeatType: ScheduleRepeatType,
        weekdays: Set<DayOfWeek> = emptySet()
    ): Boolean = scheduleRecurrenceIncludesDate(date, start, end, repeatType, weekdays)
}
