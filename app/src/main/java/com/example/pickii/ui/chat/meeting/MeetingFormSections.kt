package com.example.pickii.ui.chat

import android.app.TimePickerDialog
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pickii.domain.model.CalendarSchedule
import com.example.pickii.ui.theme.PickiiBlackAlt
import com.example.pickii.ui.theme.PickiiCharcoal
import com.example.pickii.ui.theme.PickiiDividerAlt
import com.example.pickii.ui.theme.PickiiGray500
import com.example.pickii.ui.theme.PickiiGray700
import com.example.pickii.ui.theme.PickiiGraySecondary
import com.example.pickii.ui.theme.PickiiGraySlate
import com.example.pickii.ui.theme.PickiiSlateDark
import com.example.pickii.ui.theme.PickiiSurfaceGray
import java.text.SimpleDateFormat
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.util.Date
import java.util.Locale

/**
 * 회의 소요 시간을 선택한다. 30/60/90/120분만 유효하다(데모 시나리오 명시).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun MeetingDurationSection(
    selectedDuration: Int,
    onDurationSelected: (Int) -> Unit
) {
    val durationOptions =
        listOf(
            30 to "30분",
            60 to "1시간",
            90 to "1시간 30분",
            120 to "2시간",
            150 to "2시간 30분",
            180 to "3시간",
            181 to "3시간 +"
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
internal fun MeetingDateRangeSection(
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
                color = PickiiGray500,
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
 * 하루 중 후보 슬롯을 탐색할 시간대(dayStart~dayEnd)를 선택한다.
 */
@Composable
internal fun MeetingTimeRangeSection(
    dayStartMinute: Int,
    dayEndMinute: Int,
    onDayStartChange: (Int) -> Unit,
    onDayEndChange: (Int) -> Unit
) {
    Column {
        SectionTitle(
            title = "탐색 시간대",
            description = "하루 중 후보를 찾을 시간"
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TimeOfDayBox(
                minuteOfDay = dayStartMinute,
                modifier = Modifier.weight(1f),
                onMinuteOfDayChange = onDayStartChange
            )

            Text(
                text = "~",
                modifier = Modifier.padding(horizontal = 8.dp),
                color = PickiiGray500,
                fontSize = 14.sp
            )

            TimeOfDayBox(
                minuteOfDay = dayEndMinute,
                modifier = Modifier.weight(1f),
                onMinuteOfDayChange = onDayEndChange
            )
        }
    }
}

/**
 * 자정 기준 분을 시:분으로 표시하고, 클릭하면 네이티브 시간 선택기를 연다.
 */
@Composable
private fun TimeOfDayBox(
    minuteOfDay: Int,
    modifier: Modifier = Modifier,
    onMinuteOfDayChange: (Int) -> Unit
) {
    val context = LocalContext.current

    Box(
        modifier =
            modifier
                .height(52.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(PickiiSurfaceGray)
                .clickable {
                    TimePickerDialog(
                        context,
                        { _, hourOfDay, minute -> onMinuteOfDayChange(hourOfDay * 60 + minute) },
                        minuteOfDay / 60,
                        minuteOfDay % 60,
                        true
                    ).show()
                }.padding(horizontal = 14.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = "%02d:%02d".format(minuteOfDay / 60, minuteOfDay % 60),
            color = PickiiSlateDark,
            fontSize = 14.sp
        )
    }
}

/**
 * 응답 마감까지 남길 시간을 프리셋 중에서 고른다(서버 기본값 12시간).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun MeetingDeadlineSection(
    selectedHours: Int,
    onHoursSelected: (Int) -> Unit
) {
    val hourOptions = listOf(6, 12, 24, 48)

    Column {
        SectionTitle(
            title = "응답 마감",
            description = "기본 12시간"
        )

        Spacer(modifier = Modifier.height(10.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            hourOptions.forEach { hours ->
                MeetingSelectChip(
                    text = "${hours}시간",
                    selected = selectedHours == hours,
                    onClick = { onHoursSelected(hours) }
                )
            }
        }
    }
}

/**
 * 조율에 참여할 팀원을 고른다. 기본은 전원 선택(체크 해제하면 그 팀원은 응답 대상에서 빠진다).
 */
@Composable
internal fun MeetingParticipantsSection(
    members: List<ChatRoomMemberUiModel>,
    selectedMemberIds: Set<Long>,
    onToggleMember: (Long) -> Unit
) {
    Column {
        SectionTitle(
            title = "참가자",
            description = "이 회의에 필요한 팀원만 선택"
        )

        Spacer(modifier = Modifier.height(10.dp))

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            members.forEach { member ->
                val isSelected = member.memberId in selectedMemberIds
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onToggleMember(member.memberId) }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier =
                            Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) PickiiCharcoal else PickiiDividerAlt),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Text(text = "✓", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = member.name,
                        color = PickiiSlateDark,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
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
                .background(PickiiSurfaceGray)
                .clickable(onClick = onClick)
                .padding(horizontal = 14.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = dateMillis?.let(::formatMeetingDate) ?: placeholder,
            color =
                if (dateMillis == null) {
                    PickiiGray500
                } else {
                    PickiiSlateDark
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
internal fun MeetingDateRangePickerDialog(
    initialStartDateMillis: Long?,
    initialEndDateMillis: Long?,
    registeredSchedules: List<CalendarSchedule> = emptyList(),
    onDismiss: () -> Unit,
    onConfirm: (Long, Long) -> Unit
) {
    val latestRegisteredSchedules by rememberUpdatedState(registeredSchedules)
    val selectableDates =
        remember {
            object : SelectableDates {
                @OptIn(ExperimentalTime::class)
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    val candidate = Instant.fromEpochMilliseconds(utcTimeMillis).toLocalDateTime(TimeZone.UTC).date
                    return latestRegisteredSchedules.none { it.includesDate(candidate) }
                }
            }
        }

    val dateRangePickerState =
        rememberDateRangePickerState(
            initialSelectedStartDateMillis = initialStartDateMillis,
            initialSelectedEndDateMillis = initialEndDateMillis,
            selectableDates = selectableDates
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
                    color = PickiiCharcoal,
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
                    color = PickiiGraySlate
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
                    color = PickiiBlackAlt,
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
                    color = PickiiGraySlate,
                    fontSize = 14.sp
                )
            },
            showModeToggle = false
        )
    }
}

/**
 * 소요 시간을 선택할 때 사용하는 공통 버튼이다.
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
                        PickiiCharcoal
                    } else {
                        PickiiSurfaceGray
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
                    PickiiGraySecondary
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
            color = PickiiGray700,
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
                    color = PickiiGray500,
                    fontSize = 11.sp
                )
            }
        }

        if (description != null) {
            Text(
                text = "  ($description)",
                color = PickiiGray500,
                fontSize = 10.sp,
                maxLines = 1
            )
        }
    }
}

/**
 * 밀리초 날짜를 화면에 표시할 문자열로 변환한다.
 */
private fun formatMeetingDate(millis: Long): String =
    SimpleDateFormat(
        "yyyy.MM.dd",
        Locale.getDefault()
    ).format(Date(millis))
