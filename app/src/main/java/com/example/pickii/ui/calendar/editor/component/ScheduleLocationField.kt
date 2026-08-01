package com.example.pickii.ui.calendar.editor.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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

private val LocationFieldBackgroundColor = Color(0xFFFFFFFF)
private val LocationLabelColor = Color(0xFF777777)
private val LocationTextColor = Color(0xFF1B1B1B)
private val LocationPlaceholderColor = Color(0xFFAAAAAA)
private val LocationCursorColor = Color(0xFF1B1B1B)

/**
 * 일정 위치 입력 영역이다.
 */
@Composable
fun ScheduleLocationField(
    location: String,
    onLocationChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = LocationFieldBackgroundColor,
                shape = RoundedCornerShape(18.dp),
            )
            .padding(
                horizontal = 20.dp,
                vertical = 18.dp,
            ),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "⌖",
                color = LocationLabelColor,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
            )

            Spacer(
                modifier = Modifier.width(8.dp),
            )

            Text(
                text = "위치",
                color = LocationLabelColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
            )
        }

        BasicTextField(
            value = location,
            onValueChange = onLocationChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 14.dp),
            textStyle = TextStyle(
                color = LocationTextColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
            ),
            cursorBrush = SolidColor(LocationCursorColor),
            singleLine = true,
            decorationBox = { innerTextField ->
                if (location.isBlank()) {
                    Text(
                        text = "위치를 입력해주세요.",
                        color = LocationPlaceholderColor,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }

                innerTextField()
            },
        )
    }
}