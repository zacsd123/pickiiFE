package com.example.pickii.ui.chat

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pickii.domain.model.ScheduleCategory
import com.example.pickii.ui.calendar.editor.component.ScheduleCategorySection

private val ProjectInfoPanelBackgroundColor = Color.White
private val ProjectInfoPanelScrimColor = Color.Black.copy(alpha = 0.35f)
private val ProjectInfoPrimaryTextColor = Color(0xFF20283A)
private val ProjectInfoSecondaryTextColor = Color(0xFF8E95A3)
private val ProjectInfoCardColor = Color(0xFFF6F7F9)
private val ProjectInfoHighlightColor = Color(0xFFF4F7A6)

/**
 * 채팅방 프로젝트 정보를 표시한다.
 */
@Composable
fun ChatProjectInfoPanel(
    projectInfo: ChatProjectInfoUiModel,
    scheduleCategories: List<ScheduleCategory>,
    selectedCategoryId: Long?,
    onBackClick: () -> Unit,
    onSelectColor: (Long) -> Unit
) {
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
                    .background(ProjectInfoPanelScrimColor)
                    .clickable(onClick = onBackClick)
        )

        Column(
            modifier =
                Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.79f)
                    .align(Alignment.CenterEnd)
                    .background(ProjectInfoPanelBackgroundColor)
        ) {
            ProjectInfoHeader(
                onBackClick = onBackClick
            )

            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 20.dp,
                            vertical = 20.dp
                        ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ProjectProgressCard(
                    projectInfo = projectInfo
                )

                ProjectInfoItem(
                    label = "시작일",
                    value = projectInfo.startDate
                )

                ProjectInfoItem(
                    label = "종료일",
                    value = projectInfo.endDate
                )

                ProjectInfoItem(
                    label = "팀원 수",
                    value = "${projectInfo.memberCount}명"
                )

                if (projectInfo.leaderName.isNotBlank()) {
                    ProjectInfoItem(
                        label = "팀장",
                        value = projectInfo.leaderName
                    )
                }

                if (scheduleCategories.isNotEmpty()) {
                    Text(
                        text = "이 프로젝트 캘린더 색상",
                        color = ProjectInfoSecondaryTextColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    ScheduleCategorySection(
                        categories = scheduleCategories,
                        selectedCategoryId = selectedCategoryId,
                        onCategoryClick = { categoryId -> categoryId?.let(onSelectColor) },
                        onManageClick = {}
                    )
                }
            }
        }
    }
}

/**
 * 프로젝트 정보 화면의 상단 헤더를 표시한다.
 */
@Composable
private fun ProjectInfoHeader(onBackClick: () -> Unit) {
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
            text = "프로젝트 정보",
            color = ProjectInfoPrimaryTextColor,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * 프로젝트 제목과 진행 상태를 보여주는 카드를 표시한다.
 *
 * 진행률(%)은 8-2 API 응답에 근거 데이터가 없어 표시하지 않는다(알려진 API 제약).
 */
@Composable
private fun ProjectProgressCard(projectInfo: ChatProjectInfoUiModel) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(
                    color = ProjectInfoHighlightColor,
                    shape = RoundedCornerShape(20.dp)
                ).padding(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = projectInfo.projectTitle,
                modifier = Modifier.weight(1f),
                color = ProjectInfoPrimaryTextColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = projectInfo.projectStatus.toDisplayText(),
                modifier =
                    Modifier
                        .background(
                            color = Color(0xFF1F1F1F),
                            shape = RoundedCornerShape(20.dp)
                        ).padding(
                            horizontal = 12.dp,
                            vertical = 6.dp
                        ),
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * 프로젝트 정보 한 행을 표시한다.
 */
@Composable
private fun ProjectInfoItem(
    label: String,
    value: String
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(
                    color = ProjectInfoCardColor,
                    shape = RoundedCornerShape(18.dp)
                ).padding(
                    horizontal = 18.dp,
                    vertical = 18.dp
                ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            color = ProjectInfoSecondaryTextColor,
            fontSize = 14.sp
        )

        Text(
            text = value,
            color = ProjectInfoPrimaryTextColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
