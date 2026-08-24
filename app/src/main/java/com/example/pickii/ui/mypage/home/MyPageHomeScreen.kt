package com.example.pickii.ui.mypage.home

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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pickii.shared.generated.resources.Res
import com.example.pickii.shared.generated.resources.ic_bookmark_border
import com.example.pickii.shared.generated.resources.ic_campaign
import com.example.pickii.shared.generated.resources.ic_chat
import com.example.pickii.shared.generated.resources.ic_chevron_right_filled
import com.example.pickii.shared.generated.resources.ic_description
import com.example.pickii.shared.generated.resources.ic_emoji_events
import com.example.pickii.shared.generated.resources.ic_settings
import com.example.pickii.shared.generated.resources.mypage_button_create_profile
import com.example.pickii.shared.generated.resources.mypage_button_edit_profile
import com.example.pickii.shared.generated.resources.mypage_contact_email
import com.example.pickii.shared.generated.resources.mypage_contact_instagram
import com.example.pickii.shared.generated.resources.mypage_contact_label
import com.example.pickii.shared.generated.resources.mypage_create_profile_prompt
import com.example.pickii.shared.generated.resources.mypage_menu_applications_subtitle
import com.example.pickii.shared.generated.resources.mypage_menu_applications_title
import com.example.pickii.shared.generated.resources.mypage_menu_feedback_subtitle
import com.example.pickii.shared.generated.resources.mypage_menu_feedback_title
import com.example.pickii.shared.generated.resources.mypage_menu_my_comments_subtitle
import com.example.pickii.shared.generated.resources.mypage_menu_my_comments_title
import com.example.pickii.shared.generated.resources.mypage_menu_my_recruits_subtitle
import com.example.pickii.shared.generated.resources.mypage_menu_my_recruits_title
import com.example.pickii.shared.generated.resources.mypage_menu_scraps_subtitle
import com.example.pickii.shared.generated.resources.mypage_menu_scraps_title
import com.example.pickii.shared.generated.resources.mypage_menu_settings_title
import com.example.pickii.shared.generated.resources.mypage_section_activity
import com.example.pickii.shared.generated.resources.mypage_section_settings
import com.example.pickii.ui.common.AvatarPlaceholder
import com.example.pickii.ui.common.LevelAvatar
import com.example.pickii.ui.common.LevelProgressBar
import com.example.pickii.ui.common.LoadingIndicator
import com.example.pickii.ui.common.PickiiBottomNavOverlaySpacing
import com.example.pickii.ui.common.PickiiTopBar
import com.example.pickii.ui.theme.PickiiBlue
import com.example.pickii.ui.theme.PickiiFieldBackground
import com.example.pickii.ui.theme.PickiiTextGray
import com.example.pickii.ui.theme.PickiiYellowLight
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.androidx.compose.koinViewModel

/**
 * 마이페이지 홈. 프로필 요약(닉네임/레벨/경험치), 활동내역 5종, 설정 진입점을 보여준다.
 *
 * @param onProfileClick 프로필 요약 카드 클릭 콜백(이력서 형태의 프로필 조회 화면으로 이동)
 * @param onEditProfileClick "프로필 수정" 버튼 클릭 콜백
 * @param onCreateProfileClick 프로필이 없을 때 "프로필 만들기" 버튼 클릭 콜백
 */
@Composable
fun MyPageHomeScreen(
    onProfileClick: () -> Unit,
    onEditProfileClick: () -> Unit,
    onCreateProfileClick: () -> Unit,
    onApplicationsClick: () -> Unit,
    onMyRecruitsClick: () -> Unit,
    onScrapsClick: () -> Unit,
    onMyCommentsClick: () -> Unit,
    onFeedbackClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onNotificationClick: () -> Unit,
    viewModel: MyPageHomeViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    MyPageHomeScreenContent(
        uiState = uiState,
        onProfileClick = onProfileClick,
        onEditProfileClick = onEditProfileClick,
        onCreateProfileClick = onCreateProfileClick,
        onApplicationsClick = onApplicationsClick,
        onMyRecruitsClick = onMyRecruitsClick,
        onScrapsClick = onScrapsClick,
        onMyCommentsClick = onMyCommentsClick,
        onFeedbackClick = onFeedbackClick,
        onSettingsClick = onSettingsClick,
        onNotificationClick = onNotificationClick
    )
}

