package com.example.pickii.ui.calendar.editor.component

import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pickii.domain.model.ScheduleRepeatType
import com.example.pickii.ui.theme.PickiiGray600
import com.example.pickii.ui.theme.PickiiInk
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.format
import kotlinx.datetime.format.char

private val DateTimeSectionBackgroundColor = Color(0xFFFFFFFF)
private val DateTimeLabelColor = PickiiGray600
private val DateTimeValueColor = PickiiInk
private val DateTimeDividerColor = Color(0xFFE7E7E1)

private val SwitchCheckedColor = PickiiInk
private val SwitchUncheckedColor = Color(0xFFD2D2CC)

private val DateFormatter = LocalDate.Formats.ISO

private val TimeFormatter =
    LocalTime.Format {
        hour()
        char(':')
        minute()
    }

/**
 * 일정 날짜(범위), 시작/종료 시간, 하루 종일 설정 영역이다.
 */
@Composable
fun ScheduleDateTimeSection(
    startDate: LocalDate,
    endDate: LocalDate,
    startTime: LocalTime,
    endTime: LocalTime,
    isAllDay: Boolean,
    repeatType: ScheduleRepeatType,
    onDateRangeChange: (LocalDate, LocalDate) -> Unit,
    onStartTimeChange: (LocalTime) -> Unit,
    onEndTimeChange: (LocalTime) -> Unit,
    onAllDayChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember {
        mutableStateOf(false)
    }

    val isRepeatEnabled = repeatType != ScheduleRepeatType.NONE

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(
                    color = DateTimeSectionBackgroundColor,
                    shape = RoundedCornerShape(18.dp)
                ).padding(
                    horizontal = 20.dp,
                    vertical = 18.dp
                ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "◷",
                color = DateTimeLabelColor,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.width(8.dp)
            )

            Text(
                text = "날짜 / 시간",
                color = DateTimeLabelColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
        }

        DateTimeDivider()

        DateRangeRow(
            startDate = startDate,
            endDate = endDate,
            onClick = {
                showDatePicker = true
            }
        )

        DateTimeDivider()

        TimePickerRow(
            label = "시작",
            time = startTime,
            isAllDay = isAllDay,
            onTimeChange = onStartTimeChange
        )

        DateTimeDivider()

        TimePickerRow(
            label = "종료",
            time = endTime,
            isAllDay = isAllDay,
            onTimeChange = onEndTimeChange
        )

        DateTimeDivider()

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "하루 종일",
                color = DateTimeValueColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )

            Switch(
                checked = isAllDay,
                onCheckedChange = onAllDayChange,
                colors =
                    SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = SwitchCheckedColor,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = SwitchUncheckedColor,
                        uncheckedBorderColor = Color.Transparent
                    )
            )
        }
    }

    if (showDatePicker) {
        ScheduleDatePickerSheet(
            initialStartDate = startDate,
            initialEndDate = endDate,
            isRepeatEnabled = isRepeatEnabled,
            onDismiss = {
                showDatePicker = false
            },
            onConfirm = { newStartDate, newEndDate ->
                onDateRangeChange(newStartDate, newEndDate)
                showDatePicker = false
            },
            onClear = {
                onDateRangeChange(startDate, startDate)
                showDatePicker = false
            }
        )
    }
}

/**
 * 날짜(또는 날짜 범위)를 한 줄로 보여준다. 누르면 날짜 선택 바텀시트가 열린다.
 */
@Composable
private fun DateRangeRow(
    startDate: LocalDate,
    endDate: LocalDate,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "날짜",
            modifier = Modifier.width(44.dp),
            color = DateTimeLabelColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )

        Text(
            text =
                if (startDate == endDate) {
                    startDate.format(DateFormatter)
                } else {
                    "${startDate.format(DateFormatter)} ~ ${endDate.format(DateFormatter)}"
                },
            modifier =
                Modifier
                    .weight(1f)
                    .clickable(onClick = onClick)
                    .padding(vertical = 8.dp),
            color = DateTimeValueColor,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * 시작 또는 종료 시간을 선택하는 한 줄이다.
 */
@Composable
private fun TimePickerRow(
    label: String,
    time: LocalTime,
    isAllDay: Boolean,
    onTimeChange: (LocalTime) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.width(44.dp),
            color = DateTimeLabelColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )

        if (isAllDay) {
            Text(
                text = "하루 종일",
                modifier = Modifier.padding(vertical = 8.dp),
                color = DateTimeLabelColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
        } else {
            Text(
                text = time.format(TimeFormatter),
                modifier =
                    Modifier
                        .clickable {
                            TimePickerDialog(
                                context,
                                { _, hourOfDay, minute ->
                                    onTimeChange(
                                        LocalTime(
                                            hourOfDay,
                                            minute
                                        )
                                    )
                                },
                                time.hour,
                                time.minute,
                                true
                            ).show()
                        }.padding(
                            vertical = 8.dp
                        ),
                color = DateTimeValueColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * 날짜 및 시간 설정 항목 사이 구분선이다.
 */
@Composable
private fun DateTimeDivider(modifier: Modifier = Modifier) {
    androidx.compose.foundation.layout.Box(
        modifier =
            modifier
                .fillMaxWidth()
                .background(DateTimeDividerColor)
                .padding(top = 1.dp)
    )
}
