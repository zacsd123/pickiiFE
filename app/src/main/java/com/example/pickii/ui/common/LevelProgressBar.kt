package com.example.pickii.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pickii.R
import com.example.pickii.ui.theme.PickiiBlue
import com.example.pickii.ui.theme.PickiiDisabledGray
import com.example.pickii.ui.theme.PickiiTextGray
import com.example.pickii.util.calculateLevel
import kotlin.math.roundToInt

/** 레벨/경험치 바. 레벨 산출 공식은 기획/백엔드 확정 전까지 임시([com.example.pickii.util.calculateLevel] 참고). */
@Composable
fun LevelProgressBar(
    exp: Int,
    modifier: Modifier = Modifier
) {
    val levelInfo = calculateLevel(exp)
    val percent = (levelInfo.progress.coerceIn(0f, 1f) * 100).roundToInt()

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = stringResource(R.string.mypage_level_label, levelInfo.level),
            color = PickiiTextGray,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier =
                Modifier
                    .weight(1f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(50))
                    .background(PickiiDisabledGray)
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth(levelInfo.progress.coerceIn(0f, 1f))
                        .height(6.dp)
                        .clip(RoundedCornerShape(50))
                        .background(PickiiBlue)
            )
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "$percent%",
            color = PickiiTextGray,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
