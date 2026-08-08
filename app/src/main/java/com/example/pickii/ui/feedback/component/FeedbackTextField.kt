package com.example.pickii.ui.feedback.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private const val FEEDBACK_MAX_LENGTH = 300

private val FeedbackTextFieldCardColor = Color(0xFFFFFEF6)
private val FeedbackTextFieldBackgroundColor = Color(0xFFF4F4EE)
private val FeedbackTextFieldTextColor = Color(0xFF1D1D1B)
private val FeedbackTextFieldDescriptionColor = Color(0xFF85857E)
private val FeedbackTextFieldCountColor = Color(0xFF67C784)

@Composable
fun FeedbackTextField(
    title: String,
    description: String,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(FeedbackTextFieldCardColor)
                .padding(18.dp)
    ) {
        Text(
            text = title,
            color = FeedbackTextFieldTextColor,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = description,
            color = FeedbackTextFieldDescriptionColor,
            fontSize = 10.sp,
            lineHeight = 15.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(FeedbackTextFieldBackgroundColor)
                    .padding(14.dp)
        ) {
            if (value.isEmpty()) {
                Text(
                    text = placeholder,
                    color = FeedbackTextFieldDescriptionColor,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }

            BasicTextField(
                value = value,
                onValueChange = { changedValue ->
                    if (changedValue.length <= FEEDBACK_MAX_LENGTH) {
                        onValueChange(changedValue)
                    }
                },
                modifier = Modifier.fillMaxSize(),
                textStyle =
                    TextStyle(
                        color = FeedbackTextFieldTextColor,
                        fontSize = 13.sp,
                        lineHeight = 19.sp
                    )
            )
        }

        Spacer(modifier = Modifier.height(7.dp))

        Text(
            text = "${value.length}자",
            color = FeedbackTextFieldCountColor,
            fontSize = 10.sp,
            modifier = Modifier.align(Alignment.End)
        )
    }
}
