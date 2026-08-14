package com.example.pickii.ui.chat

data class QuickMeetingForm(
    val title: String,
    val durationMinutes: Int,
    val startDateMillis: Long,
    val endDateMillis: Long,
    val dayStartMinuteOfDay: Int,
    val dayEndMinuteOfDay: Int,
    val deadlineHours: Int,
    /** 비어 있으면 프로젝트 팀원 전원을 대상으로 한다(7-10 `memberIds` 미지정과 동일한 의미). */
    val memberIds: Set<Long> = emptySet()
)
