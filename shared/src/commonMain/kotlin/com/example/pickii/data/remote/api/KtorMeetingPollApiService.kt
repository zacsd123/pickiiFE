package com.example.pickii.data.remote.api

import com.example.pickii.data.remote.dto.ConfirmMeetingPollRequest
import com.example.pickii.data.remote.dto.CreateMeetingPollRequest
import com.example.pickii.data.remote.dto.RegisterScheduleDirectlyRequest
import com.example.pickii.data.remote.dto.SetProjectScheduleCategoryRequest
import com.example.pickii.data.remote.dto.SubmitMeetingPollResponseRequest
import com.example.pickii.data.remote.dto.UpdateAttendanceRequest
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse

/** [MeetingPollApiService]를 Ktor [HttpClient]로 구현한다. */
class KtorMeetingPollApiService(
    private val client: HttpClient
) : MeetingPollApiService {
    override suspend fun createPoll(
        projectId: Long,
        request: CreateMeetingPollRequest
    ): HttpResponse =
        client.post("projects/$projectId/meeting-polls") {
            setBody(request)
        }

    override suspend fun getPoll(pollId: Long): HttpResponse = client.get("meeting-polls/$pollId")

    override suspend fun submitResponse(
        pollId: Long,
        request: SubmitMeetingPollResponseRequest
    ): HttpResponse =
        client.post("meeting-polls/$pollId/responses") {
            setBody(request)
        }

    override suspend fun confirmPoll(
        pollId: Long,
        request: ConfirmMeetingPollRequest
    ): HttpResponse =
        client.patch("meeting-polls/$pollId/confirm") {
            setBody(request)
        }

    override suspend fun cancelPoll(pollId: Long): HttpResponse = client.delete("meeting-polls/$pollId")

    override suspend fun getTeamSchedules(
        projectId: Long,
        year: Int,
        month: Int
    ): HttpResponse =
        client.get("projects/$projectId/schedules") {
            parameter("year", year)
            parameter("month", month)
        }

    override suspend fun deleteTeamSchedule(scheduleId: Long): HttpResponse = client.delete("party-schedules/$scheduleId")

    override suspend fun updateAttendance(
        scheduleId: Long,
        request: UpdateAttendanceRequest
    ): HttpResponse =
        client.patch("party-schedules/$scheduleId/attendance") {
            setBody(request)
        }

    override suspend fun registerScheduleDirectly(
        projectId: Long,
        request: RegisterScheduleDirectlyRequest
    ): HttpResponse =
        client.post("projects/$projectId/schedules/single") {
            setBody(request)
        }

    override suspend fun setProjectScheduleColor(
        projectId: Long,
        request: SetProjectScheduleCategoryRequest
    ): HttpResponse =
        client.put("projects/$projectId/schedule-category") {
            setBody(request)
        }
}
