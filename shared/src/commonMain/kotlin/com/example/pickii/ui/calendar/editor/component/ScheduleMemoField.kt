package com.example.pickii.ui.calendar.editor.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pickii.ui.theme.PickiiGray600
import com.example.pickii.ui.theme.PickiiGrayMedium
import com.example.pickii.ui.theme.PickiiInk

private val MemoFieldBackgroundColor = Color(0xFFFFFFFF)
private val MemoLabelColor = PickiiGray600
private val MemoTextColor = PickiiInk
private val MemoPlaceholderColor = PickiiGrayMedium
private val MemoCursorColor = PickiiInk

/**
 * 일정 메모 입력 영역이다.
 */
@Composable
fun ScheduleMemoField(
    memo: String,
    onMemoChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(
                    color = MemoFieldBackgroundColor,
                    shape = RoundedCornerShape(18.dp)
                ).padding(
                    horizontal = 20.dp,
                    vertical = 18.dp
                )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "▤",
                color = MemoLabelColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.width(8.dp)
            )

            Text(
                text = "메모",
                color = MemoLabelColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
        }

        BasicTextField(
            value = memo,
            onValueChange = onMemoChange,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 110.dp)
                    .padding(top = 14.dp),
            textStyle =
                TextStyle(
                    color = MemoTextColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 23.sp
                ),
            cursorBrush = SolidColor(MemoCursorColor),
            singleLine = false,
            decorationBox = { innerTextField ->
                if (memo.isBlank()) {
                    Text(
                        text = "메모를 입력해주세요.",
                        color = MemoPlaceholderColor,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                innerTextField()
            }
        )
    }
}
