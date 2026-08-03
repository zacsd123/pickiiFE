package com.example.pickii.ui.signup

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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pickii.R
import com.example.pickii.domain.model.SignupTerms
import com.example.pickii.ui.theme.PickiiBlue
import com.example.pickii.ui.theme.PickiiFieldBackground
import com.example.pickii.ui.theme.PickiiTextGray
import com.example.pickii.ui.theme.PickiiYellowLight

/** 입력 필드/버튼 공통 모서리 둥글기. */
private val FieldCornerRadius = 14.dp

/** 주요 액션 버튼 높이. */
private val ActionButtonHeight = 52.dp

/**
 * 회원가입 화면. 가입 폼([SignupPhase.FORM])과 가입완료 환영 화면([SignupPhase.WELCOME])을 한 화면에서 전환한다.
 *
 * @param onBackClick 가입 폼에서 뒤로가기 클릭 콜백
 * @param onNavigateHome 환영 화면에서 로그인 성공 + 프로필 보유 시 홈으로 이동하는 콜백
 * @param onNavigateOnboarding 환영 화면에서 로그인 성공 + 프로필 미보유 시 온보딩으로 이동하는 콜백
 */
@Composable
fun SignupScreen(
    onBackClick: () -> Unit = {},
    onNavigateHome: () -> Unit = {},
    onNavigateOnboarding: () -> Unit = {},
    viewModel: SignupViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (uiState.phase) {
        SignupPhase.FORM ->
            SignupFormContent(
                uiState = uiState,
                onBackClick = onBackClick,
                onNicknameChange = viewModel::onNicknameChange,
                onCheckNicknameClick = viewModel::onCheckNicknameClick,
                onEmailChange = viewModel::onEmailChange,
                onSendEmailCodeClick = viewModel::onSendEmailCodeClick,
                onEmailCodeChange = viewModel::onEmailCodeChange,
                onVerifyEmailCodeClick = viewModel::onVerifyEmailCodeClick,
                onPasswordChange = viewModel::onPasswordChange,
                onTogglePasswordVisibility = viewModel::onTogglePasswordVisibility,
                onPasswordConfirmChange = viewModel::onPasswordConfirmChange,
                onAgeAgreedChange = viewModel::onAgeAgreedChange,
                onTermsAgreedChange = viewModel::onTermsAgreedChange,
                onPrivacyAgreedChange = viewModel::onPrivacyAgreedChange,
                onProfileShareAgreedChange = viewModel::onProfileShareAgreedChange,
                onDataCollectionAgreedChange = viewModel::onDataCollectionAgreedChange,
                onPushNotiAgreedChange = viewModel::onPushNotiAgreedChange,
                onToggleAllTerms = viewModel::onToggleAllTerms,
                onSubmitClick = viewModel::onSubmitClick
            )

        SignupPhase.WELCOME ->
            SignupWelcomeContent(
                uiState = uiState,
                onLoginClick = {
                    viewModel.onLoginClick(onNavigateHome = onNavigateHome, onNavigateOnboarding = onNavigateOnboarding)
                }
            )
    }
}

