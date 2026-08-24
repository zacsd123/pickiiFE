package com.example.pickii.data.remote.api

import com.example.pickii.data.remote.dto.RecurringScheduleRequest
import com.example.pickii.data.remote.dto.ScheduleCategoryUpsertRequest
import com.example.pickii.data.remote.dto.SingleScheduleRequest
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse

/** [CalendarApiService]를 Ktor [HttpClient]로 구현한다. */
class KtorCalendarApiService(
    private val client: HttpClient
) : CalendarApiService {
    override suspend fun getCategories(): HttpResponse = client.get("users/me/schedule-categories")

    override suspend fun createCategory(request: ScheduleCategoryUpsertRequest): HttpResponse =
        client.post("users/me/schedule-categories") {
            setBody(request)
        }

    override suspend fun updateCategory(
        categoryId: Long,
        request: ScheduleCategoryUpsertRequest
    ): HttpResponse =
        client.patch("users/me/schedule-categories/$categoryId") {
            setBody(request)
        }

    override suspend fun deleteCategory(categoryId: Long): HttpResponse = client.delete("users/me/schedule-categories/$categoryId")

    override suspend fun getSchedules(
        year: Int,
        month: Int
    ): HttpResponse =
        client.get("users/me/schedules") {
            parameter("year", year)
            parameter("month", month)
        }

    override suspend fun createSingleSchedule(request: SingleScheduleRequest): HttpResponse =
        client.post("users/me/schedules/single") {
            setBody(request)
        }

    override suspend fun createRecurringSchedule(request: RecurringScheduleRequest): HttpResponse =
        client.post("users/me/schedules/recurring") {
            setBody(request)
        }

    override suspend fun updateSingleSchedule(
        scheduleId: Long,
        request: SingleScheduleRequest
    ): HttpResponse =
        client.patch("users/me/schedules/$scheduleId") {
            setBody(request)
        }

    override suspend fun updateRecurringSchedule(
        scheduleId: Long,
        request: RecurringScheduleRequest
    ): HttpResponse =
        client.patch("users/me/schedules/$scheduleId") {
            setBody(request)
        }

    override suspend fun deleteSchedule(scheduleId: Long): HttpResponse = client.delete("users/me/schedules/$scheduleId")
}
