package com.example.pickii.ui.calendar.editor.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

private val SheetLabelColor = Color(0xFF777777)
private val SheetValueColor = Color(0xFF1B1B1B)

private val WeekdayDefaultColor = Color(0xFFA8A8A8)
private val SaturdayColor = Color(0xFF6D9EEB)
private val SundayColor = Color(0xFFE86F73)

private val TodayRingColor = Color(0xFFD7E85B)
private val SelectedDayBackgroundColor = Color(0xFF1B1B1B)
private val InRangeBackgroundColor = Color(0xFFFAF6C9)

private val FooterButtonEnabledColor = Color(0xFF1B1B1B)
private val FooterButtonDisabledColor = Color(0xFFEDEDE7)
private val FooterButtonEnabledTextColor = Color(0xFFFFFFFF)
private val FooterButtonDisabledTextColor = Color(0xFFAAAAAA)

private val SheetDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

private const val DAYS_PER_WEEK = 7

private val OrderedWeekdays =
    listOf(
        DayOfWeek.MONDAY,
        DayOfWeek.TUESDAY,
        DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY,
        DayOfWeek.FRIDAY,
        DayOfWeek.SATURDAY,
        DayOfWeek.SUNDAY
    )

/**
 * 일정 시작일/종료일을 범위로 선택하는 바텀시트 달력이다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleDatePickerSheet(
    initialStartDate: LocalDate,
    initialEndDate: LocalDate,
    isRepeatEnabled: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (startDate: LocalDate, endDate: LocalDate) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    var displayedYearMonth by remember {
        mutableStateOf(YearMonth.from(initialStartDate))
    }

    var selectionStart by remember {
        mutableStateOf<LocalDate?>(initialStartDate)
    }

    var selectionEnd by remember {
        mutableStateOf<LocalDate?>(initialEndDate)
    }

    val sheetState =
        rememberModalBottomSheetState(
            skipPartiallyExpanded = true
        )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        dragHandle = {
            BottomSheetDefaults.DragHandle(
                width = 36.dp,
                height = 4.dp
            )
        },
        shape =
            RoundedCornerShape(
                topStart = 28.dp,
                topEnd = 28.dp
            ),
        modifier = modifier
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(
                        horizontal = 24.dp,
                        vertical = 8.dp
                    ),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            SelectionSummaryRow(
                startDate = selectionStart,
                endDate = selectionEnd
            )

            MonthNavigationHeader(
                displayedYearMonth = displayedYearMonth,
                onPreviousMonth = {
                    displayedYearMonth = displayedYearMonth.minusMonths(1)
                },
                onNextMonth = {
                    displayedYearMonth = displayedYearMonth.plusMonths(1)
                }
            )

            WeekdayHeaderRow()

            CalendarDateGrid(
                displayedYearMonth = displayedYearMonth,
                selectionStart = selectionStart,
                selectionEnd = selectionEnd,
                onDateClick = { date ->
                    val start = selectionStart
                    val end = selectionEnd

                    when {
                        start == null -> {
                            selectionStart = date
                            selectionEnd = null
                        }

                        end == null -> {
                            if (date.isBefore(start)) {
                                selectionStart = date
                            } else {
                                selectionEnd = date
                            }
                        }

                        else -> {
                            selectionStart = date
                            selectionEnd = null
                        }
                    }
                }
            )

            SheetFooter(
                canClear = selectionStart != null || selectionEnd != null,
                canSkipEndDate = !isRepeatEnabled && selectionStart != null,
                canConfirm = selectionStart != null && selectionEnd != null,
                showSkipEndDateButton = !isRepeatEnabled,
                onClearClick = {
                    selectionStart = null
                    selectionEnd = null
                    onClear()
                },
                onSkipEndDateClick = {
                    val start = selectionStart
                    if (start != null) {
                        selectionEnd = start
                    }
                },
                onConfirmClick = {
                    val start = selectionStart
                    val end = selectionEnd
                    if (start != null && end != null) {
                        onConfirm(start, end)
                    }
                }
            )
        }
    }
}

/**
 * 상단에 현재 선택된 시작일/종료일을 보여준다.
 *
 * 종료일이 아직 없으면 시작일 쪽 점을 채우고, 둘 다 있으면 종료일 쪽 점을 채운다.
 */
