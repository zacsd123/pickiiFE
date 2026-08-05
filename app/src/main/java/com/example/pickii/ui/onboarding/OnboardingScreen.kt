package com.example.pickii.ui.onboarding

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pickii.R
import com.example.pickii.domain.model.AcademicStatus
import com.example.pickii.domain.model.TechStack
import com.example.pickii.domain.model.University
import com.example.pickii.ui.common.AddEntryButton
import com.example.pickii.ui.common.AiGenerationDialog
import com.example.pickii.ui.common.CharacterCounterText
import com.example.pickii.ui.common.ConfirmDialog
import com.example.pickii.ui.common.RemovableEntryCard
import com.example.pickii.ui.common.SearchDropdownField
import com.example.pickii.ui.common.SelectableChip
import com.example.pickii.ui.common.YearMonthField
import com.example.pickii.ui.theme.PickiiBlue
import com.example.pickii.ui.theme.PickiiFieldBackground
import com.example.pickii.ui.theme.PickiiTextGray
import com.example.pickii.ui.theme.PickiiYellowLight
import java.time.YearMonth

/** 필드/카드 공통 모서리 둥글기. */
private val FieldCornerRadius = 14.dp

/** 이전/다음 원형 버튼 크기. */
private val NavButtonSize = 48.dp

/**
 * 최초 로그인 시 이력서(프로필)를 입력받는 8단계 온보딩 화면.
 *
 * @param onFinished 마지막 단계 제출 완료, 또는 "건너뛰기" 확정 시 호출되는 콜백(홈 이동 용도)
 */
@Composable
fun OnboardingScreen(
    onFinished: () -> Unit = {},
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    OnboardingScreenContent(
        uiState = uiState,
        onAcademicStatusSelect = viewModel::onAcademicStatusSelect,
        onUniversityQueryChange = viewModel::onUniversityQueryChange,
        onUniversitySelect = viewModel::onUniversitySelect,
        onMajorChange = viewModel::onMajorChange,
        onTopicToggle = viewModel::onTopicToggle,
        onHopeChange = viewModel::onHopeChange,
        onStrengthChange = viewModel::onStrengthChange,
        onAddExperience = viewModel::onAddExperience,
        onRemoveExperience = viewModel::onRemoveExperience,
        onExperienceStartDateChange = viewModel::onExperienceStartDateChange,
        onExperienceEndDateChange = viewModel::onExperienceEndDateChange,
        onExperienceTitleChange = viewModel::onExperienceTitleChange,
        onExperienceOrganizationChange = viewModel::onExperienceOrganizationChange,
        onExperienceDescriptionChange = viewModel::onExperienceDescriptionChange,
        onAddSkillTool = viewModel::onAddSkillTool,
        onRemoveSkillTool = viewModel::onRemoveSkillTool,
        onSkillToolNameChange = viewModel::onSkillToolNameChange,
        onSkillToolSelect = viewModel::onSkillToolSelect,
        onSkillToolLevelChange = viewModel::onSkillToolLevelChange,
        onAddLink = viewModel::onAddLink,
        onRemoveLink = viewModel::onRemoveLink,
        onLinkCategorySelect = viewModel::onLinkCategorySelect,
        onLinkUrlChange = viewModel::onLinkUrlChange,
        onAddLicense = viewModel::onAddLicense,
        onRemoveLicense = viewModel::onRemoveLicense,
        onLicenseNameChange = viewModel::onLicenseNameChange,
        onLicenseDateChange = viewModel::onLicenseDateChange,
        onNextClick = { viewModel.onNextClick(onCompleted = onFinished) },
        onPreviousClick = viewModel::onPreviousClick,
        onSkipRequested = viewModel::onSkipRequested,
        onDismissSkipDialog = viewModel::onDismissSkipDialog,
        onConfirmSkip = { viewModel.onConfirmSkip(onSkipped = onFinished) },
        onAiRetryClick = { viewModel.onAiRetryClick(onCompleted = onFinished) },
        onAiDialogDismiss = viewModel::onAiDialogDismiss
    )
}

