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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.example.pickii.ui.theme.KakaoYellow
import com.example.pickii.ui.theme.PickiiFieldBackground
import com.example.pickii.ui.theme.PickiiPaletteBaseWhite
import com.example.pickii.ui.theme.PickiiPaletteRed
import com.example.pickii.ui.theme.PickiiTextGray
import com.example.pickii.util.kakao.KakaoAuthClient
import kotlinx.coroutines.launch

/**
 * 설정 화면. 계정 설정(비밀번호 변경/카카오 연동·해제/로그아웃/회원탈퇴)과 알림 설정을 한 화면에서 보여준다(사진 목업 기준).
 *
 * 알림 설정의 "공고 알림"은 실제로는 지원자/댓글/일정/매칭/프로젝트 5개 서버 값으로 나뉘어 있지만(4-9-6),
 * 목업 사진처럼 화면에서는 토글 하나로 묶어서 보여주고 다섯 값을 한꺼번에 켜고 끈다.
 *
 * @param onNavigateToPasswordChange "비밀번호 바꾸기" 클릭 콜백
 * @param onNavigateToWithdrawal "회원탈퇴" 클릭 콜백
 * @param onNavigateToLogout "로그아웃" 클릭 콜백(확인 화면으로 이동)
 */
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onNavigateToPasswordChange: () -> Unit,
    onNavigateToWithdrawal: () -> Unit,
    onNavigateToLogout: () -> Unit,
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    notificationViewModel: NotificationSettingsViewModel = hiltViewModel()
) {
    val uiState by settingsViewModel.uiState.collectAsStateWithLifecycle()
    val notificationUiState by notificationViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val kakaoLinkErrorMessage = stringResource(R.string.mypage_profile_social_link_error)

    if (notificationUiState.toastMessageRes != null) {
        val messageRes = notificationUiState.toastMessageRes
        LaunchedEffect(messageRes) {
            if (messageRes != null) Toast.makeText(context, messageRes, Toast.LENGTH_SHORT).show()
            notificationViewModel.onToastShown()
        }
    }

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
        onLogoutClick = onNavigateToLogout,
        onWithdrawClick = onNavigateToWithdrawal,
        onToggleAllEssential = notificationViewModel::onToggleAllEssential,
        onToggleChat = notificationViewModel::onToggleChat,
        onToggleAnnouncement = { enabled ->
            notificationViewModel.onToggleApplicant(enabled)
            notificationViewModel.onToggleComment(enabled)
            notificationViewModel.onToggleSchedule(enabled)
            notificationViewModel.onToggleMatch(enabled)
            notificationViewModel.onToggleProject(enabled)
        },
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
    onLogoutClick: () -> Unit,
    onWithdrawClick: () -> Unit,
    onToggleAllEssential: (Boolean) -> Unit,
    onToggleChat: (Boolean) -> Unit,
    onToggleAnnouncement: (Boolean) -> Unit,
    onToggleMarketing: (Boolean) -> Unit
) {
    val settings = notificationUiState.settings
    val isAnnouncementEnabled =
        settings.applicantNoti &&
            settings.commentNoti &&
            settings.scheduleNoti &&
            settings.matchNoti &&
            settings.projectNoti

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(PickiiPaletteBaseWhite)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        BackHeader(title = stringResource(R.string.mypage_menu_settings_title), onBackClick = onBackClick)
        Spacer(modifier = Modifier.height(20.dp))

        SectionTitle(stringResource(R.string.mypage_settings_section_account))
        Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color.White)) {
            SettingsRow(
                icon = Icons.Filled.Lock,
                iconBackground = PickiiFieldBackground,
                iconTint = Color.Black,
                title = stringResource(R.string.mypage_settings_password_change),
                onClick = onPasswordChangeClick
            )
            KakaoLinkRow(
                isLinked = uiState.isKakaoLinked,
                onLinkClick = onLinkClick,
                onUnlinkClick = { onUnlinkRequest("KAKAO") }
            )
            SettingsRow(
                icon = Icons.AutoMirrored.Filled.Logout,
                iconBackground = PickiiFieldBackground,
                iconTint = Color.Black,
                title = stringResource(R.string.mypage_settings_logout),
                onClick = onLogoutClick
            )
            SettingsRow(
                icon = Icons.Filled.PersonOff,
                iconBackground = PickiiPaletteRed.copy(alpha = 0.12f),
                iconTint = PickiiPaletteRed,
                title = stringResource(R.string.mypage_settings_withdraw),
                titleColor = PickiiPaletteRed,
                onClick = onWithdrawClick,
                showDivider = false
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        SectionTitle(stringResource(R.string.mypage_settings_section_notification))
        Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color.White)) {
            ToggleRow(
                icon = Icons.Filled.Settings,
                title = stringResource(R.string.mypage_notification_all),
                checked = settings.allEssentialEnabled,
                onCheckedChange = onToggleAllEssential
            )
            ToggleRow(
                icon = Icons.AutoMirrored.Filled.Chat,
                title = stringResource(R.string.mypage_notification_chat),
                subtitle = stringResource(R.string.mypage_notification_chat_desc),
                checked = settings.chatNoti,
                onCheckedChange = onToggleChat
            )
            ToggleRow(
                icon = Icons.Filled.Campaign,
                title = stringResource(R.string.mypage_notification_announcement),
                subtitle = stringResource(R.string.mypage_notification_announcement_desc),
                checked = isAnnouncementEnabled,
                onCheckedChange = onToggleAnnouncement
            )
            ToggleRow(
                icon = Icons.Filled.Campaign,
                title = stringResource(R.string.mypage_notification_marketing),
                subtitle = stringResource(R.string.mypage_notification_marketing_desc),
                checked = settings.marketingNoti,
                onCheckedChange = onToggleMarketing,
                showDivider = false
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
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
    icon: ImageVector,
    iconBackground: Color,
    iconTint: Color,
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
                    ).padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RowIcon(icon = icon, background = iconBackground, tint = iconTint)
            Spacer(modifier = Modifier.width(12.dp))
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

@Composable
private fun RowIcon(
    icon: ImageVector,
    background: Color,
    tint: Color
) {
    Box(
        modifier = Modifier.size(32.dp).clip(CircleShape).background(background),
        contentAlignment = Alignment.Center
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
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
            RowIcon(icon = Icons.Filled.Campaign, background = KakaoYellow, tint = Color.Black)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.mypage_settings_kakao_link),
                    color = Color.Black,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
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
                    color = PickiiTextGray,
                    fontSize = 11.sp
                )
            }
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
    icon: ImageVector,
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
            RowIcon(icon = icon, background = PickiiFieldBackground, tint = Color.Black)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                if (subtitle != null) {
                    Text(text = subtitle, color = PickiiTextGray, fontSize = 11.sp)
                }
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(checkedTrackColor = Color.Black, checkedThumbColor = Color.White)
            )
        }
        if (showDivider) {
            Box(
                modifier = Modifier.fillMaxWidth().height(1.dp).background(PickiiFieldBackground)
            )
        }
    }
}
