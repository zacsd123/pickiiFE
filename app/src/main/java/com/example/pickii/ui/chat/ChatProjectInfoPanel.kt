package com.example.pickii.ui.chat

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pickii.R

private val ProjectInfoPanelBackgroundColor = Color.White
private val ProjectInfoPanelScrimColor = Color.Black.copy(alpha = 0.35f)
private val ProjectInfoPrimaryTextColor = Color(0xFF20283A)
private val ProjectInfoSecondaryTextColor = Color(0xFF8E95A3)
private val ProjectInfoCardColor = Color(0xFFF6F7F9)
private val ProjectInfoHighlightColor = Color(0xFFF4F7A6)
private val ProjectInfoProgressTrackColor = Color(0xFFE5E7EB)
private val ProjectInfoProgressColor = Color(0xFF202020)

/**
 * 채팅방 프로젝트 정보를 표시한다.
 */
@Composable
fun ChatProjectInfoPanel(
    projectInfo: ChatProjectInfoUiModel,
    onBackClick: () -> Unit,
) {
    BackHandler(
        enabled = true,
        onBack = onBackClick,
    )

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ProjectInfoPanelScrimColor)
                .clickable(onClick = onBackClick),
        )

        Column(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.79f)
                .align(Alignment.CenterEnd)
                .background(ProjectInfoPanelBackgroundColor),
        ) {
            ProjectInfoHeader(
                onBackClick = onBackClick,
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 20.dp,
                        vertical = 20.dp,
                    ),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                ProjectProgressCard(
                    projectInfo = projectInfo,
                )

                ProjectInfoItem(
                    label = "시작일",
                    value = projectInfo.startDate,
                )

                ProjectInfoItem(
                    label = "종료일",
                    value = projectInfo.endDate,
                )

                ProjectInfoItem(
                    label = "팀원 수",
                    value = "${projectInfo.memberCount}명",
                )

                ProjectInfoItem(
                    label = "팀장",
                    value = projectInfo.leaderName,
                )
            }
        }
    }
}

/**
 * 프로젝트 정보 화면의 상단 헤더를 표시한다.
 */
@Composable
private fun ProjectInfoHeader(
    onBackClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 20.dp,
                vertical = 20.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clickable(onClick = onBackClick),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(
                    id = R.drawable.ic_back,
                ),
                contentDescription = "뒤로가기",
                modifier = Modifier.size(24.dp),
            )
        }

        Spacer(
            modifier = Modifier.width(8.dp),
        )

        Text(
            text = "프로젝트 정보",
            color = ProjectInfoPrimaryTextColor,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

/**
 * 프로젝트 진행률 카드를 표시한다.
 */
@Composable
private fun ProjectProgressCard(
    projectInfo: ChatProjectInfoUiModel,
) {
    val progressPercent = projectInfo.progressPercent
        .coerceIn(0, 100)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = ProjectInfoHighlightColor,
                shape = RoundedCornerShape(20.dp),
            )
            .padding(18.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = projectInfo.projectTitle,
                modifier = Modifier.weight(1f),
                color = ProjectInfoPrimaryTextColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )

            Text(
                text = projectInfo.projectStatus.toDisplayText(),
                modifier = Modifier
                    .background(
                        color = Color(0xFF1F1F1F),
                        shape = RoundedCornerShape(20.dp),
                    )
                    .padding(
                        horizontal = 12.dp,
                        vertical = 6.dp,
                    ),
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        Spacer(
            modifier = Modifier.height(16.dp),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "진행률",
                modifier = Modifier.weight(1f),
                color = ProjectInfoSecondaryTextColor,
                fontSize = 12.sp,
            )

            Text(
                text = "$progressPercent%",
                color = ProjectInfoPrimaryTextColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        Spacer(
            modifier = Modifier.height(8.dp),
        )

        LinearProgressIndicator(
            progress = {
                progressPercent / 100f
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(7.dp)
                .clip(RoundedCornerShape(20.dp)),
            color = ProjectInfoProgressColor,
            trackColor = ProjectInfoProgressTrackColor,
        )
    }
}

/**
 * 프로젝트 정보 한 행을 표시한다.
 */
@Composable
private fun ProjectInfoItem(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = ProjectInfoCardColor,
                shape = RoundedCornerShape(18.dp),
            )
            .padding(
                horizontal = 18.dp,
                vertical = 18.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            color = ProjectInfoSecondaryTextColor,
            fontSize = 14.sp,
        )

        Text(
            text = value,
            color = ProjectInfoPrimaryTextColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}