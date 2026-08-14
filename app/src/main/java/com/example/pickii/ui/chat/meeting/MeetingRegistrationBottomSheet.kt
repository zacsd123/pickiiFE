package com.example.pickii.ui.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pickii.domain.model.CalendarSchedule
import com.example.pickii.ui.theme.PickiiBlackAlt
import com.example.pickii.ui.theme.PickiiCharcoal
import com.example.pickii.ui.theme.PickiiDividerAlt
import com.example.pickii.ui.theme.PickiiGray400
import com.example.pickii.ui.theme.PickiiGray500
import com.example.pickii.ui.theme.PickiiGray700
import com.example.pickii.ui.theme.PickiiGraySlate
import com.example.pickii.ui.theme.PickiiSlateDark
import com.example.pickii.ui.theme.PickiiSurfaceGray

private const val MEETING_TITLE_MAX_LENGTH = 20
private const val DEFAULT_DAY_START_MINUTE = 9 * 60
private const val DEFAULT_DAY_END_MINUTE = 22 * 60
private const val DEFAULT_DEADLINE_HOURS = 12

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
    members: List<ChatRoomMemberUiModel>,
    registeredSchedules: List<CalendarSchedule> = emptyList(),
    onDismiss: () -> Unit,
    onNextClick: (QuickMeetingForm) -> Unit
) {
    var meetingTitle by remember {
        mutableStateOf("")
    }

    var selectedDuration by remember {
        mutableIntStateOf(60)
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

    var dayStartMinute by remember {
        mutableIntStateOf(DEFAULT_DAY_START_MINUTE)
    }

    var dayEndMinute by remember {
        mutableIntStateOf(DEFAULT_DAY_END_MINUTE)
    }

    var deadlineHours by remember {
        mutableIntStateOf(DEFAULT_DEADLINE_HOURS)
    }

    var selectedMemberIds by remember(members) {
        mutableStateOf(members.map { it.memberId }.toSet())
    }

    val sheetState =
        rememberModalBottomSheetState(
            skipPartiallyExpanded = true
        )

    val isFormValid =
        meetingTitle.isNotBlank() &&
            startDateMillis != null &&
            endDateMillis != null &&
            dayStartMinute < dayEndMinute

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

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "회의 시간을 조율하면 팀원들에게 알림이 가고, 팀원들이 가능한 시간을 선택하면 확정할 수 있어요.",
                color = PickiiGray400,
                fontSize = 12.sp
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

            MeetingTimeRangeSection(
                dayStartMinute = dayStartMinute,
                dayEndMinute = dayEndMinute,
                onDayStartChange = { dayStartMinute = it },
                onDayEndChange = { dayEndMinute = it }
            )

            Spacer(modifier = Modifier.height(22.dp))

            MeetingDeadlineSection(
                selectedHours = deadlineHours,
                onHoursSelected = { deadlineHours = it }
            )

            if (members.isNotEmpty()) {
                Spacer(modifier = Modifier.height(22.dp))

                MeetingParticipantsSection(
                    members = members,
                    selectedMemberIds = selectedMemberIds,
                    onToggleMember = { memberId ->
                        selectedMemberIds =
                            if (memberId in selectedMemberIds) {
                                selectedMemberIds - memberId
                            } else {
                                selectedMemberIds + memberId
                            }
                    }
                )
            }

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
                                dayStartMinuteOfDay = dayStartMinute,
                                dayEndMinuteOfDay = dayEndMinute,
                                deadlineHours = deadlineHours,
                                // 전원 선택 상태면 굳이 보내지 않는다(7-10 "미지정 시 전원"과 같은 의미).
                                memberIds =
                                    if (selectedMemberIds.size == members.size) {
                                        emptySet()
                                    } else {
                                        selectedMemberIds
                                    }
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
                        containerColor = PickiiCharcoal,
                        contentColor = Color.White,
                        disabledContainerColor = PickiiDividerAlt,
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
            registeredSchedules = registeredSchedules,
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
            color = PickiiBlackAlt,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        IconButton(
            onClick = onCloseClick
        ) {
            Text(
                text = "×",
                color = PickiiGraySlate,
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "회의명",
                    color = PickiiGray700,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = " 필수",
                    color = Color(0xFFFF7373),
                    fontSize = 11.sp
                )
            }

            Text(
                text = "${title.length}/$MEETING_TITLE_MAX_LENGTH",
                color = PickiiGray500,
                fontSize = 11.sp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        TextField(
            value = title,
            onValueChange = { onTitleChange(it.take(MEETING_TITLE_MAX_LENGTH)) },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            placeholder = {
                Text(
                    text = "회의명을 입력하세요",
                    color = PickiiGray400,
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
 * 회의 등록 입력창에 공통으로 적용하는 색상이다.
 */
@Composable
private fun meetingTextFieldColors() =
    TextFieldDefaults.colors(
        focusedContainerColor = PickiiSurfaceGray,
        unfocusedContainerColor = PickiiSurfaceGray,
        disabledContainerColor = PickiiSurfaceGray,
        errorContainerColor = PickiiSurfaceGray,
        focusedTextColor = PickiiSlateDark,
        unfocusedTextColor = PickiiSlateDark,
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
        disabledIndicatorColor = Color.Transparent,
        errorIndicatorColor = Color.Transparent,
        cursorColor = PickiiCharcoal
    )