/** 회원가입 폼 화면 본문. */
@Composable
private fun SignupFormContent(
    uiState: SignupUiState,
    onBackClick: () -> Unit,
    onNicknameChange: (String) -> Unit,
    onCheckNicknameClick: () -> Unit,
    onEmailChange: (String) -> Unit,
    onSendEmailCodeClick: () -> Unit,
    onEmailCodeChange: (String) -> Unit,
    onVerifyEmailCodeClick: () -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onPasswordConfirmChange: (String) -> Unit,
    onAgeAgreedChange: (Boolean) -> Unit,
    onTermsAgreedChange: (Boolean) -> Unit,
    onPrivacyAgreedChange: (Boolean) -> Unit,
    onProfileShareAgreedChange: (Boolean) -> Unit,
    onDataCollectionAgreedChange: (Boolean) -> Unit,
    onPushNotiAgreedChange: (Boolean) -> Unit,
    onToggleAllTerms: () -> Unit,
    onSubmitClick: () -> Unit
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
                    text = stringResource(R.string.login_brand),
                    color = Color.Black,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.common_button_cancel),
                    color = PickiiTextGray,
                    fontSize = 13.sp,
                    modifier = Modifier.clickable(onClick = onBackClick)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.signup_title),
                color = Color.Black,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(28.dp))

            FieldLabel(text = stringResource(R.string.signup_label_nickname))
            FieldWithButton(
                value = uiState.nickname,
                onValueChange = onNicknameChange,
                buttonLabel = stringResource(R.string.signup_button_check_nickname),
                onButtonClick = onCheckNicknameClick,
                buttonEnabled = uiState.nickname.isNotBlank() && !uiState.isCheckingNickname
            )
            StatusMessage(message = uiState.nicknameMessage, isError = uiState.isNicknameMessageError)

            Spacer(modifier = Modifier.height(20.dp))

            FieldLabel(text = stringResource(R.string.signup_label_email))
            FieldWithButton(
                value = uiState.email,
                onValueChange = onEmailChange,
                buttonLabel = stringResource(R.string.signup_button_send_code),
                onButtonClick = onSendEmailCodeClick,
                buttonEnabled = uiState.email.isNotBlank() && !uiState.isSendingEmailCode
            )

            if (uiState.isEmailCodeSent) {
                Spacer(modifier = Modifier.height(8.dp))
                FieldWithButton(
                    value = uiState.emailCode,
                    onValueChange = onEmailCodeChange,
                    buttonLabel = stringResource(R.string.signup_button_verify_code),
                    onButtonClick = onVerifyEmailCodeClick,
                    buttonEnabled = uiState.emailCode.isNotBlank() && !uiState.isVerifyingEmailCode
                )
            }
            StatusMessage(message = uiState.emailMessage, isError = uiState.isEmailMessageError)

            Spacer(modifier = Modifier.height(20.dp))

            FieldLabel(text = stringResource(R.string.signup_label_password))
            PasswordField(
                value = uiState.password,
                onValueChange = onPasswordChange,
                isVisible = uiState.isPasswordVisible,
                onToggleVisibility = onTogglePasswordVisibility
            )
            Text(
                text = stringResource(R.string.signup_helper_password_rule),
                color = if (uiState.password.isEmpty() || uiState.isPasswordValid) PickiiTextGray else Color.Red,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            PasswordField(
                value = uiState.passwordConfirm,
                onValueChange = onPasswordConfirmChange,
                isVisible = uiState.isPasswordVisible,
                onToggleVisibility = onTogglePasswordVisibility
            )
            if (uiState.passwordConfirm.isNotEmpty()) {
                StatusMessage(
                    message =
                        stringResource(
                            if (uiState.isPasswordMatching) {
                                R.string.signup_message_password_match
                            } else {
                                R.string.signup_message_password_mismatch
                            }
                        ),
                    isError = !uiState.isPasswordMatching
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            TermsSection(
                terms = uiState.terms,
                onAgeAgreedChange = onAgeAgreedChange,
                onTermsAgreedChange = onTermsAgreedChange,
                onPrivacyAgreedChange = onPrivacyAgreedChange,
                onProfileShareAgreedChange = onProfileShareAgreedChange,
                onDataCollectionAgreedChange = onDataCollectionAgreedChange,
                onPushNotiAgreedChange = onPushNotiAgreedChange,
                onToggleAllTerms = onToggleAllTerms
            )

            Spacer(modifier = Modifier.height(24.dp))

            PrimaryButton(
                text = stringResource(R.string.signup_button_submit),
                onClick = onSubmitClick,
                enabled = uiState.isSubmitEnabled
            )

            uiState.errorMessage?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = it, color = Color.Red, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/** 가입완료 환영 화면 본문. */
@Composable
private fun SignupWelcomeContent(
    uiState: SignupUiState,
    onLoginClick: () -> Unit
) {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(colors = listOf(PickiiYellowLight, Color.White)))
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.login_brand),
                color = Color.Black,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = stringResource(R.string.signup_welcome_title, uiState.nickname),
                color = Color.Black,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 34.sp
            )

            Spacer(modifier = Modifier.height(40.dp))

            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .padding(20.dp)
            ) {
                RecapRow(label = stringResource(R.string.signup_label_email), value = uiState.email)
                Spacer(modifier = Modifier.height(8.dp))
                RecapRow(label = stringResource(R.string.signup_label_password), value = uiState.password)
            }

            Spacer(modifier = Modifier.height(32.dp))

            PrimaryButton(
                text = stringResource(R.string.signup_welcome_button_login),
                onClick = onLoginClick,
                enabled = !uiState.isSubmitting
            )

            uiState.errorMessage?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = it, color = Color.Red, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun RecapRow(
    label: String,
    value: String
) {
    Row {
        Text(text = label, color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = "|", color = PickiiTextGray, fontSize = 14.sp)
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = value, color = PickiiBlue, fontSize = 14.sp)
    }
}

