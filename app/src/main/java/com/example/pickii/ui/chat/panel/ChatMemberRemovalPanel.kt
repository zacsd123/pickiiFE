package com.example.pickii.ui.chat

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val MemberRemovalPanelBackgroundColor = Color.White
private val MemberRemovalPanelScrimColor = Color.Black.copy(alpha = 0.35f)
private val MemberRemovalPrimaryTextColor = Color(0xFF20283A)
private val MemberRemovalSecondaryTextColor = Color(0xFF9CA3AF)
private val MemberRemovalDividerColor = Color(0xFFF0F1F4)
private val MemberRemovalSelectedColor = Color(0xFFFFF4F3)
private val MemberRemovalButtonColor = Color(0xFFD23B32)
private val MemberRemovalDisabledButtonColor = Color(0xFFE2E3E7)

/**
 * 채팅방에서 내보낼 팀원을 선택하는 패널을 표시한다.
 */
@Composable
fun ChatMemberRemovalPanel(
    members: List<ChatRoomMemberUiModel>,
    onBackClick: () -> Unit,
    onRemoveClick: (Long) -> Unit
) {
    var selectedMemberId by rememberSaveable {
        mutableStateOf<Long?>(null)
    }

    val removableMembers =
        members.filterNot { member ->
            member.isLeader
        }

    BackHandler(
        enabled = true,
        onBack = onBackClick
    )

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(MemberRemovalPanelScrimColor)
                    .clickable(onClick = onBackClick)
        )

        Column(
            modifier =
                Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.79f)
                    .align(Alignment.CenterEnd)
                    .background(MemberRemovalPanelBackgroundColor)
        ) {
            MemberRemovalHeader(
                onBackClick = onBackClick
            )

            HorizontalDivider(
                color = MemberRemovalDividerColor
            )

            Text(
                text = "내보낼 팀원을 선택하세요",
                modifier =
                    Modifier.padding(
                        start = 24.dp,
                        top = 18.dp,
                        bottom = 8.dp
                    ),
                color = MemberRemovalSecondaryTextColor,
                fontSize = 13.sp
            )

            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(
                    items = removableMembers,
                    key = { member ->
                        member.memberId
                    }
                ) { member ->
                    MemberRemovalItem(
                        member = member,
                        isSelected = selectedMemberId == member.memberId,
                        onClick = {
                            selectedMemberId = member.memberId
                        }
                    )
                }
            }

            MemberRemovalButton(
                isEnabled = selectedMemberId != null,
                onClick = {
                    selectedMemberId?.let(onRemoveClick)
                }
            )
        }
    }
}

/**
 * 팀원 내보내기 패널의 상단 영역을 표시한다.
 */
@Composable
private fun MemberRemovalHeader(onBackClick: () -> Unit) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 20.dp,
                    vertical = 20.dp
                ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier =
                Modifier
                    .size(40.dp)
                    .clickable(onClick = onBackClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(
            modifier = Modifier.width(8.dp)
        )

        Text(
            text = "팀원 내보내기",
            color = MemberRemovalPrimaryTextColor,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * 내보내기 대상 팀원 한 명을 표시한다.
 */
@Composable
private fun MemberRemovalItem(
    member: ChatRoomMemberUiModel,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(
                        color =
                            if (isSelected) {
                                MemberRemovalSelectedColor
                            } else {
                                Color.Transparent
                            }
                    ).clickable(onClick = onClick)
                    .padding(
                        horizontal = 24.dp,
                        vertical = 14.dp
                    ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier =
                    Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            color =
                                createRemovalProfileColor(
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
                    color = MemberRemovalPrimaryTextColor,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(
                modifier = Modifier.width(14.dp)
            )

            Text(
                text = member.name,
                modifier = Modifier.weight(1f),
                color = MemberRemovalPrimaryTextColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            if (isSelected) {
                Box(
                    modifier =
                        Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(MemberRemovalButtonColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✓",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        HorizontalDivider(
            color = MemberRemovalDividerColor
        )
    }
}

/**
 * 팀원 내보내기 실행 버튼을 표시한다.
 */
@Composable
private fun MemberRemovalButton(
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(
                    horizontal = 24.dp,
                    vertical = 18.dp
                ).clip(RoundedCornerShape(18.dp))
                .background(
                    color =
                        if (isEnabled) {
                            MemberRemovalButtonColor
                        } else {
                            MemberRemovalDisabledButtonColor
                        }
                ).clickable(
                    enabled = isEnabled,
                    onClick = onClick
                ).padding(vertical = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "내보내기",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * 팀원별 임시 프로필 배경색을 반환한다.
 */
private fun createRemovalProfileColor(memberId: Long): Color {
    val colors =
        listOf(
            Color(0xFFC9B7FF),
            Color(0xFFF0B3C6),
            Color(0xFFF2C58F),
            Color(0xFFB8E3D1),
            Color(0xFFB9D7FA)
        )

    return colors[(memberId % colors.size).toInt()]
}
