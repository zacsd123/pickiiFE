package com.example.pickii.domain.repository

import com.example.pickii.domain.model.CalendarSchedule
import com.example.pickii.domain.model.ScheduleCategory
import kotlinx.coroutines.flow.StateFlow

/**
 * 캘린더 일정과 태그 데이터를 관리하는 저장소 인터페이스다.
 */
interface CalendarRepository {

    /**
     * 전체 일정 목록이다.
     */
    val schedules: StateFlow<List<CalendarSchedule>>

    /**
     * 전체 태그 목록이다.
     */
    val categories: StateFlow<List<ScheduleCategory>>

    /**
     * 새로운 일정을 추가한다.
     */
    fun addSchedule(
        schedule: CalendarSchedule,
    )

    /**
     * 기존 일정을 수정한다.
     */
    fun updateSchedule(
        schedule: CalendarSchedule,
    )

    /**
     * 일정 ID를 기준으로 일정을 삭제한다.
     */
    fun deleteSchedule(
        scheduleId: Long,
    )

    /**
     * 새로운 태그를 추가한다.
     */
    fun addCategory(
        category: ScheduleCategory,
    )

    /**
     * 기존 태그를 수정한다.
     */
    fun updateCategory(
        category: ScheduleCategory,
    )

    /**
     * 태그 ID를 기준으로 태그를 삭제한다.
     */
    fun deleteCategory(
        categoryId: Long,
    )

    /**
     * 새로운 일정 ID를 생성한다.
     */
    fun createScheduleId(): Long

    /**
     * 새로운 태그 ID를 생성한다.
     */
    fun createCategoryId(): Long
}