package com.example.pickii.ui.calendar.daily.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

private val TimeLineColor = Color(0xFFE7E7E7)

/**
 * 일일 캘린더의 시간 구분선을 표시한다.
 */
@Composable
fun DailyTimeLine(modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(TimeLineColor)
    )
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFFFFFFFF
)
@Composable
private fun DailyTimeLinePreview() {
    DailyTimeLine()
}