@Suppress("LongParameterList")
@Composable
private fun OnboardingScreenContent(
    uiState: OnboardingUiState,
    onAcademicStatusSelect: (AcademicStatus) -> Unit,
    onUniversityQueryChange: (String) -> Unit,
    onUniversitySelect: (University) -> Unit,
    onMajorChange: (String) -> Unit,
    onTopicToggle: (Int) -> Unit,
    onHopeChange: (String) -> Unit,
    onStrengthChange: (String) -> Unit,
    onAddExperience: () -> Unit,
    onRemoveExperience: (String) -> Unit,
    onExperienceStartDateChange: (String, YearMonth) -> Unit,
    onExperienceEndDateChange: (String, YearMonth) -> Unit,
    onExperienceTitleChange: (String, String) -> Unit,
    onExperienceOrganizationChange: (String, String) -> Unit,
    onExperienceDescriptionChange: (String, String) -> Unit,
    onAddSkillTool: () -> Unit,
    onRemoveSkillTool: (String) -> Unit,
    onSkillToolNameChange: (String, String) -> Unit,
    onSkillToolSelect: (String, TechStack) -> Unit,
    onSkillToolLevelChange: (String, Int) -> Unit,
    onAddLink: () -> Unit,
    onRemoveLink: (String) -> Unit,
    onLinkCategorySelect: (String, String) -> Unit,
    onLinkUrlChange: (String, String) -> Unit,
    onAddLicense: () -> Unit,
    onRemoveLicense: (String) -> Unit,
    onLicenseNameChange: (String, String) -> Unit,
    onLicenseDateChange: (String, YearMonth) -> Unit,
    onNextClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onSkipRequested: () -> Unit,
    onDismissSkipDialog: () -> Unit,
    onConfirmSkip: () -> Unit,
    onAiRetryClick: () -> Unit,
    onAiDialogDismiss: () -> Unit
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
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.login_brand),
                color = Color.Black,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(20.dp))
            val (title, subtitle) = stepCopy(uiState.step)
            Text(text = title, color = Color.Black, fontSize = 24.sp, fontWeight = FontWeight.Bold, lineHeight = 32.sp)
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = subtitle, color = PickiiTextGray, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Column(
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp)
            ) {
                when (uiState.step) {
                    1 ->
                        SchoolInfoStep(
                            uiState = uiState,
                            onAcademicStatusSelect = onAcademicStatusSelect,
                            onUniversityQueryChange = onUniversityQueryChange,
                            onUniversitySelect = onUniversitySelect,
                            onMajorChange = onMajorChange
                        )
                    2 -> InterestTopicStep(uiState = uiState, onTopicToggle = onTopicToggle)
                    3 -> HopeStep(uiState = uiState, onHopeChange = onHopeChange)
                    4 -> StrengthStep(uiState = uiState, onStrengthChange = onStrengthChange)
                    5 ->
                        ExperienceStep(
                            uiState = uiState,
                            onAddExperience = onAddExperience,
                            onRemoveExperience = onRemoveExperience,
                            onStartDateChange = onExperienceStartDateChange,
                            onEndDateChange = onExperienceEndDateChange,
                            onTitleChange = onExperienceTitleChange,
                            onOrganizationChange = onExperienceOrganizationChange,
                            onDescriptionChange = onExperienceDescriptionChange
                        )
                    6 ->
                        SkillToolStep(
                            uiState = uiState,
                            onAddSkillTool = onAddSkillTool,
                            onRemoveSkillTool = onRemoveSkillTool,
                            onNameChange = onSkillToolNameChange,
                            onSelect = onSkillToolSelect,
                            onLevelChange = onSkillToolLevelChange
                        )
                    7 ->
                        LinkStep(
                            uiState = uiState,
                            onAddLink = onAddLink,
                            onRemoveLink = onRemoveLink,
                            onCategorySelect = onLinkCategorySelect,
                            onUrlChange = onLinkUrlChange
                        )
                    else ->
                        LicenseStep(
                            uiState = uiState,
                            onAddLicense = onAddLicense,
                            onRemoveLicense = onRemoveLicense,
                            onNameChange = onLicenseNameChange,
                            onDateChange = onLicenseDateChange
                        )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OnboardingNavRow(
                step = uiState.step,
                canGoNext = uiState.step != 1 || uiState.isStep1Valid,
                onPreviousClick = onPreviousClick,
                onNextClick = onNextClick
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.onboarding_button_skip),
                color = PickiiTextGray,
                fontSize = 13.sp,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onSkipRequested)
                        .padding(bottom = 20.dp),
                textAlign = TextAlign.Center
            )
        }

        if (uiState.isSkipDialogVisible) {
            ConfirmDialog(
                title = stringResource(R.string.onboarding_dialog_skip_title),
                body = stringResource(R.string.onboarding_dialog_skip_body),
                confirmLabel = stringResource(R.string.common_button_confirm),
                dismissLabel = stringResource(R.string.common_button_cancel),
                onConfirm = onConfirmSkip,
                onDismiss = onDismissSkipDialog
            )
        }

        AiGenerationDialog(state = uiState.aiDialogState, onRetry = onAiRetryClick, onDismiss = onAiDialogDismiss)
    }
}

