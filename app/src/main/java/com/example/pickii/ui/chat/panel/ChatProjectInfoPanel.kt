package com.example.pickii.ui.chat

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pickii.domain.model.ScheduleCategory
import com.example.pickii.shared.generated.resources.Res
import com.example.pickii.shared.generated.resources.chat_project_close_button
import com.example.pickii.shared.generated.resources.chat_project_close_dialog_body
import com.example.pickii.shared.generated.resources.chat_project_close_dialog_title
import com.example.pickii.shared.generated.resources.common_button_cancel
import com.example.pickii.ui.calendar.editor.component.ScheduleCategorySection
import com.example.pickii.ui.common.BackHeader
import com.example.pickii.ui.common.ConfirmDialog
import com.example.pickii.ui.common.SlideInSidePanelScaffold
import com.example.pickii.ui.theme.PickiiNavyText
import org.jetbrains.compose.resources.stringResource

private val ProjectInfoPanelBackgroundColor = Color.White
private val ProjectInfoPanelScrimColor = Color.Black.copy(alpha = 0.35f)
private val ProjectInfoPrimaryTextColor = PickiiNavyText
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
    isCurrentUserLeader: Boolean,
    onBackClick: () -> Unit,
    onSelectColor: (Long?) -> Unit,
    onCloseProjectClick: () -> Unit
) {
    var isCloseConfirmVisible by remember { mutableStateOf(false) }

    BackHandler(
        enabled = true,
        onBack = onBackClick
    )

    SlideInSidePanelScaffold(
        onScrimClick = onBackClick,
        scrimColor = ProjectInfoPanelScrimColor,
        panelBackgroundColor = ProjectInfoPanelBackgroundColor
    ) {
        ProjectInfoHeader(
            onBackClick = onBackClick
        )

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
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
                    onCategoryClick = onSelectColor,
                    onManageClick = {}
                )
            }

            if (isCurrentUserLeader && projectInfo.projectStatus != ProjectStatus.END) {
                ProjectCloseButton(onClick = { isCloseConfirmVisible = true })
            }
        }
    }

    if (isCloseConfirmVisible) {
        ConfirmDialog(
            title = stringResource(Res.string.chat_project_close_dialog_title),
            body = stringResource(Res.string.chat_project_close_dialog_body),
            confirmLabel = stringResource(Res.string.chat_project_close_button),
            dismissLabel = stringResource(Res.string.common_button_cancel),
            onConfirm = {
                isCloseConfirmVisible = false
                onCloseProjectClick()
            },
            onDismiss = { isCloseConfirmVisible = false }
        )
    }
}

/**
 * 프로젝트 종료 버튼을 표시한다(프로젝트장에게만 노출).
 */
@Composable
private fun ProjectCloseButton(onClick: () -> Unit) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFF1F1F1F))
                .clickable(onClick = onClick)
                .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(Res.string.chat_project_close_button),
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * 프로젝트 정보 화면의 상단 헤더를 표시한다.
 */
@Composable
private fun ProjectInfoHeader(onBackClick: () -> Unit) {
    BackHeader(
        title = "프로젝트 정보",
        onBackClick = onBackClick,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp),
        titleColor = ProjectInfoPrimaryTextColor,
        spacing = 8.dp,
        iconTouchSize = 40.dp
    )
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
