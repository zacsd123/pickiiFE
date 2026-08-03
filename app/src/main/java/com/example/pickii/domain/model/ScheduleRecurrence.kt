package com.example.pickii.domain.model

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * 반복 설정을 고려해 [date]가 [startDate]~[endDate] 원본 일정의 반복 발생 범위에 포함되는지 확인한다.
 *
 * [repeatType]이 [ScheduleRepeatType.NONE]이면 원본 기간에만 포함 여부를 확인하고,
 * 그 외에는 [startDate] 이후로 반복 주기에 맞는 모든 발생을 대상으로 확인한다.
 */
fun scheduleRecurrenceIncludesDate(
    date: LocalDate,
    startDate: LocalDate,
    endDate: LocalDate,
    repeatType: ScheduleRepeatType,
    repeatWeekdays: Set<DayOfWeek>
): Boolean {
    if (repeatType == ScheduleRepeatType.NONE) {
        return !date.isBefore(startDate) && !date.isAfter(endDate)
    }

    val durationDays = ChronoUnit.DAYS.between(startDate, endDate)
    val effectiveWeekdays = repeatWeekdays.ifEmpty { setOf(startDate.dayOfWeek) }

    for (offset in 0..durationDays) {
        val occurrenceStart = date.minusDays(offset)

        if (occurrenceStart.isBefore(startDate)) {
            continue
        }

        val matchesPattern =
            when (repeatType) {
                ScheduleRepeatType.NONE -> false

                ScheduleRepeatType.DAILY -> true

                ScheduleRepeatType.WEEKLY -> occurrenceStart.dayOfWeek in effectiveWeekdays

                ScheduleRepeatType.MONTHLY -> occurrenceStart.dayOfMonth == startDate.dayOfMonth

                ScheduleRepeatType.YEARLY ->
                    occurrenceStart.dayOfMonth == startDate.dayOfMonth &&
                        occurrenceStart.month == startDate.month
            }

        if (matchesPattern) {
            return true
        }
    }

    return false
}
