package com.example.pickii.ui.feedback.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val FeedbackScoreSelectedColor = Color(0xFF6685ED)
private val FeedbackScoreBorderColor = Color(0xFFD8D8D2)
private val FeedbackScoreDescriptionColor = Color(0xFF85857E)

@Composable
fun FeedbackScoreSelector(
    selectedScore: Int,
    onScoreSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(5) { index ->
            val score = index + 1
            val isSelected = score == selectedScore

            FeedbackScoreItem(
                score = score,
                isSelected = isSelected,
                onClick = {
                    onScoreSelect(score)
                }
            )
        }
    }
}

@Composable
private fun FeedbackScoreItem(
    score: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier =
                Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) {
                            FeedbackScoreSelectedColor
                        } else {
                            Color.Transparent
                        }
                    ).border(
                        width = 1.dp,
                        color =
                            if (isSelected) {
                                FeedbackScoreSelectedColor
                            } else {
                                FeedbackScoreBorderColor
                            },
                        shape = CircleShape
                    ).clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Text(
                    text = "✓",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text =
                when (score) {
                    1 -> "전혀 아니다"
                    5 -> "매우 그렇다"
                    else -> ""
                },
            color = FeedbackScoreDescriptionColor,
            fontSize = 9.sp
        )
    }
}
