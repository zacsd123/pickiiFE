package com.example.pickii.ui.common

import androidx.compose.runtime.Composable
import com.example.pickii.shared.generated.resources.Res
import com.example.pickii.shared.generated.resources.common_login_required_body
import com.example.pickii.shared.generated.resources.common_login_required_button_cancel
import com.example.pickii.shared.generated.resources.common_login_required_button_login
import com.example.pickii.shared.generated.resources.common_login_required_title
import org.jetbrains.compose.resources.stringResource

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
    ConfirmDialog(
        title = stringResource(Res.string.common_login_required_title),
        body = stringResource(Res.string.common_login_required_body),
        confirmLabel = stringResource(Res.string.common_login_required_button_login),
        dismissLabel = stringResource(Res.string.common_login_required_button_cancel),
        onConfirm = onLoginClick,
        onDismiss = onDismiss
    )
}
