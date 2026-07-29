package com.example.pickii.ui.chat

import com.example.pickii.R
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val MeetingSheetBackgroundColor = Color(0xFFF9FCA8)
private val MeetingCardBackgroundColor = Color.White
private val MeetingPrimaryColor = Color(0xFF1B2130)
private val MeetingSecondaryColor = Color(0xFF9BA1B1)
private val MeetingPointColor = Color(0xFF675CFF)
private val MeetingMemberBackgroundColor = Color(0xFFF6F7FC)
private val MeetingDeleteColor = Color(0xFFFF4D4D)
private val MeetingDeleteBackgroundColor = Color(0xFFFFF3F3)

/**
 * 회의 관리 화면에 표시할 회의 정보다.
 */
data class ManagedMeetingUiModel(
    val id: Long,
    val title: String,
    val date: String,
    val startTime: String,
    val endTime: String,
    val participants: List<MeetingMemberUiModel>,
    val absentees: List<MeetingMemberUiModel>,
)

/**
 * 회의 참여자 정보를 나타낸다.
 */
data class MeetingMemberUiModel(
    val id: Long,
    val name: String,
)

/**
 * 예정된 회의와 참여 여부를 관리하는 바텀시트다.
 *
 * @param meetings 예정된 회의 목록
 * @param onDismiss 바텀시트를 닫을 때 실행할 동작
 * @param onDeleteMeeting 회의를 삭제할 때 실행할 동작
 * @param onMoveToAbsent 참여자를 불참자로 변경할 때 실행할 동작
 * @param onMoveToParticipant 불참자를 참여자로 변경할 때 실행할 동작
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeetingManagementBottomSheet(
    meetings: List<ManagedMeetingUiModel>,
    onDismiss: () -> Unit,
    onDeleteMeeting: (Long) -> Unit,
    onMoveToAbsent: (
        meetingId: Long,
        memberId: Long,
    ) -> Unit,
    onMoveToParticipant: (
        meetingId: Long,
        memberId: Long,
    ) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MeetingSheetBackgroundColor,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 14.dp)
                    .size(
                        width = 52.dp,
                        height = 5.dp,
                    )
                    .background(
                        color = Color(0xFFBFC18B),
                        shape = RoundedCornerShape(50.dp),
                    ),
            )
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight(0.9f)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(
                    horizontal = 22.dp,
                ),
        ) {
            MeetingManagementHeader(
                meetingCount = meetings.size,
                onDismiss = onDismiss,
            )

            Spacer(
                modifier = Modifier.height(18.dp),
            )

            if (meetings.isEmpty()) {
                EmptyMeetingManagementContent()
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    items(
                        items = meetings,
                        key = ManagedMeetingUiModel::id,
                    ) { meeting ->
                        MeetingManagementCard(
                            meeting = meeting,
                            onDeleteMeeting = {
                                onDeleteMeeting(meeting.id)
                            },
                            onMoveToAbsent = { memberId ->
                                onMoveToAbsent(
                                    meeting.id,
                                    memberId,
                                )
                            },
                            onMoveToParticipant = { memberId ->
                                onMoveToParticipant(
                                    meeting.id,
                                    memberId,
                                )
                            },
                        )
                    }

                    item {
                        Spacer(
                            modifier = Modifier.height(24.dp),
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
    onDismiss: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = "회의 관리",
                color = MeetingPrimaryColor,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
            )

            Spacer(
                modifier = Modifier.height(3.dp),
            )

            Text(
                text = "예정된 회의 ${meetingCount}건",
                color = MeetingSecondaryColor,
                fontSize = 14.sp,
            )
        }

        Box(
            modifier = Modifier
                .size(42.dp)
                .background(
                    color = Color(0xFFE9EBAD),
                    shape = CircleShape,
                )
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(
                    id = R.drawable.ic_close,
                ),
                contentDescription = "닫기",
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

@Composable
private fun MeetingManagementCard(
    meeting: ManagedMeetingUiModel,
    onDeleteMeeting: () -> Unit,
    onMoveToAbsent: (Long) -> Unit,
    onMoveToParticipant: (Long) -> Unit,
) {
    var isExpanded by rememberSaveable(meeting.id) {
        mutableStateOf(false)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 3.dp,
                shape = RoundedCornerShape(20.dp),
            )
            .background(
                color = MeetingCardBackgroundColor,
                shape = RoundedCornerShape(20.dp),
            ),
    ) {
        MeetingSummaryContent(
            meeting = meeting,
            isExpanded = isExpanded,
            onClick = {
                isExpanded = !isExpanded
            },
        )

        if (isExpanded) {
            HorizontalDivider(
                color = Color(0xFFF0F0F2),
            )

            Column(
                modifier = Modifier.padding(
                    start = 20.dp,
                    end = 20.dp,
                    bottom = 20.dp,
                ),
            ) {
                Spacer(
                    modifier = Modifier.height(16.dp),
                )

                MeetingMemberSection(
                    title = "참여",
                    count = meeting.participants.size,
                    isParticipantSection = true,
                    members = meeting.participants,
                    onStatusClick = onMoveToAbsent,
                )

                Spacer(
                    modifier = Modifier.height(18.dp),
                )

                MeetingMemberSection(
                    title = "불참",
                    count = meeting.absentees.size,
                    isParticipantSection = false,
                    members = meeting.absentees,
                    onStatusClick = onMoveToParticipant,
                )

                Spacer(
                    modifier = Modifier.height(16.dp),
                )

                MeetingDeleteButton(
                    onClick = onDeleteMeeting,
                )
            }
        }
    }
}

@Composable
private fun MeetingSummaryContent(
    meeting: ManagedMeetingUiModel,
    isExpanded: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(
                horizontal = 20.dp,
                vertical = 18.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(
                    color = MeetingPointColor,
                    shape = CircleShape,
                ),
        )

        Spacer(
            modifier = Modifier.width(14.dp),
        )

        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = meeting.title,
                color = MeetingPrimaryColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )

            Spacer(
                modifier = Modifier.height(4.dp),
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = meeting.date,
                    color = MeetingPointColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                )

                Spacer(
                    modifier = Modifier.width(4.dp),
                )

                Text(
                    text = "${meeting.startTime} ~ ${meeting.endTime}",
                    color = MeetingSecondaryColor,
                    fontSize = 14.sp,
                )
            }
        }

        MeetingParticipantCountBadge(
            participantCount = meeting.participants.size,
        )

        Spacer(
            modifier = Modifier.width(12.dp),
        )

        Image(
            painter = painterResource(
                id = if (isExpanded) {
                    R.drawable.ic_chevron_up
                } else {
                    R.drawable.ic_chevron_down
                },
            ),
            contentDescription = if (isExpanded) {
                "회의 정보 접기"
            } else {
                "회의 정보 펼치기"
            },
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun MeetingParticipantCountBadge(
    participantCount: Int,
) {
    Row(
        modifier = Modifier
            .background(
                color = Color(0xFFF1F0FF),
                shape = RoundedCornerShape(20.dp),
            )
            .padding(
                horizontal = 10.dp,
                vertical = 6.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(
                id = R.drawable.ic_people,
            ),
            contentDescription = null,
            modifier = Modifier.size(16.dp),
        )

        Spacer(
            modifier = Modifier.width(4.dp),
        )

        Text(
            text = "${participantCount}명",
            color = MeetingPointColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun MeetingMemberSection(
    title: String,
    count: Int,
    isParticipantSection: Boolean,
    members: List<MeetingMemberUiModel>,
    onStatusClick: (Long) -> Unit,
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(
                    id = if (isParticipantSection) {
                        R.drawable.ic_profile
                    } else {
                        R.drawable.ic_person_off
                    },
                ),
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )

            Spacer(
                modifier = Modifier.width(7.dp),
            )

            Text(
                text = title,
                color = MeetingPrimaryColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
            )

            Spacer(
                modifier = Modifier.weight(1f),
            )

            Text(
                text = "${count}명",
                color = if (isParticipantSection) {
                    MeetingPointColor
                } else {
                    MeetingSecondaryColor
                },
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        Spacer(
            modifier = Modifier.height(8.dp),
        )

        if (members.isEmpty()) {
            Text(
                text = "해당하는 팀원이 없습니다.",
                modifier = Modifier.padding(
                    vertical = 12.dp,
                ),
                color = MeetingSecondaryColor,
                fontSize = 13.sp,
            )
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                members.forEach { member ->
                    MeetingMemberItem(
                        member = member,
                        isParticipant = isParticipantSection,
                        onStatusClick = {
                            onStatusClick(member.id)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun MeetingMemberItem(
    member: MeetingMemberUiModel,
    isParticipant: Boolean,
    onStatusClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MeetingMemberBackgroundColor,
                shape = RoundedCornerShape(16.dp),
            )
            .padding(
                horizontal = 14.dp,
                vertical = 10.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .background(
                    color = Color(0xFFE8E9F2),
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(
                    id = R.drawable.ic_profile,
                ),
                contentDescription = null,
                modifier = Modifier.size(17.dp),
            )
        }

        Spacer(
            modifier = Modifier.width(10.dp),
        )

        Text(
            text = member.name,
            modifier = Modifier.weight(1f),
            color = if (isParticipant) {
                MeetingPrimaryColor
            } else {
                MeetingSecondaryColor
            },
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
        )

        Box(
            modifier = Modifier
                .size(28.dp)
                .background(
                    color = if (isParticipant) {
                        Color(0xFFFFE3E3)
                    } else {
                        Color(0xFFE9E9FF)
                    },
                    shape = CircleShape,
                )
                .clickable(onClick = onStatusClick),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = if (isParticipant) "−" else "+",
                color = if (isParticipant) {
                    MeetingDeleteColor
                } else {
                    MeetingPointColor
                },
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun MeetingDeleteButton(
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MeetingDeleteBackgroundColor,
                shape = RoundedCornerShape(16.dp),
            )
            .clickable(onClick = onClick)
            .padding(
                vertical = 16.dp,
            ),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(
                id = R.drawable.ic_delete,
            ),
            contentDescription = null,
            modifier = Modifier.size(19.dp),
        )

        Spacer(
            modifier = Modifier.width(7.dp),
        )

        Text(
            text = "회의 삭제",
            color = MeetingDeleteColor,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun EmptyMeetingManagementContent() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                top = 120.dp,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "예정된 회의가 없습니다.",
            color = MeetingSecondaryColor,
            fontSize = 15.sp,
        )
    }
}