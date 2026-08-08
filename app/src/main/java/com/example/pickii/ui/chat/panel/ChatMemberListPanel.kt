package com.example.pickii.ui.chat

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pickii.ui.common.BackHeader
import com.example.pickii.ui.common.SlideInSidePanelScaffold

private val MemberListPanelBackgroundColor = Color.White
private val MemberListPanelScrimColor = Color.Black.copy(alpha = 0.35f)
private val MemberListPrimaryTextColor = Color(0xFF20283A)
private val MemberListSecondaryTextColor = Color(0xFF9CA3AF)
private val MemberListDividerColor = Color(0xFFF0F1F4)
private val LeaderBadgeBackgroundColor = Color(0xFFF2F8A6)
private val LeaderBadgeTextColor = Color(0xFF4A4F00)

/**
 * 채팅방 팀원 목록 패널을 표시한다.
 */
@Composable
fun ChatMemberListPanel(
    members: List<ChatRoomMemberUiModel>,
    onBackClick: () -> Unit,
    onMemberClick: (Long) -> Unit
) {
    BackHandler(
        enabled = true,
        onBack = onBackClick
    )

    SlideInSidePanelScaffold(
        onScrimClick = onBackClick,
        scrimColor = MemberListPanelScrimColor,
        panelBackgroundColor = MemberListPanelBackgroundColor
    ) {
        MemberListHeader(
            onBackClick = onBackClick
        )

        HorizontalDivider(
            color = MemberListDividerColor
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(
                items =
                    members.sortedByDescending { member ->
                        member.isLeader
                    },
                key = { member ->
                    member.memberId
                }
            ) { member ->
                ChatMemberListItem(
                    member = member,
                    onClick = { onMemberClick(member.memberId) }
                )
            }
        }
    }
}

/**
 * 팀원 목록 화면의 상단 헤더를 표시한다.
 */
@Composable
private fun MemberListHeader(onBackClick: () -> Unit) {
    BackHeader(
        title = "팀원 목록",
        onBackClick = onBackClick,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp),
        titleColor = MemberListPrimaryTextColor,
        spacing = 8.dp,
        iconTouchSize = 40.dp
    )
}

/**
 * 팀원 한 명의 정보를 세로 목록 행으로 표시한다.
 */
@Composable
private fun ChatMemberListItem(
    member: ChatRoomMemberUiModel,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClick)
                    .padding(
                        horizontal = 24.dp,
                        vertical = 14.dp
                    ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MemberProfileImage(
                member = member
            )

            Spacer(
                modifier = Modifier.width(14.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = member.name,
                    color = MemberListPrimaryTextColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.size(3.dp)
                )

                Text(
                    text =
                        if (member.isLeader) {
                            "팀장"
                        } else {
                            "팀원"
                        },
                    color = MemberListSecondaryTextColor,
                    fontSize = 12.sp
                )
            }

            if (member.isLeader) {
                Text(
                    text = "팀장",
                    modifier =
                        Modifier
                            .background(
                                color = LeaderBadgeBackgroundColor,
                                shape = RoundedCornerShape(20.dp)
                            ).padding(
                                horizontal = 12.dp,
                                vertical = 6.dp
                            ),
                    color = LeaderBadgeTextColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        HorizontalDivider(
            color = MemberListDividerColor
        )
    }
}

/**
 * 팀원 프로필 이미지를 표시한다.
 */
@Composable
private fun MemberProfileImage(member: ChatRoomMemberUiModel) {
    Box(
        modifier = Modifier.size(52.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier =
                Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        color =
                            createMemberProfileColor(
                                memberId = member.memberId
                            )
                    ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text =
                    member.name
                        .firstOrNull()
                        ?.toString()
                        .orEmpty(),
                color = MemberListPrimaryTextColor,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (member.isLeader) {
            Text(
                text = "👑",
                modifier = Modifier.align(Alignment.TopEnd),
                fontSize = 14.sp
            )
        }
    }
}

/**
 * 팀원별 임시 프로필 색상을 생성한다.
 */
fun createMemberProfileColor(memberId: Long): Color {
    val colors =
        listOf(
            Color(0xFFC9B7FF),
            Color(0xFFF0B3C6),
            Color(0xFFF2C58F),
            Color(0xFFB8E3D1),
            Color(0xFFB9D7FA)
        )

    val colorIndex =
        (memberId % colors.size)
            .toInt()
            .coerceAtLeast(0)

    return colors[colorIndex]
}
