package com.example.pickii.ui.login

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pickii.R
import com.example.pickii.ui.common.ConfirmDialog
import com.example.pickii.ui.common.FieldLabel
import com.example.pickii.ui.theme.KakaoLabel
import com.example.pickii.ui.theme.KakaoYellow
import com.example.pickii.ui.theme.PickiiFieldBackground
import com.example.pickii.ui.theme.PickiiTextGray
import com.example.pickii.ui.theme.PickiiYellowLight
import com.example.pickii.util.kakao.KakaoAuthClient
import kotlinx.coroutines.launch

/** 입력 필드와 버튼에 공통으로 사용하는 모서리 둥글기. */
private val FieldCornerRadius = 14.dp

/** 로그인 버튼, 보조 버튼에 공통으로 사용하는 높이. */
private val ActionButtonHeight = 52.dp

/**
 * 이메일/비밀번호 로그인 화면.
 *
 * [LoginViewModel]에서 입력 상태를 받아와 [LoginScreenContent]에 전달한다.
 * 실제 로그인/비회원 전환 처리는 [LoginViewModel]이 담당하며, 성공했을 때만 각 콜백이 호출된다.
 *
 * @param onNavigateHome 로그인에 성공하고 프로필(이력서)이 있을 때 호출되는 콜백
 * @param onNavigateOnboarding 로그인에 성공했지만 프로필이 없을 때(최초 로그인) 호출되는 콜백
 * @param onNavigateToPasswordReset "비밀번호 재설정" 링크를 눌렀을 때 호출되는 콜백
 * @param onSignUpClick 회원가입 버튼 클릭 콜백
 * @param onGuestClick 비회원으로 둘러보기를 완료했을 때 호출되는 콜백
 */
@Composable
fun LoginScreen(
    onNavigateHome: () -> Unit = {},
    onNavigateOnboarding: () -> Unit = {},
    onNavigateToPasswordReset: () -> Unit = {},
    onSignUpClick: () -> Unit = {},
    onGuestClick: () -> Unit = {},
    viewModel: LoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val kakaoNotLinkedMessage = stringResource(R.string.login_kakao_not_linked)
    val kakaoErrorMessage = stringResource(R.string.login_kakao_error)

    LoginScreenContent(
        uiState = uiState,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onTogglePasswordVisibility = viewModel::onTogglePasswordVisibility,
        onToggleAutoLogin = viewModel::onToggleAutoLogin,
        onLoginClick = {
            viewModel.onLoginClick(onNavigateHome = onNavigateHome, onNavigateOnboarding = onNavigateOnboarding)
        },
        onErrorDialogDismiss = viewModel::onErrorDialogDismiss,
        onKakaoLoginClick = {
            scope.launch {
                KakaoAuthClient
                    .login(context)
                    .onSuccess { token ->
                        viewModel.onKakaoLoginClick(
                            kakaoAccessToken = token.accessToken,
                            onNavigateHome = onNavigateHome,
                            onNavigateOnboarding = onNavigateOnboarding,
                            onNotLinked = {
                                Toast.makeText(context, kakaoNotLinkedMessage, Toast.LENGTH_LONG).show()
                            },
                            onError = {
                                Toast.makeText(context, kakaoErrorMessage, Toast.LENGTH_SHORT).show()
                            }
                        )
                    }.onFailure {
                        Toast.makeText(context, kakaoErrorMessage, Toast.LENGTH_SHORT).show()
                    }
            }
        },
        onNavigateToPasswordReset = onNavigateToPasswordReset,
        onSignUpClick = onSignUpClick,
        onGuestClick = { viewModel.onGuestClick(onComplete = onGuestClick) }
    )
}

