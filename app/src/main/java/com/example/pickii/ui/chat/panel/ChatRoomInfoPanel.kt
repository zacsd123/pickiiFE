package com.example.pickii.ui.chat

import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pickii.R

private val PanelBackgroundColor = Color.White
private val PanelScrimColor = Color.Black.copy(alpha = 0.35f)
private val PanelPrimaryTextColor = Color(0xFF111827)
private val PanelSecondaryTextColor = Color(0xFF9CA3AF)
private val PanelDividerColor = Color(0xFFE5E7EB)
// private val PanelIconBackgroundColor = Color(0xFFF3F4F6)

/**
 * 채팅방 우측 정보 패널을 표시한다.
 */
@Composable
fun ChatRoomInfoPanel(
    uiState: ChatRoomUiState,
    onCloseClick: () -> Unit,
    onNotificationSettingClick: () -> Unit,
    onMemberListClick: () -> Unit,
    onProjectInfoClick: () -> Unit,
    onLeaderDelegationClick: () -> Unit,
    onMemberRemovalClick: () -> Unit,
    onLeaveChatRoomClick: () -> Unit
) {
    BackHandler(
        enabled = true,
        onBack = onCloseClick
    )

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(PanelScrimColor)
                    .clickable(onClick = onCloseClick)
        )

        Column(
            modifier =
                Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.86f)
                    .align(Alignment.CenterEnd)
                    .background(PanelBackgroundColor)
        ) {
            ChatRoomInfoHeader(
                title = createChatRoomInfoTitle(uiState),
                onCloseClick = onCloseClick
            )

            HorizontalDivider(
                color = PanelDividerColor
            )

            ChatRoomMemberSection(
                members = uiState.members
            )

            HorizontalDivider(
                color = PanelDividerColor
            )

            ChatRoomInfoMenuItem(
                iconRes = R.drawable.ic_notification,
                title = "알람 설정",
                status =
                    if (uiState.isNotificationEnabled) {
                        "켜짐"
                    } else {
                        "꺼짐"
                    },
                onClick = onNotificationSettingClick
            )

            ChatRoomInfoMenuItem(
                iconRes = R.drawable.ic_member_list,
                title = "팀원 목록",
                status = "${uiState.memberCount}명",
                onClick = onMemberListClick
            )

            ChatRoomInfoMenuItem(
                iconRes = R.drawable.ic_project_info,
                title = "프로젝트 정보",
                status = uiState.projectStatus.toDisplayText(),
                onClick = onProjectInfoClick
            )

            if (uiState.isCurrentUserLeader) {
                HorizontalDivider(
                    color = PanelDividerColor
                )

                Text(
                    text = "팀장 전용",
                    modifier =
                        Modifier.padding(
                            start = 20.dp,
                            top = 20.dp,
                            bottom = 8.dp
                        ),
                    color = PanelSecondaryTextColor,
                    fontSize = 12.sp
                )

                ChatRoomInfoMenuItem(
                    iconRes = R.drawable.ic_leader_delegation,
                    title = "팀장 위임",
                    status = null,
                    onClick = onLeaderDelegationClick
                )

                ChatRoomInfoMenuItem(
                    iconRes = R.drawable.ic_member_removal,
                    title = "팀원 내보내기",
                    status = null,
                    onClick = onMemberRemovalClick
                )
            }

            HorizontalDivider(
                color = PanelDividerColor
            )

            ChatRoomInfoMenuItem(
                iconRes = R.drawable.ic_leave_chat_room,
                title = "채팅방 나가기",
                status = null,
                onClick = onLeaveChatRoomClick,
                isDestructive = true
            )
        }
    }
}

/**
 * 채팅방 정보 패널 상단 영역이다.
 */
@Composable
private fun ChatRoomInfoHeader(
    title: String,
    onCloseClick: () -> Unit
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 16.dp,
                    vertical = 20.dp
                ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier =
                Modifier
                    .size(40.dp)
                    .clickable(onClick = onCloseClick),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter =
                    painterResource(
                        id = R.drawable.ic_close
                    ),
                contentDescription = "채팅방 정보 닫기",
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(
            modifier = Modifier.width(8.dp)
        )

        Text(
            text = title,
            modifier = Modifier.weight(1f),
            color = PanelPrimaryTextColor,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * 채팅방 팀원 프로필 영역이다.
 */
@Composable
private fun ChatRoomMemberSection(members: List<ChatRoomMemberUiModel>) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 20.dp,
                    vertical = 20.dp
                )
    ) {
        Text(
            text = "팀원 ${members.size}명",
            color = PanelPrimaryTextColor,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(
            modifier = Modifier.size(16.dp)
        )

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(
                        state = rememberScrollState()
                    ),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            members
                .sortedByDescending { member ->
                    member.isLeader
                }.forEach { member ->
                    ChatRoomMemberProfile(
                        member = member
                    )
                }
        }
    }
}

/**
 * 채팅방 팀원 프로필을 표시한다.
 */
@Composable
private fun ChatRoomMemberProfile(member: ChatRoomMemberUiModel) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            contentAlignment = Alignment.TopCenter
        ) {
            Box(
                modifier =
                    Modifier
                        .padding(top = 8.dp)
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE5E7EB)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text =
                        member.name
                            .firstOrNull()
                            ?.toString()
                            .orEmpty(),
                    color = Color(0xFF6B7280),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (member.isLeader) {
                Text(
                    text = "👑",
                    color = Color(0xFFE7B93F),
                    fontSize = 18.sp
                )
            }
        }

        Spacer(
            modifier = Modifier.size(6.dp)
        )

        Text(
            text = member.name,
            color = PanelPrimaryTextColor,
            fontSize = 12.sp,
            maxLines = 1
        )
    }
}

/**
 * 채팅방 정보 메뉴 한 행을 표시한다.
 */
@Composable
private fun ChatRoomInfoMenuItem(
    @DrawableRes iconRes: Int,
    title: String,
    status: String?,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    val titleColor =
        if (isDestructive) {
            Color(0xFFE5484D)
        } else {
            PanelPrimaryTextColor
        }

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(
                    horizontal = 20.dp,
                    vertical = 15.dp
                ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = title,
            modifier = Modifier.size(48.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = title,
            modifier = Modifier.weight(1f),
            color = titleColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )

        if (status != null) {
            Text(
                text = status,
                color = PanelSecondaryTextColor,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.width(10.dp))
        }

        Image(
            painter =
                painterResource(
                    id = R.drawable.ic_chevron_right
                ),
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
    }
}

/**
 * 채팅방 종류에 따라 정보 패널 제목을 생성한다.
 */
private fun createChatRoomInfoTitle(uiState: ChatRoomUiState): String =
    when (uiState.roomType) {
        ChatRoomType.PERSONAL -> {
            "${uiState.personalChatMemberName}님의 채팅방"
        }

        ChatRoomType.GROUP -> {
            val otherMemberCount =
                (uiState.memberCount - 1).coerceAtLeast(0)

            if (otherMemberCount == 0) {
                "${uiState.leaderName}님의 채팅방"
            } else {
                "${uiState.leaderName} 외 ${otherMemberCount}명의 채팅방"
            }
        }
    }
