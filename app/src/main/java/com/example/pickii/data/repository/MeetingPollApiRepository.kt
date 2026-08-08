package com.example.pickii.data.repository

import com.example.pickii.data.remote.api.MeetingPollApiService
import com.example.pickii.data.remote.dto.ConfirmMeetingPollRequest
import com.example.pickii.data.remote.dto.CreateMeetingPollRequest
import com.example.pickii.data.remote.dto.MeetingPollDetailDto
import com.example.pickii.data.remote.dto.MeetingPollSlotDto
import com.example.pickii.data.remote.dto.RegisterScheduleDirectlyRequest
import com.example.pickii.data.remote.dto.ScheduleDto
import com.example.pickii.data.remote.dto.SetProjectScheduleCategoryRequest
import com.example.pickii.data.remote.dto.SubmitMeetingPollResponseRequest
import com.example.pickii.data.remote.dto.UpdateAttendanceRequest
import com.example.pickii.domain.model.MeetingPollCreated
import com.example.pickii.domain.model.MeetingPollDetail
import com.example.pickii.domain.model.MeetingPollSlot
import com.example.pickii.domain.model.MeetingPollStatus
import com.example.pickii.domain.model.TeamSchedule
import com.example.pickii.domain.repository.MeetingPollRepository
import com.example.pickii.util.network.safeApiCall
import com.example.pickii.util.network.safeApiCallUnit
import com.example.pickii.util.parseIsoOffsetDateTime
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

private val TimeFormat = DateTimeFormatter.ofPattern("HH:mm")

/** `7-C 팀 일정(회의 일정 조율)` API로 [MeetingPollRepository]를 구현한다. DTO ↔ 도메인 매핑을 전담한다. */
@Singleton
class MeetingPollApiRepository
    @Inject
    constructor(
        private val apiService: MeetingPollApiService,
        private val json: Json
    ) : MeetingPollRepository {
        override suspend fun createPoll(
            projectId: Long,
            title: String,
            durationMin: Int,
            rangeStart: LocalDate,
            rangeEnd: LocalDate,
            dayStart: LocalTime,
            dayEnd: LocalTime,
            deadlineHours: Int?,
            memberIds: Set<Long>
        ): Result<MeetingPollCreated> =
            safeApiCall(json) {
                apiService.createPoll(
                    projectId,
                    CreateMeetingPollRequest(
                        title = title,
                        durationMin = durationMin,
                        rangeStart = rangeStart.toString(),
                        rangeEnd = rangeEnd.toString(),
                        dayStart = dayStart.format(TimeFormat),
                        dayEnd = dayEnd.format(TimeFormat),
                        deadlineHours = deadlineHours,
                        memberIds = memberIds.toList().ifEmpty { null }
                    )
                )
            }.map { envelope ->
                MeetingPollCreated(
                    pollId = envelope.data.pollId,
                    deadline = parseIsoDateTime(envelope.data.deadline),
                    slotCount = envelope.data.slotCount
                )
            }

        override suspend fun getPoll(pollId: Long): Result<MeetingPollDetail> =
            safeApiCall(json) { apiService.getPoll(pollId) }.map { it.data.toDomain() }

        override suspend fun submitResponse(
            pollId: Long,
            unavailableSlotIds: List<Long>
        ): Result<Unit> =
            safeApiCall(json) {
                apiService.submitResponse(pollId, SubmitMeetingPollResponseRequest(unavailableSlotIds))
            }.map { }

        override suspend fun confirmPoll(
            pollId: Long,
            slotId: Long,
            force: Boolean
        ): Result<Long> =
            safeApiCall(json) {
                apiService.confirmPoll(pollId, ConfirmMeetingPollRequest(slotId = slotId, force = force))
            }.map { it.data.scheduleId }

        override suspend fun cancelPoll(pollId: Long): Result<Unit> =
            safeApiCallUnit(json) { apiService.cancelPoll(pollId) }

        override suspend fun getTeamSchedules(
            projectId: Long,
            yearMonth: YearMonth
        ): Result<List<TeamSchedule>> =
            safeApiCall(json) {
                apiService.getTeamSchedules(projectId, yearMonth.year, yearMonth.monthValue)
            }.map { envelope -> envelope.data.map { it.toDomain() } }

        override suspend fun deleteTeamSchedule(scheduleId: Long): Result<Unit> =
            safeApiCallUnit(json) { apiService.deleteTeamSchedule(scheduleId) }

        override suspend fun updateAttendance(
            scheduleId: Long,
            attending: Boolean
        ): Result<Unit> =
            safeApiCallUnit(json) {
                apiService.updateAttendance(scheduleId, UpdateAttendanceRequest(attending))
            }

        override suspend fun registerScheduleDirectly(
            projectId: Long,
            title: String,
            date: LocalDate,
            startTime: LocalTime,
            endTime: LocalTime
        ): Result<Long> =
            safeApiCall(json) {
                apiService.registerScheduleDirectly(
                    projectId,
                    RegisterScheduleDirectlyRequest(
                        title = title,
                        date = date.toString(),
                        startTime = startTime.format(TimeFormat),
                        endTime = endTime.format(TimeFormat)
                    )
                )
            }.map { it.data.scheduleId }

        override suspend fun setProjectScheduleColor(
            projectId: Long,
            categoryId: Long
        ): Result<Unit> =
            safeApiCallUnit(json) {
                apiService.setProjectScheduleColor(projectId, SetProjectScheduleCategoryRequest(categoryId))
            }

        private fun MeetingPollDetailDto.toDomain(): MeetingPollDetail =
            MeetingPollDetail(
                pollId = pollId,
                title = title,
                status = status.toMeetingPollStatus(),
                deadline = parseIsoDateTime(deadline),
                totalMembers = totalMembers,
                respondedCount = respondedCount,
                myResponded = myResponded,
                slots = slots.map { it.toDomain() }
            )

        private fun MeetingPollSlotDto.toDomain(): MeetingPollSlot =
            MeetingPollSlot(
                slotId = slotId,
                startAt = parseIsoDateTime(startAt),
                endAt = parseIsoDateTime(endAt),
                myAvailable = myAvailable,
                availableCount = availableCount,
                unansweredCount = unansweredCount
            )

        private fun ScheduleDto.toDomain(): TeamSchedule =
            TeamSchedule(
                scheduleId = scheduleId,
                title = title,
                startDate = (date ?: startDate)?.let(::parseIsoDate) ?: LocalDate.now(),
                endDate = (date ?: endDate)?.let(::parseIsoDate) ?: LocalDate.now(),
                startTime = startTime?.let { parseTime(it) },
                endTime = endTime?.let { parseTime(it) }
            )
    }

private fun String.toMeetingPollStatus(): MeetingPollStatus =
    runCatching { MeetingPollStatus.valueOf(this) }.getOrDefault(MeetingPollStatus.COLLECTING)

private fun parseIsoDateTime(value: String): LocalDateTime =
    runCatching { parseIsoOffsetDateTime(value) }.getOrDefault(LocalDateTime.now())

private fun parseIsoDate(value: String): LocalDate =
    runCatching {
        LocalDate.parse(value)
    }.getOrDefault(LocalDate.now())

private fun parseTime(value: String): LocalTime? = runCatching { LocalTime.parse(value, TimeFormat) }.getOrNull()
