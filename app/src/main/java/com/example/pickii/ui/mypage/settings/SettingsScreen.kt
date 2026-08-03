package com.example.pickii.ui.mypage.settings

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pickii.R
import com.example.pickii.ui.common.BackHeader
import com.example.pickii.ui.common.ConfirmDialog
import com.example.pickii.ui.theme.PickiiBlue
import com.example.pickii.ui.theme.PickiiFieldBackground
import com.example.pickii.ui.theme.PickiiTextGray
import com.example.pickii.ui.theme.PickiiYellowLight
import com.example.pickii.util.kakao.KakaoAuthClient
import kotlinx.coroutines.launch

/**
 * 설정 화면. 계정 설정(비밀번호 변경/카카오 연동·해제/로그아웃/회원탈퇴)과 알림 설정을 한 화면에서 보여준다(사진 목업 기준).
 *
 * @param onNavigateToPasswordChange "비밀번호 바꾸기" 클릭 콜백
 * @param onNavigateToWithdrawal "회원탈퇴" 클릭 콜백
 * @param onLoggedOut 로그아웃 완료 콜백(앱 레벨 네비게이션)
 */
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onNavigateToPasswordChange: () -> Unit,
    onNavigateToWithdrawal: () -> Unit,
    onLoggedOut: () -> Unit,
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    notificationViewModel: NotificationSettingsViewModel = hiltViewModel()
) {
    val uiState by settingsViewModel.uiState.collectAsStateWithLifecycle()
    val notificationUiState by notificationViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val kakaoLinkErrorMessage = stringResource(R.string.mypage_profile_social_link_error)

    SettingsScreenContent(
        uiState = uiState,
        notificationUiState = notificationUiState,
        onBackClick = onBackClick,
        onPasswordChangeClick = onNavigateToPasswordChange,
        onLinkClick = {
            scope.launch {
                KakaoAuthClient
                    .login(context)
                    .onSuccess {
                        KakaoAuthClient
                            .getUserId()
                            .onSuccess { providerId ->
                                settingsViewModel.onKakaoLinkConfirmed(
                                    providerId = providerId.toString(),
                                    onError = {
                                        Toast.makeText(context, kakaoLinkErrorMessage, Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }.onFailure {
                                Toast.makeText(context, kakaoLinkErrorMessage, Toast.LENGTH_SHORT).show()
                            }
                    }.onFailure {
                        Toast.makeText(context, kakaoLinkErrorMessage, Toast.LENGTH_SHORT).show()
                    }
            }
        },
        onUnlinkRequest = settingsViewModel::onUnlinkRequest,
        onDismissUnlinkDialog = settingsViewModel::onDismissUnlinkDialog,
        onConfirmUnlink = settingsViewModel::onConfirmUnlink,
        onLogoutRequest = settingsViewModel::onLogoutRequest,
        onDismissLogoutDialog = settingsViewModel::onDismissLogoutDialog,
        onConfirmLogout = { settingsViewModel.onConfirmLogout(onLoggedOut) },
        onWithdrawClick = onNavigateToWithdrawal,
        onToggleAllEssential = notificationViewModel::onToggleAllEssential,
        onToggleChat = notificationViewModel::onToggleChat,
        onToggleApplicant = notificationViewModel::onToggleApplicant,
        onToggleComment = notificationViewModel::onToggleComment,
        onToggleSchedule = notificationViewModel::onToggleSchedule,
        onToggleMatch = notificationViewModel::onToggleMatch,
        onToggleProject = notificationViewModel::onToggleProject,
        onToggleMarketing = notificationViewModel::onToggleMarketing
    )
}

@Suppress("LongParameterList")
@Composable
private fun SettingsScreenContent(
    uiState: SettingsUiState,
    notificationUiState: NotificationSettingsUiState,
    onBackClick: () -> Unit,
    onPasswordChangeClick: () -> Unit,
    onLinkClick: () -> Unit,
    onUnlinkRequest: (String) -> Unit,
    onDismissUnlinkDialog: () -> Unit,
    onConfirmUnlink: () -> Unit,
    onLogoutRequest: () -> Unit,
    onDismissLogoutDialog: () -> Unit,
    onConfirmLogout: () -> Unit,
    onWithdrawClick: () -> Unit,
    onToggleAllEssential: (Boolean) -> Unit,
    onToggleChat: (Boolean) -> Unit,
    onToggleApplicant: (Boolean) -> Unit,
    onToggleComment: (Boolean) -> Unit,
    onToggleSchedule: (Boolean) -> Unit,
    onToggleMatch: (Boolean) -> Unit,
    onToggleProject: (Boolean) -> Unit,
    onToggleMarketing: (Boolean) -> Unit
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
        BackHeader(title = stringResource(R.string.mypage_menu_settings_title), onBackClick = onBackClick)
        Spacer(modifier = Modifier.height(20.dp))

        SectionTitle(stringResource(R.string.mypage_settings_section_account))
        Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color.White)) {
            SettingsRow(
                title = stringResource(R.string.mypage_settings_password_change),
                onClick = onPasswordChangeClick
            )
            KakaoLinkRow(
                isLinked = uiState.isKakaoLinked,
                onLinkClick = onLinkClick,
                onUnlinkClick = { onUnlinkRequest("KAKAO") }
            )
            SettingsRow(title = stringResource(R.string.mypage_settings_logout), onClick = onLogoutRequest)
            SettingsRow(
                title = stringResource(R.string.mypage_settings_withdraw),
                titleColor = Color(0xFFE53935),
                onClick = onWithdrawClick,
                showDivider = false
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        SectionTitle(stringResource(R.string.mypage_settings_section_notification))
        Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color.White)) {
            val settings = notificationUiState.settings
            ToggleRow(
                title = stringResource(R.string.mypage_notification_all),
                checked = settings.allEssentialEnabled,
                onCheckedChange = onToggleAllEssential
            )
            ToggleRow(
                title = stringResource(R.string.mypage_notification_chat),
                subtitle = stringResource(R.string.mypage_notification_chat_desc),
                checked = settings.chatNoti,
                onCheckedChange = onToggleChat
            )
            ToggleRow(
                title = stringResource(R.string.mypage_notification_applicant),
                subtitle = stringResource(R.string.mypage_notification_applicant_desc),
                checked = settings.applicantNoti,
                onCheckedChange = onToggleApplicant
            )
            ToggleRow(
                title = stringResource(R.string.mypage_notification_comment),
                subtitle = stringResource(R.string.mypage_notification_comment_desc),
                checked = settings.commentNoti,
                onCheckedChange = onToggleComment
            )
            ToggleRow(
                title = stringResource(R.string.mypage_notification_schedule),
                subtitle = stringResource(R.string.mypage_notification_schedule_desc),
                checked = settings.scheduleNoti,
                onCheckedChange = onToggleSchedule
            )
            ToggleRow(
                title = stringResource(R.string.mypage_notification_match),
                subtitle = stringResource(R.string.mypage_notification_match_desc),
                checked = settings.matchNoti,
                onCheckedChange = onToggleMatch
            )
            ToggleRow(
                title = stringResource(R.string.mypage_notification_project),
                subtitle = stringResource(R.string.mypage_notification_project_desc),
                checked = settings.projectNoti,
                onCheckedChange = onToggleProject
            )
            ToggleRow(
                title = stringResource(R.string.mypage_notification_marketing),
                subtitle = stringResource(R.string.mypage_notification_marketing_desc),
                checked = settings.marketingNoti,
                onCheckedChange = onToggleMarketing,
                showDivider = false
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    if (uiState.isLogoutConfirmVisible) {
        ConfirmDialog(
            title = stringResource(R.string.mypage_logout_title),
            body = stringResource(R.string.mypage_logout_body),
            confirmLabel = stringResource(R.string.mypage_logout_confirm),
            dismissLabel = stringResource(R.string.common_button_cancel),
            onConfirm = onConfirmLogout,
            onDismiss = onDismissLogoutDialog
        )
    }

    if (uiState.isUnlinkConfirmVisible) {
        ConfirmDialog(
            title = stringResource(R.string.mypage_profile_social_unlink_confirm_title),
            body = stringResource(R.string.mypage_profile_social_unlink_confirm_body),
            confirmLabel = stringResource(R.string.common_button_confirm),
            dismissLabel = stringResource(R.string.common_button_cancel),
            onConfirm = onConfirmUnlink,
            onDismiss = onDismissUnlinkDialog
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text = text, color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(10.dp))
}

@Composable
private fun SettingsRow(
    title: String,
    onClick: () -> Unit,
    trailingText: String? = null,
    titleColor: Color = Color.Black,
    showDivider: Boolean = true
) {
    Column {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable(
                        onClick = onClick
                    ).padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = titleColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            if (trailingText != null) {
                Text(text = trailingText, color = PickiiTextGray, fontSize = 12.sp)
            }
        }
        if (showDivider) {
            Box(
                modifier = Modifier.fillMaxWidth().height(1.dp).background(PickiiFieldBackground)
            )
        }
    }
}

/** 카카오 연동/해제를 이 화면에서 바로 처리하는 행이다(연동 관리를 프로필 화면에서 이 화면으로 이전). */
@Composable
private fun KakaoLinkRow(
    isLinked: Boolean,
    onLinkClick: () -> Unit,
    onUnlinkClick: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.mypage_settings_kakao_link),
                color = Color.Black,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            Text(
                text =
                    stringResource(
                        if (isLinked) {
                            R.string.mypage_profile_social_linked
                        } else {
                            R.string.mypage_profile_social_not_linked
                        }
                    ),
                color = if (isLinked) PickiiBlue else PickiiTextGray,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Box(
                modifier =
                    Modifier
                        .clip(RoundedCornerShape(50))
                        .background(PickiiFieldBackground)
                        .clickable(onClick = { if (isLinked) onUnlinkClick() else onLinkClick() })
                        .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text =
                        stringResource(
                            if (isLinked) {
                                R.string.mypage_profile_social_button_unlink
                            } else {
                                R.string.mypage_profile_social_button_link
                            }
                        ),
                    color = Color.Black,
                    fontSize = 12.sp
                )
            }
        }
        Box(
            modifier = Modifier.fillMaxWidth().height(1.dp).background(PickiiFieldBackground)
        )
    }
}

@Composable
private fun ToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    subtitle: String? = null,
    showDivider: Boolean = true
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                if (subtitle != null) {
                    Text(text = subtitle, color = PickiiTextGray, fontSize = 11.sp)
                }
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(checkedTrackColor = PickiiBlue)
            )
        }
        if (showDivider) {
            Box(
                modifier = Modifier.fillMaxWidth().height(1.dp).background(PickiiFieldBackground)
            )
        }
    }
}
