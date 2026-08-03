package com.example.pickii.ui.calendar.editor.component

import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.pickii.domain.model.ScheduleRepeatType

/**
 * 반복 종류(없음/매일/매주/매월/매년)를 고르는 앵커드 팝업 드롭다운이다.
 */
@Composable
fun RepeatTypeDropdown(
    expanded: Boolean,
    selectedRepeatType: ScheduleRepeatType,
    onDismissRequest: () -> Unit,
    onOptionSelected: (ScheduleRepeatType) -> Unit,
    modifier: Modifier = Modifier
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier
    ) {
        ScheduleRepeatType.entries.forEach { option ->
            DropdownMenuItem(
                text = {
                    Text(text = option.toDisplayName())
                },
                onClick = {
                    onDismissRequest()
                    onOptionSelected(option)
                },
                trailingIcon = {
                    if (option == selectedRepeatType) {
                        Text(text = "✓")
                    }
                }
            )
        }
    }
}

/**
 * 반복 종류를 화면 표시 문자열로 변환한다.
 */
internal fun ScheduleRepeatType.toDisplayName(): String =
    when (this) {
        ScheduleRepeatType.NONE -> "없음"
        ScheduleRepeatType.DAILY -> "매일"
        ScheduleRepeatType.WEEKLY -> "매주"
        ScheduleRepeatType.MONTHLY -> "매월"
        ScheduleRepeatType.YEARLY -> "매년"
    }
