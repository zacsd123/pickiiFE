package com.example.pickii.ui.passwordreset

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pickii.shared.generated.resources.Res
import com.example.pickii.shared.generated.resources.common_button_cancel
import com.example.pickii.shared.generated.resources.login_brand
import com.example.pickii.shared.generated.resources.password_reset_button_go_login
import com.example.pickii.shared.generated.resources.password_reset_button_submit
import com.example.pickii.shared.generated.resources.password_reset_dialog_complete_title
import com.example.pickii.shared.generated.resources.password_reset_title
import com.example.pickii.shared.generated.resources.signup_button_send_code
import com.example.pickii.shared.generated.resources.signup_button_verify_code
import com.example.pickii.shared.generated.resources.signup_helper_password_rule
import com.example.pickii.shared.generated.resources.signup_label_email
import com.example.pickii.shared.generated.resources.signup_label_password
import com.example.pickii.shared.generated.resources.signup_message_password_match
import com.example.pickii.shared.generated.resources.signup_message_password_mismatch
import com.example.pickii.ui.common.ConfirmDialog
import com.example.pickii.ui.common.FieldLabel
import com.example.pickii.ui.theme.PickiiBlue
import com.example.pickii.ui.theme.PickiiFieldBackground
import com.example.pickii.ui.theme.PickiiTextGray
import com.example.pickii.ui.theme.PickiiYellowLight
import org.jetbrains.compose.resources.stringResource
import org.koin.androidx.compose.koinViewModel

/** 입력 필드/버튼 공통 모서리 둥글기. */
private val FieldCornerRadius = 14.dp

/** 주요 액션 버튼 높이. */
private val ActionButtonHeight = 52.dp

/**
 * 비로그인 상태 비밀번호 재설정 화면.
 *
 * @param onBackClick 뒤로가기 클릭 콜백
 * @param onComplete 재설정 완료 팝업에서 "로그인하러 가기"를 눌렀을 때 호출되는 콜백
 */
@Composable
fun PasswordResetScreen(
    onBackClick: () -> Unit = {},
    onComplete: () -> Unit = {},
    viewModel: PasswordResetViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    PasswordResetScreenContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onEmailChange = viewModel::onEmailChange,
        onSendEmailCodeClick = viewModel::onSendEmailCodeClick,
        onEmailCodeChange = viewModel::onEmailCodeChange,
        onVerifyEmailCodeClick = viewModel::onVerifyEmailCodeClick,
        onNewPasswordChange = viewModel::onNewPasswordChange,
        onTogglePasswordVisibility = viewModel::onTogglePasswordVisibility,
        onNewPasswordConfirmChange = viewModel::onNewPasswordConfirmChange,
        onSubmitClick = viewModel::onSubmitClick,
        onCompleteConfirm = onComplete
    )
}

