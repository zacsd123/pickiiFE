package com.example.pickii.ui.common

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

/**
 * 제목/본문과 확인·취소 버튼을 갖는 공용 확인 팝업.
 *
 * @param title 팝업 제목
 * @param body 팝업 본문
 * @param confirmLabel 확인 버튼 문구
 * @param dismissLabel 취소 버튼 문구
 * @param onConfirm 확인 버튼 클릭 콜백
 * @param onDismiss 팝업 닫기(취소 버튼 또는 바깥 영역 클릭) 콜백
 */
@Composable
fun ConfirmDialog(
    title: String,
    body: String,
    confirmLabel: String,
    dismissLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = { Text(text = body) },
        confirmButton = { TextButton(onClick = onConfirm) { Text(text = confirmLabel) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(text = dismissLabel) } }
    )
}
