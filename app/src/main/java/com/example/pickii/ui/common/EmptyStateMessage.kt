package com.example.pickii.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pickii.ui.theme.PickiiTextGray

/** 목록이 비어있을 때 가운데 표시하는 안내 문구(예: "아직 지원한 공고가 없습니다"). */
@Composable
fun EmptyStateMessage(
    message: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth().height(200.dp).padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = message, color = PickiiTextGray, fontSize = 14.sp, textAlign = TextAlign.Center)
    }
}
