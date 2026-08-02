package com.example.pickii.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 빠른 회의 등록 화면이다.
 *
 * 회의명, 소요 시간, 일정 수집 기간, 회의 후보 수, 메모를 입력받는다.
 */
@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalLayoutApi::class
)
@Composable
fun MeetingRegistrationBottomSheet(
    onDismiss: () -> Unit,
    onNextClick: (QuickMeetingForm) -> Unit
) {
    var meetingTitle by remember {
        mutableStateOf("")
    }

    var selectedDuration by remember {
        mutableIntStateOf(60)
    }

    var selectedCandidateCount by remember {
        mutableIntStateOf(3)
    }

    var memo by remember {
        mutableStateOf("")
    }

    var startDateMillis by remember {
        mutableStateOf<Long?>(null)
    }

    var endDateMillis by remember {
        mutableStateOf<Long?>(null)
    }

    var showDatePicker by remember {
        mutableStateOf(false)
    }

    val sheetState =
        rememberModalBottomSheetState(
            skipPartiallyExpanded = true
        )

    val isFormValid =
        meetingTitle.isNotBlank() &&
            startDateMillis != null &&
            endDateMillis != null

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
            )
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(
                        start = 24.dp,
                        end = 24.dp,
                        bottom = 24.dp
                    )
        ) {
            MeetingSheetHeader(
                onCloseClick = onDismiss
            )

            Spacer(modifier = Modifier.height(18.dp))

            MeetingTitleSection(
                title = meetingTitle,
                onTitleChange = {
                    meetingTitle = it
                }
            )

            Spacer(modifier = Modifier.height(22.dp))

            MeetingDurationSection(
                selectedDuration = selectedDuration,
                onDurationSelected = {
                    selectedDuration = it
                }
            )

            Spacer(modifier = Modifier.height(22.dp))

            MeetingDateRangeSection(
                startDateMillis = startDateMillis,
                endDateMillis = endDateMillis,
                onClick = {
                    showDatePicker = true
                }
            )

            Spacer(modifier = Modifier.height(22.dp))

            MeetingCandidateSection(
                selectedCount = selectedCandidateCount,
                onCountSelected = {
                    selectedCandidateCount = it
                }
            )

            Spacer(modifier = Modifier.height(22.dp))

            MeetingMemoSection(
                memo = memo,
                onMemoChange = {
                    memo = it
                }
            )

            Spacer(modifier = Modifier.height(26.dp))

            Button(
                onClick = {
                    val selectedStartDate = startDateMillis
                    val selectedEndDate = endDateMillis

                    if (
                        selectedStartDate != null &&
                        selectedEndDate != null
                    ) {
                        onNextClick(
                            QuickMeetingForm(
                                title = meetingTitle.trim(),
                                durationMinutes = selectedDuration,
                                startDateMillis = selectedStartDate,
                                endDateMillis = selectedEndDate,
                                candidateCount = selectedCandidateCount,
                                memo = memo.trim()
                            )
                        )
                    }
                },
                enabled = isFormValid,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                shape = RoundedCornerShape(14.dp),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF171714),
                        contentColor = Color.White,
                        disabledContainerColor = Color(0xFFE5E7EC),
                        disabledContentColor = Color(0xFFA7ADBC)
                    )
            ) {
                Text(
                    text = "다음 →",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }

    if (showDatePicker) {
        MeetingDateRangePickerDialog(
            initialStartDateMillis = startDateMillis,
            initialEndDateMillis = endDateMillis,
            onDismiss = {
                showDatePicker = false
            },
            onConfirm = { start, end ->
                startDateMillis = start
                endDateMillis = end
                showDatePicker = false
            }
        )
    }
}

/**
 * 회의 등록 바텀시트의 상단 제목과 닫기 버튼이다.
 */
@Composable
private fun MeetingSheetHeader(onCloseClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "회의 등록",
            modifier = Modifier.weight(1f),
            color = Color(0xFF18181B),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        IconButton(
            onClick = onCloseClick
        ) {
            Text(
                text = "×",
                color = Color(0xFF667085),
                fontSize = 28.sp,
                fontWeight = FontWeight.Light
            )
        }
    }
}

/**
 * 필수 입력 항목인 회의명을 입력받는다.
 */
