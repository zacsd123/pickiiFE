package com.example.pickii.util

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val DateFormat = DateTimeFormatter.ofPattern("yyyy.MM.dd")
private val CompactDateTimeFormat = DateTimeFormatter.ofPattern("yy.MM.dd")
private val FullDateTimeFormat = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss")

/** 날짜를 "YYYY.MM.DD" 형식으로 표시한다. */
fun LocalDate.toDisplayString(): String = format(DateFormat)

/** 날짜/시각을 "YY.MM.DD" 형식으로 짧게 표시한다(모집 글 카드 등). */
fun LocalDateTime.toCompactDisplayString(): String = format(CompactDateTimeFormat)

/** 날짜/시각을 "YYYY.MM.DD HH:mm:ss" 형식으로 표시한다(작성 시각 등). */
fun LocalDateTime.toFullDisplayString(): String = format(FullDateTimeFormat)
