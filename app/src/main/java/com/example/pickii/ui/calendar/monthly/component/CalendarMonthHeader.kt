package com.example.pickii.ui.calendar.monthly.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

private val CalendarTextColor = Color(0xFF171717)
private val CalendarYearColor = Color(0xFF7D7D70)

/**
 * 월간 캘린더 상단의 연도, 월 이름,
 * 이전·다음 달 이동 버튼을 표시한다.
 *
 * 화살표 문자는 추후 SVG 리소스로 교체한다.
 */
@Composable
fun CalendarMonthHeader(
    displayedYearMonth: YearMonth,
    onPreviousMonthClick: () -> Unit,
    onNextMonthClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val monthName = displayedYearMonth.month.getDisplayName(
        TextStyle.FULL,
        Locale.ENGLISH,
    )

    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        CalendarYearSelector(
            year = displayedYearMonth.year,
            onPreviousMonthClick = onPreviousMonthClick,
            onNextMonthClick = onNextMonthClick,
        )

        Spacer(
            modifier = Modifier.height(8.dp),
        )

        CalendarMonthSelector(
            monthName = monthName,
            onPreviousMonthClick = onPreviousMonthClick,
            onNextMonthClick = onNextMonthClick,
        )
    }
}

/**
 * 연도와 이전·다음 달 이동 화살표를 표시한다.
 */
@Composable
private fun CalendarYearSelector(
    year: Int,
    onPreviousMonthClick: () -> Unit,
    onNextMonthClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 28.dp),   // << 값은 24~36dp 정도
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "‹",
            modifier = Modifier
                .clickable(onClick = onPreviousMonthClick)
                .padding(4.dp),
            color = CalendarYearColor,
            fontSize = 26.sp,
            fontWeight = FontWeight.Medium,
        )

        Text(
            text = year.toString(),
            color = CalendarYearColor,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
        )

        Text(
            text = "›",
            modifier = Modifier
                .clickable(onClick = onNextMonthClick)
                .padding(4.dp),
            color = CalendarYearColor,
            fontSize = 26.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

/**
 * 월 이름과 이전·다음 달 이동 화살표를 표시한다.
 */
@Composable
private fun CalendarMonthSelector(
    monthName: String,
    onPreviousMonthClick: () -> Unit,
    onNextMonthClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "‹",
            modifier = Modifier
                .size(44.dp)
                .clickable(onClick = onPreviousMonthClick),
            color = CalendarTextColor,
            textAlign = TextAlign.Center,
            fontSize = 32.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = 44.sp,
        )

        Text(
            text = monthName,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            color = CalendarTextColor,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontSize = 37.sp,
            fontWeight = FontWeight.ExtraBold,
        )

        Text(
            text = "›",
            modifier = Modifier
                .size(44.dp)
                .clickable(onClick = onNextMonthClick),
            color = CalendarTextColor,
            textAlign = TextAlign.Center,
            fontSize = 32.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = 44.sp,
        )
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFFF8FFAA,
)
@Composable
private fun CalendarMonthHeaderPreview() {
    CalendarMonthHeader(
        displayedYearMonth = YearMonth.of(2026, 12),
        onPreviousMonthClick = {},
        onNextMonthClick = {},
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
    )
}