@Composable
private fun MeetingTitleSection(
    title: String,
    onTitleChange: (String) -> Unit
) {
    Column {
        SectionTitle(
            title = "회의명",
            required = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        TextField(
            value = title,
            onValueChange = onTitleChange,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            placeholder = {
                Text(
                    text = "회의명을 입력하세요",
                    color = Color(0xFF9CA3AF),
                    fontSize = 14.sp
                )
            },
            singleLine = true,
            keyboardOptions =
                KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Next
                ),
            shape = RoundedCornerShape(16.dp),
            colors = meetingTextFieldColors()
        )
    }
}

/**
 * 회의 소요 시간을 선택한다.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MeetingDurationSection(
    selectedDuration: Int,
    onDurationSelected: (Int) -> Unit
) {
    val durationOptions =
        listOf(
            30 to "30분",
            60 to "1시간",
            90 to "1시간 30분",
            120 to "2시간",
            180 to "3시간"
        )

    Column {
        SectionTitle(
            title = "소요 시간"
        )

        Spacer(modifier = Modifier.height(10.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            durationOptions.forEach { option ->
                val minutes = option.first
                val label = option.second

                MeetingSelectChip(
                    text = label,
                    selected = selectedDuration == minutes,
                    onClick = {
                        onDurationSelected(minutes)
                    }
                )
            }
        }
    }
}

/**
 * 일정 수집 시작일과 종료일을 표시한다.
 */
@Composable
private fun MeetingDateRangeSection(
    startDateMillis: Long?,
    endDateMillis: Long?,
    onClick: () -> Unit
) {
    Column {
        SectionTitle(
            title = "일정 수집 기간",
            description = "팀원이 개인 일정을 등록하는 기간"
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DateBox(
                dateMillis = startDateMillis,
                placeholder = "시작일",
                modifier = Modifier.weight(1f),
                onClick = onClick
            )

            Text(
                text = "~",
                modifier = Modifier.padding(horizontal = 8.dp),
                color = Color(0xFFA0A5B1),
                fontSize = 14.sp
            )

            DateBox(
                dateMillis = endDateMillis,
                placeholder = "종료일",
                modifier = Modifier.weight(1f),
                onClick = onClick
            )
        }
    }
}

/**
 * 선택된 날짜를 표시하고, 클릭하면 날짜 선택기를 연다.
 */
@Composable
private fun DateBox(
    dateMillis: Long?,
    placeholder: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier =
            modifier
                .height(52.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFFF4F5F7))
                .clickable(onClick = onClick)
                .padding(horizontal = 14.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = dateMillis?.let(::formatMeetingDate) ?: placeholder,
            color =
                if (dateMillis == null) {
                    Color(0xFFA0A5B1)
                } else {
                    Color(0xFF292D35)
                },
            fontSize = 14.sp
        )
    }
}

/**
 * 날짜 범위를 선택할 수 있는 달력 다이얼로그다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MeetingDateRangePickerDialog(
    initialStartDateMillis: Long?,
    initialEndDateMillis: Long?,
    onDismiss: () -> Unit,
    onConfirm: (Long, Long) -> Unit
) {
    val dateRangePickerState =
        rememberDateRangePickerState(
            initialSelectedStartDateMillis = initialStartDateMillis,
            initialSelectedEndDateMillis = initialEndDateMillis
        )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val selectedStart =
                        dateRangePickerState.selectedStartDateMillis
                    val selectedEnd =
                        dateRangePickerState.selectedEndDateMillis

                    if (
                        selectedStart != null &&
                        selectedEnd != null
                    ) {
                        onConfirm(
                            selectedStart,
                            selectedEnd
                        )
                    }
                },
                enabled =
                    dateRangePickerState.selectedStartDateMillis != null &&
                        dateRangePickerState.selectedEndDateMillis != null
            ) {
                Text(
                    text = "확인",
                    color = Color(0xFF171714),
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(
                    text = "취소",
                    color = Color(0xFF667085)
                )
            }
        }
    ) {
        DateRangePicker(
            state = dateRangePickerState,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(520.dp),
            title = {
                Text(
                    text = "일정 수집 기간",
                    modifier =
                        Modifier.padding(
                            start = 24.dp,
                            top = 16.dp,
                            end = 24.dp
                        ),
                    color = Color(0xFF18181B),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            headline = {
                Text(
                    text = "시작일과 종료일을 선택하세요",
                    modifier =
                        Modifier.padding(
                            start = 24.dp,
                            end = 24.dp,
                            bottom = 8.dp
                        ),
                    color = Color(0xFF667085),
                    fontSize = 14.sp
                )
            },
            showModeToggle = false
        )
    }
}

/**
 * 생성할 회의 후보 개수를 선택한다.
 */
