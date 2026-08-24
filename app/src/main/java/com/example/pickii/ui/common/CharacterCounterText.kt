package com.example.pickii.ui.common

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.example.pickii.shared.generated.resources.Res
import com.example.pickii.shared.generated.resources.common_counter_format
import com.example.pickii.ui.theme.PickiiTextGray
import org.jetbrains.compose.resources.stringResource

/**
 * "현재/최대" 형태의 글자 수 카운터. [current]가 [max] 이상이면 강조 색으로 표시한다.
 *
 * @param current 현재 입력된 글자 수
 * @param max 허용하는 최대 글자 수
 */
@Composable
fun CharacterCounterText(
    current: Int,
    max: Int,
    modifier: Modifier = Modifier
) {
    Text(
        text = stringResource(Res.string.common_counter_format, current, max),
        color = if (current >= max) Color.Red else PickiiTextGray,
        fontSize = 12.sp,
        modifier = modifier
    )
}
