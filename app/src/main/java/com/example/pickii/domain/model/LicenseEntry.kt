package com.example.pickii.domain.model

import java.time.YearMonth

/** 사용자가 입력한 자격증 한 항목. */
data class LicenseEntry(
    val licenseName: String,
    val acquiredDate: YearMonth
)
