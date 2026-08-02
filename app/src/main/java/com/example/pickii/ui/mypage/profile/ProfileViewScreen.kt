package com.example.pickii.ui.mypage.profile

import android.widget.Toast
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.pickii.domain.model.AdditionalLinkEntry
import com.example.pickii.domain.model.ExperienceEntry
import com.example.pickii.domain.model.LicenseEntry
import com.example.pickii.domain.model.MemberProfile
import com.example.pickii.domain.model.SkillToolEntry
import com.example.pickii.domain.model.SocialAccount
import com.example.pickii.ui.common.AvatarPlaceholder
import com.example.pickii.ui.common.ConfirmDialog
import com.example.pickii.ui.common.LevelProgressBar
import com.example.pickii.ui.common.LoadingIndicator
import com.example.pickii.ui.common.SelectableChip
import com.example.pickii.ui.theme.PickiiBlue
import com.example.pickii.ui.theme.PickiiFieldBackground
import com.example.pickii.ui.theme.PickiiTextGray
import com.example.pickii.ui.theme.PickiiYellowLight
import java.time.YearMonth

/**
 * 이력서 형태의 내 프로필 조회 화면.
 *
 * @param onBackClick 뒤로가기 콜백
 * @param onEditClick "수정" 버튼 클릭 콜백
 */
@Composable
fun ProfileViewScreen(
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    if (uiState.isSocialLinkComingSoonVisible) {
        val message = stringResource(R.string.mypage_profile_social_link_coming_soon)
        androidx.compose.runtime.LaunchedEffect(uiState.isSocialLinkComingSoonVisible) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.onSocialLinkComingSoonShown()
        }
    }

    ProfileViewScreenContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onEditClick = onEditClick,
        onLinkClick = viewModel::onLinkClick,
        onUnlinkRequest = viewModel::onUnlinkRequest,
        onDismissUnlinkDialog = viewModel::onDismissUnlinkDialog,
        onConfirmUnlink = viewModel::onConfirmUnlink
    )
}

