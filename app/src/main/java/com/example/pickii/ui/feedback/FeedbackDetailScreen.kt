package com.example.pickii.ui.feedback

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pickii.ui.common.LoadingIndicator
import com.example.pickii.ui.common.PickiiTopBar
import com.example.pickii.ui.theme.PickiiBlue
import com.example.pickii.ui.theme.PickiiFieldBackground
import com.example.pickii.ui.theme.PickiiTextGray

private val FeedbackDetailBackgroundColor = Color(0xFFF8F8F5)
private val FeedbackDetailCardColor = Color.White
private val FeedbackDetailTextColor = Color(0xFF1D1D1B)
private val FeedbackDetailBorderColor = Color(0xFFE4E4DF)

@Composable
fun FeedbackDetailScreen(
    uiState: FeedbackDetailUiState,
    onCloseClick: () -> Unit,
    onNotificationClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    BackHandler(onBack = onCloseClick)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FeedbackDetailBackgroundColor)
            .padding(horizontal = 20.dp),
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        PickiiTopBar(
            notificationCount = 9,
            onNotificationClick = onNotificationClick,
        )

        Spacer(modifier = Modifier.height(22.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "피드백",
                color = FeedbackDetailTextColor,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.weight(1f))

            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(PickiiFieldBackground)
                    .clickable(onClick = onCloseClick),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "×",
                    color = FeedbackDetailTextColor,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Light,
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (uiState.isLoading) {
            LoadingIndicator(modifier = Modifier.weight(1f))
        } else {
            FeedbackSummaryCard(
                uiState = uiState,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun FeedbackSummaryCard(
    uiState: FeedbackDetailUiState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(FeedbackDetailCardColor)
            .padding(
                horizontal = 22.dp,
                vertical = 26.dp,
            ),
    ) {
        Text(
            text = "자주 언급된 강점 키워드",
            color = FeedbackDetailTextColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (uiState.keywords.isEmpty()) {
            Text(
                text = "아직 집계된 키워드가 없습니다.",
                color = PickiiTextGray,
                fontSize = 13.sp,
            )
        } else {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                uiState.keywords.forEach { keyword ->
                    Text(
                        text = "#$keyword",
                        color = PickiiBlue,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        FeedbackSummarySection(
            title = "협업 칭찬 요약",
            content = uiState.complimentSummary,
        )

        Spacer(modifier = Modifier.height(26.dp))

        FeedbackSummarySection(
            title = "개선 포인트",
            content = uiState.improvementSummary,
        )
    }
}

@Composable
private fun FeedbackSummarySection(
    title: String,
    content: String,
) {
    Column {
        Text(
            text = title,
            color = FeedbackDetailTextColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(168.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFFAFAFA))
                .then(
                    Modifier.background(Color.Transparent),
                )
                .padding(16.dp),
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Transparent),
            )

            Text(
                text = content.ifBlank {
                    "아직 확인할 수 있는 피드백이 없습니다."
                },
                color = if (content.isBlank()) {
                    PickiiTextGray
                } else {
                    FeedbackDetailTextColor
                },
                fontSize = 14.sp,
                lineHeight = 21.sp,
            )
        }
    }
}

@Preview(
    showBackground = true,
    widthDp = 390,
    heightDp = 844,
)
@Composable
private fun FeedbackDetailScreenPreview() {
    FeedbackDetailScreen(
        uiState = FeedbackDetailUiState(
            keywords = listOf(
                "빠른일처리",
                "열정",
            ),
            complimentSummary = "대충 쿠션어 때려박은 우쭈쭈 피드백",
            improvementSummary = "대충 쿠션어 때려박은 우쭈쭈 피드백",
        ),
        onCloseClick = {},
        onNotificationClick = {},
    )
}
