package com.example.pickii.ui.mypage.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
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
import com.example.pickii.ui.theme.PickiiDisabledGray
import com.example.pickii.ui.theme.PickiiFieldBackground
import com.example.pickii.ui.theme.PickiiTextGray
import com.example.pickii.ui.theme.PickiiYellowLight

private val FieldCornerRadius = 14.dp

/**
 * 로그인 상태 비밀번호 변경 화면(사진 목업 기준: 현재 비밀번호 입력 포함).
 *
 * @param onSuccess 변경 성공 콜백(보안을 위해 로그아웃 후 로그인 화면으로 이동)
 */
@Composable
fun PasswordChangeScreen(
    onBackClick: () -> Unit,
    onSuccess: () -> Unit,
    viewModel: PasswordChangeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    PasswordChangeScreenContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onCurrentPasswordChange = viewModel::onCurrentPasswordChange,
        onNewPasswordChange = viewModel::onNewPasswordChange,
        onNewPasswordConfirmChange = viewModel::onNewPasswordConfirmChange,
        onToggleCurrentPasswordVisibility = viewModel::onToggleCurrentPasswordVisibility,
        onToggleNewPasswordVisibility = viewModel::onToggleNewPasswordVisibility,
        onSubmitClick = { viewModel.onSubmitClick(onSuccess) }
    )
}

@Suppress("LongParameterList")
@Composable
private fun PasswordChangeScreenContent(
    uiState: PasswordChangeUiState,
    onBackClick: () -> Unit,
    onCurrentPasswordChange: (String) -> Unit,
    onNewPasswordChange: (String) -> Unit,
    onNewPasswordConfirmChange: (String) -> Unit,
    onToggleCurrentPasswordVisibility: () -> Unit,
    onToggleNewPasswordVisibility: () -> Unit,
    onSubmitClick: () -> Unit
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    PickiiYellowLight
                ).verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        BackHeader(title = stringResource(R.string.mypage_password_change_title), onBackClick = onBackClick)
        Spacer(modifier = Modifier.height(24.dp))

        FieldLabel(stringResource(R.string.mypage_password_change_label_current))
        PasswordField(
            value = uiState.currentPassword,
            onValueChange = onCurrentPasswordChange,
            isVisible = uiState.isCurrentPasswordVisible,
            onToggleVisibility = onToggleCurrentPasswordVisibility
        )

        Spacer(modifier = Modifier.height(16.dp))
        FieldLabel(stringResource(R.string.mypage_password_change_label_new))
        PasswordField(
            value = uiState.newPassword,
            onValueChange = onNewPasswordChange,
            isVisible = uiState.isNewPasswordVisible,
            onToggleVisibility = onToggleNewPasswordVisibility
        )
        Text(text = stringResource(R.string.mypage_password_change_hint_new), color = PickiiTextGray, fontSize = 11.sp)

        Spacer(modifier = Modifier.height(16.dp))
        FieldLabel(stringResource(R.string.mypage_password_change_label_confirm))
        PasswordField(
            value = uiState.newPasswordConfirm,
            onValueChange = onNewPasswordConfirmChange,
            isVisible = uiState.isNewPasswordVisible,
            onToggleVisibility = onToggleNewPasswordVisibility
        )
        if (uiState.isMismatch) {
            Text(
                text = stringResource(R.string.mypage_password_change_error_mismatch),
                color = Color.Red,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(FieldCornerRadius))
                    .background(if (uiState.isValid) Color.Black else PickiiDisabledGray)
                    .clickable(enabled = uiState.isValid && !uiState.isSubmitting, onClick = onSubmitClick)
                    .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.mypage_password_change_button_submit),
                color = if (uiState.isValid) Color.White else PickiiTextGray,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (uiState.errorMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = uiState.errorMessage, color = Color.Red, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(text = text, color = Color.Black, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    Spacer(modifier = Modifier.height(8.dp))
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
        colors =
            OutlinedTextFieldDefaults.colors(
                focusedContainerColor = PickiiFieldBackground,
                unfocusedContainerColor = PickiiFieldBackground,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent
            )
    )
}
