package com.example.pickii.ui.calendar.editor.component

import android.app.DatePickerDialog
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val DateTimeSectionBackgroundColor = Color(0xFFFFFFFF)
private val DateTimeLabelColor = Color(0xFF777777)
private val DateTimeValueColor = Color(0xFF1B1B1B)
private val DateTimeDividerColor = Color(0xFFE7E7E1)

private val SwitchCheckedColor = Color(0xFF1B1B1B)
private val SwitchUncheckedColor = Color(0xFFD2D2CC)

private val DateFormatter =
    DateTimeFormatter.ofPattern(
        "yyyy.MM.dd"
    )

private val TimeFormatter =
    DateTimeFormatter.ofPattern(
        "HH:mm"
    )

/**
 * 일정 시작일, 종료일, 시간 및 하루 종일 설정 영역이다.
 */
@Composable
fun ScheduleDateTimeSection(
    startDate: LocalDate,
    endDate: LocalDate,
    startTime: LocalTime,
    endTime: LocalTime,
    isAllDay: Boolean,
    onStartDateChange: (LocalDate) -> Unit,
    onEndDateChange: (LocalDate) -> Unit,
    onStartTimeChange: (LocalTime) -> Unit,
    onEndTimeChange: (LocalTime) -> Unit,
    onAllDayChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
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
                text = "날짜 및 시간",
                color = DateTimeLabelColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
        }

        DateTimeDivider()

        DateTimePickerRow(
            label = "시작",
            date = startDate,
            time = startTime,
            isAllDay = isAllDay,
            onDateChange = onStartDateChange,
            onTimeChange = onStartTimeChange
        )

        DateTimeDivider()

        DateTimePickerRow(
            label = "종료",
            date = endDate,
            time = endTime,
            isAllDay = isAllDay,
            onDateChange = onEndDateChange,
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
}

/**
 * 시작 또는 종료 날짜와 시간을 선택하는 한 줄이다.
 */
@Composable
private fun DateTimePickerRow(
    label: String,
    date: LocalDate,
    time: LocalTime,
    isAllDay: Boolean,
    onDateChange: (LocalDate) -> Unit,
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

        Text(
            text = date.format(DateFormatter),
            modifier =
                Modifier
                    .weight(1f)
                    .clickable {
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                onDateChange(
                                    LocalDate.of(
                                        year,
                                        month + 1,
                                        dayOfMonth
                                    )
                                )
                            },
                            date.year,
                            date.monthValue - 1,
                            date.dayOfMonth
                        ).show()
                    }.padding(
                        vertical = 8.dp
                    ),
            color = DateTimeValueColor,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )

        if (!isAllDay) {
            Text(
                text = time.format(TimeFormatter),
                modifier =
                    Modifier
                        .clickable {
                            TimePickerDialog(
                                context,
                                { _, hourOfDay, minute ->
                                    onTimeChange(
                                        LocalTime.of(
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
                            horizontal = 8.dp,
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
