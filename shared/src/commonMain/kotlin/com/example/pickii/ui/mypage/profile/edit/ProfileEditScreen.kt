package com.example.pickii.ui.mypage.profile.edit

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pickii.domain.model.AcademicStatus
import com.example.pickii.domain.model.TechStack
import com.example.pickii.domain.model.University
import com.example.pickii.shared.generated.resources.Res
import com.example.pickii.shared.generated.resources.mypage_profile_edit_button_save
import com.example.pickii.shared.generated.resources.mypage_profile_edit_label_about_me
import com.example.pickii.shared.generated.resources.mypage_profile_edit_title
import com.example.pickii.shared.generated.resources.onboarding_button_add_experience
import com.example.pickii.shared.generated.resources.onboarding_button_add_license
import com.example.pickii.shared.generated.resources.onboarding_button_add_link
import com.example.pickii.shared.generated.resources.onboarding_button_add_skill
import com.example.pickii.shared.generated.resources.onboarding_label_academic_status
import com.example.pickii.shared.generated.resources.onboarding_label_major
import com.example.pickii.shared.generated.resources.onboarding_label_university
import com.example.pickii.shared.generated.resources.onboarding_placeholder_acquired_date
import com.example.pickii.shared.generated.resources.onboarding_placeholder_end_date
import com.example.pickii.shared.generated.resources.onboarding_placeholder_experience_desc
import com.example.pickii.shared.generated.resources.onboarding_placeholder_experience_org
import com.example.pickii.shared.generated.resources.onboarding_placeholder_experience_title
import com.example.pickii.shared.generated.resources.onboarding_placeholder_hope
import com.example.pickii.shared.generated.resources.onboarding_placeholder_license_name
import com.example.pickii.shared.generated.resources.onboarding_placeholder_link_url
import com.example.pickii.shared.generated.resources.onboarding_placeholder_major
import com.example.pickii.shared.generated.resources.onboarding_placeholder_skill_name
import com.example.pickii.shared.generated.resources.onboarding_placeholder_start_date
import com.example.pickii.shared.generated.resources.onboarding_placeholder_strength
import com.example.pickii.shared.generated.resources.onboarding_skill_level_high
import com.example.pickii.shared.generated.resources.onboarding_skill_level_low
import com.example.pickii.shared.generated.resources.onboarding_skill_level_mid
import com.example.pickii.shared.generated.resources.onboarding_skill_not_selected_hint
import com.example.pickii.shared.generated.resources.onboarding_step2_title
import com.example.pickii.shared.generated.resources.onboarding_step3_title
import com.example.pickii.shared.generated.resources.onboarding_step4_title
import com.example.pickii.shared.generated.resources.onboarding_step5_title
import com.example.pickii.shared.generated.resources.onboarding_step6_title
import com.example.pickii.shared.generated.resources.onboarding_step7_title
import com.example.pickii.shared.generated.resources.onboarding_step8_title
import com.example.pickii.ui.common.AddEntryButton
import com.example.pickii.ui.common.BackHeader
import com.example.pickii.ui.common.CharacterCounterText
import com.example.pickii.ui.common.LoadingIndicator
import com.example.pickii.ui.common.LocalSnackbarHostState
import com.example.pickii.ui.common.RemovableEntryCard
import com.example.pickii.ui.common.SearchDropdownField
import com.example.pickii.ui.common.SelectableChip
import com.example.pickii.ui.common.YearMonthField
import com.example.pickii.ui.common.entrydraft.ExperienceDraft
import com.example.pickii.ui.common.entrydraft.LicenseDraft
import com.example.pickii.ui.common.entrydraft.LinkDraft
import com.example.pickii.ui.common.entrydraft.SkillToolDraft
import com.example.pickii.ui.theme.PickiiBlue
import com.example.pickii.ui.theme.PickiiDisabledGray
import com.example.pickii.ui.theme.PickiiFieldBackground
import com.example.pickii.ui.theme.PickiiTextGray
import com.example.pickii.ui.theme.PickiiYellowLight
import kotlinx.datetime.YearMonth
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import com.example.pickii.ui.common.FieldLabel as CommonFieldLabel

/** 모서리 둥글기(입력 필드 공통). */
private val FieldCornerRadius = 14.dp

/**
 * 프로필(이력서) 수정 화면. 온보딩과 같은 항목을 한 화면 스크롤 폼으로 보여주고, aboutMe/연락용 이메일도 여기서 직접 고친다(4-3).
 *
 * @param onBackClick 뒤로가기 콜백
 * @param onSaved 저장 완료 콜백(프로필 조회 화면으로 복귀)
 */
