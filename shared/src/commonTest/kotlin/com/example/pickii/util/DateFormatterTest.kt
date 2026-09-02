package com.example.pickii.util

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals

class DateFormatterTest {
    @Test
    fun `toDisplayString은 한 자리 월_일을 0으로 채운다`() {
        val date = LocalDate(2026, 1, 5)

        assertEquals("2026.01.05", date.toDisplayString())
    }

    @Test
    fun `toDisplayString은 두 자리 월_일을 그대로 표시한다`() {
        val date = LocalDate(2026, 12, 31)

        assertEquals("2026.12.31", date.toDisplayString())
    }

    @Test
    fun `toCompactDisplayString은 한 자리 월_일을 0으로 채운다`() {
        val dateTime = LocalDateTime(2026, 1, 5, 0, 0)

        assertEquals("26.01.05", dateTime.toCompactDisplayString())
    }

    @Test
    fun `toFullDisplayString은 한 자리 시_분_초를 0으로 채운다`() {
        val dateTime = LocalDateTime(2026, 1, 5, 3, 4, 5)

        assertEquals("2026.01.05 03:04:05", dateTime.toFullDisplayString())
    }
}