/** 단계별 타이틀/서브타이틀. */
@Composable
private fun stepCopy(step: Int): Pair<String, String?> =
    when (step) {
        1 -> stringResource(R.string.onboarding_step1_title) to null
        2 -> stringResource(R.string.onboarding_step2_title) to stringResource(R.string.onboarding_step2_subtitle)
        3 -> stringResource(R.string.onboarding_step3_title) to stringResource(R.string.onboarding_step3_subtitle)
        4 -> stringResource(R.string.onboarding_step4_title) to stringResource(R.string.onboarding_step4_subtitle)
        5 -> stringResource(R.string.onboarding_step5_title) to stringResource(R.string.onboarding_step5_subtitle)
        6 -> stringResource(R.string.onboarding_step6_title) to stringResource(R.string.onboarding_step6_subtitle)
        7 -> stringResource(R.string.onboarding_step7_title) to stringResource(R.string.onboarding_step7_subtitle)
        else -> stringResource(R.string.onboarding_step8_title) to stringResource(R.string.onboarding_step8_subtitle)
    }

/** 1단계: 학적상태/학교/전공. */
@Composable
private fun SchoolInfoStep(
    uiState: OnboardingUiState,
    onAcademicStatusSelect: (AcademicStatus) -> Unit,
    onUniversityQueryChange: (String) -> Unit,
    onUniversitySelect: (University) -> Unit,
    onMajorChange: (String) -> Unit
) {
    StepFieldLabel(text = stringResource(R.string.onboarding_label_academic_status))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        AcademicStatus.entries.forEach { status ->
            SelectableChip(
                label = status.label,
                selected = status == uiState.academicStatus,
                enabled = true,
                onClick = { onAcademicStatusSelect(status) }
            )
        }
    }

    Spacer(modifier = Modifier.height(20.dp))

    StepFieldLabel(text = stringResource(R.string.onboarding_label_university))
    SearchDropdownField(
        query = uiState.universityQuery,
        onQueryChange = onUniversityQueryChange,
        suggestions = uiState.universitySuggestions,
        onSelect = onUniversitySelect,
        itemLabel = { it.name },
        placeholder = stringResource(R.string.onboarding_placeholder_university)
    )

    Spacer(modifier = Modifier.height(20.dp))

    StepFieldLabel(text = stringResource(R.string.onboarding_label_major))
    OutlinedTextField(
        value = uiState.major,
        onValueChange = onMajorChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(text = stringResource(R.string.onboarding_placeholder_major), color = PickiiTextGray) },
        singleLine = true,
        shape = RoundedCornerShape(FieldCornerRadius),
        colors = pickiiFieldColors()
    )
}

