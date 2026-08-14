package com.example.pickii.ui.chat

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
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
import com.example.pickii.ui.common.BackHeader
import com.example.pickii.ui.common.SlideInSidePanelScaffold
import com.example.pickii.ui.theme.PickiiBlackSoft
import com.example.pickii.ui.theme.PickiiGray400
import com.example.pickii.ui.theme.PickiiNavyText
import com.example.pickii.ui.theme.PickiiSurfaceGrayLight

private val LeaderDelegationPanelBackgroundColor = Color.White
private val LeaderDelegationPanelScrimColor =
    Color.Black.copy(alpha = 0.35f)

private val LeaderDelegationPrimaryTextColor = PickiiNavyText
private val LeaderDelegationSecondaryTextColor = PickiiGray400
private val LeaderDelegationDividerColor = PickiiSurfaceGrayLight
private val LeaderDelegationSelectedColor = Color(0xFFF7F8FA)
private val LeaderDelegationButtonColor = PickiiBlackSoft
private val LeaderDelegationButtonTextColor = Color(0xFFF2F77F)

/**
 * 팀장 권한을 위임할 팀원을 선택하는 패널을 표시한다.
 */
@Composable
fun ChatLeaderDelegationPanel(
    members: List<ChatRoomMemberUiModel>,
    onBackClick: () -> Unit,
    onDelegateClick: (Long) -> Unit
) {
    var selectedMemberId by rememberSaveable {
        mutableStateOf<Long?>(null)
    }

    val delegatableMembers =
        members.filterNot { member ->
            member.isLeader
        }

    BackHandler(
        enabled = true,
        onBack = onBackClick
    )

    SlideInSidePanelScaffold(
        onScrimClick = onBackClick,
        scrimColor = LeaderDelegationPanelScrimColor,
        panelBackgroundColor = LeaderDelegationPanelBackgroundColor
    ) {
        LeaderDelegationHeader(
            onBackClick = onBackClick
        )

        HorizontalDivider(
            color = LeaderDelegationDividerColor
        )

        Text(
            text = "팀장 권한을 위임할 팀원을 선택하세요",
            modifier =
                Modifier.padding(
                    start = 24.dp,
                    top = 18.dp,
                    bottom = 8.dp
                ),
            color = LeaderDelegationSecondaryTextColor,
            fontSize = 13.sp
        )

        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(
                items = delegatableMembers,
                key = { member ->
                    member.memberId
                }
            ) { member ->
                LeaderDelegationMemberItem(
                    member = member,
                    isSelected = selectedMemberId == member.memberId,
                    onClick = {
                        selectedMemberId = member.memberId
                    }
                )
            }
        }

        LeaderDelegationButton(
            isEnabled = selectedMemberId != null,
            onClick = {
                selectedMemberId?.let(onDelegateClick)
            }
        )
    }
}

/**
 * 팀장 위임 패널의 상단 영역을 표시한다.
 */
@Composable
private fun LeaderDelegationHeader(onBackClick: () -> Unit) {
    BackHeader(
        title = "팀장 위임",
        onBackClick = onBackClick,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp),
        titleColor = LeaderDelegationPrimaryTextColor,
        spacing = 8.dp,
        iconTouchSize = 40.dp
    )
}

/**
 * 팀장 위임 후보 한 명을 표시한다.
 */
@Composable
private fun LeaderDelegationMemberItem(
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
                                LeaderDelegationSelectedColor
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
                                createDelegationProfileColor(
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
                    color = LeaderDelegationPrimaryTextColor,
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
                color = LeaderDelegationPrimaryTextColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            if (isSelected) {
                Box(
                    modifier =
                        Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(LeaderDelegationButtonColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✓",
                        color = LeaderDelegationButtonTextColor,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        HorizontalDivider(
            color = LeaderDelegationDividerColor
        )
    }
}

/**
 * 팀장 위임 실행 버튼을 표시한다.
 */
@Composable
private fun LeaderDelegationButton(
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
                            LeaderDelegationButtonColor
                        } else {
                            Color(0xFFD1D3D8)
                        }
                ).clickable(
                    enabled = isEnabled,
                    onClick = onClick
                ).padding(vertical = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "위임하기",
            color =
                if (isEnabled) {
                    LeaderDelegationButtonTextColor
                } else {
                    Color.White
                },
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * 팀원별 임시 프로필 배경색을 반환한다.
 */
private fun createDelegationProfileColor(memberId: Long): Color {
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
