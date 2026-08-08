package com.example.pickii.data.remote.api

import com.example.pickii.data.remote.dto.ApiEnvelope
import com.example.pickii.data.remote.dto.ConfirmMeetingPollRequest
import com.example.pickii.data.remote.dto.ConfirmMeetingPollResponseDto
import com.example.pickii.data.remote.dto.CreateMeetingPollRequest
import com.example.pickii.data.remote.dto.MeetingPollCreatedDto
import com.example.pickii.data.remote.dto.MeetingPollDetailDto
import com.example.pickii.data.remote.dto.RegisterScheduleDirectlyRequest
import com.example.pickii.data.remote.dto.RegisterScheduleDirectlyResponseDto
import com.example.pickii.data.remote.dto.ScheduleDto
import com.example.pickii.data.remote.dto.SetProjectScheduleCategoryRequest
import com.example.pickii.data.remote.dto.SubmitMeetingPollResponseDto
import com.example.pickii.data.remote.dto.SubmitMeetingPollResponseRequest
import com.example.pickii.data.remote.dto.UpdateAttendanceRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/** `7-C 팀 일정(회의 일정 조율)` API(7-10~7-20). */
interface MeetingPollApiService {
    @POST("projects/{projectId}/meeting-polls")
    suspend fun createPoll(
        @Path("projectId") projectId: Long,
        @Body request: CreateMeetingPollRequest
    ): Response<ApiEnvelope<MeetingPollCreatedDto>>

    @GET("meeting-polls/{pollId}")
    suspend fun getPoll(
        @Path("pollId") pollId: Long
    ): Response<ApiEnvelope<MeetingPollDetailDto>>

    @POST("meeting-polls/{pollId}/responses")
    suspend fun submitResponse(
        @Path("pollId") pollId: Long,
        @Body request: SubmitMeetingPollResponseRequest
    ): Response<ApiEnvelope<SubmitMeetingPollResponseDto>>

    @PATCH("meeting-polls/{pollId}/confirm")
    suspend fun confirmPoll(
        @Path("pollId") pollId: Long,
        @Body request: ConfirmMeetingPollRequest
    ): Response<ApiEnvelope<ConfirmMeetingPollResponseDto>>

    @DELETE("meeting-polls/{pollId}")
    suspend fun cancelPoll(
        @Path("pollId") pollId: Long
    ): Response<Unit>

    @GET("projects/{projectId}/schedules")
    suspend fun getTeamSchedules(
        @Path("projectId") projectId: Long,
        @Query("year") year: Int,
        @Query("month") month: Int
    ): Response<ApiEnvelope<List<ScheduleDto>>>

    @DELETE("party-schedules/{scheduleId}")
    suspend fun deleteTeamSchedule(
        @Path("scheduleId") scheduleId: Long
    ): Response<Unit>

    @PATCH("party-schedules/{scheduleId}/attendance")
    suspend fun updateAttendance(
        @Path("scheduleId") scheduleId: Long,
        @Body request: UpdateAttendanceRequest
    ): Response<Unit>

    @POST("projects/{projectId}/schedules/single")
    suspend fun registerScheduleDirectly(
        @Path("projectId") projectId: Long,
        @Body request: RegisterScheduleDirectlyRequest
    ): Response<ApiEnvelope<RegisterScheduleDirectlyResponseDto>>

    @PUT("projects/{projectId}/schedule-category")
    suspend fun setProjectScheduleColor(
        @Path("projectId") projectId: Long,
        @Body request: SetProjectScheduleCategoryRequest
    ): Response<Unit>
}