@Composable
fun ProfileEditScreen(
    onBackClick: () -> Unit,
    onSaved: () -> Unit,
    viewModel: ProfileEditViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = LocalSnackbarHostState.current

    if (uiState.toastMessageRes != null) {
        val messageRes = uiState.toastMessageRes
        LaunchedEffect(messageRes) {
            if (messageRes != null) snackbarHostState.showSnackbar(getString(messageRes))
            viewModel.onToastShown()
        }
    }

    ProfileEditScreenContent(
        uiState = uiState,
        onBackClick = onBackClick,
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
        onAboutMeChange = viewModel::onAboutMeChange,
        onSaveClick = { viewModel.onSaveClick(onSaved = onSaved) },
        onRetryLoadMasterDataClick = viewModel::onRetryLoadMasterDataClick
    )
}

@Suppress("LongParameterList")
@Composable
private fun ProfileEditScreenContent(
    uiState: ProfileEditUiState,
    onBackClick: () -> Unit,
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
    onAboutMeChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onRetryLoadMasterDataClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize().background(PickiiYellowLight)) {
        if (uiState.isLoading) {
            LoadingIndicator()
            return@Box
        }

        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
            Spacer(modifier = Modifier.height(16.dp))

            BackHeader(
                title = stringResource(Res.string.mypage_profile_edit_title),
                onBackClick = onBackClick
            )

            Spacer(modifier = Modifier.height(20.dp))

            FieldLabel(stringResource(Res.string.onboarding_label_academic_status))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AcademicStatus.entries.forEach { status ->
                    SelectableChip(
                        label = status.label,
                        selected = status == uiState.academicStatus,
                        enabled = true,
                        onClick = { onAcademicStatusSelect(status) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            FieldLabel(stringResource(Res.string.onboarding_label_university))

            OutlinedTextField(
                value = uiState.universityQuery,
                onValueChange = {},
                enabled = false,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(FieldCornerRadius),
                colors =
                    OutlinedTextFieldDefaults.colors(
                        disabledContainerColor = PickiiFieldBackground,
                        disabledBorderColor = Color.Transparent,
                        disabledTextColor = PickiiTextGray
                    )
            )

            Spacer(modifier = Modifier.height(16.dp))
            FieldLabel(stringResource(Res.string.onboarding_label_major))
            OutlinedTextField(
                value = uiState.major,
                onValueChange = onMajorChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = stringResource(Res.string.onboarding_placeholder_major),
                        color = PickiiTextGray
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(FieldCornerRadius),
                colors = fieldColors()
            )

            Spacer(modifier = Modifier.height(24.dp))
            FieldLabel(stringResource(Res.string.onboarding_step2_title))
            Text(
                text = "${uiState.selectedTopicIds.size}/$MAX_TOPIC_SELECTION",
                color = PickiiTextGray,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                uiState.availableTopics.forEach { topic ->
                    val isSelected = topic.id in uiState.selectedTopicIds
                    SelectableChip(
                        label = topic.label,
                        selected = isSelected,
                        enabled = isSelected || uiState.selectedTopicIds.size < MAX_TOPIC_SELECTION,
                        onClick = { onTopicToggle(topic.id) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            FieldLabel(stringResource(Res.string.onboarding_step3_title))
            OutlinedTextField(
                value = uiState.hope,
                onValueChange = onHopeChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = stringResource(Res.string.onboarding_placeholder_hope),
                        color = PickiiTextGray
                    )
                },
                shape = RoundedCornerShape(FieldCornerRadius),
                colors = fieldColors()
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                CharacterCounterText(current = uiState.hope.length, max = PROFILE_EDIT_MAX_HOPE_LENGTH)
            }

            Spacer(modifier = Modifier.height(16.dp))
            FieldLabel(stringResource(Res.string.onboarding_step4_title))
            OutlinedTextField(
                value = uiState.strength,
                onValueChange = onStrengthChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(text = stringResource(Res.string.onboarding_placeholder_strength), color = PickiiTextGray)
                },
                shape = RoundedCornerShape(FieldCornerRadius),
                colors = fieldColors()
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                CharacterCounterText(current = uiState.strength.length, max = PROFILE_EDIT_MAX_STRENGTH_LENGTH)
            }

            Spacer(modifier = Modifier.height(24.dp))
            FieldLabel(stringResource(Res.string.onboarding_step5_title))
            uiState.experienceDrafts.forEach { draft ->
                RemovableEntryCard(
                    onRemove = { onRemoveExperience(draft.id) },
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    ExperienceFields(
                        draft = draft,
                        onTitleChange = { onExperienceTitleChange(draft.id, it) },
                        onOrganizationChange = { onExperienceOrganizationChange(draft.id, it) },
                        onStartDateChange = { onExperienceStartDateChange(draft.id, it) },
                        onEndDateChange = { onExperienceEndDateChange(draft.id, it) },
                        onDescriptionChange = { onExperienceDescriptionChange(draft.id, it) }
                    )
                }
            }
            AddEntryButton(
                label = stringResource(Res.string.onboarding_button_add_experience),
                onClick = onAddExperience
            )

            Spacer(modifier = Modifier.height(24.dp))
            FieldLabel(stringResource(Res.string.onboarding_step6_title))
            uiState.skillToolDrafts.forEach { draft ->
                RemovableEntryCard(
                    onRemove = { onRemoveSkillTool(draft.id) },
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    SkillToolFields(
                        draft = draft,
                        suggestions =
                            uiState.availableTechStacks.filter {
                                it.name.contains(
                                    draft.techStackName,
                                    true
                                )
                            },
                        onNameChange = { onSkillToolNameChange(draft.id, it) },
                        onSelect = { onSkillToolSelect(draft.id, it) },
                        onLevelChange = { onSkillToolLevelChange(draft.id, it) }
                    )
                }
            }
            AddEntryButton(label = stringResource(Res.string.onboarding_button_add_skill), onClick = onAddSkillTool)

            Spacer(modifier = Modifier.height(24.dp))
            FieldLabel(stringResource(Res.string.onboarding_step7_title))
            uiState.linkDrafts.forEach { draft ->
                RemovableEntryCard(onRemove = { onRemoveLink(draft.id) }, modifier = Modifier.padding(bottom = 12.dp)) {
                    LinkFields(
                        draft = draft,
                        availableLinkCategories = uiState.availableLinkCategories.map { it.name },
                        onCategorySelect = { onLinkCategorySelect(draft.id, it) },
                        onUrlChange = { onLinkUrlChange(draft.id, it) }
                    )
                }
            }
            AddEntryButton(label = stringResource(Res.string.onboarding_button_add_link), onClick = onAddLink)

            Spacer(modifier = Modifier.height(24.dp))
            FieldLabel(stringResource(Res.string.onboarding_step8_title))
            uiState.licenseDrafts.forEach { draft ->
                RemovableEntryCard(
                    onRemove = { onRemoveLicense(draft.id) },
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    LicenseFields(
                        draft = draft,
                        suggestions =
                            uiState.availableLicenseOptions.filter {
                                it.name.contains(
                                    draft.licenseName,
                                    true
                                )
                            },
                        onNameChange = { onLicenseNameChange(draft.id, it) },
                        onDateChange = { onLicenseDateChange(draft.id, it) }
                    )
                }
            }
            AddEntryButton(label = stringResource(Res.string.onboarding_button_add_license), onClick = onAddLicense)

            Spacer(modifier = Modifier.height(24.dp))
            FieldLabel(stringResource(Res.string.mypage_profile_edit_label_about_me))
            OutlinedTextField(
                value = uiState.aboutMe,
                onValueChange = onAboutMeChange,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(FieldCornerRadius),
                colors = fieldColors()
            )

            Spacer(modifier = Modifier.height(24.dp))
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(FieldCornerRadius))
                        .background(if (uiState.isValid) PickiiBlue else PickiiDisabledGray)
                        .clickable(enabled = uiState.isValid && !uiState.isSaving, onClick = onSaveClick)
                        .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(Res.string.mypage_profile_edit_button_save),
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (uiState.errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = uiState.errorMessage, color = Color.Red, fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "다시 시도",
                        color = Color.Black,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier.clickable(onClick = onRetryLoadMasterDataClick)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ExperienceFields(
    draft: ExperienceDraft,
    onTitleChange: (String) -> Unit,
    onOrganizationChange: (String) -> Unit,
    onStartDateChange: (YearMonth) -> Unit,
    onEndDateChange: (YearMonth) -> Unit,
    onDescriptionChange: (String) -> Unit
) {
    OutlinedTextField(
        value = draft.title,
        onValueChange = onTitleChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = {
            Text(
                text = stringResource(Res.string.onboarding_placeholder_experience_title),
                color = PickiiTextGray
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(FieldCornerRadius),
        colors = fieldColors()
    )
    Spacer(modifier = Modifier.height(8.dp))
    OutlinedTextField(
        value = draft.organization,
        onValueChange = onOrganizationChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = {
            Text(
                text = stringResource(Res.string.onboarding_placeholder_experience_org),
                color = PickiiTextGray
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(FieldCornerRadius),
        colors = fieldColors()
    )
    Spacer(modifier = Modifier.height(8.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        YearMonthField(
            value = draft.startDate,
            onValueChange = onStartDateChange,
            placeholder = stringResource(Res.string.onboarding_placeholder_start_date),
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = "~", color = PickiiTextGray)
        Spacer(modifier = Modifier.width(8.dp))
        YearMonthField(
            value = draft.endDate,
            onValueChange = onEndDateChange,
            placeholder = stringResource(Res.string.onboarding_placeholder_end_date),
            modifier = Modifier.weight(1f)
        )
    }
    Spacer(modifier = Modifier.height(8.dp))
    OutlinedTextField(
        value = draft.description,
        onValueChange = onDescriptionChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = {
            Text(
                text = stringResource(Res.string.onboarding_placeholder_experience_desc),
                color = PickiiTextGray
            )
        },
        shape = RoundedCornerShape(FieldCornerRadius),
        colors = fieldColors()
    )
}

@Composable
private fun SkillToolFields(
    draft: SkillToolDraft,
    suggestions: List<TechStack>,
    onNameChange: (String) -> Unit,
    onSelect: (TechStack) -> Unit,
    onLevelChange: (Int) -> Unit
) {
    SearchDropdownField(
        query = draft.techStackName,
        onQueryChange = onNameChange,
        suggestions = if (draft.techStackName.isBlank()) emptyList() else suggestions,
        onSelect = onSelect,
        itemLabel = { it.name },
        placeholder = stringResource(Res.string.onboarding_placeholder_skill_name)
    )
    if (draft.techStackName.isNotBlank() && !draft.isSelected) {
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(Res.string.onboarding_skill_not_selected_hint),
            color = PickiiTextGray,
            fontSize = 12.sp
        )
    }
    Spacer(modifier = Modifier.height(8.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        SelectableChip(
            label = stringResource(Res.string.onboarding_skill_level_high),
            selected = draft.level == 3,
            enabled = true,
            onClick = { onLevelChange(3) }
        )
        SelectableChip(
            label = stringResource(Res.string.onboarding_skill_level_mid),
            selected = draft.level == 2,
            enabled = true,
            onClick = { onLevelChange(2) }
        )
        SelectableChip(
            label = stringResource(Res.string.onboarding_skill_level_low),
            selected = draft.level == 1,
            enabled = true,
            onClick = { onLevelChange(1) }
        )
    }
}

@Composable
private fun LinkFields(
    draft: LinkDraft,
    availableLinkCategories: List<String>,
    onCategorySelect: (String) -> Unit,
    onUrlChange: (String) -> Unit
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        availableLinkCategories.forEach { category ->
            SelectableChip(
                label = category,
                selected = category == draft.linkName,
                enabled = true,
                onClick = { onCategorySelect(category) }
            )
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
    OutlinedTextField(
        value = draft.url,
        onValueChange = onUrlChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = {
            Text(
                text = stringResource(Res.string.onboarding_placeholder_link_url),
                color = PickiiTextGray
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(FieldCornerRadius),
        colors = fieldColors()
    )
}

@Composable
private fun LicenseFields(
    draft: LicenseDraft,
    suggestions: List<com.example.pickii.domain.model.LicenseOption>,
    onNameChange: (String) -> Unit,
    onDateChange: (YearMonth) -> Unit
) {
    SearchDropdownField(
        query = draft.licenseName,
        onQueryChange = onNameChange,
        suggestions = if (draft.licenseName.isBlank()) emptyList() else suggestions,
        onSelect = { onNameChange(it.name) },
        itemLabel = { it.name },
        placeholder = stringResource(Res.string.onboarding_placeholder_license_name)
    )
    Spacer(modifier = Modifier.height(8.dp))
    YearMonthField(
        value = draft.acquiredDate,
        onValueChange = onDateChange,
        placeholder = stringResource(Res.string.onboarding_placeholder_acquired_date)
    )
}

@Composable
private fun FieldLabel(text: String) {
    CommonFieldLabel(text = text, color = PickiiTextGray, fontSize = 12.sp, fontWeight = null)
}

@Composable
private fun fieldColors() =
    OutlinedTextFieldDefaults.colors(
        focusedContainerColor = PickiiFieldBackground,
        unfocusedContainerColor = PickiiFieldBackground,
        focusedBorderColor = Color.Transparent,
        unfocusedBorderColor = Color.Transparent
    )