@Composable
private fun SelectionSummaryRow(
    startDate: LocalDate?,
    endDate: LocalDate?,
    modifier: Modifier = Modifier
) {
    val isStartActive = endDate == null

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text =
                "${if (isStartActive) "●" else "○"} 시작: " +
                    (startDate?.format(SheetDateFormatter) ?: "선택"),
            modifier = Modifier.weight(1f),
            color = if (isStartActive) SheetValueColor else SheetLabelColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )

        Text(
            text =
                "${if (isStartActive) "○" else "●"} 종료: " +
                    (endDate?.format(SheetDateFormatter) ?: "선택"),
            modifier = Modifier.weight(1f),
            color = if (isStartActive) SheetLabelColor else SheetValueColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * 이전/다음 달로 이동하는 헤더다.
 */
@Composable
private fun MonthNavigationHeader(
    displayedYearMonth: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "‹",
            modifier =
                Modifier
                    .clickable(onClick = onPreviousMonth)
                    .padding(12.dp),
            color = SheetValueColor,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "${displayedYearMonth.year}년 ${displayedYearMonth.monthValue}월",
            color = SheetValueColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "›",
            modifier =
                Modifier
                    .clickable(onClick = onNextMonth)
                    .padding(12.dp),
            color = SheetValueColor,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * 월화수목금토일 요일 헤더다. 토요일은 파랑, 일요일은 빨강으로 표시한다.
 */
@Composable
private fun WeekdayHeaderRow(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        OrderedWeekdays.forEach { day ->
            Text(
                text = day.toShortLabel(),
                modifier =
                    Modifier
                        .size(40.dp),
                textAlign = TextAlign.Center,
                color =
                    when (day) {
                        DayOfWeek.SATURDAY -> SaturdayColor
                        DayOfWeek.SUNDAY -> SundayColor
                        else -> WeekdayDefaultColor
                    },
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * 이번 달 날짜 그리드다. 앞뒤 빈 칸은 이전/다음 달로 채우지 않고 비워둔다.
 */
@Composable
private fun CalendarDateGrid(
    displayedYearMonth: YearMonth,
    selectionStart: LocalDate?,
    selectionEnd: LocalDate?,
    onDateClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val today = LocalDate.now()
    val calendarDates = createCalendarDates(displayedYearMonth)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        calendarDates.chunked(DAYS_PER_WEEK).forEach { weekDates ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                weekDates.forEach { date ->
                    CalendarDateCell(
                        date = date,
                        isToday = date == today,
                        isSelected = date != null && (date == selectionStart || date == selectionEnd),
                        isInRange =
                            date != null &&
                                selectionStart != null &&
                                selectionEnd != null &&
                                date.isAfter(selectionStart) &&
                                date.isBefore(selectionEnd),
                        onDateClick = onDateClick
                    )
                }
            }
        }
    }
}

/**
 * 날짜 한 칸이다. 선택/오늘/범위 여부에 따라 스타일이 달라진다.
 */
@Composable
private fun CalendarDateCell(
    date: LocalDate?,
    isToday: Boolean,
    isSelected: Boolean,
    isInRange: Boolean,
    onDateClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val textColor =
        when {
            date == null -> Color.Transparent
            isSelected -> Color.White
            isInRange -> SheetValueColor
            isToday -> SheetValueColor
            date.dayOfWeek == DayOfWeek.SATURDAY -> SaturdayColor
            date.dayOfWeek == DayOfWeek.SUNDAY -> SundayColor
            else -> SheetValueColor
        }

    Box(
        modifier =
            modifier
                .size(40.dp)
                .background(
                    color =
                        when {
                            isSelected -> SelectedDayBackgroundColor
                            isInRange -> InRangeBackgroundColor
                            else -> Color.Transparent
                        },
                    shape = CircleShape
                ).let {
                    if (isToday && !isSelected) {
                        it.border(1.5.dp, TodayRingColor, CircleShape)
                    } else {
                        it
                    }
                }.let {
                    if (date != null) {
                        it.clickable {
                            onDateClick(date)
                        }
                    } else {
                        it
                    }
                },
        contentAlignment = Alignment.Center
    ) {
        if (date != null) {
            Text(
                text = date.dayOfMonth.toString(),
                color = textColor,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

/**
 * 지우기 / 지정 안함 / 완료 버튼 세 개다.
 */
@Composable
private fun SheetFooter(
    canClear: Boolean,
    canSkipEndDate: Boolean,
    canConfirm: Boolean,
    showSkipEndDateButton: Boolean,
    onClearClick: () -> Unit,
    onSkipEndDateClick: () -> Unit,
    onConfirmClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        FooterTextButton(
            text = "지우기",
            enabled = canClear,
            modifier = Modifier.weight(1f),
            onClick = onClearClick
        )

        if (showSkipEndDateButton) {
            FooterTextButton(
                text = "지정 안함",
                enabled = canSkipEndDate,
                modifier = Modifier.weight(1f),
                onClick = onSkipEndDateClick
            )
        }

        Box(
            modifier =
                Modifier
                    .weight(1f)
                    .background(
                        color =
                            if (canConfirm) {
                                FooterButtonEnabledColor
                            } else {
                                FooterButtonDisabledColor
                            },
                        shape = RoundedCornerShape(14.dp)
                    ).clickable(
                        enabled = canConfirm,
                        onClick = onConfirmClick
                    ).padding(vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "완료",
                color =
                    if (canConfirm) {
                        FooterButtonEnabledTextColor
                    } else {
                        FooterButtonDisabledTextColor
                    },
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun FooterTextButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier =
            modifier
                .background(
                    color = FooterButtonDisabledColor,
                    shape = RoundedCornerShape(14.dp)
                ).clickable(
                    enabled = enabled,
                    onClick = onClick
                ).padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color =
                if (enabled) {
                    SheetValueColor
                } else {
                    FooterButtonDisabledTextColor
                },
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * 전달받은 월을 7열 달력에 맞게 날짜 목록으로 변환한다.
 *
 * 이번 달에 포함되지 않는 앞뒤 칸은 null로 채운다.
 */
private fun createCalendarDates(yearMonth: YearMonth): List<LocalDate?> {
    val firstDate = yearMonth.atDay(1)
    val lastDate = yearMonth.atEndOfMonth()

    val leadingEmptyCount = firstDate.dayOfWeek.value - 1

    val dates =
        buildList<LocalDate?> {
            repeat(leadingEmptyCount) {
                add(null)
            }

            for (dayOfMonth in 1..lastDate.dayOfMonth) {
                add(yearMonth.atDay(dayOfMonth))
            }
        }

    val trailingEmptyCount = (DAYS_PER_WEEK - dates.size % DAYS_PER_WEEK) % DAYS_PER_WEEK

    return buildList {
        addAll(dates)

        repeat(trailingEmptyCount) {
            add(null)
        }
    }
}

/**
 * 요일을 한 글자 한글 표기로 변환한다.
 */
private fun DayOfWeek.toShortLabel(): String =
    when (this) {
        DayOfWeek.MONDAY -> "월"
        DayOfWeek.TUESDAY -> "화"
        DayOfWeek.WEDNESDAY -> "수"
        DayOfWeek.THURSDAY -> "목"
        DayOfWeek.FRIDAY -> "금"
        DayOfWeek.SATURDAY -> "토"
        DayOfWeek.SUNDAY -> "일"
    }
