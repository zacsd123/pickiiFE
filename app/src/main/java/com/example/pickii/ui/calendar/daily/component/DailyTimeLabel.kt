package com.example.pickii.ui.calendar.daily.component

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp

private val TimeLabelColor = Color(0xFFB4B4B4)

/**
 * 일일 캘린더 왼쪽의 시간 텍스트를 표시한다.
 */
@Composable
fun DailyTimeLabel(
    hour: Int,
    modifier: Modifier = Modifier
) {
    Text(
        text = formatHour(hour),
        modifier = modifier,
        color = TimeLabelColor,
        fontSize = 15.sp,
        fontWeight = FontWeight.Medium,
        maxLines = 1
    )
}

/**
 * 정수 시간을 00:00 형식으로 변환한다.
 */
private fun formatHour(hour: Int): String = "%02d:00".format(hour)

@Preview(
    showBackground = true,
    backgroundColor = 0xFFFFFFFF
)
@Composable
private fun DailyTimeLabelPreview() {
    DailyTimeLabel(
        hour = 6
    )
}
