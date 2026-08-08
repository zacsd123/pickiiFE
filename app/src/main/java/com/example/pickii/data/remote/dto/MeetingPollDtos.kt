package com.example.pickii.data.remote.dto

import kotlinx.serialization.Serializable

/** `POST /projects/{projectId}/meeting-polls`(7-10) 요청. */
@Serializable
data class CreateMeetingPollRequest(
    val title: String,
    val durationMin: Int,
    val rangeStart: String,
    val rangeEnd: String,
    val dayStart: String,
    val dayEnd: String,
    val deadlineHours: Int? = null,
    val memberIds: List<Long>? = null
)

/** `POST /projects/{projectId}/meeting-polls`(7-10) 응답. */
@Serializable
data class MeetingPollCreatedDto(
    val pollId: Long,
    val status: String,
    val deadline: String,
    val totalMembers: Int,
    val respondedCount: Int,
    val slotCount: Int
)

/** `GET /meeting-polls/{pollId}`(7-11) 응답. */
@Serializable
data class MeetingPollDetailDto(
    val pollId: Long,
    val title: String,
    val status: String,
    val durationMin: Int,
    val deadline: String,
    val totalMembers: Int,
    val respondedCount: Int,
    val myResponded: Boolean,
    val slots: List<MeetingPollSlotDto>
)

/** [MeetingPollDetailDto.slots]의 슬롯 하나. */
@Serializable
data class MeetingPollSlotDto(
    val slotId: Long,
    val startAt: String,
    val endAt: String,
    val myAvailable: Boolean,
    val prefilledByCalendar: Boolean,
    val availableCount: Int,
    val unansweredCount: Int
)

/** `POST /meeting-polls/{pollId}/responses`(7-12) 요청. 불가능한 슬롯만 보낸다. */
@Serializable
data class SubmitMeetingPollResponseRequest(
    val unavailableSlotIds: List<Long>
)

/** `POST /meeting-polls/{pollId}/responses`(7-12) 응답. */
@Serializable
data class SubmitMeetingPollResponseDto(
    val pollId: Long,
    val respondedCount: Int,
    val totalMembers: Int
)

/** `PATCH /meeting-polls/{pollId}/confirm`(7-13) 요청. */
@Serializable
data class ConfirmMeetingPollRequest(
    val slotId: Long,
    val force: Boolean = false
)

/** `PATCH /meeting-polls/{pollId}/confirm`(7-13) 응답. */
@Serializable
data class ConfirmMeetingPollResponseDto(
    val pollId: Long,
    val status: String,
    val scheduleId: Long
)

/** `PATCH /party-schedules/{scheduleId}/attendance`(7-20) 요청. */
@Serializable
data class UpdateAttendanceRequest(
    val attending: Boolean
)

/** `POST /projects/{projectId}/schedules/single`(7-16) 요청. */
@Serializable
data class RegisterScheduleDirectlyRequest(
    val title: String,
    val date: String,
    val startTime: String,
    val endTime: String
)

/** `POST /projects/{projectId}/schedules/single`(7-16) 응답. */
@Serializable
data class RegisterScheduleDirectlyResponseDto(
    val scheduleId: Long
)

/** `PUT /projects/{projectId}/schedule-category`(7-19) 요청. */
@Serializable
data class SetProjectScheduleCategoryRequest(
    val categoryId: Long
)
