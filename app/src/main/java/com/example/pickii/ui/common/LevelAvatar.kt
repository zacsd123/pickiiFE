package com.example.pickii.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pickii.ui.theme.PickiiFieldBackground
import com.example.pickii.ui.theme.PickiiTextGray
import com.example.pickii.util.calculateLevel

/** 프로필 사진이 아직 없어, 그 자리에 [exp]로 계산한 레벨 숫자를 원형으로 보여주는 placeholder. */
@Composable
fun LevelAvatar(
    exp: Int,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp
) {
    val level = calculateLevel(exp).level

    Box(
        modifier =
            modifier
                .size(size)
                .clip(CircleShape)
                .background(PickiiFieldBackground),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = level.toString(),
            color = PickiiTextGray,
            fontSize = (size.value * 0.4f).sp,
            fontWeight = FontWeight.Bold
        )
    }
}
