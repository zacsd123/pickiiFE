package com.example.pickii.util

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format.DateTimeComponents

/** 서버가 내려주는 ISO-8601 offset 날짜/시각 문자열(예: `2025-01-01T12:00:00+09:00`)을 [LocalDateTime]으로 변환한다. */
fun parseIsoOffsetDateTime(value: String): LocalDateTime =
    DateTimeComponents.Formats.ISO_DATE_TIME_OFFSET
        .parse(value)
        .toLocalDateTime()
