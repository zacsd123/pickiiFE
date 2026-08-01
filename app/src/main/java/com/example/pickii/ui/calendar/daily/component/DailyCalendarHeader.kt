package com.example.pickii.ui.calendar.daily.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val HeaderBackgroundColor = Color(0xFFF7F7F7)
private val HeaderTextColor = Color(0xFF1B1B1B)
private val HeaderArrowColor = Color(0xFF999999)
private val MenuButtonBackgroundColor = Color(0xFFFFFFFF)
private val AddButtonBackgroundColor = Color(0xFF6D8DF5)
private val AddButtonTextColor = Color(0xFFFFFFFF)

private val HeaderDateFormatter = DateTimeFormatter.ofPattern(
    "yyyy년 M월 d일",
    Locale.KOREAN,
)

/**
 * 일일 캘린더 상단 날짜 이동 영역을 표시한다.
 */
@Composable
fun DailyCalendarHeader(
    selectedDate: LocalDate,
    onMenuClick: () -> Unit,
    onPreviousDateClick: () -> Unit,
    onNextDateClick: () -> Unit,
    onAddScheduleClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = HeaderBackgroundColor,
                shape = RoundedCornerShape(18.dp),
            )
            .padding(
                horizontal = 8.dp,
                vertical = 8.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        HeaderSquareButton(
            text = "☰",
            backgroundColor = MenuButtonBackgroundColor,
            textColor = HeaderTextColor,
            onClick = onMenuClick,
        )

        Text(
            text = "‹",
            modifier = Modifier
                .size(40.dp)
                .clickable(onClick = onPreviousDateClick),
            color = HeaderArrowColor,
            textAlign = TextAlign.Center,
            fontSize = 28.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = 40.sp,
        )

        Text(
            text = selectedDate.format(HeaderDateFormatter),
            modifier = Modifier.weight(1f),
            color = HeaderTextColor,
            textAlign = TextAlign.Center,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
        )

        Text(
            text = "›",
            modifier = Modifier
                .size(40.dp)
                .clickable(onClick = onNextDateClick),
            color = HeaderArrowColor,
            textAlign = TextAlign.Center,
            fontSize = 28.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = 40.sp,
        )

        HeaderSquareButton(
            text = "+",
            backgroundColor = AddButtonBackgroundColor,
            textColor = AddButtonTextColor,
            onClick = onAddScheduleClick,
        )
    }
}

/**
 * 헤더의 정사각형 버튼을 표시한다.
 */
@Composable
private fun HeaderSquareButton(
    text: String,
    backgroundColor: Color,
    textColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(44.dp)
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(13.dp),
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = if (text == "+") 30.sp else 20.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = 30.sp,
        )
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
)
@Composable
private fun DailyCalendarHeaderPreview() {
    DailyCalendarHeader(
        selectedDate = LocalDate.of(2026, 7, 4),
        onMenuClick = {},
        onPreviousDateClick = {},
        onNextDateClick = {},
        onAddScheduleClick = {},
        modifier = Modifier.padding(20.dp),
    )
}