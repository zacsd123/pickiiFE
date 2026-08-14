package com.example.pickii.domain.repository

import com.example.pickii.domain.model.CalendarSchedule
import com.example.pickii.domain.model.ScheduleCategory
import com.example.pickii.domain.model.ScheduleColorType
import kotlinx.coroutines.flow.StateFlow
import java.time.YearMonth

/**
 * 캘린더 일정 카테고리(7-1~7-4)와 개인 일정(7-5~7-9)을 관리하는 저장소 인터페이스다.
 *
 * [schedules]/[categories]는 지금까지 [loadSchedules]/[loadCategories]로 불러온 결과를 누적한 캐시다 —
 * 실제 목록은 서버에서 오므로, 화면은 필요한 시점에 로드 함수를 호출해 채워야 한다.
 */
interface CalendarRepository {
    /** 지금까지 불러온 일정 목록(누적 캐시)이다. */
    val schedules: StateFlow<List<CalendarSchedule>>

    /** 전체 태그 목록이다. */
    val categories: StateFlow<List<ScheduleCategory>>

    /** 해당 연/월의 일정을 서버에서 불러와 [schedules] 캐시에 병합한다(7-5). */
    suspend fun loadSchedules(yearMonth: YearMonth): Result<Unit>

    /** 태그 목록을 서버에서 불러온다(7-1). */
    suspend fun loadCategories(): Result<Unit>

    /** 새로운 일정을 추가한다(7-6 단발 / 7-7 반복, [CalendarSchedule.repeatType] 기준). */
    suspend fun addSchedule(schedule: CalendarSchedule): Result<Unit>

    /** 기존 일정을 수정한다(7-8). */
    suspend fun updateSchedule(schedule: CalendarSchedule): Result<Unit>

    /** 일정 ID를 기준으로 일정을 삭제한다(7-9). */
    suspend fun deleteSchedule(scheduleId: Long): Result<Unit>

    /** 새로운 태그를 추가한다(7-2). */
    suspend fun addCategory(
        name: String,
        color: ScheduleColorType
    ): Result<Unit>

    /** 기존 태그를 수정한다(7-3). */
    suspend fun updateCategory(
        categoryId: Long,
        name: String,
        color: ScheduleColorType
    ): Result<Unit>

    /** 태그 ID를 기준으로 태그를 삭제한다(7-4). */
    suspend fun deleteCategory(categoryId: Long): Result<Unit>
}
