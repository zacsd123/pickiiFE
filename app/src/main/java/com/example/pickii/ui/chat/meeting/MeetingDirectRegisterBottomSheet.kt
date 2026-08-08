package com.example.pickii.ui.chat

import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private const val DIRECT_REGISTER_TITLE_MAX_LENGTH = 20
private val DirectRegisterDateFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd (E)", Locale.KOREAN)
private val DirectRegisterTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

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
    var startTime by remember { mutableStateOf(LocalTime.of(14, 0)) }
    var endTime by remember { mutableStateOf(LocalTime.of(15, 0)) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isFormValid = title.isNotBlank() && date != null && startTime.isBefore(endTime)

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
                    color = Color(0xFF18181B),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Text(text = "×", color = Color(0xFF667085), fontSize = 28.sp, fontWeight = FontWeight.Light)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "이미 팀원들끼리 정한 일정이 있을 때만 사용하세요. 조율 없이 바로 팀 일정으로 등록됩니다.",
                color = Color(0xFF9CA3AF),
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(18.dp))

            TextField(
                value = title,
                onValueChange = { title = it.take(DIRECT_REGISTER_TITLE_MAX_LENGTH) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                placeholder = { Text(text = "회의명을 입력하세요", color = Color(0xFF9CA3AF), fontSize = 14.sp) },
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
                Text(text = "~", modifier = Modifier.padding(horizontal = 8.dp), color = Color(0xFFA0A5B1))
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
                        containerColor = Color(0xFF171714),
                        contentColor = Color.White,
                        disabledContainerColor = Color(0xFFE5E7EC),
                        disabledContentColor = Color(0xFFA7ADBC)
                    )
            ) {
                Text(text = "등록하기", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun DirectRegisterDateBox(
    date: LocalDate?,
    onDateChange: (LocalDate) -> Unit
) {
    val context = LocalContext.current
    val base = date ?: LocalDate.now()

    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFFF4F5F7))
                .clickable {
                    DatePickerDialog(
                        context,
                        { _, year, month, dayOfMonth -> onDateChange(LocalDate.of(year, month + 1, dayOfMonth)) },
                        base.year,
                        base.monthValue - 1,
                        base.dayOfMonth
                    ).show()
                }.padding(horizontal = 14.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = date?.format(DirectRegisterDateFormatter) ?: "날짜 선택",
            color = if (date == null) Color(0xFFA0A5B1) else Color(0xFF292D35),
            fontSize = 14.sp
        )
    }
}

@Composable
private fun DirectRegisterTimeBox(
    label: String,
    time: LocalTime,
    modifier: Modifier = Modifier,
    onTimeChange: (LocalTime) -> Unit
) {
    val context = LocalContext.current

    Box(
        modifier =
            modifier
                .height(52.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFFF4F5F7))
                .clickable {
                    TimePickerDialog(
                        context,
                        { _, hourOfDay, minute -> onTimeChange(LocalTime.of(hourOfDay, minute)) },
                        time.hour,
                        time.minute,
                        true
                    ).show()
                }.padding(horizontal = 14.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(text = "$label ${time.format(DirectRegisterTimeFormatter)}", color = Color(0xFF292D35), fontSize = 14.sp)
    }
}
