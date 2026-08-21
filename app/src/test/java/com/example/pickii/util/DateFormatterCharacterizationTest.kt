package com.example.pickii.util

import org.junit.Assert.assertEquals
import org.junit.Test
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

/**
 * kotlinx-datetime 전환 전 java.time 기준 현재 출력 문자열을 고정한다.
 * 전환 후에도 이 테스트가 값 변경 없이 그대로 통과해야 한다.
 */
class DateFormatterCharacterizationTest {
    @Test
    fun `toDisplayString formats as yyyy dot MM dot dd`() {
        assertEquals("2025.03.07", LocalDate(2025, 3, 7).toDisplayString())
        assertEquals("2026.12.31", LocalDate(2026, 12, 31).toDisplayString())
    }

    @Test
    fun `toCompactDisplayString formats as yy dot MM dot dd`() {
        assertEquals("25.03.07", LocalDateTime(2025, 3, 7, 9, 5, 3).toCompactDisplayString())
        assertEquals("05.01.01", LocalDateTime(2005, 1, 1, 0, 0, 0).toCompactDisplayString())
        assertEquals("26.12.31", LocalDateTime(2026, 12, 31, 23, 59, 59).toCompactDisplayString())
    }

    @Test
    fun `toFullDisplayString formats as yyyy dot MM dot dd HH colon mm colon ss`() {
        assertEquals("2025.03.07 09:05:03", LocalDateTime(2025, 3, 7, 9, 5, 3).toFullDisplayString())
        assertEquals("2026.12.31 23:59:59", LocalDateTime(2026, 12, 31, 23, 59, 59).toFullDisplayString())
        assertEquals("2005.01.01 00:00:00", LocalDateTime(2005, 1, 1, 0, 0, 0).toFullDisplayString())
    }
}
