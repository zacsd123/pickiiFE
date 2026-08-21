package com.example.pickii.data.repository

import com.example.pickii.data.remote.api.CalendarApiService
import com.example.pickii.data.remote.dto.RecurringScheduleRequest
import com.example.pickii.data.remote.dto.ScheduleCategoryDto
import com.example.pickii.data.remote.dto.ScheduleCategoryUpsertRequest
import com.example.pickii.data.remote.dto.ScheduleDto
import com.example.pickii.data.remote.dto.SingleScheduleRequest
import com.example.pickii.domain.model.CalendarSchedule
import com.example.pickii.domain.model.ScheduleCategory
import com.example.pickii.domain.model.ScheduleColorType
import com.example.pickii.domain.model.ScheduleRepeatType
import com.example.pickii.domain.repository.CalendarRepository
import com.example.pickii.util.network.safeApiCall
import com.example.pickii.util.network.safeApiCallUnit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.json.Json
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.YearMonth
import kotlinx.datetime.format
import kotlinx.datetime.format.char
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.number
import com.example.pickii.util.today
import javax.inject.Inject
import javax.inject.Singleton

/** API가 요구하는 필수 시작/종료 시간이 없는 "하루 종일" 일정에 사용하는 시간 범위. */
private val ALL_DAY_START = LocalTime(0, 0)
private val ALL_DAY_END = LocalTime(23, 59)
private val TimeFormat =
    LocalTime.Format {
        hour()
        char(':')
        minute()
    }

