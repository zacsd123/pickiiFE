package com.example.pickii.util

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format
import kotlinx.datetime.format.char

private val DateFormat =
    LocalDate.Format {
        year()
        char('.')
        monthNumber()
        char('.')
        day()
    }

private val CompactDateTimeFormat =
    LocalDateTime.Format {
        yearTwoDigits(baseYear = 2000)
        char('.')
        monthNumber()
        char('.')
        day()
    }

private val FullDateTimeFormat =
    LocalDateTime.Format {
        year()
        char('.')
        monthNumber()
        char('.')
        day()
        char(' ')
        hour()
        char(':')
        minute()
        char(':')
        second()
    }

/** 날짜를 "YYYY.MM.DD" 형식으로 표시한다. */
fun LocalDate.toDisplayString(): String = format(DateFormat)

/** 날짜/시각을 "YY.MM.DD" 형식으로 짧게 표시한다(모집 글 카드 등). */
fun LocalDateTime.toCompactDisplayString(): String = format(CompactDateTimeFormat)

/** 날짜/시각을 "YYYY.MM.DD HH:mm:ss" 형식으로 표시한다(작성 시각 등). */
fun LocalDateTime.toFullDisplayString(): String = format(FullDateTimeFormat)
