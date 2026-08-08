package com.example.pickii.ui.chat

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pickii.R

private val MeetingSheetBackgroundColor = Color(0xFFF9FCA8)
private val MeetingCardBackgroundColor = Color.White
private val MeetingPrimaryColor = Color(0xFF1B2130)
private val MeetingSecondaryColor = Color(0xFF9BA1B1)
private val MeetingPointColor = Color(0xFF675CFF)
private val MeetingAttendColor = Color(0xFFE9E9FF)
private val MeetingAbsentColor = Color(0xFFFFE3E3)
private val MeetingDeleteColor = Color(0xFFFF4D4D)
private val MeetingDeleteBackgroundColor = Color(0xFFFFF3F3)

/**
 * 회의 관리 화면에 표시할 확정된 팀 일정 정보다(7-15).
 *
 * 서버가 참여자별 참석/불참 목록을 내려주지 않아(7-15/7-20 모두 본인 처리만 가능), 팀원별 참석 현황 대신
 * 본인의 참석/불참 액션만 제공한다.
 */
data class ManagedMeetingUiModel(
    val id: Long,
    val title: String,
    val date: String,
    val startTime: String,
    val endTime: String
)

/**
 * 확정된 회의(팀 일정) 목록과 내 참석 여부를 관리하는 바텀시트다.
 *
 * @param meetings 확정된 회의 목록
 * @param onDismiss 바텀시트를 닫을 때 실행할 동작
 * @param onDeleteMeeting 회의를 삭제할 때 실행할 동작
 * @param onAttendClick 참석으로 표시할 때 실행할 동작
 * @param onAbsentClick 불참으로 표시할 때 실행할 동작
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeetingManagementBottomSheet(
    meetings: List<ManagedMeetingUiModel>,
    onDismiss: () -> Unit,
    onDeleteMeeting: (Long) -> Unit,
    onAttendClick: (Long) -> Unit,
    onAbsentClick: (Long) -> Unit
) {
    val sheetState =
        rememberModalBottomSheetState(
            skipPartiallyExpanded = true
        )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MeetingSheetBackgroundColor,
        dragHandle = {
            Box(
                modifier =
                    Modifier
                        .padding(top = 14.dp)
                        .size(
                            width = 52.dp,
                            height = 5.dp
                        ).background(
                            color = Color(0xFFBFC18B),
                            shape = RoundedCornerShape(50.dp)
                        )
            )
        }
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxHeight(0.9f)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(
                        horizontal = 22.dp
                    )
        ) {
            MeetingManagementHeader(
                meetingCount = meetings.size,
                onDismiss = onDismiss
            )

            Spacer(
                modifier = Modifier.height(18.dp)
            )

            if (meetings.isEmpty()) {
                EmptyMeetingManagementContent()
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(
                        items = meetings,
                        key = ManagedMeetingUiModel::id
                    ) { meeting ->
                        MeetingManagementCard(
                            meeting = meeting,
                            onDeleteMeeting = {
                                onDeleteMeeting(meeting.id)
                            },
                            onAttendClick = {
                                onAttendClick(meeting.id)
                            },
                            onAbsentClick = {
                                onAbsentClick(meeting.id)
                            }
                        )
                    }

                    item {
                        Spacer(
                            modifier = Modifier.height(24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MeetingManagementHeader(
    meetingCount: Int,
    onDismiss: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "회의 관리",
                color = MeetingPrimaryColor,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(3.dp)
            )

            Text(
                text = "확정된 회의 ${meetingCount}건",
                color = MeetingSecondaryColor,
                fontSize = 14.sp
            )
        }

        Box(
            modifier =
                Modifier
                    .size(42.dp)
                    .background(
                        color = Color(0xFFE9EBAD),
                        shape = CircleShape
                    ).clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter =
                    painterResource(
                        id = R.drawable.ic_close
                    ),
                contentDescription = "닫기",
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun MeetingManagementCard(
    meeting: ManagedMeetingUiModel,
    onDeleteMeeting: () -> Unit,
    onAttendClick: () -> Unit,
    onAbsentClick: () -> Unit
) {
    var isExpanded by rememberSaveable(meeting.id) {
        mutableStateOf(false)
    }

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 3.dp,
                    shape = RoundedCornerShape(20.dp)
                ).background(
                    color = MeetingCardBackgroundColor,
                    shape = RoundedCornerShape(20.dp)
                )
    ) {
        MeetingSummaryContent(
            meeting = meeting,
            isExpanded = isExpanded,
            onClick = {
                isExpanded = !isExpanded
            }
        )

        if (isExpanded) {
            HorizontalDivider(
                color = Color(0xFFF0F0F2)
            )

            Column(
                modifier =
                    Modifier.padding(
                        start = 20.dp,
                        end = 20.dp,
                        bottom = 20.dp
                    )
            ) {
                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MeetingAttendanceButton(
                        text = "참석",
                        backgroundColor = MeetingAttendColor,
                        contentColor = MeetingPointColor,
                        onClick = onAttendClick,
                        modifier = Modifier.weight(1f)
                    )

                    MeetingAttendanceButton(
                        text = "불참",
                        backgroundColor = MeetingAbsentColor,
                        contentColor = MeetingDeleteColor,
                        onClick = onAbsentClick,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                MeetingDeleteButton(
                    onClick = onDeleteMeeting
                )
            }
        }
    }
}

@Composable
private fun MeetingSummaryContent(
    meeting: ManagedMeetingUiModel,
    isExpanded: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(
                    horizontal = 20.dp,
                    vertical = 18.dp
                ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier =
                Modifier
                    .size(10.dp)
                    .background(
                        color = MeetingPointColor,
                        shape = CircleShape
                    )
        )

        Spacer(
            modifier = Modifier.width(14.dp)
        )

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = meeting.title,
                color = MeetingPrimaryColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = meeting.date,
                    color = MeetingPointColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.width(4.dp)
                )

                Text(
                    text = "${meeting.startTime} ~ ${meeting.endTime}",
                    color = MeetingSecondaryColor,
                    fontSize = 14.sp
                )
            }
        }

        Image(
            painter =
                painterResource(
                    id =
                        if (isExpanded) {
                            R.drawable.ic_chevron_up
                        } else {
                            R.drawable.ic_chevron_down
                        }
                ),
            contentDescription =
                if (isExpanded) {
                    "회의 정보 접기"
                } else {
                    "회의 정보 펼치기"
                },
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun MeetingAttendanceButton(
    text: String,
    backgroundColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier =
            modifier
                .height(44.dp)
                .background(
                    color = backgroundColor,
                    shape = RoundedCornerShape(14.dp)
                ).clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = contentColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun MeetingDeleteButton(onClick: () -> Unit) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(
                    color = MeetingDeleteBackgroundColor,
                    shape = RoundedCornerShape(16.dp)
                ).clickable(onClick = onClick)
                .padding(
                    vertical = 16.dp
                ),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter =
                painterResource(
                    id = R.drawable.ic_delete
                ),
            contentDescription = null,
            modifier = Modifier.size(19.dp)
        )

        Spacer(
            modifier = Modifier.width(7.dp)
        )

        Text(
            text = "회의 삭제",
            color = MeetingDeleteColor,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun EmptyMeetingManagementContent() {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    top = 120.dp
                ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "확정된 회의가 없습니다.",
            color = MeetingSecondaryColor,
            fontSize = 15.sp
        )
    }
}
