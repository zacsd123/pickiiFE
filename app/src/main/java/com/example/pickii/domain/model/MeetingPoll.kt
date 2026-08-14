package com.example.pickii.domain.model

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/** 회의 일정 조율(Meeting Poll)의 진행 상태(7-C). */
enum class MeetingPollStatus {
    COLLECTING,
    CONFIRMED,
    CANCELLED
}

/** `POST /projects/{projectId}/meeting-polls`(7-10) 결과. */
data class MeetingPollCreated(
    val pollId: Long,
    val deadline: LocalDateTime,
    val slotCount: Int
)

/** `GET /meeting-polls/{pollId}`(7-11) 응답. */
data class MeetingPollDetail(
    val pollId: Long,
    val title: String,
    val status: MeetingPollStatus,
    val deadline: LocalDateTime,
    val totalMembers: Int,
    val respondedCount: Int,
    val myResponded: Boolean,
    val slots: List<MeetingPollSlot>
)

/** 회의 조율 응답 슬롯 하나(30분 단위 후보). */
data class MeetingPollSlot(
    val slotId: Long,
    val startAt: LocalDateTime,
    val endAt: LocalDateTime,
    val myAvailable: Boolean,
    val prefilledByCalendar: Boolean,
    val availableCount: Int,
    val unansweredCount: Int
)

/** `GET /projects/{projectId}/schedules`(7-15)로 조회하는 확정된 팀 일정 하나. */
data class TeamSchedule(
    val scheduleId: Long,
    val title: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val startTime: LocalTime?,
    val endTime: LocalTime?
)
