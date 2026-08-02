package com.example.pickii.ui.chat

data class QuickMeetingForm(
    val title: String,
    val durationMinutes: Int,
    val startDateMillis: Long,
    val endDateMillis: Long,
    val candidateCount: Int,
    val memo: String
)