@Composable
private fun PasswordResetScreenContent(
    uiState: PasswordResetUiState,
    onBackClick: () -> Unit,
    onEmailChange: (String) -> Unit,
    onSendEmailCodeClick: () -> Unit,
    onEmailCodeChange: (String) -> Unit,
    onVerifyEmailCodeClick: () -> Unit,
    onNewPasswordChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onNewPasswordConfirmChange: (String) -> Unit,
    onSubmitClick: () -> Unit,
    onCompleteConfirm: () -> Unit
) {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(colors = listOf(PickiiYellowLight, Color.White)))
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(Res.string.login_brand),
                    color = Color.Black,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(Res.string.common_button_cancel),
                    color = PickiiTextGray,
                    fontSize = 13.sp,
                    modifier = Modifier.clickable(onClick = onBackClick)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(Res.string.password_reset_title),
                color = Color.Black,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 34.sp
            )

            Spacer(modifier = Modifier.height(28.dp))

            FieldLabel(text = stringResource(Res.string.signup_label_email))
            FieldWithButton(
                value = uiState.email,
                onValueChange = onEmailChange,
                buttonLabel = stringResource(Res.string.signup_button_send_code),
                onButtonClick = onSendEmailCodeClick,
                buttonEnabled = uiState.email.isNotBlank() && !uiState.isSendingEmailCode
            )

            if (uiState.isEmailCodeSent) {
                Spacer(modifier = Modifier.height(8.dp))
                FieldWithButton(
                    value = uiState.emailCode,
                    onValueChange = onEmailCodeChange,
                    buttonLabel = stringResource(Res.string.signup_button_verify_code),
                    onButtonClick = onVerifyEmailCodeClick,
                    buttonEnabled = uiState.emailCode.isNotBlank() && !uiState.isVerifyingEmailCode
                )
            }
            StatusMessage(message = uiState.emailMessage, isError = uiState.isEmailMessageError)

            Spacer(modifier = Modifier.height(20.dp))

            FieldLabel(text = stringResource(Res.string.signup_label_password))
            PasswordField(
                value = uiState.newPassword,
                onValueChange = onNewPasswordChange,
                isVisible = uiState.isPasswordVisible,
                onToggleVisibility = onTogglePasswordVisibility
            )
            Text(
                text = stringResource(Res.string.signup_helper_password_rule),
                color = if (uiState.newPassword.isEmpty() || uiState.isPasswordValid) PickiiTextGray else Color.Red,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            PasswordField(
                value = uiState.newPasswordConfirm,
                onValueChange = onNewPasswordConfirmChange,
                isVisible = uiState.isPasswordVisible,
                onToggleVisibility = onTogglePasswordVisibility
            )
            if (uiState.newPasswordConfirm.isNotEmpty()) {
                StatusMessage(
                    message =
                        stringResource(
                            if (uiState.isPasswordMatching) {
                                Res.string.signup_message_password_match
                            } else {
                                Res.string.signup_message_password_mismatch
                            }
                        ),
                    isError = !uiState.isPasswordMatching
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            PrimaryButton(
                text = stringResource(Res.string.password_reset_button_submit),
                onClick = onSubmitClick,
                enabled = uiState.isSubmitEnabled
            )

            uiState.errorMessage?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = it, color = Color.Red, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        if (uiState.isComplete) {
            ConfirmDialog(
                title = stringResource(Res.string.password_reset_dialog_complete_title),
                confirmLabel = stringResource(Res.string.password_reset_button_go_login),
                onConfirm = onCompleteConfirm,
                onDismiss = {}
            )
        }
    }
}

@Composable
private fun FieldWithButton(
    value: String,
    onValueChange: (String) -> Unit,
    buttonLabel: String,
    onButtonClick: () -> Unit,
    buttonEnabled: Boolean
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            singleLine = true,
            shape = RoundedCornerShape(FieldCornerRadius),
            colors = pickiiFieldColors()
        )
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier =
                Modifier
                    .clip(RoundedCornerShape(FieldCornerRadius))
                    .background(if (buttonEnabled) Color.Black else PickiiFieldBackground)
                    .clickable(enabled = buttonEnabled, onClick = onButtonClick)
                    .padding(horizontal = 14.dp, vertical = 16.dp)
        ) {
            Text(
                text = buttonLabel,
                color = if (buttonEnabled) Color.White else PickiiTextGray,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    isVisible: Boolean,
    onToggleVisibility: () -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(FieldCornerRadius),
        visualTransformation = if (isVisible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = onToggleVisibility) {
                Icon(
                    imageVector = if (isVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                    contentDescription = null,
                    tint = PickiiTextGray
                )
            }
        },
        colors = pickiiFieldColors()
    )
}

@Composable
private fun StatusMessage(
    message: String?,
    isError: Boolean
) {
    if (message == null) return
    Spacer(modifier = Modifier.height(4.dp))
    Text(text = message, color = if (isError) Color.Red else PickiiBlue, fontSize = 12.sp)
}

@Composable
private fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean
) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(ActionButtonHeight)
                .clip(RoundedCornerShape(FieldCornerRadius))
                .background(if (enabled) Color.Black else PickiiFieldBackground)
                .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (enabled) Color.White else PickiiTextGray,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun pickiiFieldColors() =
    OutlinedTextFieldDefaults.colors(
        focusedContainerColor = PickiiFieldBackground,
        unfocusedContainerColor = PickiiFieldBackground,
        focusedBorderColor = Color.Transparent,
        unfocusedBorderColor = Color.Transparent
    )

/** [PasswordResetScreen]의 프리뷰. */
@Preview(showBackground = true)
@Composable
private fun PasswordResetScreenPreview() {
    MaterialTheme {
        PasswordResetScreenContent(
            uiState = PasswordResetUiState(),
            onBackClick = {},
            onEmailChange = {},
            onSendEmailCodeClick = {},
            onEmailCodeChange = {},
            onVerifyEmailCodeClick = {},
            onNewPasswordChange = {},
            onTogglePasswordVisibility = {},
            onNewPasswordConfirmChange = {},
            onSubmitClick = {},
            onCompleteConfirm = {}
        )
    }
}
