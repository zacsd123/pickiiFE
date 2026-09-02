package com.example.pickii.util

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import java.util.TimeZone as JavaTimeZone

/**
 * chat/meeting에서 쓰던 `SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).format(Date(millis))`를
 * `Instant.fromEpochMilliseconds(millis).toLocalDateTime(TimeZone.UTC).date.toDisplayString()`로
 * 바꾸기 전, 같은 millis에 대해 두 방식이 같은 문자열을 내는지 확인한다.
 *
 * 옛 코드는 `TimeZone.getDefault()`(기기 기본 타임존)로 포맷한다 — 이 프로젝트는 한국 사용자
 * 대상이라 실제 기기 타임존은 Asia/Seoul(UTC+9)이므로, 이 테스트도 그 값을 명시적으로 고정한다
 * (CI/로컬 머신의 기본 타임존에 좌우되면 재현이 안 됨). millis는 Material3 DateRangePicker가 주는
 * UTC 자정 기준이므로, UTC+9 변환은 자정을 넘기지 않아 항상 같은 날짜가 나온다 — 이 성질이
 * 새 코드(UTC 그대로 읽기)와 옛 코드(Asia/Seoul로 변환)가 일치하는 이유다. JVM 전용 API라
 * androidHostTest에만 있다.
 */
private fun formatWithOldSimpleDateFormat(millis: Long): String =
    SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
        .apply { timeZone = JavaTimeZone.getTimeZone("Asia/Seoul") }
        .format(Date(millis))

@OptIn(ExperimentalTime::class)
private fun formatWithNewKotlinxDatetime(millis: Long): String =
    Instant.fromEpochMilliseconds(millis).toLocalDateTime(TimeZone.UTC).date.toDisplayString()

@OptIn(ExperimentalTime::class)
private fun utcMidnightMillis(
    year: Int,
    month: Int,
    day: Int
): Long = LocalDate(year, month, day).atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()

class DateFormatterCharacterizationTest {
    @Test
    fun `한 자리 월_일도 옛 SimpleDateFormat과 같은 문자열을 낸다`() {
        val millis = utcMidnightMillis(2026, 1, 5)

        assertEquals(formatWithOldSimpleDateFormat(millis), formatWithNewKotlinxDatetime(millis))
        assertEquals("2026.01.05", formatWithNewKotlinxDatetime(millis))
    }

    @Test
    fun `12월 31일도 옛 SimpleDateFormat과 같은 문자열을 낸다`() {
        val millis = utcMidnightMillis(2026, 12, 31)

        assertEquals(formatWithOldSimpleDateFormat(millis), formatWithNewKotlinxDatetime(millis))
        assertEquals("2026.12.31", formatWithNewKotlinxDatetime(millis))
    }

    @Test
    fun `윤년 2월 29일도 옛 SimpleDateFormat과 같은 문자열을 낸다`() {
        val millis = utcMidnightMillis(2028, 2, 29)

        assertEquals(formatWithOldSimpleDateFormat(millis), formatWithNewKotlinxDatetime(millis))
        assertEquals("2028.02.29", formatWithNewKotlinxDatetime(millis))
    }
}