/** [LoginScreen]의 실제 UI. ViewModel 없이도 미리보기/테스트가 가능하도록 상태를 파라미터로 받는다. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Suppress("LongParameterList")
private fun LoginScreenContent(
    uiState: LoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onToggleAutoLogin: () -> Unit,
    onLoginClick: () -> Unit,
    onKakaoLoginClick: () -> Unit,
    onNavigateToPasswordReset: () -> Unit,
    onSignUpClick: () -> Unit,
    onGuestClick: () -> Unit,
    onErrorDialogDismiss: () -> Unit = {}
) {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(PickiiYellowLight, Color.White)
                    )
                )
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.login_brand),
                color = Color.Black,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.login_title),
                color = Color.Black,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(32.dp))

            FieldLabel(text = stringResource(R.string.login_label_email))
            PickiiTextField(
                value = uiState.email,
                onValueChange = onEmailChange,
                placeholder = stringResource(R.string.login_placeholder_email),
                keyboardType = KeyboardType.Email
            )

            Spacer(modifier = Modifier.height(20.dp))

            FieldLabel(text = stringResource(R.string.login_label_password))
            PickiiTextField(
                value = uiState.password,
                onValueChange = onPasswordChange,
                placeholder = stringResource(R.string.login_placeholder_password),
                keyboardType = KeyboardType.Password,
                visualTransformation =
                    if (uiState.isPasswordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                trailingIcon = {
                    IconButton(onClick = onTogglePasswordVisibility) {
                        Icon(
                            imageVector =
                                if (uiState.isPasswordVisible) {
                                    Icons.Filled.Visibility
                                } else {
                                    Icons.Filled.VisibilityOff
                                },
                            contentDescription = null,
                            tint = PickiiTextGray
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.account_recovery_button_reset_password),
                color = PickiiTextGray,
                fontSize = 13.sp,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onNavigateToPasswordReset),
                textAlign = TextAlign.End
            )

            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(ActionButtonHeight)
                        .clip(RoundedCornerShape(FieldCornerRadius))
                        .background(Color.Black)
                        .clickable(onClick = onLoginClick),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.login_button_submit),
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onToggleAutoLogin),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(18.dp)
                            .border(1.5.dp, PickiiTextGray, CircleShape)
                            .then(
                                if (uiState.isAutoLoginChecked) {
                                    Modifier.background(Color.Black, CircleShape)
                                } else {
                                    Modifier
                                }
                            )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.login_auto_login),
                    color = PickiiTextGray,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f))
                Text(
                    text = stringResource(R.string.login_divider_or),
                    color = PickiiTextGray,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                HorizontalDivider(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(24.dp))

            SecondaryButton(text = stringResource(R.string.login_button_signup), onClick = onSignUpClick)

            Spacer(modifier = Modifier.height(12.dp))

            KakaoLoginButton(onClick = onKakaoLoginClick)

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = stringResource(R.string.login_guest_browse),
                color = PickiiTextGray,
                fontSize = 13.sp,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onGuestClick)
                        .padding(bottom = 24.dp),
                textAlign = TextAlign.Center
            )
        }

        if (uiState.errorMessage != null) {
            ConfirmDialog(
                title = stringResource(R.string.login_error_title),
                body = uiState.errorMessage,
                confirmLabel = stringResource(R.string.login_error_confirm),
                onConfirm = onErrorDialogDismiss
            )
        }
    }
}

/** Pickii 로그인 화면 전용 스타일이 적용된 텍스트 입력 필드. */
@Composable
private fun PickiiTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: (@Composable () -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(text = placeholder, color = PickiiTextGray) },
        singleLine = true,
        shape = RoundedCornerShape(FieldCornerRadius),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = visualTransformation,
        trailingIcon = trailingIcon,
        colors =
            OutlinedTextFieldDefaults.colors(
                focusedContainerColor = PickiiFieldBackground,
                unfocusedContainerColor = PickiiFieldBackground,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent
            )
    )
}

/** 회원가입, 회원 찾기 등에 사용되는 화살표(`>`)가 붙은 보조 버튼. */
@Composable
private fun SecondaryButton(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(ActionButtonHeight)
                .clip(RoundedCornerShape(FieldCornerRadius))
                .background(PickiiFieldBackground)
                .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$text >",
            color = Color.Black,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

/** 카카오 브랜드 컬러가 적용된 로그인 버튼. */
@Composable
private fun KakaoLoginButton(onClick: () -> Unit) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(ActionButtonHeight)
                .clip(RoundedCornerShape(FieldCornerRadius))
                .background(KakaoYellow)
                .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.login_button_kakao),
            color = KakaoLabel,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

/** [LoginScreen]의 프리뷰. */
@Preview(showBackground = true)
@Composable
private fun LoginScreenPreview() {
    MaterialTheme {
        LoginScreenContent(
            uiState = LoginUiState(),
            onEmailChange = {},
            onPasswordChange = {},
            onTogglePasswordVisibility = {},
            onToggleAutoLogin = {},
            onLoginClick = {},
            onKakaoLoginClick = {},
            onNavigateToPasswordReset = {},
            onSignUpClick = {},
            onGuestClick = {}
        )
    }
}
