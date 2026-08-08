package com.example.pickii.ui.feedback.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pickii.ui.feedback.FeedbackMemberUiModel
import com.example.pickii.ui.feedback.FeedbackProjectUiModel

private val FeedbackCardColor = Color(0xFFFFFEF6)
private val FeedbackTextColor = Color(0xFF1E1E1C)
private val FeedbackDescriptionColor = Color(0xFF777771)
private val FeedbackPrimaryColor = Color.Black
private val FeedbackDisabledColor = Color(0xFFCCCCCC)

@Composable
fun WritableFeedbackCard(
    project: FeedbackProjectUiModel,
    onWriteFeedbackClick: (
        projectId: Long,
        memberId: Long
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(17.dp))
                .background(FeedbackCardColor)
                .padding(18.dp)
    ) {
        FeedbackProjectTitleAndInformation(project = project)

        Spacer(modifier = Modifier.height(14.dp))

        project.members.forEachIndexed { index, member ->
            FeedbackMemberRow(
                member = member,
                onClick = {
                    onWriteFeedbackClick(
                        project.id,
                        member.id
                    )
                }
            )

            if (index != project.members.lastIndex) {
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@Composable
fun ReceivedFeedbackCard(
    project: FeedbackProjectUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(17.dp))
                .background(FeedbackCardColor)
                .padding(18.dp)
    ) {
        Text(
            text = project.title,
            color = FeedbackTextColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "평가 기간: ${project.startDate} ~ ${project.endDate}",
            color = FeedbackDescriptionColor,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "남은 기간: ",
                color = FeedbackDescriptionColor,
                fontSize = 12.sp
            )

            Text(
                text = "${project.remainingDays}일",
                color = FeedbackTextColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "평가된 피드백 수: ${project.completedCount}/${project.totalCount}",
            color = FeedbackDescriptionColor,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text =
                if (project.canReadFeedback) {
                    "AI 합성 피드백이 도착했어요.\n이번 프로젝트에서 받은 협업 후기를 바탕으로 AI가 강점과 개선 포인트를 요약했어요."
                } else {
                    "아직 AI 피드백 조회를 위한 평가 수가 충분하지 않아요. 익명성 보호를 위해 2개 이상의 피드백이 모인 경우에만 조회할 수 있어요."
                },
            color = FeedbackDescriptionColor,
            fontSize = 11.sp,
            lineHeight = 17.sp
        )

        Spacer(modifier = Modifier.height(13.dp))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            FeedbackActionButton(
                text = "피드백 확인하기",
                isEnabled = project.canReadFeedback,
                onClick = onClick
            )
        }
    }
}

@Composable
private fun FeedbackProjectTitleAndInformation(project: FeedbackProjectUiModel) {
    Column {
        Text(
            text = project.title,
            color = FeedbackTextColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "평가 기간: ${project.startDate} ~ ${project.endDate}",
                color = FeedbackDescriptionColor,
                fontSize = 12.sp
            )

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "남은 기간: ",
                    color = FeedbackDescriptionColor,
                    fontSize = 12.sp
                )

                Text(
                    text = "${project.remainingDays}일",
                    color = FeedbackTextColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "본인 피드백을 확인하려면 최소 2명의 팀원 평가가 필요해요.",
                color = FeedbackDescriptionColor,
                fontSize = 10.sp
            )

            Text(
                text = "현재 ${project.completedCount}/${project.totalCount}명 완료",
                color = FeedbackDescriptionColor,
                fontSize = 10.sp
            )
        }
    }
}

private val FeedbackCompletedColor = Color(0xFF4CAF50)

@Composable
private fun FeedbackMemberRow(
    member: FeedbackMemberUiModel,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = member.name,
            color = FeedbackTextColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )

        if (member.isCompleted) {
            Box(
                modifier =
                    Modifier
                        .height(34.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(FeedbackCompletedColor)
                        .padding(horizontal = 15.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "평가 완료",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            FeedbackActionButton(
                text = "평가하러 가기",
                isEnabled = true,
                onClick = onClick
            )
        }
    }
}

@Composable
private fun FeedbackActionButton(
    text: String,
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier =
            Modifier
                .height(34.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(
                    if (isEnabled) {
                        FeedbackPrimaryColor
                    } else {
                        FeedbackDisabledColor
                    }
                ).clickable(
                    enabled = isEnabled,
                    onClick = onClick
                ).padding(horizontal = 15.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}