@Composable
private fun MeetingCandidateSection(
    selectedCount: Int,
    onCountSelected: (Int) -> Unit
) {
    val candidateOptions = listOf(2, 3, 4, 5)

    Column {
        SectionTitle(
            title = "회의 후보 수",
            description = "최대 몇 개까지 추릴까요?"
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            candidateOptions.forEach { count ->
                MeetingSelectChip(
                    text = "${count}개",
                    selected = selectedCount == count,
                    onClick = {
                        onCountSelected(count)
                    },
                    modifier = Modifier.weight(1f),
                    useContentPadding = false
                )
            }
        }
    }
}

/**
 * 선택 입력 항목인 회의 메모를 입력받는다.
 */
@Composable
private fun MeetingMemoSection(
    memo: String,
    onMemoChange: (String) -> Unit
) {
    Column {
        SectionTitle(
            title = "메모",
            optional = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        TextField(
            value = memo,
            onValueChange = onMemoChange,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(110.dp),
            placeholder = {
                Text(
                    text = "회의 목적이나 안건을 간단히 적어주세요 (선택)",
                    color = Color(0xFF9CA3AF),
                    fontSize = 14.sp
                )
            },
            keyboardOptions =
                KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Default
                ),
            shape = RoundedCornerShape(16.dp),
            colors = meetingTextFieldColors()
        )
    }
}

/**
 * 소요 시간이나 후보 개수를 선택할 때 사용하는 공통 버튼이다.
 */
@Composable
private fun MeetingSelectChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    useContentPadding: Boolean = true
) {
    Box(
        modifier =
            modifier
                .height(40.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(
                    if (selected) {
                        Color(0xFF171714)
                    } else {
                        Color(0xFFF4F5F7)
                    }
                ).clickable(onClick = onClick)
                .then(
                    if (useContentPadding) {
                        Modifier.padding(horizontal = 16.dp)
                    } else {
                        Modifier
                    }
                ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color =
                if (selected) {
                    Color.White
                } else {
                    Color(0xFF717784)
                },
            fontSize = 13.sp,
            fontWeight =
                if (selected) {
                    FontWeight.SemiBold
                } else {
                    FontWeight.Normal
                }
        )
    }
}

/**
 * 각 입력 영역의 제목, 필수 여부, 선택 여부, 설명을 표시한다.
 */
@Composable
private fun SectionTitle(
    title: String,
    required: Boolean = false,
    optional: Boolean = false,
    description: String? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = Color(0xFF4B5563),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )

        when {
            required -> {
                Text(
                    text = " 필수",
                    color = Color(0xFFFF7373),
                    fontSize = 11.sp
                )
            }

            optional -> {
                Text(
                    text = " 선택",
                    color = Color(0xFFA0A5B1),
                    fontSize = 11.sp
                )
            }
        }

        if (description != null) {
            Text(
                text = "  ($description)",
                color = Color(0xFFA0A5B1),
                fontSize = 10.sp,
                maxLines = 1
            )
        }
    }
}

/**
 * 회의 등록 입력창에 공통으로 적용하는 색상이다.
 */
@Composable
private fun meetingTextFieldColors() =
    TextFieldDefaults.colors(
        focusedContainerColor = Color(0xFFF4F5F7),
        unfocusedContainerColor = Color(0xFFF4F5F7),
        disabledContainerColor = Color(0xFFF4F5F7),
        errorContainerColor = Color(0xFFF4F5F7),
        focusedTextColor = Color(0xFF292D35),
        unfocusedTextColor = Color(0xFF292D35),
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
        disabledIndicatorColor = Color.Transparent,
        errorIndicatorColor = Color.Transparent,
        cursorColor = Color(0xFF171714)
    )

/**
 * 밀리초 날짜를 화면에 표시할 문자열로 변환한다.
 */
private fun formatMeetingDate(millis: Long): String =
    SimpleDateFormat(
        "yyyy.MM.dd",
        Locale.getDefault()
    ).format(Date(millis))
