package com.example.pickii.ui.mypage.profile

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Interests
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pickii.R
import com.example.pickii.domain.model.AcademicStatus
import com.example.pickii.domain.model.AdditionalLinkEntry
import com.example.pickii.domain.model.ExperienceEntry
import com.example.pickii.domain.model.LicenseEntry
import com.example.pickii.domain.model.MemberProfile
import com.example.pickii.domain.model.SkillToolEntry
import com.example.pickii.ui.common.BackHeader
import com.example.pickii.ui.common.LevelAvatar
import com.example.pickii.ui.common.LevelProgressBar
import com.example.pickii.ui.common.LoadingIndicator
import com.example.pickii.ui.mypage.profile.component.ProfileCardEmptyState
import com.example.pickii.ui.mypage.profile.component.ProfileCardFrame
import com.example.pickii.ui.mypage.profile.component.ProfileCardStack
import com.example.pickii.ui.theme.PickiiProfileCardGoldBright
import com.example.pickii.ui.theme.PickiiProfileCardGoldMid
import com.example.pickii.ui.theme.PickiiTextGray
import com.example.pickii.ui.theme.PickiiYellowLight
import kotlinx.datetime.YearMonth
import org.koin.androidx.compose.koinViewModel

private const val PROFILE_CARD_COUNT = 6

/**
 * 이력서 형태의 내 프로필 조회 화면. 카드 6장을 넘겨보는 캐러셀로 보여준다.
 *
 * @param onBackClick 뒤로가기 콜백
 * @param onEditClick "수정" 버튼 클릭 콜백
 */
@Composable
fun ProfileViewScreen(
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    viewModel: ProfileViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    viewModel.refresh()
                }
            }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    if (uiState.toastMessageRes != null) {
        val messageRes = uiState.toastMessageRes
        LaunchedEffect(messageRes) {
            if (messageRes != null) Toast.makeText(context, messageRes, Toast.LENGTH_SHORT).show()
            viewModel.onToastShown()
        }
    }

    ProfileViewScreenContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onEditClick = onEditClick
    )
}

