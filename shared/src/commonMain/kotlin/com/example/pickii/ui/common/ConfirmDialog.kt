package com.example.pickii.ui.common

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

/**
 * 제목/본문과 확인 버튼(취소 버튼은 선택)을 갖는 공용 확인 팝업.
 *
 * @param title 팝업 제목
 * @param confirmLabel 확인 버튼 문구
 * @param onConfirm 확인 버튼 클릭 콜백
 * @param body 팝업 본문. 생략하면 본문 없이 제목+버튼만 표시된다.
 * @param dismissLabel 취소 버튼 문구. 생략하면 취소 버튼 없이 확인 버튼 하나만 표시된다.
 * @param onDismiss 팝업 닫기(취소 버튼 또는 바깥 영역 클릭) 콜백. 생략하면 [onConfirm]과 동일하게 동작한다.
 */
@Composable
fun ConfirmDialog(
    title: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    body: String? = null,
    dismissLabel: String? = null,
    onDismiss: () -> Unit = onConfirm
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = { if (body != null) Text(text = body) },
        confirmButton = { TextButton(onClick = onConfirm) { Text(text = confirmLabel) } },
        dismissButton = dismissLabel?.let { label -> { TextButton(onClick = onDismiss) { Text(text = label) } } }
    )
}
