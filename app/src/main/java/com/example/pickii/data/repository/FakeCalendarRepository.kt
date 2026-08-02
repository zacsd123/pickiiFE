package com.example.pickii.data.repository

import com.example.pickii.domain.model.CalendarSchedule
import com.example.pickii.domain.model.ScheduleCategory
import com.example.pickii.domain.model.ScheduleColorType
import com.example.pickii.domain.model.ScheduleRepeatType
import com.example.pickii.domain.repository.CalendarRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 백엔드 연동 전 캘린더 데이터를 메모리에서 관리하는 임시 저장소다.
 */
@Singleton
class FakeCalendarRepository
    @Inject
    constructor() : CalendarRepository {
        private val _categories =
            MutableStateFlow(
                createInitialCategories()
            )

        override val categories: StateFlow<List<ScheduleCategory>> =
            _categories.asStateFlow()

        private val _schedules =
            MutableStateFlow(
                createInitialSchedules()
            )

        override val schedules: StateFlow<List<CalendarSchedule>> =
            _schedules.asStateFlow()

        override fun addSchedule(schedule: CalendarSchedule) {
            _schedules.value = _schedules.value + schedule
        }

        override fun updateSchedule(schedule: CalendarSchedule) {
            _schedules.value =
                _schedules.value.map { currentSchedule ->
                    if (currentSchedule.id == schedule.id) {
                        schedule
                    } else {
                        currentSchedule
                    }
                }
        }

        override fun deleteSchedule(scheduleId: Long) {
            _schedules.value =
                _schedules.value.filterNot { schedule ->
                    schedule.id == scheduleId
                }
        }

        override fun addCategory(category: ScheduleCategory) {
            _categories.value = _categories.value + category
        }

        override fun updateCategory(category: ScheduleCategory) {
            _categories.value =
                _categories.value.map { currentCategory ->
                    if (currentCategory.id == category.id) {
                        category
                    } else {
                        currentCategory
                    }
                }
        }

        override fun deleteCategory(categoryId: Long) {
            _categories.value =
                _categories.value.filterNot { category ->
                    category.id == categoryId
                }

            _schedules.value =
                _schedules.value.map { schedule ->
                    if (schedule.categoryId == categoryId) {
                        schedule.copy(
                            categoryId = null
                        )
                    } else {
                        schedule
                    }
                }
        }

        override fun createScheduleId(): Long =
            (
                _schedules.value.maxOfOrNull { schedule ->
                    schedule.id
                } ?: 0L
            ) + 1L

        override fun createCategoryId(): Long =
            (
                _categories.value.maxOfOrNull { category ->
                    category.id
                } ?: 0L
            ) + 1L

        /**
         * 화면 확인용 기본 태그를 만든다.
         */
        private fun createInitialCategories(): List<ScheduleCategory> =
            listOf(
                ScheduleCategory(
                    id = 1L,
                    name = "알바",
                    color = ScheduleColorType.RED
                ),
                ScheduleCategory(
                    id = 2L,
                    name = "개인",
                    color = ScheduleColorType.PURPLE
                ),
                ScheduleCategory(
                    id = 3L,
                    name = "약속",
                    color = ScheduleColorType.GREEN
                )
            )

        /**
         * 화면 확인용 기본 일정을 만든다.
         */
        private fun createInitialSchedules(): List<CalendarSchedule> =
            listOf(
                CalendarSchedule(
                    id = 1L,
                    title = "아르바이트",
                    categoryId = 1L,
                    startDate = LocalDate.of(2026, 7, 4),
                    endDate = LocalDate.of(2026, 7, 4),
                    startTime = LocalTime.of(9, 0),
                    endTime = LocalTime.of(18, 0),
                    location = "스타벅스 강남점",
                    repeatType = ScheduleRepeatType.WEEKLY,
                    repeatWeekdays =
                        setOf(
                            DayOfWeek.SATURDAY,
                            DayOfWeek.SUNDAY
                        ),
                    memo = "머리끈 꼭 챙기기. 오픈 담당이라 8:50까지 도착해야 함.",
                    isAllDay = false
                ),
                CalendarSchedule(
                    id = 2L,
                    title = "아르바이트",
                    categoryId = 1L,
                    startDate = LocalDate.of(2026, 7, 4),
                    endDate = LocalDate.of(2026, 7, 4),
                    startTime = LocalTime.of(19, 0),
                    endTime = LocalTime.of(22, 0),
                    location = "카페",
                    repeatType = ScheduleRepeatType.NONE,
                    memo = "",
                    isAllDay = false
                ),
                CalendarSchedule(
                    id = 3L,
                    title = "프로젝트 진행 기간",
                    categoryId = 3L,
                    startDate = LocalDate.of(2026, 7, 6),
                    endDate = LocalDate.of(2026, 7, 10),
                    startTime = null,
                    endTime = null,
                    location = "",
                    repeatType = ScheduleRepeatType.NONE,
                    memo = "",
                    isAllDay = true
                )
            )
    }