@Suppress("LongParameterList")
@Composable
private fun MyPageHomeScreenContent(
    uiState: MyPageHomeUiState,
    onProfileClick: () -> Unit,
    onEditProfileClick: () -> Unit,
    onCreateProfileClick: () -> Unit,
    onApplicationsClick: () -> Unit,
    onMyRecruitsClick: () -> Unit,
    onScrapsClick: () -> Unit,
    onMyCommentsClick: () -> Unit,
    onFeedbackClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onNotificationClick: () -> Unit
) {
    if (uiState.isLoading) {
        LoadingIndicator(modifier = Modifier.fillMaxSize().background(PickiiYellowLight))
        return
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(PickiiYellowLight)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = PickiiBottomNavOverlaySpacing)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        PickiiTopBar(notificationCount = uiState.unreadNotificationCount, onNotificationClick = onNotificationClick)

        Spacer(modifier = Modifier.height(20.dp))

        ProfileSummaryCard(
            uiState = uiState,
            onProfileClick = onProfileClick,
            onEditProfileClick = onEditProfileClick,
            onCreateProfileClick = onCreateProfileClick
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(Res.string.mypage_section_activity),
            color = Color.Black,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
        ) {
            ActivityMenuRow(
                icon = Res.drawable.ic_description,
                title = stringResource(Res.string.mypage_menu_applications_title),
                subtitle = stringResource(Res.string.mypage_menu_applications_subtitle),
                onClick = onApplicationsClick
            )
            ActivityMenuRow(
                icon = Res.drawable.ic_campaign,
                title = stringResource(Res.string.mypage_menu_my_recruits_title),
                subtitle = stringResource(Res.string.mypage_menu_my_recruits_subtitle),
                onClick = onMyRecruitsClick
            )
            ActivityMenuRow(
                icon = Res.drawable.ic_bookmark_border,
                title = stringResource(Res.string.mypage_menu_scraps_title),
                subtitle = stringResource(Res.string.mypage_menu_scraps_subtitle),
                onClick = onScrapsClick
            )
            ActivityMenuRow(
                icon = Res.drawable.ic_chat,
                title = stringResource(Res.string.mypage_menu_my_comments_title),
                subtitle = stringResource(Res.string.mypage_menu_my_comments_subtitle),
                onClick = onMyCommentsClick
            )
            ActivityMenuRow(
                icon = Res.drawable.ic_emoji_events,
                title = stringResource(Res.string.mypage_menu_feedback_title),
                subtitle = stringResource(Res.string.mypage_menu_feedback_subtitle),
                onClick = onFeedbackClick,
                showDivider = false
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(Res.string.mypage_section_settings),
            color = Color.Black,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color.White)) {
            ActivityMenuRow(
                icon = Res.drawable.ic_settings,
                title = stringResource(Res.string.mypage_menu_settings_title),
                subtitle = null,
                onClick = onSettingsClick,
                showDivider = false
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(text = stringResource(Res.string.mypage_contact_label), color = PickiiTextGray, fontSize = 12.sp)
        Text(text = stringResource(Res.string.mypage_contact_email), color = PickiiTextGray, fontSize = 12.sp)
        Text(text = stringResource(Res.string.mypage_contact_instagram), color = PickiiTextGray, fontSize = 12.sp)

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun ProfileSummaryCard(
    uiState: MyPageHomeUiState,
    onProfileClick: () -> Unit,
    onEditProfileClick: () -> Unit,
    onCreateProfileClick: () -> Unit
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .padding(16.dp)
    ) {
        if (uiState.hasProfile == true) {
            Row(
                modifier = Modifier.fillMaxWidth().clickable(onClick = onProfileClick),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LevelAvatar(exp = uiState.exp)

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(text = uiState.nickname, color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    LevelProgressBar(exp = uiState.exp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Box(
                    modifier =
                        Modifier
                            .clip(RoundedCornerShape(50))
                            .background(PickiiFieldBackground)
                            .clickable(onClick = onEditProfileClick)
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = stringResource(Res.string.mypage_button_edit_profile),
                        color = Color.Black,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AvatarPlaceholder()
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(Res.string.mypage_create_profile_prompt),
                    color = Color.Black,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(PickiiBlue)
                        .clickable(onClick = onCreateProfileClick)
                        .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(Res.string.mypage_button_create_profile),
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ActivityMenuRow(
    icon: DrawableResource,
    title: String,
    subtitle: String?,
    onClick: () -> Unit,
    showDivider: Boolean = true
) {
    Column {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClick)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.width(22.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = subtitle, color = PickiiTextGray, fontSize = 12.sp)
                }
            }
            Icon(
                painter = painterResource(Res.drawable.ic_chevron_right_filled),
                contentDescription = null,
                tint = PickiiTextGray
            )
        }
        if (showDivider) {
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(PickiiFieldBackground))
        }
    }
}

/** [MyPageHomeScreen]의 프리뷰(프로필 보유 상태). */
@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun MyPageHomeScreenPreview() {
    MaterialTheme {
        MyPageHomeScreenContent(
            uiState = MyPageHomeUiState(isLoading = false, hasProfile = true, nickname = "픽키", exp = 250),
            onProfileClick = {},
            onEditProfileClick = {},
            onCreateProfileClick = {},
            onApplicationsClick = {},
            onMyRecruitsClick = {},
            onScrapsClick = {},
            onMyCommentsClick = {},
            onFeedbackClick = {},
            onSettingsClick = {},
            onNotificationClick = {}
        )
    }
}