@Suppress("LongParameterList")
@Composable
private fun ProfileViewScreenContent(
    uiState: ProfileViewUiState,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onLinkClick: () -> Unit,
    onUnlinkRequest: (String) -> Unit,
    onDismissUnlinkDialog: () -> Unit,
    onConfirmUnlink: () -> Unit
) {
    val profile = uiState.profile

    Box(modifier = Modifier.fillMaxSize().background(PickiiYellowLight)) {
        if (uiState.isLoading) {
            LoadingIndicator()
        } else if (profile == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = stringResource(R.string.mypage_create_profile_prompt), color = PickiiTextGray)
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                ProfileViewHeader(onBackClick = onBackClick, onEditClick = onEditClick)

                Spacer(modifier = Modifier.height(20.dp))
                ProfileSummaryRow(profile = profile, topicLabels = uiState.topicLabels)

                Spacer(modifier = Modifier.height(20.dp))
                SectionCard(title = stringResource(R.string.mypage_profile_label_about_me)) {
                    Text(
                        text = profile.aboutMe.orEmpty(),
                        color = Color.Black,
                        fontSize = 13.sp,
                        modifier = Modifier.heightIn(min = 80.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                SectionCard(title = null) {
                    InfoRow(
                        label = stringResource(R.string.mypage_profile_label_email),
                        value = profile.contactEmail.orEmpty()
                    )
                    InfoRow(label = stringResource(R.string.mypage_profile_label_university), value = profile.univ)
                    InfoRow(label = stringResource(R.string.mypage_profile_label_major), value = profile.major)
                    InfoRow(
                        label = stringResource(R.string.mypage_profile_label_academic_status),
                        value = profile.academicStatus.label,
                        showDivider = false
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                SocialAccountsSection(
                    socialAccounts = uiState.socialAccounts,
                    onLinkClick = onLinkClick,
                    onUnlinkClick = onUnlinkRequest
                )

                if (profile.additionalLinks.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    SectionCard(title = stringResource(R.string.mypage_profile_label_links)) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(profile.additionalLinks) { link -> AdditionalLinkChip(link) }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                SectionCard(title = stringResource(R.string.mypage_profile_label_skills)) {
                    if (profile.skillTools.isEmpty()) {
                        EmptySectionText()
                    } else {
                        profile.skillTools.forEach { SkillToolRow(it) }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                SectionCard(title = stringResource(R.string.mypage_profile_label_licenses)) {
                    if (profile.licenses.isEmpty()) {
                        EmptySectionText()
                    } else {
                        profile.licenses.forEach { LicenseRow(it) }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                SectionCard(title = stringResource(R.string.mypage_profile_label_experience)) {
                    if (profile.experiences.isEmpty()) {
                        EmptySectionText()
                    } else {
                        profile.experiences.forEachIndexed { index, entry ->
                            ExperienceRow(entry)
                            if (index != profile.experiences.lastIndex) Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
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
private fun ProfileViewHeader(
    onBackClick: () -> Unit,
    onEditClick: () -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = null,
            tint = Color.Black,
            modifier = Modifier.clickable(onClick = onBackClick)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = stringResource(R.string.mypage_profile_title),
            color = Color.Black,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        Box(
            modifier =
                Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color.Black)
                    .clickable(onClick = onEditClick)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = stringResource(R.string.mypage_profile_button_edit),
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ProfileSummaryRow(
    profile: MemberProfile,
    topicLabels: List<String>
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = profile.nickname, color = Color.Black, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            if (topicLabels.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    topicLabels.take(3).forEach { label ->
                        SelectableChip(label = label, selected = true, enabled = false, onClick = {})
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            AvatarPlaceholder(size = 56.dp)
            Spacer(modifier = Modifier.height(8.dp))
            LevelProgressBar(exp = profile.exp, modifier = Modifier.width(96.dp))
        }
    }
}

@Composable
private fun SectionCard(
    title: String?,
    content: @Composable () -> Unit
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .padding(16.dp)
    ) {
        if (title != null) {
            Text(text = title, color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))
        }
        content()
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    showDivider: Boolean = true
) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(text = label, color = PickiiTextGray, fontSize = 13.sp, modifier = Modifier.width(84.dp))
        Text(text = value, color = Color.Black, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
    if (showDivider) {
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(PickiiFieldBackground))
    }
}

@Composable
private fun SocialAccountsSection(
    socialAccounts: List<SocialAccount>,
    onLinkClick: () -> Unit,
    onUnlinkClick: (String) -> Unit
) {
    val kakao = socialAccounts.find { it.provider == "KAKAO" }
    val isLinked = kakao?.linked == true

    SectionCard(title = stringResource(R.string.mypage_profile_label_social_accounts)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.mypage_profile_social_kakao),
                color = Color.Black,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            Text(
                text =
                    stringResource(
                        if (isLinked) R.string.mypage_profile_social_linked else R.string.mypage_profile_social_not_linked
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
                        .clickable(onClick = { if (isLinked) onUnlinkClick("KAKAO") else onLinkClick() })
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
    }
}

@Composable
private fun AdditionalLinkChip(link: AdditionalLinkEntry) {
    Column(
        modifier =
            Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(PickiiFieldBackground)
                .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Text(text = link.linkName, color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Text(text = link.url, color = PickiiTextGray, fontSize = 11.sp)
    }
}

@Composable
private fun SkillToolRow(entry: SkillToolEntry) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(text = entry.techStackName, color = Color.Black, fontSize = 13.sp, modifier = Modifier.weight(1f))
        Text(text = skillLevelLabel(entry.level), color = PickiiTextGray, fontSize = 12.sp)
    }
}

@Composable
private fun skillLevelLabel(level: Int): String =
    when (level) {
        3 -> stringResource(R.string.onboarding_skill_level_high)
        2 -> stringResource(R.string.onboarding_skill_level_mid)
        else -> stringResource(R.string.onboarding_skill_level_low)
    }

@Composable
private fun LicenseRow(entry: LicenseEntry) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(text = entry.licenseName, color = Color.Black, fontSize = 13.sp, modifier = Modifier.weight(1f))
        Text(text = entry.acquiredDate.toString(), color = PickiiTextGray, fontSize = 12.sp)
    }
}

@Composable
private fun ExperienceRow(entry: ExperienceEntry) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = entry.title, color = Color.Black, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = entry.organization, color = PickiiTextGray, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(2.dp))
        val ongoingLabel = stringResource(R.string.mypage_profile_experience_ongoing)
        Text(
            text = experiencePeriod(entry.startDate, entry.endDate, ongoingLabel),
            color = PickiiTextGray,
            fontSize = 12.sp
        )
        if (entry.description.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = entry.description, color = Color.Black, fontSize = 12.sp)
        }
    }
}

private fun experiencePeriod(
    start: YearMonth,
    end: YearMonth?,
    ongoingLabel: String
): String = "$start ~ ${end?.toString() ?: ongoingLabel}"

@Composable
private fun EmptySectionText() {
    Text(text = "-", color = PickiiTextGray, fontSize = 13.sp)
}
