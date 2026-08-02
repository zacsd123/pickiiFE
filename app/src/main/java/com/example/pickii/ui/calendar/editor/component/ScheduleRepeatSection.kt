package com.example.pickii.ui.calendar.editor.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pickii.domain.model.ScheduleRepeatType
import java.time.DayOfWeek

private val RepeatBackgroundColor = Color(0xFFFFFFFF)
private val RepeatLabelColor = Color(0xFF777777)
private val RepeatValueColor = Color(0xFF1B1B1B)
private val RepeatValuePlaceholderColor = Color(0xFFAAAAAA)

private val RepeatOptionSelectedBackgroundColor = Color(0xFFF5F6D8)
private val RepeatOptionDividerColor = Color(0xFFF1F1F1)

/**
 * 일정 반복 설정 영역이다.
 *
 * 행을 누르면 카드 내부에 반복 종류 목록이 인라인으로 펼쳐진다.
 */
@Composable
fun ScheduleRepeatSection(
    repeatType: ScheduleRepeatType,
    onRepeatTypeChange: (ScheduleRepeatType) -> Unit,
    selectedWeekdays: Set<DayOfWeek>,
    onWeekdayToggle: (DayOfWeek) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember {
        mutableStateOf(false)
    }

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(
                    color = RepeatBackgroundColor,
                    shape = RoundedCornerShape(18.dp)
                ).padding(
                    horizontal = 20.dp,
                    vertical = 18.dp
                ),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable {
                        expanded = !expanded
                    },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "↻",
                    color = RepeatLabelColor,
                    fontSize = 17.sp
                )

                Spacer(
                    modifier = Modifier.width(8.dp)
                )

                Text(
                    text = "반복",
                    color = RepeatLabelColor,
                    fontSize = 15.sp
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = repeatType.toDisplayName(),
                    color =
                        if (repeatType == ScheduleRepeatType.NONE) {
                            RepeatValuePlaceholderColor
                        } else {
                            RepeatValueColor
                        },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(
                    modifier = Modifier.width(6.dp)
                )

                Text(
                    text = if (expanded) "⌃" else "⌄",
                    color = RepeatLabelColor,
                    fontSize = 13.sp
                )
            }
        }

        if (expanded) {
            Column {
                ScheduleRepeatType.entries.forEachIndexed { index, option ->
                    if (index > 0) {
                        androidx.compose.foundation.layout.Box(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .background(RepeatOptionDividerColor)
                                    .padding(top = 1.dp)
                        )
                    }

                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .background(
                                    color =
                                        if (option == repeatType) {
                                            RepeatOptionSelectedBackgroundColor
                                        } else {
                                            Color.Transparent
                                        }
                                ).clickable {
                                    expanded = false
                                    onRepeatTypeChange(option)
                                }.padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = option.toDisplayName(),
                            color = RepeatValueColor,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )

                        if (option == repeatType) {
                            Text(
                                text = "✓",
                                color = RepeatValueColor,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        if (repeatType == ScheduleRepeatType.WEEKLY) {
            RepeatWeekdaySelector(
                selectedDays = selectedWeekdays,
                onToggle = onWeekdayToggle
            )
        }
    }
}

/**
 * 반복 종류를 화면 표시 문자열로 변환한다.
 */
private fun ScheduleRepeatType.toDisplayName(): String =
    when (this) {
        ScheduleRepeatType.NONE -> "없음"
        ScheduleRepeatType.DAILY -> "매일"
        ScheduleRepeatType.WEEKLY -> "매주"
        ScheduleRepeatType.MONTHLY -> "매월"
        ScheduleRepeatType.YEARLY -> "매년"
    }
