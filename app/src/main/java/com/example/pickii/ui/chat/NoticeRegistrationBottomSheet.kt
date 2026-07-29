package com.example.pickii.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoticeRegistrationBottomSheet(
    initialContent: String = "",
    onDismiss: () -> Unit,
    onCompleteClick: (String) -> Unit,
) {
    var noticeContent by remember(initialContent) {
        mutableStateOf(initialContent)
    }

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    )

    val isCompleteEnabled = noticeContent.isNotBlank()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(
            topStart = 28.dp,
            topEnd = 28.dp,
        ),
        dragHandle = null,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .navigationBarsPadding()
                .padding(
                    horizontal = 20.dp,
                    vertical = 20.dp,
                ),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "취소",
                    modifier = Modifier.clickable(onClick = onDismiss),
                    color = Color(0xFF6B7280),
                    fontSize = 15.sp,
                )

                Text(
                    text = "공지 등록",
                    modifier = Modifier.weight(1f),
                    color = Color(0xFF111827),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )

                Text(
                    text = "완료",
                    modifier = Modifier.clickable(
                        enabled = isCompleteEnabled,
                    ) {
                        onCompleteClick(noticeContent.trim())
                    },
                    color = if (isCompleteEnabled) {
                        Color(0xFF111827)
                    } else {
                        Color(0xFFBFC3C9)
                    },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            BasicTextField(
                value = noticeContent,
                onValueChange = { newContent ->
                    noticeContent = newContent
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(
                        color = Color(0xFFF4F5F7),
                        shape = RoundedCornerShape(18.dp),
                    )
                    .padding(18.dp),
                textStyle = TextStyle(
                    color = Color(0xFF111827),
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                ),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.TopStart,
                    ) {
                        if (noticeContent.isEmpty()) {
                            Text(
                                text = "멤버들과 공유하고 싶은 소식을 남겨보세요.",
                                color = Color(0xFF9CA3AF),
                                fontSize = 15.sp,
                                lineHeight = 22.sp,
                            )
                        }

                        innerTextField()
                    }
                },
            )

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}