/** `7-1`~`7-9` API로 [CalendarRepository]를 구현한다. */
@Singleton
class CalendarApiRepository
    @Inject
    constructor(
        private val apiService: CalendarApiService,
        private val json: Json
    ) : CalendarRepository {
        private val _schedules = MutableStateFlow<List<CalendarSchedule>>(emptyList())
        override val schedules: StateFlow<List<CalendarSchedule>> = _schedules.asStateFlow()

        private val _categories = MutableStateFlow<List<ScheduleCategory>>(emptyList())
        override val categories: StateFlow<List<ScheduleCategory>> = _categories.asStateFlow()

        override suspend fun loadSchedules(yearMonth: YearMonth): Result<Unit> =
            safeApiCall(json) {
                apiService.getSchedules(yearMonth.year, yearMonth.month.number)
            }.map { envelope ->
                val fetched = envelope.data.map { it.toDomain() }
                _schedules.update { current -> current.mergeById(fetched) { it.id } }
            }

        override suspend fun loadCategories(): Result<Unit> =
            safeApiCall(json) { apiService.getCategories() }.map { envelope ->
                _categories.value = envelope.data.map { it.toDomain() }
            }

        override suspend fun addSchedule(schedule: CalendarSchedule): Result<Unit> =
            if (schedule.isSingleDay) {
                safeApiCall(json) { apiService.createSingleSchedule(schedule.toSingleRequest()) }
                    .map { created -> _schedules.update { it + schedule.copy(id = created.data.scheduleId) } }
            } else {
                safeApiCall(json) { apiService.createRecurringSchedule(schedule.toRecurringRequest()) }
                    .map { created -> _schedules.update { it + schedule.copy(id = created.data.scheduleId) } }
            }

        override suspend fun updateSchedule(schedule: CalendarSchedule): Result<Unit> =
            if (schedule.isSingleDay) {
                safeApiCallUnit(json) { apiService.updateSingleSchedule(schedule.id, schedule.toSingleRequest()) }
                    .onSuccess { _schedules.update { current -> current.mergeById(listOf(schedule)) { it.id } } }
            } else {
                safeApiCallUnit(json) { apiService.updateRecurringSchedule(schedule.id, schedule.toRecurringRequest()) }
                    .onSuccess { _schedules.update { current -> current.mergeById(listOf(schedule)) { it.id } } }
            }

        override suspend fun deleteSchedule(scheduleId: Long): Result<Unit> =
            safeApiCallUnit(json) { apiService.deleteSchedule(scheduleId) }
                .onSuccess { _schedules.update { current -> current.filterNot { it.id == scheduleId } } }

        override suspend fun addCategory(
            name: String,
            color: ScheduleColorType
        ): Result<Unit> =
            safeApiCall(json) {
                apiService.createCategory(ScheduleCategoryUpsertRequest(title = name, color = color.toHex()))
            }.map { created ->
                _categories.update { it + ScheduleCategory(id = created.data.categoryId, name = name, color = color) }
            }

        override suspend fun updateCategory(
            categoryId: Long,
            name: String,
            color: ScheduleColorType
        ): Result<Unit> =
            safeApiCallUnit(json) {
                val request = ScheduleCategoryUpsertRequest(title = name, color = color.toHex())
                apiService.updateCategory(categoryId, request)
            }.onSuccess {
                _categories.update { current ->
                    current.map { if (it.id == categoryId) ScheduleCategory(categoryId, name, color) else it }
                }
            }

        override suspend fun deleteCategory(categoryId: Long): Result<Unit> =
            safeApiCallUnit(json) { apiService.deleteCategory(categoryId) }
                .onSuccess {
                    _categories.update { current -> current.filterNot { it.id == categoryId } }
                    _schedules.update { current ->
                        current.map { if (it.categoryId == categoryId) it.copy(categoryId = null) else it }
                    }
                }

        /**
         * 반복이 없고(startDate == endDate) 하루짜리인 일정만 단발(`/single`) 엔드포인트로 보낸다.
         * 그 외(진짜 반복이거나, 반복 없이 기간만 있는 경우)는 반복(`/recurring`) 엔드포인트로 보낸다.
         */
        private val CalendarSchedule.isSingleDay: Boolean
            get() = repeatType == ScheduleRepeatType.NONE && startDate == endDate

        private fun CalendarSchedule.toSingleRequest(): SingleScheduleRequest =
            SingleScheduleRequest(
                title = title,
                date = startDate.toIsoString(),
                startTime = (if (isAllDay) ALL_DAY_START else startTime ?: ALL_DAY_START).format(TimeFormat),
                endTime = (if (isAllDay) ALL_DAY_END else endTime ?: ALL_DAY_END).format(TimeFormat),
                content = memo.ifBlank { null },
                categoryId = categoryId
            )

        private fun CalendarSchedule.toRecurringRequest(): RecurringScheduleRequest =
            RecurringScheduleRequest(
                title = title,
                startDate = startDate.toIsoString(),
                endDate = endDate.toIsoString(),
                startTime = (if (isAllDay) ALL_DAY_START else startTime ?: ALL_DAY_START).format(TimeFormat),
                endTime = (if (isAllDay) ALL_DAY_END else endTime ?: ALL_DAY_END).format(TimeFormat),
                rrule = buildRrule(repeatType, repeatWeekdays, startDate.dayOfWeek),
                content = memo.ifBlank { null },
                categoryId = categoryId
            )

        private fun ScheduleDto.toDomain(): CalendarSchedule {
            val (repeatType, repeatWeekdays) = parseRrule(rrule)
            val start = (date ?: startDate)?.let(LocalDate::parse) ?: today()
            val end = (date ?: endDate)?.let(LocalDate::parse) ?: start
            val parsedStartTime = startTime?.let { runCatching { LocalTime.parse(it, TimeFormat) }.getOrNull() }
            val parsedEndTime = endTime?.let { runCatching { LocalTime.parse(it, TimeFormat) }.getOrNull() }
            val isAllDay = parsedStartTime == ALL_DAY_START && parsedEndTime == ALL_DAY_END
            return CalendarSchedule(
                id = scheduleId,
                title = title,
                categoryId = category?.categoryId,
                startDate = start,
                endDate = end,
                startTime = if (isAllDay) null else parsedStartTime,
                endTime = if (isAllDay) null else parsedEndTime,
                location = "",
                repeatType = repeatType,
                repeatWeekdays = repeatWeekdays,
                memo = content.orEmpty(),
                isAllDay = isAllDay
            )
        }

        private fun ScheduleCategoryDto.toDomain(): ScheduleCategory =
            ScheduleCategory(id = categoryId, name = title, color = color.toScheduleColorType())
    }

private fun LocalDate.toIsoString(): String = toString()

