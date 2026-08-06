package com.example.pickii.data.remote.dto

import kotlinx.serialization.Serializable

/** `GET/POST/PATCH /users/me/schedule-categories`(7-1~7-3) 응답 항목. */
@Serializable
data class ScheduleCategoryDto(
    val categoryId: Long,
    val title: String,
    val color: String
)

/** `POST /users/me/schedule-categories`(7-2) 요청, `PATCH .../{categoryId}`(7-3) 요청. */
@Serializable
data class ScheduleCategoryUpsertRequest(
    val title: String,
    val color: String
)

/** `POST /users/me/schedule-categories`(7-2) 응답. */
@Serializable
data class ScheduleCategoryCreatedDto(
    val categoryId: Long
)

/** `GET /users/me/schedules`(7-5) 응답에 포함된 카테고리 정보. */
@Serializable
data class ScheduleCategoryRefDto(
    val categoryId: Long,
    val title: String,
    val color: String
)

/**
 * `GET /users/me/schedules`(7-5) 응답 항목. 단발 일정은 [date]만, 반복 일정은
 * [startDate]/[endDate]/[rrule]을 사용한다([isRecurring]으로 구분).
 */
@Serializable
data class ScheduleDto(
    val scheduleId: Long,
    val title: String,
    val isRecurring: Boolean,
    val date: String? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val startTime: String? = null,
    val endTime: String? = null,
    val rrule: String? = null,
    val content: String? = null,
    val category: ScheduleCategoryRefDto? = null
)

/** `POST /users/me/schedules/single`(7-6) 요청, `PATCH /users/me/schedules/{scheduleId}`(7-8) 단발 요청. */
@Serializable
data class SingleScheduleRequest(
    val title: String,
    val date: String,
    val startTime: String,
    val endTime: String,
    val content: String? = null,
    val categoryId: Long? = null
)

/** `POST /users/me/schedules/recurring`(7-7) 요청, `PATCH /users/me/schedules/{scheduleId}`(7-8) 반복 요청. */
@Serializable
data class RecurringScheduleRequest(
    val title: String,
    val startDate: String,
    val endDate: String,
    val startTime: String,
    val endTime: String,
    val rrule: String,
    val content: String? = null,
    val categoryId: Long? = null
)

/** `POST /users/me/schedules/single`(7-6), `POST /users/me/schedules/recurring`(7-7) 응답. */
@Serializable
data class ScheduleCreatedDto(
    val scheduleId: Long
)
