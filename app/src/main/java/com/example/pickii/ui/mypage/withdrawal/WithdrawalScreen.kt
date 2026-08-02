package com.example.pickii.ui.mypage.withdrawal

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pickii.R
import com.example.pickii.ui.common.BackHeader
import com.example.pickii.ui.theme.PickiiBlue
import com.example.pickii.ui.theme.PickiiDisabledGray
import com.example.pickii.ui.theme.PickiiFieldBackground
import com.example.pickii.ui.theme.PickiiTextGray
import com.example.pickii.ui.theme.PickiiYellowLight

private val FieldCornerRadius = 14.dp
private val WithdrawRed = Color(0xFFE53935)
private val WithdrawWarningBackground = Color(0xFFFDECEA)

/**
 * 회원 탈퇴 화면(15번). 실제 백엔드 동작(1-9) 기준으로 만들었다 — 작성한 공고/댓글/채팅은 삭제되지 않고
 * 작성자만 "알 수 없음"으로 바뀐다(목업 사진의 "모두 사라져요" 문구는 실제 동작과 달라 쓰지 않았다).
 *
 * @param onComplete 탈퇴 완료 팝업 확인 콜백(앱 레벨 네비게이션, 로그인 화면으로 이동)
 */
@Composable
fun WithdrawalScreen(
    onBackClick: () -> Unit,
    onComplete: () -> Unit,
    viewModel: WithdrawalViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    WithdrawalScreenContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onEmailChange = viewModel::onEmailChange,
        onSendEmailCodeClick = viewModel::onSendEmailCodeClick,
        onEmailCodeChange = viewModel::onEmailCodeChange,
        onVerifyEmailCodeClick = viewModel::onVerifyEmailCodeClick,
        onPasswordChange = viewModel::onPasswordChange,
        onTogglePasswordVisibility = viewModel::onTogglePasswordVisibility,
        onToggleDataDeletionAgreed = viewModel::onToggleDataDeletionAgreed,
        onToggleRejoinPolicyAgreed = viewModel::onToggleRejoinPolicyAgreed,
        onWithdrawClick = viewModel::onWithdrawClick,
        onComplete = onComplete
    )
}

@Suppress("LongParameterList")
@Composable
private fun WithdrawalScreenContent(
    uiState: WithdrawalUiState,
    onBackClick: () -> Unit,
    onEmailChange: (String) -> Unit,
    onSendEmailCodeClick: () -> Unit,
    onEmailCodeChange: (String) -> Unit,
    onVerifyEmailCodeClick: () -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onToggleDataDeletionAgreed: () -> Unit,
    onToggleRejoinPolicyAgreed: () -> Unit,
    onWithdrawClick: () -> Unit,
    onComplete: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize().background(PickiiYellowLight)) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
            Spacer(modifier = Modifier.height(16.dp))
            BackHeader(title = stringResource(R.string.mypage_withdraw_title), onBackClick = onBackClick)
            Spacer(modifier = Modifier.height(24.dp))

            FieldWithButton(
                value = uiState.email,
                onValueChange = onEmailChange,
                placeholder = stringResource(R.string.mypage_withdraw_placeholder_email),
                buttonLabel = stringResource(R.string.mypage_withdraw_button_verify),
                onButtonClick = onSendEmailCodeClick,
                buttonEnabled = uiState.email.isNotBlank() && !uiState.isSendingEmailCode
            )

            if (uiState.isEmailCodeSent) {
                Spacer(modifier = Modifier.height(8.dp))
                FieldWithButton(
                    value = uiState.emailCode,
                    onValueChange = onEmailCodeChange,
                    placeholder = stringResource(R.string.mypage_withdraw_placeholder_code),
                    buttonLabel = stringResource(R.string.mypage_withdraw_button_verify),
                    onButtonClick = onVerifyEmailCodeClick,
                    buttonEnabled = uiState.emailCode.isNotBlank() && !uiState.isVerifyingEmailCode
                )
            }
            if (uiState.emailMessage != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = uiState.emailMessage,
                    color = if (uiState.isEmailMessageError) Color.Red else PickiiBlue,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = uiState.password,
                onValueChange = onPasswordChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(text = stringResource(R.string.mypage_withdraw_placeholder_password), color = PickiiTextGray)
                },
                singleLine = true,
                visualTransformation = if (uiState.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                shape = RoundedCornerShape(FieldCornerRadius),
                colors = fieldColors()
            )

            Spacer(modifier = Modifier.height(20.dp))
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(WithdrawWarningBackground)
                        .padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.mypage_withdraw_warning_title),
                    color = WithdrawRed,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                listOf(
                    R.string.mypage_withdraw_warning_1,
                    R.string.mypage_withdraw_warning_2,
                    R.string.mypage_withdraw_warning_3
                ).forEach { res ->
                    Text(text = "· " + stringResource(res), color = Color.Black, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            AgreementRow(
                text = stringResource(R.string.mypage_withdraw_agreement_1),
                checked = uiState.isDataDeletionAgreed,
                onToggle = onToggleDataDeletionAgreed
            )
            AgreementRow(
                text = stringResource(R.string.mypage_withdraw_agreement_2),
                checked = uiState.isRejoinPolicyAgreed,
                onToggle = onToggleRejoinPolicyAgreed
            )

            Spacer(modifier = Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier =
                        Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(FieldCornerRadius))
                            .background(PickiiFieldBackground)
                            .clickable(onClick = onBackClick)
                            .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.mypage_withdraw_button_cancel),
                        color = Color.Black,
                        fontSize = 14.sp
                    )
                }
                Box(
                    modifier =
                        Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(FieldCornerRadius))
                            .background(if (uiState.isSubmitEnabled) WithdrawRed else PickiiDisabledGray)
                            .clickable(enabled = uiState.isSubmitEnabled, onClick = onWithdrawClick)
                            .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.mypage_withdraw_button_submit),
                        color = if (uiState.isSubmitEnabled) Color.White else PickiiTextGray,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (uiState.errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = uiState.errorMessage, color = Color.Red, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (uiState.isComplete) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text(text = stringResource(R.string.mypage_withdraw_complete)) },
            text = {},
            confirmButton = {
                TextButton(onClick = onComplete) { Text(text = stringResource(R.string.common_button_confirm)) }
            }
        )
    }
}

@Composable
private fun FieldWithButton(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    buttonLabel: String,
    onButtonClick: () -> Unit,
    buttonEnabled: Boolean
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text(text = placeholder, color = PickiiTextGray) },
            singleLine = true,
            shape = RoundedCornerShape(FieldCornerRadius),
            colors = fieldColors()
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
private fun AgreementRow(
    text: String,
    checked: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onToggle),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = checked, onCheckedChange = {
            onToggle()
        }, colors = CheckboxDefaults.colors(checkedColor = PickiiBlue))
        Text(text = text, color = Color.Black, fontSize = 12.sp)
    }
}

@Composable
private fun fieldColors() =
    OutlinedTextFieldDefaults.colors(
        focusedContainerColor = PickiiFieldBackground,
        unfocusedContainerColor = PickiiFieldBackground,
        focusedBorderColor = Color.Transparent,
        unfocusedBorderColor = Color.Transparent
    )
