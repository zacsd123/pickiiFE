package com.example.pickii.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pickii.R
import com.example.pickii.ui.theme.PickiiFieldBackground
import com.example.pickii.ui.theme.PickiiTextGray
import com.example.pickii.util.today
import kotlinx.datetime.YearMonth
import kotlinx.datetime.number

/** 필드/다이얼로그 안 요소의 모서리 둥글기. */
private val YearMonthFieldCornerRadius = 14.dp

/** 연도 선택 범위: 현재 연도 기준 과거 10년 ~ 미래 1년. */
private const val PAST_YEAR_RANGE = 10
private const val FUTURE_YEAR_RANGE = 1

/** 연-월(YearMonth)을 고르는 클릭형 입력 필드. 자격증 취득일자, 경력 기간처럼 월 단위 날짜에 쓴다. */
@Composable
fun YearMonthField(
    value: YearMonth?,
    onValueChange: (YearMonth) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    var isDialogVisible by remember { mutableStateOf(false) }

    Box(
        modifier =
            modifier
                .clip(RoundedCornerShape(YearMonthFieldCornerRadius))
                .background(PickiiFieldBackground)
                .clickable { isDialogVisible = true }
                .padding(horizontal = 12.dp, vertical = 14.dp)
    ) {
        Text(
            text = value?.toDisplayString() ?: placeholder,
            color = if (value != null) Color.Black else PickiiTextGray,
            fontSize = 13.sp
        )
    }

    if (isDialogVisible) {
        YearMonthPickerDialog(
            initial = value ?: currentYearMonth(),
            onConfirm = {
                onValueChange(it)
                isDialogVisible = false
            },
            onDismiss = { isDialogVisible = false }
        )
    }
}

/** [YearMonth]를 "YYYY.MM" 형식으로 표시한다. */
private fun YearMonth.toDisplayString(): String = "$year.${month.number.toString().padStart(2, '0')}"

private fun currentYearMonth(): YearMonth = today().let { YearMonth(it.year, it.month) }

@Composable
private fun YearMonthPickerDialog(
    initial: YearMonth,
    onConfirm: (YearMonth) -> Unit,
    onDismiss: () -> Unit
) {
    var year by remember { mutableIntStateOf(initial.year) }
    var month by remember { mutableIntStateOf(initial.month.number) }
    val currentYear = currentYearMonth().year

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onConfirm(YearMonth(year, month)) }) {
                Text(text = stringResource(R.string.common_button_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(text = stringResource(R.string.common_button_cancel)) }
        },
        text = {
            Row(modifier = Modifier.fillMaxWidth()) {
                NumberDropdown(
                    selected = year,
                    options = (currentYear - PAST_YEAR_RANGE)..(currentYear + FUTURE_YEAR_RANGE),
                    onSelect = { year = it },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                NumberDropdown(
                    selected = month,
                    options = 1..12,
                    onSelect = { month = it },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    )
}

/** 정수 하나를 드롭다운으로 고르는 작은 선택 필드. 연도/월 선택에 재사용한다. */
@Composable
private fun NumberDropdown(
    selected: Int,
    options: IntRange,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(PickiiFieldBackground)
                    .clickable { isExpanded = true }
                    .padding(horizontal = 10.dp, vertical = 10.dp)
        ) {
            Text(text = selected.toString(), fontSize = 13.sp)
        }
        DropdownMenu(expanded = isExpanded, onDismissRequest = { isExpanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(text = option.toString()) },
                    onClick = {
                        onSelect(option)
                        isExpanded = false
                    }
                )
            }
        }
    }
}
