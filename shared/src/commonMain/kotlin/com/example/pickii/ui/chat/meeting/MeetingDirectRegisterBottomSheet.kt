package com.example.pickii.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pickii.ui.theme.PickiiBlackAlt
import com.example.pickii.ui.theme.PickiiCharcoal
import com.example.pickii.ui.theme.PickiiDividerAlt
import com.example.pickii.ui.theme.PickiiGray400
import com.example.pickii.ui.theme.PickiiGray500
import com.example.pickii.ui.theme.PickiiGraySlate
import com.example.pickii.ui.theme.PickiiSlateDark
import com.example.pickii.ui.theme.PickiiSurfaceGray
import com.example.pickii.util.today
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.format
import kotlinx.datetime.format.DayOfWeekNames
import kotlinx.datetime.format.char
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

private const val DIRECT_REGISTER_TITLE_MAX_LENGTH = 20
private val DirectRegisterDateFormatter =
    LocalDate.Format {
        year()
        char('.')
        monthNumber()
        char('.')
        day()
        char(' ')
        char('(')
        dayOfWeek(DayOfWeekNames("월", "화", "수", "목", "금", "토", "일"))
        char(')')
    }
private val DirectRegisterTimeFormatter =
    LocalTime.Format {
        hour()
        char(':')
        minute()
    }

/**
 * 조율 없이 회의를 직접 등록하는 화면이다(7-16, 프로젝트장 전용).
 *
 * 이미 오프라인 등으로 확정된 예외적인 일정을 등록할 때만 쓴다 — 반복 일정은 지원하지 않는다(단발만).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeetingDirectRegisterBottomSheet(
    onDismiss: () -> Unit,
    onRegisterClick: (title: String, date: LocalDate, startTime: LocalTime, endTime: LocalTime) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var date by remember { mutableStateOf<LocalDate?>(null) }
    var startTime by remember { mutableStateOf(LocalTime(14, 0)) }
    var endTime by remember { mutableStateOf(LocalTime(15, 0)) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isFormValid = title.isNotBlank() && date != null && startTime < endTime

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        dragHandle = { BottomSheetDefaults.DragHandle(width = 36.dp, height = 4.dp) },
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(start = 24.dp, end = 24.dp, bottom = 24.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "회의 직접 등록",
                    modifier = Modifier.weight(1f),
                    color = PickiiBlackAlt,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Text(text = "×", color = PickiiGraySlate, fontSize = 28.sp, fontWeight = FontWeight.Light)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "이미 팀원들끼리 정한 일정이 있을 때만 사용하세요. 조율 없이 바로 팀 일정으로 등록됩니다.",
                color = PickiiGray400,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(18.dp))

            TextField(
                value = title,
                onValueChange = { title = it.take(DIRECT_REGISTER_TITLE_MAX_LENGTH) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                placeholder = { Text(text = "회의명을 입력하세요", color = PickiiGray400, fontSize = 14.sp) },
                singleLine = true,
                keyboardOptions =
                    KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next),
                shape = RoundedCornerShape(16.dp)
            )

            Spacer(modifier = Modifier.height(18.dp))

            DirectRegisterDateBox(date = date, onDateChange = { date = it })

            Spacer(modifier = Modifier.height(18.dp))

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                DirectRegisterTimeBox(
                    label = "시작",
                    time = startTime,
                    modifier = Modifier.weight(1f),
                    onTimeChange = { startTime = it }
                )
                Spacer(modifier = Modifier.height(1.dp))
                Text(text = "~", modifier = Modifier.padding(horizontal = 8.dp), color = PickiiGray500)
                DirectRegisterTimeBox(
                    label = "종료",
                    time = endTime,
                    modifier = Modifier.weight(1f),
                    onTimeChange = { endTime = it }
                )
            }

            Spacer(modifier = Modifier.height(26.dp))

            Button(
                onClick = {
                    val selectedDate = date
                    if (selectedDate != null) onRegisterClick(title.trim(), selectedDate, startTime, endTime)
                },
                enabled = isFormValid,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(14.dp),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = PickiiCharcoal,
                        contentColor = Color.White,
                        disabledContainerColor = PickiiDividerAlt,
                        disabledContentColor = Color(0xFFA7ADBC)
                    )
            ) {
                Text(text = "등록하기", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DirectRegisterDateBox(
    date: LocalDate?,
    onDateChange: (LocalDate) -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }

    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(PickiiSurfaceGray)
                .clickable { showDatePicker = true }
                .padding(horizontal = 14.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = date?.format(DirectRegisterDateFormatter) ?: "날짜 선택",
            color = if (date == null) PickiiGray500 else PickiiSlateDark,
            fontSize = 14.sp
        )
    }

    if (showDatePicker) {
        val datePickerState =
            rememberDatePickerState(initialSelectedDateMillis = (date ?: today()).toEpochMillisUtc())

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.toLocalDateUtc()?.let(onDateChange)
                        showDatePicker = false
                    }
                ) {
                    Text("확인")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("취소")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DirectRegisterTimeBox(
    label: String,
    time: LocalTime,
    modifier: Modifier = Modifier,
    onTimeChange: (LocalTime) -> Unit
) {
    var showTimePicker by remember { mutableStateOf(false) }

    Box(
        modifier =
            modifier
                .height(52.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(PickiiSurfaceGray)
                .clickable { showTimePicker = true }
                .padding(horizontal = 14.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(text = "$label ${time.format(DirectRegisterTimeFormatter)}", color = PickiiSlateDark, fontSize = 14.sp)
    }

    if (showTimePicker) {
        val timePickerState =
            rememberTimePickerState(initialHour = time.hour, initialMinute = time.minute, is24Hour = true)

        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        onTimeChange(LocalTime(timePickerState.hour, timePickerState.minute))
                        showTimePicker = false
                    }
                ) {
                    Text("확인")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("취소")
                }
            },
            text = {
                TimePicker(state = timePickerState)
            }
        )
    }
}

/** 이 날짜의 UTC 자정 시각을 epoch millisecond로 변환한다([DatePicker] 초기값 용도). */
@OptIn(ExperimentalTime::class)
private fun LocalDate.toEpochMillisUtc(): Long = atTime(0, 0).toInstant(TimeZone.UTC).toEpochMilliseconds()

/** [DatePicker]가 반환한 epoch millisecond를 UTC 기준 날짜로 변환한다. */
@OptIn(ExperimentalTime::class)
private fun Long.toLocalDateUtc(): LocalDate = Instant.fromEpochMilliseconds(this).toLocalDateTime(TimeZone.UTC).date