/** 입력 필드 위에 표시되는 라벨 텍스트. */
@Composable
private fun FieldLabel(text: String) {
    Text(text = text, color = Color.Black, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    Spacer(modifier = Modifier.height(8.dp))
}

/** 텍스트 필드 오른쪽에 보조 버튼(중복확인/코드발송/인증하기)이 붙은 행. */
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

/** 비밀번호 표시/숨김 토글이 붙은 텍스트 필드. */
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

/** 성공/실패 상태 안내 문구. [message]가 없으면 아무것도 그리지 않는다. */
@Composable
private fun StatusMessage(
    message: String?,
    isError: Boolean
) {
    if (message == null) return
    Spacer(modifier = Modifier.height(4.dp))
    Text(text = message, color = if (isError) Color.Red else PickiiBlue, fontSize = 12.sp)
}

/** 약관 동의 섹션. "전체 선택" 토글 + 필수 5개/선택 1개 체크리스트. */
@Composable
private fun TermsSection(
    terms: SignupTerms,
    onAgeAgreedChange: (Boolean) -> Unit,
    onTermsAgreedChange: (Boolean) -> Unit,
    onPrivacyAgreedChange: (Boolean) -> Unit,
    onProfileShareAgreedChange: (Boolean) -> Unit,
    onDataCollectionAgreedChange: (Boolean) -> Unit,
    onPushNotiAgreedChange: (Boolean) -> Unit,
    onToggleAllTerms: () -> Unit
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(PickiiFieldBackground)
                .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.signup_terms_title),
                color = Color.Black,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            TermCheckboxRow(
                label = stringResource(R.string.signup_terms_select_all),
                checked = terms.isAllAgreed,
                onCheckedChange = { onToggleAllTerms() }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        TermCheckboxRow(
            label = stringResource(R.string.signup_term_age),
            checked = terms.ageAgreed,
            onCheckedChange = onAgeAgreedChange
        )
        TermCheckboxRow(
            label = stringResource(R.string.signup_term_service),
            checked = terms.termsAgreed,
            onCheckedChange = onTermsAgreedChange
        )
        TermCheckboxRow(
            label = stringResource(R.string.signup_term_privacy),
            checked = terms.privacyAgreed,
            onCheckedChange = onPrivacyAgreedChange
        )
        TermCheckboxRow(
            label = stringResource(R.string.signup_term_profile_share),
            checked = terms.profileShareAgreed,
            onCheckedChange = onProfileShareAgreedChange
        )
        TermCheckboxRow(
            label = stringResource(R.string.signup_term_data_collection),
            checked = terms.dataCollectionAgreed,
            onCheckedChange = onDataCollectionAgreedChange
        )
        TermCheckboxRow(
            label = stringResource(R.string.signup_term_push_noti),
            checked = terms.pushNotiAgreed,
            onCheckedChange = onPushNotiAgreedChange
        )
    }
}

@Composable
private fun TermCheckboxRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable { onCheckedChange(!checked) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(checkedColor = Color.Black)
        )
        Text(text = label, color = Color.Black, fontSize = 13.sp)
    }
}

/** 검은 배경의 주요 액션 버튼. 비활성화 시 회색으로 표시된다. */
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

/** [SignupScreen]의 가입 폼 프리뷰. */
@Preview(showBackground = true)
@Composable
private fun SignupFormPreview() {
    MaterialTheme {
        SignupFormContent(
            uiState = SignupUiState(),
            onBackClick = {},
            onNicknameChange = {},
            onCheckNicknameClick = {},
            onEmailChange = {},
            onSendEmailCodeClick = {},
            onEmailCodeChange = {},
            onVerifyEmailCodeClick = {},
            onPasswordChange = {},
            onTogglePasswordVisibility = {},
            onPasswordConfirmChange = {},
            onAgeAgreedChange = {},
            onTermsAgreedChange = {},
            onPrivacyAgreedChange = {},
            onProfileShareAgreedChange = {},
            onDataCollectionAgreedChange = {},
            onPushNotiAgreedChange = {},
            onToggleAllTerms = {},
            onSubmitClick = {}
        )
    }
}

/** [SignupScreen]의 환영 화면 프리뷰. */
@Preview(showBackground = true)
@Composable
private fun SignupWelcomePreview() {
    MaterialTheme {
        SignupWelcomeContent(
            uiState =
                SignupUiState(
                    phase = SignupPhase.WELCOME,
                    nickname = "김기획",
                    email = "example@google.com",
                    password = "password@!"
                ),
            onLoginClick = {}
        )
    }
}
