package com.example.pickii.data.remote.api

import com.example.pickii.data.remote.dto.ApiEnvelope
import com.example.pickii.data.remote.dto.RecurringScheduleRequest
import com.example.pickii.data.remote.dto.ScheduleCategoryCreatedDto
import com.example.pickii.data.remote.dto.ScheduleCategoryDto
import com.example.pickii.data.remote.dto.ScheduleCategoryUpsertRequest
import com.example.pickii.data.remote.dto.ScheduleCreatedDto
import com.example.pickii.data.remote.dto.ScheduleDto
import com.example.pickii.data.remote.dto.SingleScheduleRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/** 일정 카테고리(7-1~7-4) 및 개인 일정(7-5~7-9) API. */
interface CalendarApiService {
    @GET("users/me/schedule-categories")
    suspend fun getCategories(): Response<ApiEnvelope<List<ScheduleCategoryDto>>>

    @POST("users/me/schedule-categories")
    suspend fun createCategory(
        @Body request: ScheduleCategoryUpsertRequest
    ): Response<ApiEnvelope<ScheduleCategoryCreatedDto>>

    @PATCH("users/me/schedule-categories/{categoryId}")
    suspend fun updateCategory(
        @Path("categoryId") categoryId: Long,
        @Body request: ScheduleCategoryUpsertRequest
    ): Response<Unit>

    @DELETE("users/me/schedule-categories/{categoryId}")
    suspend fun deleteCategory(
        @Path("categoryId") categoryId: Long
    ): Response<Unit>

    @GET("users/me/schedules")
    suspend fun getSchedules(
        @Query("year") year: Int,
        @Query("month") month: Int
    ): Response<ApiEnvelope<List<ScheduleDto>>>

    @POST("users/me/schedules/single")
    suspend fun createSingleSchedule(
        @Body request: SingleScheduleRequest
    ): Response<ApiEnvelope<ScheduleCreatedDto>>

    @POST("users/me/schedules/recurring")
    suspend fun createRecurringSchedule(
        @Body request: RecurringScheduleRequest
    ): Response<ApiEnvelope<ScheduleCreatedDto>>

    @PATCH("users/me/schedules/{scheduleId}")
    suspend fun updateSingleSchedule(
        @Path("scheduleId") scheduleId: Long,
        @Body request: SingleScheduleRequest
    ): Response<Unit>

    @PATCH("users/me/schedules/{scheduleId}")
    suspend fun updateRecurringSchedule(
        @Path("scheduleId") scheduleId: Long,
        @Body request: RecurringScheduleRequest
    ): Response<Unit>

    @DELETE("users/me/schedules/{scheduleId}")
    suspend fun deleteSchedule(
        @Path("scheduleId") scheduleId: Long
    ): Response<Unit>
}
