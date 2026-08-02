package com.example.pickii.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** 모집중/마감/대기/수락/거절 등 목록 카드에 붙는 상태 배지. */
@Composable
fun StatusBadge(
    label: String,
    containerColor: Color,
    modifier: Modifier = Modifier,
    contentColor: Color = Color.White
) {
    Box(
        modifier =
            modifier
                .clip(RoundedCornerShape(50))
                .background(containerColor)
                .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(text = label, color = contentColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}