/** 2단계: 관심 분야(주제) 다중 선택. 칩 길이가 제각각이라 고정 3열 대신 [FlowRow]로 자연스럽게 줄바꿈한다. */
@Composable
private fun InterestTopicStep(
    uiState: OnboardingUiState,
    onTopicToggle: (Int) -> Unit
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        uiState.availableTopics.forEach { topic ->
            SelectableChip(
                label = topic.label,
                selected = topic.id in uiState.selectedTopicIds,
                enabled = true,
                onClick = { onTopicToggle(topic.id) }
            )
        }
    }
}

/** 3단계: 희망 진로. */
@Composable
private fun HopeStep(
    uiState: OnboardingUiState,
    onHopeChange: (String) -> Unit
) {
    OutlinedTextField(
        value = uiState.hope,
        onValueChange = onHopeChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(text = stringResource(R.string.onboarding_placeholder_hope), color = PickiiTextGray) },
        shape = RoundedCornerShape(FieldCornerRadius),
        colors = pickiiFieldColors()
    )
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        CharacterCounterText(current = uiState.hope.length, max = MAX_HOPE_LENGTH)
    }
}

/** 4단계: 장점. */
@Composable
private fun StrengthStep(
    uiState: OnboardingUiState,
    onStrengthChange: (String) -> Unit
) {
    OutlinedTextField(
        value = uiState.strength,
        onValueChange = onStrengthChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(text = stringResource(R.string.onboarding_placeholder_strength), color = PickiiTextGray) },
        shape = RoundedCornerShape(FieldCornerRadius),
        colors = pickiiFieldColors()
    )
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        CharacterCounterText(current = uiState.strength.length, max = MAX_STRENGTH_LENGTH)
    }
}