/** [id]가 같은 항목을 [updates]로 교체하고, 없던 항목은 새로 추가한다. */
private fun <T> List<T>.mergeById(
    updates: List<T>,
    id: (T) -> Any
): List<T> {
    val byId = associateBy(id).toMutableMap()
    updates.forEach { byId[id(it)] = it }
    return byId.values.toList()
}

private val RRULE_DAY_CODES =
    mapOf(
        DayOfWeek.MONDAY to "MO",
        DayOfWeek.TUESDAY to "TU",
        DayOfWeek.WEDNESDAY to "WE",
        DayOfWeek.THURSDAY to "TH",
        DayOfWeek.FRIDAY to "FR",
        DayOfWeek.SATURDAY to "SA",
        DayOfWeek.SUNDAY to "SU"
    )

private fun buildRrule(
    repeatType: ScheduleRepeatType,
    repeatWeekdays: Set<DayOfWeek>,
    fallbackDay: DayOfWeek
): String =
    when (repeatType) {
        ScheduleRepeatType.DAILY -> "FREQ=DAILY"
        ScheduleRepeatType.WEEKLY -> {
            val days = repeatWeekdays.ifEmpty { setOf(fallbackDay) }
            val byDay = days.sortedBy { it.isoDayNumber }.joinToString(",") { RRULE_DAY_CODES.getValue(it) }
            "FREQ=WEEKLY;BYDAY=$byDay"
        }
        ScheduleRepeatType.MONTHLY -> "FREQ=MONTHLY"
        ScheduleRepeatType.YEARLY -> "FREQ=YEARLY"
        // 반복 없이 기간(startDate != endDate)만 있는 경우에도 이 분기를 탄다(isSingleDay 참고).
        // 서버가 반복 없는 기간 일정을 표현할 방법이 없어(단발은 하루만, 반복은 rrule 필수),
        // 매일 반복으로 저장해 연속된 기간처럼 보이게 한다. 재조회 시 '매일 반복'으로 표시될 수 있다.
        ScheduleRepeatType.NONE -> "FREQ=DAILY"
    }

private fun parseRrule(rrule: String?): Pair<ScheduleRepeatType, Set<DayOfWeek>> {
    if (rrule.isNullOrBlank()) return ScheduleRepeatType.NONE to emptySet()
    val parts =
        rrule
            .split(";")
            .mapNotNull { part ->
                val (key, value) = part.split("=", limit = 2).let { it.getOrElse(0) { "" } to it.getOrElse(1) { "" } }
                if (key.isBlank()) null else key to value
            }.toMap()
    val repeatType =
        when (parts["FREQ"]) {
            "DAILY" -> ScheduleRepeatType.DAILY
            "WEEKLY" -> ScheduleRepeatType.WEEKLY
            "MONTHLY" -> ScheduleRepeatType.MONTHLY
            "YEARLY" -> ScheduleRepeatType.YEARLY
            else -> ScheduleRepeatType.NONE
        }
    val reverseDayCodes = RRULE_DAY_CODES.entries.associate { (day, code) -> code to day }
    val byDayCodes = parts["BYDAY"]?.split(",").orEmpty()
    val weekdays = byDayCodes.mapNotNull { reverseDayCodes[it] }.toSet()
    return repeatType to weekdays
}

/** 고정된 9색 팔레트와 서버가 저장하는 HEX 색상 코드를 서로 변환한다. */
private val CATEGORY_COLOR_HEX =
    mapOf(
        ScheduleColorType.RED to "#E95F5F",
        ScheduleColorType.ORANGE to "#EB9852",
        ScheduleColorType.YELLOW to "#F2CF4A",
        ScheduleColorType.GREEN to "#73C86B",
        ScheduleColorType.BLUE to "#6699EC",
        ScheduleColorType.PURPLE to "#9271EA",
        ScheduleColorType.PINK to "#DE7FA4",
        ScheduleColorType.GRAY to "#8F959D",
        ScheduleColorType.BLACK to "#1B1B1B"
    )

private fun ScheduleColorType.toHex(): String = CATEGORY_COLOR_HEX.getValue(this)

private fun String.toScheduleColorType(): ScheduleColorType =
    CATEGORY_COLOR_HEX.entries.find { it.value.equals(this, ignoreCase = true) }?.key ?: ScheduleColorType.GRAY
