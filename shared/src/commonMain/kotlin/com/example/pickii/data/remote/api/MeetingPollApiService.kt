package com.example.pickii.data.remote.api

import com.example.pickii.data.remote.dto.ConfirmMeetingPollRequest
import com.example.pickii.data.remote.dto.CreateMeetingPollRequest
import com.example.pickii.data.remote.dto.RegisterScheduleDirectlyRequest
import com.example.pickii.data.remote.dto.SetProjectScheduleCategoryRequest
import com.example.pickii.data.remote.dto.SubmitMeetingPollResponseRequest
import com.example.pickii.data.remote.dto.UpdateAttendanceRequest
import io.ktor.client.statement.HttpResponse

/** `7-C 팀 일정(회의 일정 조율)` API(7-10~7-20). */
interface MeetingPollApiService {
    suspend fun createPoll(
        projectId: Long,
        request: CreateMeetingPollRequest
    ): HttpResponse

    suspend fun getPoll(pollId: Long): HttpResponse

    suspend fun submitResponse(
        pollId: Long,
        request: SubmitMeetingPollResponseRequest
    ): HttpResponse

    suspend fun confirmPoll(
        pollId: Long,
        request: ConfirmMeetingPollRequest
    ): HttpResponse

    suspend fun cancelPoll(pollId: Long): HttpResponse

    suspend fun getTeamSchedules(
        projectId: Long,
        year: Int,
        month: Int
    ): HttpResponse

    suspend fun deleteTeamSchedule(scheduleId: Long): HttpResponse

    suspend fun updateAttendance(
        scheduleId: Long,
        request: UpdateAttendanceRequest
    ): HttpResponse

    suspend fun registerScheduleDirectly(
        projectId: Long,
        request: RegisterScheduleDirectlyRequest
    ): HttpResponse

    suspend fun setProjectScheduleColor(
        projectId: Long,
        request: SetProjectScheduleCategoryRequest
    ): HttpResponse
}
