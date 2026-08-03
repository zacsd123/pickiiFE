package com.example.pickii.ui.calendar.editor.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.DayOfWeek

private val SelectedWeekdayBackgroundColor = Color(0xFFFBE1E7)
private val SelectedWeekdayTextColor = Color(0xFFE86F73)

private val UnselectedWeekdayBackgroundColor = Color(0xFFF1F1F1)
private val UnselectedWeekdayTextColor = Color(0xFF777777)

private val OrderedWeekdays =
    listOf(
        DayOfWeek.MONDAY,
        DayOfWeek.TUESDAY,
        DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY,
        DayOfWeek.FRIDAY,
        DayOfWeek.SATURDAY,
        DayOfWeek.SUNDAY
    )

/**
 * 매주 반복 시 요일을 다중 선택하는 칩 목록이다.
 */
@Composable
fun RepeatWeekdaySelector(
    selectedDays: Set<DayOfWeek>,
    onToggle: (DayOfWeek) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OrderedWeekdays.forEach { day ->
            val isSelected = day in selectedDays

            Text(
                text = day.toShortLabel(),
                modifier =
                    Modifier
                        .background(
                            color =
                                if (isSelected) {
                                    SelectedWeekdayBackgroundColor
                                } else {
                                    UnselectedWeekdayBackgroundColor
                                },
                            shape = RoundedCornerShape(50)
                        ).clickable {
                            onToggle(day)
                        }.padding(
                            horizontal = 12.dp,
                            vertical = 8.dp
                        ),
                color =
                    if (isSelected) {
                        SelectedWeekdayTextColor
                    } else {
                        UnselectedWeekdayTextColor
                    },
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * 요일을 한 글자 한글 표기로 변환한다.
 */
private fun DayOfWeek.toShortLabel(): String =
    when (this) {
        DayOfWeek.MONDAY -> "월"
        DayOfWeek.TUESDAY -> "화"
        DayOfWeek.WEDNESDAY -> "수"
        DayOfWeek.THURSDAY -> "목"
        DayOfWeek.FRIDAY -> "금"
        DayOfWeek.SATURDAY -> "토"
        DayOfWeek.SUNDAY -> "일"
    }
