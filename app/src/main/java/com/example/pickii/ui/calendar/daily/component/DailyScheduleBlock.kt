package com.example.pickii.ui.calendar.daily.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pickii.domain.model.ScheduleColorType
import com.example.pickii.ui.calendar.daily.DailyScheduleUiModel
import com.example.pickii.ui.common.toComposeColor
import com.example.pickii.util.today
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

/**
 * 시간표 위에 표시되는 일정 블록이다.
 */
@Composable
fun DailyScheduleBlock(
    schedule: DailyScheduleUiModel,
    onClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier =
            modifier
                .background(
                    color = schedule.colorType.toComposeColor(),
                    shape = RoundedCornerShape(12.dp)
                ).clickable {
                    onClick(schedule.id)
                }
    ) {
        Text(
            text = schedule.title,
            modifier =
                Modifier
                    .align(Alignment.TopStart)
                    .padding(
                        horizontal = 10.dp,
                        vertical = 8.dp
                    ),
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFFFFFFFF
)
@Composable
private fun DailyScheduleBlockPreview() {
    DailyScheduleBlock(
        schedule =
            DailyScheduleUiModel(
                id = 1L,
                title = "회의",
                date = today(),
                startTime = LocalTime(9, 0),
                endTime = LocalTime(11, 0),
                colorType = ScheduleColorType.BLUE
            ),
        onClick = {},
        modifier =
            Modifier
                .padding(16.dp)
                .fillMaxSize()
    )
}
