package com.example.pickii.domain.repository

import com.example.pickii.domain.model.MeetingPollCreated
import com.example.pickii.domain.model.MeetingPollDetail
import com.example.pickii.domain.model.TeamSchedule
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.YearMonth

/** `7-C 팀 일정(회의 일정 조율)` API(7-10~7-20)를 다룬다. */
interface MeetingPollRepository {
    /** 회의 조율을 개설한다(7-10). [deadlineHours]는 null이면 서버 기본값(12시간), [memberIds]는 null/빈 목록이면 전원. */
    @Suppress("LongParameterList")
    suspend fun createPoll(
        projectId: Long,
        title: String,
        durationMin: Int,
        rangeStart: LocalDate,
        rangeEnd: LocalDate,
        dayStart: LocalTime,
        dayEnd: LocalTime,
        deadlineHours: Int? = null,
        memberIds: Set<Long> = emptySet()
    ): Result<MeetingPollCreated>

    /** 응답 화면(슬롯 + 내 응답 상태)을 조회한다(7-11). */
    suspend fun getPoll(pollId: Long): Result<MeetingPollDetail>

    /** 불가능한 슬롯만 골라 응답을 제출한다(7-12). */
    suspend fun submitResponse(
        pollId: Long,
        unavailableSlotIds: List<Long>
    ): Result<Unit>

    /** 최종 슬롯을 확정한다(7-13). 성공 시 생성된 팀 일정 id를 반환한다. */
    suspend fun confirmPoll(
        pollId: Long,
        slotId: Long,
        force: Boolean
    ): Result<Long>

    /** 조율을 취소/재조율한다(7-14). */
    suspend fun cancelPoll(pollId: Long): Result<Unit>

    /** 확정된 팀 일정 목록을 조회한다(7-15). */
    suspend fun getTeamSchedules(
        projectId: Long,
        yearMonth: YearMonth
    ): Result<List<TeamSchedule>>

    /** 팀 일정을 삭제한다(7-18). */
    suspend fun deleteTeamSchedule(scheduleId: Long): Result<Unit>

    /** 회의 참석/불참 여부를 변경한다(7-20). */
    suspend fun updateAttendance(
        scheduleId: Long,
        attending: Boolean
    ): Result<Unit>

    /** 조율 없이 팀 일정을 직접 등록한다(7-16, 프로젝트장 전용, 단발 일정만 지원). 성공 시 생성된 일정 id를 반환한다. */
    suspend fun registerScheduleDirectly(
        projectId: Long,
        title: String,
        date: LocalDate,
        startTime: LocalTime,
        endTime: LocalTime
    ): Result<Long>

    /** 이 프로젝트의 팀 일정이 내 캘린더에서 보일 색상(카테고리)을 지정한다(7-19). */
    suspend fun setProjectScheduleColor(
        projectId: Long,
        categoryId: Long
    ): Result<Unit>
}
