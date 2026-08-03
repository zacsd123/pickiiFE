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

/**
 * 일정 반복 설정 영역이다.
 *
 * 행을 누르면 앵커드 팝업 드롭다운으로 반복 종류 목록이 펼쳐진다.
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
                        expanded = true
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

        RepeatTypeDropdown(
            expanded = expanded,
            selectedRepeatType = repeatType,
            onDismissRequest = {
                expanded = false
            },
            onOptionSelected = onRepeatTypeChange
        )

        if (repeatType == ScheduleRepeatType.WEEKLY) {
            RepeatWeekdaySelector(
                selectedDays = selectedWeekdays,
                onToggle = onWeekdayToggle
            )
        }
    }
}
