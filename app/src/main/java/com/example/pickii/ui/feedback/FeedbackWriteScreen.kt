package com.example.pickii.ui.feedback

import com.example.pickii.ui.feedback.component.FeedbackTextField
import com.example.pickii.ui.feedback.component.FeedbackScoreSelector
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val WriteBackgroundColor = Color(0xFFF7FFA8)
private val WriteCardColor = Color(0xFFFFFEF6)
private val WriteTextColor = Color(0xFF1D1D1B)
private val WriteDescriptionColor = Color(0xFF85857E)
private val WriteInformationColor = Color(0xFFFFFFDF)

/** `POST /feedbacks`(4-10) 요청 본문 중 화면에서 입력받는 항목. */
data class FeedbackWriteInput(
    val responsibility: Int,
    val communication: Int,
    val participation: Int,
    val cooperation: Int,
    val deadline: Int,
    val strength: String,
    val weakness: String,
)

/** 서버가 요구하는 주관식 후기 최소 길이(4-10 명세: 30~500자). */
private const val FEEDBACK_TEXT_MIN_LENGTH = 30

@Composable
fun FeedbackWriteScreen(
    projectTitle: String,
    memberName: String,
    onBackClick: () -> Unit,
    onSubmitClick: (FeedbackWriteInput) -> Unit,
    modifier: Modifier = Modifier,
) {
    var responsibilityScore by remember { mutableIntStateOf(0) }
    var communicationScore by remember { mutableIntStateOf(0) }
    var participationScore by remember { mutableIntStateOf(0) }
    var cooperationScore by remember { mutableIntStateOf(0) }
    var deadlineScore by remember { mutableIntStateOf(0) }

    var goodPoint by remember { mutableStateOf("") }
    var improvementPoint by remember { mutableStateOf("") }

    val isSubmitEnabled =
        responsibilityScore > 0 &&
            communicationScore > 0 &&
            participationScore > 0 &&
            cooperationScore > 0 &&
            deadlineScore > 0 &&
            goodPoint.length >= FEEDBACK_TEXT_MIN_LENGTH &&
            improvementPoint.length >= FEEDBACK_TEXT_MIN_LENGTH

    BackHandler(onBack = onBackClick)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(WriteBackgroundColor),
    ) {
        FeedbackWriteHeader(
            onBackClick = onBackClick,
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(
                start = 18.dp,
                end = 18.dp,
                top = 18.dp,
                bottom = 32.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                FeedbackTargetCard(
                    projectTitle = projectTitle,
                    memberName = memberName,
                )
            }

            item {
                FeedbackScoreQuestion(
                    question = "맡은 역할과 업무를 책임감 있게 수행했나요?",
                    score = responsibilityScore,
                    onScoreChange = {
                        responsibilityScore = it
                    },
                )
            }

            item {
                FeedbackScoreQuestion(
                    question = "팀원들과 원활하게 소통했나요?",
                    score = communicationScore,
                    onScoreChange = {
                        communicationScore = it
                    },
                )
            }

            item {
                FeedbackScoreQuestion(
                    question = "프로젝트에 적극적으로 참여했나요?",
                    score = participationScore,
                    onScoreChange = {
                        participationScore = it
                    },
                )
            }

            item {
                FeedbackScoreQuestion(
                    question = "다른 팀원의 의견을 존중하며 협업했나요?",
                    score = cooperationScore,
                    onScoreChange = {
                        cooperationScore = it
                    },
                )
            }

            item {
                FeedbackScoreQuestion(
                    question = "약속한 일정과 마감 시간을 잘 지켰나요?",
                    score = deadlineScore,
                    onScoreChange = {
                        deadlineScore = it
                    },
                )
            }

            item {
                FeedbackTextField(
                    title = "함께한 팀원에 대한 칭찬을 남겨주세요.",
                    description = "작성한 후기는 원문 그대로 공개되지 않고, AI가 핵심 내용을 정리해 상대방에게 전달합니다. " +
                        "최소 ${FEEDBACK_TEXT_MIN_LENGTH}자 이상 작성해주세요.",
                    placeholder = "자유롭게 작성해주세요...",
                    value = goodPoint,
                    onValueChange = {
                        goodPoint = it
                    },
                )
            }

            item {
                FeedbackTextField(
                    title = "함께한 팀원이 개선할 점을 남겨주세요.",
                    description = "작성한 후기는 원문 그대로 공개되지 않고, AI가 핵심 내용을 정리해 상대방에게 전달합니다. " +
                        "최소 ${FEEDBACK_TEXT_MIN_LENGTH}자 이상 작성해주세요.",
                    placeholder = "자유롭게 작성해주세요...",
                    value = improvementPoint,
                    onValueChange = {
                        improvementPoint = it
                    },
                )
            }

            item {
                FeedbackPrivacyInformation()
            }

            item {
                FeedbackSubmitButton(
                    isEnabled = isSubmitEnabled,
                    onClick = {
                        onSubmitClick(
                            FeedbackWriteInput(
                                responsibility = responsibilityScore,
                                communication = communicationScore,
                                participation = participationScore,
                                cooperation = cooperationScore,
                                deadline = deadlineScore,
                                strength = goodPoint,
                                weakness = improvementPoint,
                            )
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun FeedbackWriteHeader(
    onBackClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(
                horizontal = 20.dp,
                vertical = 18.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "‹",
            color = WriteTextColor,
            fontSize = 32.sp,
            modifier = Modifier.clickable(onClick = onBackClick),
        )

        Spacer(modifier = Modifier.size(12.dp))

        Text(
            text = "상호 평가 작성",
            color = WriteTextColor,
            fontSize = 21.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun FeedbackTargetCard(
    projectTitle: String,
    memberName: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(WriteCardColor)
            .padding(18.dp),
    ) {
        Text(
            text = projectTitle,
            color = WriteTextColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "${memberName}에 대한 상호 평가를 작성해주세요.",
            color = WriteDescriptionColor,
            fontSize = 13.sp,
            lineHeight = 19.sp,
        )
    }
}

@Composable
private fun FeedbackScoreQuestion(
    question: String,
    score: Int,
    onScoreChange: (Int) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(WriteCardColor)
            .padding(18.dp),
    ) {
        Text(
            text = question,
            color = WriteTextColor,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 21.sp,
        )

        Spacer(modifier = Modifier.height(16.dp))

        FeedbackScoreSelector(
            selectedScore = score,
            onScoreSelect = onScoreChange,
        )
    }
}


@Composable
private fun FeedbackPrivacyInformation() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(WriteInformationColor)
            .padding(16.dp),
    ) {
        Text(
            text = "작성 전 확인해주세요.",
            color = WriteTextColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "작성한 평가는 익명으로 처리되며, 여러 팀원의 의견과 함께 종합되어 전달됩니다.",
            color = WriteDescriptionColor,
            fontSize = 11.sp,
            lineHeight = 17.sp,
        )

        Spacer(modifier = Modifier.height(5.dp))

        Text(
            text = "제출 이후에는 수정하거나 삭제할 수 없습니다.",
            color = WriteDescriptionColor,
            fontSize = 11.sp,
            lineHeight = 17.sp,
        )
    }
}

@Composable
private fun FeedbackSubmitButton(
    isEnabled: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(
                if (isEnabled) {
                    WriteTextColor
                } else {
                    Color(0xFFD5D5CF)
                },
            )
            .clickable(
                enabled = isEnabled,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "평가 제출하기",
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Preview(
    showBackground = true,
    widthDp = 390,
    heightDp = 844,
)
@Composable
private fun FeedbackWriteScreenPreview() {
    FeedbackWriteScreen(
        projectTitle = "서비스 기획 공모전",
        memberName = "김OO님",
        onBackClick = {},
        onSubmitClick = {},
    )
}
