package com.example.pickii.ui.calendar.editor.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val TitleFieldBackgroundColor = Color(0xFFFFFFFF)
private val TitleTextColor = Color(0xFF1B1B1B)
private val TitlePlaceholderColor = Color(0xFF929292)
private val TitleCursorColor = Color(0xFF1B1B1B)

/**
 * 일정 제목 입력 필드다.
 */
@Composable
fun ScheduleTitleField(
    title: String,
    onTitleChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    BasicTextField(
        value = title,
        onValueChange = onTitleChange,
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = TitleFieldBackgroundColor,
                shape = RoundedCornerShape(18.dp),
            )
            .padding(
                horizontal = 20.dp,
                vertical = 20.dp,
            ),
        textStyle = TextStyle(
            color = TitleTextColor,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
        ),
        cursorBrush = SolidColor(TitleCursorColor),
        singleLine = true,
        decorationBox = { innerTextField ->
            if (title.isBlank()) {
                androidx.compose.material3.Text(
                    text = "일정 제목",
                    color = TitlePlaceholderColor,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            innerTextField()
        },
    )
}