package com.example.pickii.ui.calendar.monthly.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

private val PreviousMonthButtonColor = Color(0xFFC5AAFF)
private val NextMonthButtonColor = Color(0xFFF2D5A4)
private val CalendarTextColor = Color(0xFF171717)
private val CalendarYearColor = Color(0xFF7D7D70)

/**
 * 월간 캘린더 상단의 연도, 월 이름, 이전·다음 달 버튼을 표시한다.
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

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = monthName,
                color = CalendarTextColor,
                fontSize = 64.sp,
                fontWeight = FontWeight.Black,
                lineHeight = 68.sp,
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CalendarMonthMoveButton(
                    arrowText = "‹",
                    backgroundColor = PreviousMonthButtonColor,
                    onClick = onPreviousMonthClick,
                )

                CalendarMonthMoveButton(
                    arrowText = "›",
                    backgroundColor = NextMonthButtonColor,
                    onClick = onNextMonthClick,
                )
            }
        }
    }
}

/**
 * 연도와 작은 월 이동 화살표를 표시한다.
 */
@Composable
private fun CalendarYearSelector(
    year: Int,
    onPreviousMonthClick: () -> Unit,
    onNextMonthClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
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
 * 월 이동용 원형 버튼을 표시한다.
 */
@Composable
private fun CalendarMonthMoveButton(
    arrowText: String,
    backgroundColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(58.dp)
            .background(
                color = backgroundColor,
                shape = CircleShape,
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = arrowText,
            color = CalendarTextColor,
            fontSize = 40.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = 40.sp,
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
        displayedYearMonth = YearMonth.of(2026, 7),
        onPreviousMonthClick = {},
        onNextMonthClick = {},
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
    )
}