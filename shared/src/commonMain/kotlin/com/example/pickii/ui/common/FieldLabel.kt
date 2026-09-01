package com.example.pickii.ui.common

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** 입력 필드 위에 표시되는 라벨 텍스트(라벨 + 아래 여백). */
@Composable
fun FieldLabel(
    text: String,
    color: Color = Color.Black,
    fontSize: TextUnit = 13.sp,
    fontWeight: FontWeight? = FontWeight.Medium,
    spacerHeight: Dp = 8.dp
) {
    Text(text = text, color = color, fontSize = fontSize, fontWeight = fontWeight)
    Spacer(modifier = Modifier.height(spacerHeight))
}