@Composable
internal fun ProfileViewScreenContent(
    uiState: ProfileViewUiState,
    onBackClick: () -> Unit,
    onEditClick: (() -> Unit)? = null,
    title: String = stringResource(R.string.mypage_profile_title)
) {
    val profile = uiState.profile

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(PickiiYellowLight)
    ) {
        if (uiState.isLoading) {
            LoadingIndicator()
        } else if (profile == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = stringResource(R.string.mypage_create_profile_prompt), color = PickiiTextGray)
            }
        } else {
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                ProfileViewHeader(title = title, onBackClick = onBackClick, onEditClick = onEditClick)

                Spacer(modifier = Modifier.height(20.dp))

                ProfileCardStack(
                    pageCount = PROFILE_CARD_COUNT,
                    modifier = Modifier.weight(1f).fillMaxWidth()
                ) { index ->
                    when (index) {
                        0 -> CharacterCard(profile)
                        1 -> TopicSchoolLinksCard(profile, uiState.topicLabels)
                        2 -> AboutMeCard(profile)
                        3 -> SkillsCard(profile)
                        4 -> LicenseCard(profile)
                        else -> ExperienceCard(profile)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun ProfileViewHeader(
    title: String,
    onBackClick: () -> Unit,
    onEditClick: (() -> Unit)?
) {
    BackHeader(
        title = title,
        onBackClick = onBackClick,
        trailingContent =
            if (onEditClick != null) {
                {
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
            } else {
                null
            }
    )
}

/** 카드1: 캐릭터 + Lv + 경험치 바 + 닉네임. 다른 카드와 달리 헤더/구분선 없이 중앙정렬로 구성한다. */
@Composable
private fun CharacterCard(profile: MemberProfile) {
    ProfileCardFrame(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                LevelAvatar(exp = profile.exp, size = 112.dp)
                Spacer(modifier = Modifier.height(16.dp))
                LevelProgressBar(exp = profile.exp, modifier = Modifier.width(160.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = profile.nickname,
                    color = PickiiProfileCardGoldBright,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/** 카드2: Topic(관심분야) + 학교/전공 + 학적상태 + 외부링크. */
@Composable
private fun TopicSchoolLinksCard(
    profile: MemberProfile,
    topicLabels: List<String>
) {
    ProfileCardFrame(
        modifier = Modifier.fillMaxSize(),
        headerIcon = Icons.Filled.Interests,
        title = stringResource(R.string.mypage_profile_card_topic_title)
    ) {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            if (topicLabels.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    topicLabels.take(3).forEach { label ->
                        ProfileTopicChip(label = label)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            CardInfoRow(label = stringResource(R.string.mypage_profile_label_university), value = profile.univ)
            CardInfoRow(label = stringResource(R.string.mypage_profile_label_major), value = profile.major)
            CardInfoRow(
                label = stringResource(R.string.mypage_profile_label_academic_status),
                value = profile.academicStatus.label,
                showDivider = profile.additionalLinks.isNotEmpty()
            )

            if (profile.additionalLinks.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.mypage_profile_label_links),
                    color = PickiiProfileCardGoldMid,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(profile.additionalLinks) { link -> AdditionalLinkChip(link) }
                }
            }
        }
    }
}

/** 카드3: 자기소개. */
@Composable
private fun AboutMeCard(profile: MemberProfile) {
    ProfileCardFrame(
        modifier = Modifier.fillMaxSize(),
        headerIcon = Icons.Filled.Description,
        title = stringResource(R.string.mypage_profile_label_about_me)
    ) {
        Text(
            text = profile.aboutMe.orEmpty(),
            color = Color.White,
            fontSize = 14.sp,
            modifier = Modifier.verticalScroll(rememberScrollState())
        )
    }
}

/** 카드4: Skill & Tool. */
@Composable
private fun SkillsCard(profile: MemberProfile) {
    ProfileCardFrame(
        modifier = Modifier.fillMaxSize(),
        headerIcon = Icons.Filled.Build,
        title = stringResource(R.string.mypage_profile_label_skills)
    ) {
        if (profile.skillTools.isEmpty()) {
            ProfileCardEmptyState()
        } else {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                profile.skillTools.forEach { SkillToolRow(it) }
            }
        }
    }
}

/** 카드5: 자격증(License). */
@Composable
private fun LicenseCard(profile: MemberProfile) {
    ProfileCardFrame(
        modifier = Modifier.fillMaxSize(),
        headerIcon = Icons.Filled.WorkspacePremium,
        title = stringResource(R.string.mypage_profile_label_licenses)
    ) {
        if (profile.licenses.isEmpty()) {
            ProfileCardEmptyState()
        } else {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                profile.licenses.forEach { LicenseRow(it) }
            }
        }
    }
}

/** 카드6: 수상 및 경험(Experience). */
@Composable
private fun ExperienceCard(profile: MemberProfile) {
    ProfileCardFrame(
        modifier = Modifier.fillMaxSize(),
        headerIcon = Icons.Filled.EmojiEvents,
        title = stringResource(R.string.mypage_profile_label_experience)
    ) {
        if (profile.experiences.isEmpty()) {
            ProfileCardEmptyState()
        } else {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                profile.experiences.forEachIndexed { index, entry ->
                    ExperienceRow(entry)
                    if (index != profile.experiences.lastIndex) Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun CardInfoRow(
    label: String,
    value: String,
    showDivider: Boolean = true
) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(text = label, color = PickiiProfileCardGoldMid, fontSize = 13.sp, modifier = Modifier.width(84.dp))
        Text(text = value, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
    if (showDivider) {
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(PickiiProfileCardGoldMid.copy(alpha = 0.25f)))
    }
}

@Composable
private fun AdditionalLinkChip(link: AdditionalLinkEntry) {
    val context = LocalContext.current

    Box(
        modifier =
            Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(PickiiYellowLight)
                .clickable {
                    val url =
                        if (
                            link.url.startsWith("http://") ||
                            link.url.startsWith("https://")
                        ) {
                            link.url
                        } else {
                            "https://${link.url}"
                        }

                    val intent =
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(url)
                        )

                    context.startActivity(intent)
                },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter =
                painterResource(
                    id = getLinkIcon(link.linkName)
                ),
            contentDescription = link.linkName,
            tint = Color.Unspecified,
            modifier = Modifier.size(22.dp)
        )
    }
}

private fun getLinkIcon(linkName: String): Int =
    when (linkName.lowercase()) {
        "git" ->
            R.drawable.ic_github

        "notion" ->
            R.drawable.ic_notion

        "linkedin" ->
            R.drawable.ic_linkedin

        "홈페이지" ->
            R.drawable.ic_link

        else ->
            R.drawable.ic_link
    }

@Composable
private fun SkillToolRow(entry: SkillToolEntry) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(text = entry.techStackName, color = Color.White, fontSize = 13.sp, modifier = Modifier.weight(1f))
        Text(text = skillLevelLabel(entry.level), color = PickiiProfileCardGoldMid, fontSize = 12.sp)
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
        Text(text = entry.licenseName, color = Color.White, fontSize = 13.sp, modifier = Modifier.weight(1f))
        Text(text = entry.acquiredDate.toString(), color = PickiiProfileCardGoldMid, fontSize = 12.sp)
    }
}

@Composable
private fun ExperienceRow(entry: ExperienceEntry) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = entry.title, color = PickiiProfileCardGoldBright, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = entry.organization, color = PickiiProfileCardGoldMid, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(2.dp))
        val ongoingLabel = stringResource(R.string.mypage_profile_experience_ongoing)
        Text(
            text = experiencePeriod(entry.startDate, entry.endDate, ongoingLabel),
            color = PickiiProfileCardGoldMid,
            fontSize = 12.sp
        )
        if (entry.description.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = entry.description, color = Color.White, fontSize = 12.sp)
        }
    }
}

private fun experiencePeriod(
    start: YearMonth,
    end: YearMonth?,
    ongoingLabel: String
): String = "$start ~ ${end?.toString() ?: ongoingLabel}"

@Preview
@Composable
fun ProfileViewScreenContentPre() {
    ProfileViewScreenContent(
        uiState =
            ProfileViewUiState(
                isLoading = false,
                profile =
                    MemberProfile(
                        nickname = "닉네임",
                        univId = 1,
                        univ = "서울대학교",
                        major = "컴퓨터공학과",
                        academicStatus = AcademicStatus.ENROLLED,
                        hope = null,
                        strength = null,
                        aboutMe = "자기소개 텍스트",
                        contactEmail = null,
                        exp = 120,
                        topicIds = emptyList(),
                        skillTools = emptyList(),
                        licenses = emptyList(),
                        experiences = emptyList(),
                        additionalLinks = emptyList()
                    ),
                topicLabels = listOf("기획/아이디어")
            ),
        onBackClick = {},
        onEditClick = {}
    )
}

@Composable
private fun ProfileTopicChip(label: String) {
    Box(
        modifier =
            Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(PickiiYellowLight)
                .padding(
                    horizontal = 10.dp,
                    vertical = 6.dp
                )
    ) {
        Text(
            text = label,
            color = Color.Black,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
