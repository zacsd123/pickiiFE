package com.example.pickii.ui.common

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.pickii.R

/**
 * 비로그인 상태에서 로그인이 필요한 동작을 시도했을 때 노출하는 로그인 유도 팝업.
 *
 * @param onLoginClick "로그인하러 가기" 클릭 콜백
 * @param onDismiss 팝업 닫기 콜백
 */
@Composable
fun LoginRequiredDialog(
    onLoginClick: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.common_login_required_title)) },
        text = { Text(text = stringResource(R.string.common_login_required_body)) },
        confirmButton = {
            TextButton(onClick = onLoginClick) {
                Text(text = stringResource(R.string.common_login_required_button_login))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.common_login_required_button_cancel))
            }
        }
    )
}
