package com.example.pickii.data.remote.api

import com.example.pickii.data.remote.dto.RecurringScheduleRequest
import com.example.pickii.data.remote.dto.ScheduleCategoryUpsertRequest
import com.example.pickii.data.remote.dto.SingleScheduleRequest
import io.ktor.client.statement.HttpResponse

/** 일정 카테고리(7-1~7-4) 및 개인 일정(7-5~7-9) API. */
interface CalendarApiService {
    suspend fun getCategories(): HttpResponse

    suspend fun createCategory(request: ScheduleCategoryUpsertRequest): HttpResponse

    suspend fun updateCategory(
        categoryId: Long,
        request: ScheduleCategoryUpsertRequest
    ): HttpResponse

    suspend fun deleteCategory(categoryId: Long): HttpResponse

    suspend fun getSchedules(
        year: Int,
        month: Int
    ): HttpResponse

    suspend fun createSingleSchedule(request: SingleScheduleRequest): HttpResponse

    suspend fun createRecurringSchedule(request: RecurringScheduleRequest): HttpResponse

    suspend fun updateSingleSchedule(
        scheduleId: Long,
        request: SingleScheduleRequest
    ): HttpResponse

    suspend fun updateRecurringSchedule(
        scheduleId: Long,
        request: RecurringScheduleRequest
    ): HttpResponse

    suspend fun deleteSchedule(scheduleId: Long): HttpResponse
}
