package com.example.pickii.ui.common

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.pickii.shared.generated.resources.Res
import com.example.pickii.shared.generated.resources.common_ai_dialog_button_close
import com.example.pickii.shared.generated.resources.common_ai_dialog_button_retry
import com.example.pickii.shared.generated.resources.common_ai_dialog_failed_title
import com.example.pickii.shared.generated.resources.common_ai_dialog_loading
import org.jetbrains.compose.resources.stringResource

/** AI 초안 생성 팝업의 표시 상태. */
sealed interface AiDialogState {
    /** 팝업이 표시되지 않는 상태. */
    data object Hidden : AiDialogState

    /** AI가 초안을 생성하는 중임을 안내하는 상태. */
    data object Loading : AiDialogState

    /** 초안 생성이 실패해 재시도를 안내하는 상태. */
    data object Failed : AiDialogState
}

/**
 * AI 초안 생성 중/실패 상태를 보여주는 공용 팝업. [state]가 [AiDialogState.Hidden]이면 아무것도 그리지 않는다.
 *
 * @param state 현재 팝업 표시 상태
 * @param onRetry 실패 상태에서 "다시 시도" 클릭 콜백
 * @param onDismiss 팝업 닫기 콜백
 */
@Composable
fun AiGenerationDialog(
    state: AiDialogState,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    when (state) {
        AiDialogState.Hidden -> Unit

        AiDialogState.Loading ->
            AlertDialog(
                onDismissRequest = {},
                confirmButton = {},
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = stringResource(Res.string.common_ai_dialog_loading))
                    }
                }
            )

        AiDialogState.Failed ->
            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text(text = stringResource(Res.string.common_ai_dialog_failed_title)) },
                text = {},
                confirmButton = {
                    TextButton(
                        onClick = onRetry
                    ) { Text(text = stringResource(Res.string.common_ai_dialog_button_retry)) }
                },
                dismissButton = {
                    TextButton(
                        onClick = onDismiss
                    ) { Text(text = stringResource(Res.string.common_ai_dialog_button_close)) }
                }
            )
    }
}
