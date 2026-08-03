package com.example.pickii.ui.chat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeetingConfirmBottomSheet(
    meeting: QuickMeetingForm,
    onPreviousClick: () -> Unit,
    onCancelClick: () -> Unit,
    onSendClick: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    )

    ModalBottomSheet(
        onDismissRequest = onCancelClick,
        sheetState = sheetState,
        containerColor = Color.White,
        dragHandle = null,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(
                    horizontal = 20.dp,
                    vertical = 20.dp,
                ),
        ) {
            MeetingConfirmHeader(
                onPreviousClick = onPreviousClick,
                onCancelClick = onCancelClick,
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "입력한 내용을 확인해 주세요",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF171714),
            )

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = Color(0xFFFAFAB0),
                        shape = RoundedCornerShape(18.dp),
                    )
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                MeetingConfirmRow(
                    label = "회의명",
                    value = meeting.title,
                )

                MeetingConfirmRow(
                    label = "회의 시간",
                    value = formatMeetingDuration(meeting.durationMinutes),
                )

                MeetingConfirmRow(
                    label = "일정 수집 기간",
                    value = "${formatMeetingDate(meeting.startDateMillis)} ~ " +
                            formatMeetingDate(meeting.endDateMillis),
                )

                MeetingConfirmRow(
                    label = "후보 일정 개수",
                    value = "${meeting.candidateCount}개",
                )

                MeetingConfirmRow(
                    label = "메모",
                    value = meeting.memo.ifBlank {
                        "작성된 메모가 없습니다."
                    },
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "등록 후 팀원들에게 개인 일정 등록 요청이 전송됩니다. 팀원들이 일정을 등록하면 최적의 회의 시간을 추려드려요.",
                fontSize = 13.sp,
                color = Color(0xFF717784),
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onSendClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF171714),
                    contentColor = Color.White,
                ),
            ) {
                Text(
                    text = "팀원에게 전송하기",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun MeetingConfirmHeader(
    onPreviousClick: () -> Unit,
    onCancelClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "‹",
            modifier = Modifier
                .width(40.dp)
                .clickable(onClick = onPreviousClick)
                .padding(vertical = 4.dp),
            fontSize = 32.sp,
            color = Color(0xFF171714),
        )

        Text(
            text = "등록 확인",
            modifier = Modifier.weight(1f),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF171714),
        )

        Text(
            text = "취소",
            modifier = Modifier
                .clickable(onClick = onCancelClick)
                .padding(
                    horizontal = 4.dp,
                    vertical = 8.dp,
                ),
            fontSize = 14.sp,
            color = Color(0xFF717784),
        )
    }
}

@Composable
private fun MeetingConfirmRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = label,
            modifier = Modifier.width(110.dp),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF717784),
        )

        Text(
            text = value,
            modifier = Modifier.weight(1f),
            fontSize = 14.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF171714),
        )
    }
}

private fun formatMeetingDuration(
    minutes: Int,
): String {
    return when (minutes) {
        30 -> "30분"
        60 -> "1시간"
        90 -> "1시간 30분"
        120 -> "2시간"
        180 -> "3시간"
        else -> "${minutes}분"
    }
}

private fun formatMeetingDate(
    dateMillis: Long,
): String {
    return SimpleDateFormat(
        "yyyy.MM.dd",
        Locale.getDefault(),
    ).format(Date(dateMillis))
}