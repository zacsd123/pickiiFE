package com.example.pickii.ui.findid

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pickii.R
import com.example.pickii.ui.common.OneShotEventEffect
import com.example.pickii.ui.common.RecruitUiEvent
import com.example.pickii.ui.theme.PickiiBlue
import com.example.pickii.ui.theme.PickiiFieldBackground
import com.example.pickii.ui.theme.PickiiTextGray
import com.example.pickii.ui.theme.PickiiYellowLight

/** 입력 필드/버튼 공통 모서리 둥글기. */
private val FieldCornerRadius = 14.dp

/** 주요 액션 버튼 높이. */
private val ActionButtonHeight = 52.dp

/**
 * 아이디 찾기 화면.
 *
 * 이름+이메일 인증으로 아이디(닉네임)를 조회하는 API가 아직 없어, 입력 UI만 제공하고 각 버튼은
 * 안내 토스트만 띄운다({@link FindIdViewModel} 참고).
 *
 * @param onBackClick 뒤로가기 클릭 콜백
 */
@Composable
fun FindIdScreen(
    onBackClick: () -> Unit = {},
    viewModel: FindIdViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    OneShotEventEffect(flow = viewModel.events) { event ->
        when (event) {
            is RecruitUiEvent.ShowToast ->
                Toast.makeText(context, context.getString(event.messageRes), Toast.LENGTH_SHORT).show()
        }
    }

    FindIdScreenContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onNameChange = viewModel::onNameChange,
        onEmailChange = viewModel::onEmailChange,
        onSendCodeClick = viewModel::onSendCodeClick,
        onEmailCodeChange = viewModel::onEmailCodeChange,
        onVerifyClick = viewModel::onVerifyClick,
        onFindIdClick = viewModel::onFindIdClick
    )
}

@Composable
private fun FindIdScreenContent(
    uiState: FindIdUiState,
    onBackClick: () -> Unit,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onSendCodeClick: () -> Unit,
    onEmailCodeChange: (String) -> Unit,
    onVerifyClick: () -> Unit,
    onFindIdClick: () -> Unit
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
            Spacer(modifier = Modifier.height(20.dp))

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                tint = Color.Black,
                modifier =
                    Modifier
                        .size(24.dp)
                        .clickable(onClick = onBackClick)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.login_brand),
                color = Color.Black,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.find_id_title),
                color = Color.Black,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 34.sp
            )

            Spacer(modifier = Modifier.height(28.dp))

            FieldLabel(text = stringResource(R.string.find_id_label_name))
            OutlinedTextField(
                value = uiState.name,
                onValueChange = onNameChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(text = stringResource(R.string.find_id_placeholder_name), color = PickiiTextGray)
                },
                singleLine = true,
                shape = RoundedCornerShape(FieldCornerRadius),
                colors = pickiiFieldColors()
            )

            Spacer(modifier = Modifier.height(20.dp))

            FieldLabel(text = stringResource(R.string.find_id_label_email))
            FieldWithButton(
                value = uiState.email,
                onValueChange = onEmailChange,
                placeholder = stringResource(R.string.find_id_placeholder_email),
                buttonLabel = stringResource(R.string.signup_button_send_code),
                onButtonClick = onSendCodeClick,
                buttonEnabled = uiState.isSendCodeEnabled
            )

            if (uiState.isEmailCodeSent) {
                StatusMessage(message = stringResource(R.string.find_id_message_code_sent), isError = false)
            }

            if (uiState.isEmailCodeSent) {
                Spacer(modifier = Modifier.height(20.dp))

                FieldLabel(text = stringResource(R.string.find_id_label_code))
                FieldWithButton(
                    value = uiState.emailCode,
                    onValueChange = onEmailCodeChange,
                    placeholder = "",
                    buttonLabel = stringResource(R.string.signup_button_verify_code),
                    onButtonClick = onVerifyClick,
                    buttonEnabled = uiState.isVerifyEnabled
                )

                if (uiState.isEmailVerified) {
                    StatusMessage(message = stringResource(R.string.find_id_message_email_verified), isError = false)
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            PrimaryButton(
                text = stringResource(R.string.find_id_button_submit),
                onClick = onFindIdClick,
                enabled = uiState.isSubmitEnabled
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(text = text, color = Color.Black, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    Spacer(modifier = Modifier.height(8.dp))
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
private fun StatusMessage(
    message: String,
    isError: Boolean
) {
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

/** [FindIdScreen]의 초기 입력 상태 프리뷰(참고 이미지 1번). */
@Preview(showBackground = true)
@Composable
private fun FindIdScreenInitialPreview() {
    MaterialTheme {
        FindIdScreenContent(
            uiState = FindIdUiState(),
            onBackClick = {},
            onNameChange = {},
            onEmailChange = {},
            onSendCodeClick = {},
            onEmailCodeChange = {},
            onVerifyClick = {},
            onFindIdClick = {}
        )
    }
}

/** [FindIdScreen]의 이메일 인증 완료 상태 프리뷰(참고 이미지 2번). */
@Preview(showBackground = true)
@Composable
private fun FindIdScreenVerifiedPreview() {
    MaterialTheme {
        FindIdScreenContent(
            uiState =
                FindIdUiState(
                    name = "김기획",
                    email = "example@google.com",
                    isEmailCodeSent = true,
                    emailCode = "123456",
                    isEmailVerified = true
                ),
            onBackClick = {},
            onNameChange = {},
            onEmailChange = {},
            onSendCodeClick = {},
            onEmailCodeChange = {},
            onVerifyClick = {},
            onFindIdClick = {}
        )
    }
}

/**
 * 아이디 찾기 결과 화면(참고 이미지 3번).
 *
 * 이름+이메일로 아이디를 조회하는 API가 아직 없어, 현재는 [FindIdScreen] 제출과 연결돼 있지 않다.
 * 백엔드 API가 확정되면 조회 결과로 이 화면에 이동하도록 연결하면 된다.
 *
 * @param email 조회에 사용한 이메일
 * @param foundId 조회된 아이디
 * @param onLoginClick "로그인하기" 클릭 콜백
 */
@Composable
fun FindIdResultScreen(
    email: String,
    foundId: String,
    onLoginClick: () -> Unit = {}
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
                    .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = stringResource(R.string.login_brand),
                color = Color.Black,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = stringResource(R.string.find_id_result_title),
                color = Color.Black,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 34.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(FieldCornerRadius))
                        .background(Color.White)
                        .padding(horizontal = 20.dp, vertical = 18.dp)
            ) {
                FindIdResultRow(label = stringResource(R.string.find_id_result_label_email), value = email)
                Spacer(modifier = Modifier.height(6.dp))
                FindIdResultRow(label = stringResource(R.string.find_id_result_label_id), value = foundId)
            }

            Spacer(modifier = Modifier.height(28.dp))

            PrimaryButton(
                text = stringResource(R.string.find_id_result_button_login),
                onClick = onLoginClick,
                enabled = true
            )
        }
    }
}

@Composable
private fun FindIdResultRow(
    label: String,
    value: String
) {
    Row {
        Text(text = label, color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = "|", color = PickiiTextGray, fontSize = 14.sp)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = value, color = PickiiBlue, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

/** [FindIdResultScreen]의 프리뷰(참고 이미지 3번). */
@Preview(showBackground = true)
@Composable
private fun FindIdResultScreenPreview() {
    MaterialTheme {
        FindIdResultScreen(email = "example@google.com", foundId = "id!", onLoginClick = {})
    }
}
