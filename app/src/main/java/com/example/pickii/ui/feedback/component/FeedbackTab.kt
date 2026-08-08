package com.example.pickii.ui.feedback.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pickii.ui.feedback.FeedbackTabType

private val FeedbackTabBackgroundColor = Color(0xFFFFFFDB)
private val FeedbackSelectedTabColor = Color(0xFF1D1D1B)
private val FeedbackUnselectedTextColor = Color(0xFF777771)

@Composable
fun FeedbackTab(
    selectedTab: FeedbackTabType,
    onTabClick: (FeedbackTabType) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .height(46.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(FeedbackTabBackgroundColor)
                .padding(3.dp)
    ) {
        FeedbackTabButton(
            text = "작성 가능한 상호 평가",
            isSelected = selectedTab == FeedbackTabType.WRITE,
            onClick = {
                onTabClick(FeedbackTabType.WRITE)
            },
            modifier = Modifier.weight(1f)
        )

        FeedbackTabButton(
            text = "받은 피드백",
            isSelected = selectedTab == FeedbackTabType.RECEIVED,
            onClick = {
                onTabClick(FeedbackTabType.RECEIVED)
            },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun FeedbackTabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(10.dp))
                .background(
                    if (isSelected) {
                        FeedbackSelectedTabColor
                    } else {
                        Color.Transparent
                    }
                ).clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color =
                if (isSelected) {
                    Color.White
                } else {
                    FeedbackUnselectedTextColor
                },
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