/** 5단계: 수상 및 경험(반복 입력). */
@Composable
private fun ExperienceStep(
    uiState: OnboardingUiState,
    onAddExperience: () -> Unit,
    onRemoveExperience: (String) -> Unit,
    onStartDateChange: (String, YearMonth) -> Unit,
    onEndDateChange: (String, YearMonth) -> Unit,
    onTitleChange: (String, String) -> Unit,
    onOrganizationChange: (String, String) -> Unit,
    onDescriptionChange: (String, String) -> Unit
) {
    uiState.experienceDrafts.forEach { draft ->
        RemovableEntryCard(
            onRemove = { onRemoveExperience(draft.id) },
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            OutlinedTextField(
                value = draft.title,
                onValueChange = { onTitleChange(draft.id, it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = stringResource(R.string.onboarding_placeholder_experience_title),
                        color = PickiiTextGray
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(FieldCornerRadius),
                colors = pickiiFieldColors()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = draft.organization,
                onValueChange = { onOrganizationChange(draft.id, it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(text = stringResource(R.string.onboarding_placeholder_experience_org), color = PickiiTextGray)
                },
                singleLine = true,
                shape = RoundedCornerShape(FieldCornerRadius),
                colors = pickiiFieldColors()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                YearMonthField(
                    value = draft.startDate,
                    onValueChange = { onStartDateChange(draft.id, it) },
                    placeholder = stringResource(R.string.onboarding_placeholder_start_date),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "~", color = PickiiTextGray)
                Spacer(modifier = Modifier.width(8.dp))
                YearMonthField(
                    value = draft.endDate,
                    onValueChange = { onEndDateChange(draft.id, it) },
                    placeholder = stringResource(R.string.onboarding_placeholder_end_date),
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = draft.description,
                onValueChange = { onDescriptionChange(draft.id, it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(text = stringResource(R.string.onboarding_placeholder_experience_desc), color = PickiiTextGray)
                },
                shape = RoundedCornerShape(FieldCornerRadius),
                colors = pickiiFieldColors()
            )
        }
    }
    AddEntryButton(label = stringResource(R.string.onboarding_button_add_experience), onClick = onAddExperience)
}

/** 6단계: 사용 가능 Skill & Tool(반복 입력, 숙련도 상/중/하). */
@Composable
private fun SkillToolStep(
    uiState: OnboardingUiState,
    onAddSkillTool: () -> Unit,
    onRemoveSkillTool: (String) -> Unit,
    onNameChange: (String, String) -> Unit,
    onSelect: (String, TechStack) -> Unit,
    onLevelChange: (String, Int) -> Unit
) {
    uiState.skillToolDrafts.forEach { draft ->
        RemovableEntryCard(
            onRemove = { onRemoveSkillTool(draft.id) },
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            val suggestions =
                uiState.availableTechStacks.filter { it.name.contains(draft.techStackName, ignoreCase = true) }
            SearchDropdownField(
                query = draft.techStackName,
                onQueryChange = { onNameChange(draft.id, it) },
                suggestions = if (draft.techStackName.isBlank()) emptyList() else suggestions,
                onSelect = { onSelect(draft.id, it) },
                itemLabel = { it.name },
                placeholder = stringResource(R.string.onboarding_placeholder_skill_name)
            )
            if (draft.techStackName.isNotBlank() && !draft.isSelected) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.onboarding_skill_not_selected_hint),
                    color = PickiiTextGray,
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SkillLevelChip(
                    labelRes = R.string.onboarding_skill_level_high,
                    level = 3,
                    selected = draft.level,
                    onSelect = { onLevelChange(draft.id, it) }
                )
                SkillLevelChip(
                    labelRes = R.string.onboarding_skill_level_mid,
                    level = 2,
                    selected = draft.level,
                    onSelect = { onLevelChange(draft.id, it) }
                )
                SkillLevelChip(
                    labelRes = R.string.onboarding_skill_level_low,
                    level = 1,
                    selected = draft.level,
                    onSelect = { onLevelChange(draft.id, it) }
                )
            }
        }
    }
    AddEntryButton(label = stringResource(R.string.onboarding_button_add_skill), onClick = onAddSkillTool)
}

@Composable
private fun SkillLevelChip(
    labelRes: Int,
    level: Int,
    selected: Int,
    onSelect: (Int) -> Unit
) {
    SelectableChip(
        label = stringResource(labelRes),
        selected = selected == level,
        enabled = true,
        onClick = { onSelect(level) }
    )
}

/** 7단계: 외부 링크(반복 입력, 플랫폼 선택 + URL). */
@Composable
private fun LinkStep(
    uiState: OnboardingUiState,
    onAddLink: () -> Unit,
    onRemoveLink: (String) -> Unit,
    onCategorySelect: (String, String) -> Unit,
    onUrlChange: (String, String) -> Unit
) {
    uiState.linkDrafts.forEach { draft ->
        RemovableEntryCard(
            onRemove = { onRemoveLink(draft.id) },
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                uiState.availableLinkCategories.forEach { category ->
                    SelectableChip(
                        label = category.name,
                        selected = category.name == draft.linkName,
                        enabled = true,
                        onClick = { onCategorySelect(draft.id, category.name) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = draft.url,
                onValueChange = { onUrlChange(draft.id, it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(text = stringResource(R.string.onboarding_placeholder_link_url), color = PickiiTextGray)
                },
                singleLine = true,
                shape = RoundedCornerShape(FieldCornerRadius),
                colors = pickiiFieldColors()
            )
        }
    }
    AddEntryButton(label = stringResource(R.string.onboarding_button_add_link), onClick = onAddLink)
}

/** 8단계: 자격증 및 취득일자(반복 입력). */
@Composable
private fun LicenseStep(
    uiState: OnboardingUiState,
    onAddLicense: () -> Unit,
    onRemoveLicense: (String) -> Unit,
    onNameChange: (String, String) -> Unit,
    onDateChange: (String, YearMonth) -> Unit
) {
    uiState.licenseDrafts.forEach { draft ->
        RemovableEntryCard(
            onRemove = { onRemoveLicense(draft.id) },
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            val suggestions =
                uiState.availableLicenseOptions.filter { it.name.contains(draft.licenseName, ignoreCase = true) }
            SearchDropdownField(
                query = draft.licenseName,
                onQueryChange = { onNameChange(draft.id, it) },
                suggestions = if (draft.licenseName.isBlank()) emptyList() else suggestions,
                onSelect = { onNameChange(draft.id, it.name) },
                itemLabel = { it.name },
                placeholder = stringResource(R.string.onboarding_placeholder_license_name)
            )
            Spacer(modifier = Modifier.height(8.dp))
            YearMonthField(
                value = draft.acquiredDate,
                onValueChange = { onDateChange(draft.id, it) },
                placeholder = stringResource(R.string.onboarding_placeholder_acquired_date)
            )
        }
    }
    AddEntryButton(label = stringResource(R.string.onboarding_button_add_license), onClick = onAddLicense)
}

/** 단계 입력 필드 위 라벨. */
@Composable
private fun StepFieldLabel(text: String) {
    Text(text = text, color = PickiiTextGray, fontSize = 12.sp)
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
private fun pickiiFieldColors() =
    OutlinedTextFieldDefaults.colors(
        focusedContainerColor = PickiiFieldBackground,
        unfocusedContainerColor = PickiiFieldBackground,
        focusedBorderColor = Color.Transparent,
        unfocusedBorderColor = Color.Transparent
    )

/** 하단 점 페이지네이션 + 이전/다음 버튼. */
@Composable
private fun OnboardingNavRow(
    step: Int,
    canGoNext: Boolean,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(NavButtonSize)) {
            if (step > 1) {
                NavCircleButton(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    background = PickiiYellowLight,
                    onClick = onPreviousClick
                )
            }
        }

        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(ONBOARDING_TOTAL_STEPS) { index ->
                val isCurrent = index + 1 == step
                Box(
                    modifier =
                        Modifier
                            .size(if (isCurrent) 10.dp else 8.dp)
                            .clip(CircleShape)
                            .background(if (isCurrent) Color.Black else PickiiFieldBackground)
                )
                if (index != ONBOARDING_TOTAL_STEPS - 1) Spacer(modifier = Modifier.width(6.dp))
            }
        }

        NavCircleButton(
            icon = Icons.AutoMirrored.Filled.ArrowForward,
            background = if (canGoNext) PickiiBlue else PickiiFieldBackground,
            onClick = onNextClick,
            enabled = canGoNext
        )
    }
}

@Composable
private fun NavCircleButton(
    icon: ImageVector,
    background: Color,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Box(
        modifier =
            Modifier
                .size(NavButtonSize)
                .clip(CircleShape)
                .background(background)
                .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = Color.White)
    }
}

/** [OnboardingScreen] 1단계 프리뷰. */
@Preview(showBackground = true)
@Composable
private fun OnboardingStep1Preview() {
    MaterialTheme {
        OnboardingScreenContent(
            uiState = OnboardingUiState(),
            onAcademicStatusSelect = {},
            onUniversityQueryChange = {},
            onUniversitySelect = {},
            onMajorChange = {},
            onTopicToggle = {},
            onHopeChange = {},
            onStrengthChange = {},
            onAddExperience = {},
            onRemoveExperience = {},
            onExperienceStartDateChange = { _, _ -> },
            onExperienceEndDateChange = { _, _ -> },
            onExperienceTitleChange = { _, _ -> },
            onExperienceOrganizationChange = { _, _ -> },
            onExperienceDescriptionChange = { _, _ -> },
            onAddSkillTool = {},
            onRemoveSkillTool = {},
            onSkillToolNameChange = { _, _ -> },
            onSkillToolSelect = { _, _ -> },
            onSkillToolLevelChange = { _, _ -> },
            onAddLink = {},
            onRemoveLink = {},
            onLinkCategorySelect = { _, _ -> },
            onLinkUrlChange = { _, _ -> },
            onAddLicense = {},
            onRemoveLicense = {},
            onLicenseNameChange = { _, _ -> },
            onLicenseDateChange = { _, _ -> },
            onNextClick = {},
            onPreviousClick = {},
            onSkipRequested = {},
            onDismissSkipDialog = {},
            onConfirmSkip = {},
            onAiRetryClick = {},
            onAiDialogDismiss = {}
        )
    }
}
