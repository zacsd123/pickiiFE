package com.example.pickii.domain.model

import java.time.YearMonth

/** 사용자가 입력한 수상 및 경험 한 항목. 진행 중이면 [endDate]가 null이다. */
data class ExperienceEntry(
    val startDate: YearMonth,
    val endDate: YearMonth?,
    val title: String,
    val organization: String,
    val description: String
)
