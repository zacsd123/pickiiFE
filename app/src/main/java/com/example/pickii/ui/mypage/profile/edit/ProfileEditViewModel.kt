package com.example.pickii.ui.mypage.profile.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pickii.R
import com.example.pickii.domain.model.AcademicStatus
import com.example.pickii.domain.model.AdditionalLinkEntry
import com.example.pickii.domain.model.ExperienceEntry
import com.example.pickii.domain.model.LicenseEntry
import com.example.pickii.domain.model.SkillToolEntry
import com.example.pickii.domain.model.TechStack
import com.example.pickii.domain.model.University
import com.example.pickii.domain.model.UpdateProfileInput
import com.example.pickii.domain.repository.MasterDataRepository
import com.example.pickii.domain.repository.ProfileRepository
import com.example.pickii.ui.common.entrydraft.ExperienceDraft
import com.example.pickii.ui.common.entrydraft.LicenseDraft
import com.example.pickii.ui.common.entrydraft.LinkDraft
import com.example.pickii.ui.common.entrydraft.SkillToolDraft
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.YearMonth

/** 학교 검색 디바운스 간격(ms). */
private const val UNIVERSITY_SEARCH_DEBOUNCE_MS = 300L

/** 관심 분야는 최대 이 개수까지만 선택할 수 있다. */
internal const val MAX_TOPIC_SELECTION = 3

/** 기존 이력서를 불러와 한 화면 스크롤 폼으로 수정하고, [ProfileRepository.updateProfile]로 저장한다(4-3). */
class ProfileEditViewModel
    constructor(
        private val profileRepository: ProfileRepository,
        private val masterDataRepository: MasterDataRepository
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(ProfileEditUiState())
        val uiState: StateFlow<ProfileEditUiState> = _uiState.asStateFlow()

        private var universitySearchJob: Job? = null

        init {
            loadMasterData()
            loadProfile()
        }

        private fun loadMasterData() {
            viewModelScope.launch {
                masterDataRepository
                    .getTopics()
                    .onSuccess { topics -> _uiState.update { it.copy(availableTopics = topics) } }
                    .onFailure { onMasterDataLoadFailed() }
            }
            viewModelScope.launch {
                masterDataRepository
                    .getTechStacks()
                    .onSuccess { stacks -> _uiState.update { it.copy(availableTechStacks = stacks) } }
                    .onFailure { onMasterDataLoadFailed() }
            }
            viewModelScope.launch {
                masterDataRepository
                    .getLinkCategories()
                    .onSuccess { categories -> _uiState.update { it.copy(availableLinkCategories = categories) } }
                    .onFailure { onMasterDataLoadFailed() }
            }
            viewModelScope.launch {
                masterDataRepository
                    .getLicenseOptions()
                    .onSuccess { options -> _uiState.update { it.copy(availableLicenseOptions = options) } }
                    .onFailure { onMasterDataLoadFailed() }
            }
        }

        private fun onMasterDataLoadFailed() {
            _uiState.update { it.copy(errorMessage = "목록을 불러오지 못했어요. 다시 시도해주세요.") }
        }

        /** 목록 로드 실패 배너의 "다시 시도" 버튼을 클릭한다. */
        fun onRetryLoadMasterDataClick() {
            _uiState.update { it.copy(errorMessage = null) }
            loadMasterData()
        }

        private fun loadProfile() {
            viewModelScope.launch {
                profileRepository
                    .getMyProfile()
                    .onSuccess { profile ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                academicStatus = profile.academicStatus,
                                universityQuery = profile.univ,
                                selectedUniversity = University(id = profile.univId, name = profile.univ),
                                major = profile.major,
                                selectedTopicIds = profile.topicIds.toSet(),
                                hope = profile.hope.orEmpty(),
                                strength = profile.strength.orEmpty(),
                                experienceDrafts =
                                    profile.experiences.map { entry ->
                                        ExperienceDraft(
                                            startDate = entry.startDate,
                                            endDate = entry.endDate,
                                            title = entry.title,
                                            organization = entry.organization,
                                            description = entry.description
                                        )
                                    },
                                skillToolDrafts =
                                    profile.skillTools.map { entry ->
                                        SkillToolDraft(
                                            techStackName = entry.techStackName,
                                            level = entry.level,
                                            isSelected = true
                                        )
                                    },
                                linkDrafts =
                                    profile.additionalLinks.map { entry ->
                                        LinkDraft(linkName = entry.linkName, url = entry.url)
                                    },
                                licenseDrafts =
                                    profile.licenses.map { entry ->
                                        LicenseDraft(licenseName = entry.licenseName, acquiredDate = entry.acquiredDate)
                                    },
                                aboutMe = profile.aboutMe.orEmpty(),
                                contactEmail = profile.contactEmail.orEmpty()
                            )
                        }
                    }.onFailure {
                        _uiState.update {
                            it.copy(isLoading = false, toastMessageRes = R.string.profile_edit_toast_load_failed)
                        }
                    }
            }
        }

        fun onToastShown() {
            _uiState.update { it.copy(toastMessageRes = null) }
        }

        fun onAcademicStatusSelect(status: AcademicStatus) {
            _uiState.update { it.copy(academicStatus = status) }
        }

        fun onUniversityQueryChange(query: String) {
            _uiState.update { it.copy(universityQuery = query, selectedUniversity = null) }
            universitySearchJob?.cancel()
            if (query.isBlank()) {
                _uiState.update { it.copy(universitySuggestions = emptyList()) }
                return
            }
            universitySearchJob =
                viewModelScope.launch {
                    delay(UNIVERSITY_SEARCH_DEBOUNCE_MS)
                    masterDataRepository.getUniversities(query).onSuccess { universities ->
                        _uiState.update { it.copy(universitySuggestions = universities) }
                    }
                }
        }

        fun onUniversitySelect(university: University) {
            universitySearchJob?.cancel()
            _uiState.update {
                it.copy(
                    selectedUniversity = university,
                    universityQuery = university.name,
                    universitySuggestions = emptyList()
                )
            }
        }

        fun onMajorChange(value: String) {
            _uiState.update { it.copy(major = value.take(PROFILE_EDIT_MAX_MAJOR_LENGTH)) }
        }

        /** 관심 분야 칩을 토글한다. 이미 [MAX_TOPIC_SELECTION]개를 선택한 상태에서는 새로 선택할 수 없다. */
        fun onTopicToggle(topicId: Int) {
            _uiState.update { state ->
                val selected = state.selectedTopicIds
                val updated =
                    when {
                        topicId in selected -> selected - topicId
                        selected.size >= MAX_TOPIC_SELECTION -> selected
                        else -> selected + topicId
                    }
                state.copy(selectedTopicIds = updated)
            }
        }

        fun onHopeChange(value: String) {
            if (value.length <= PROFILE_EDIT_MAX_HOPE_LENGTH) _uiState.update { it.copy(hope = value) }
        }

        fun onStrengthChange(value: String) {
            if (value.length <= PROFILE_EDIT_MAX_STRENGTH_LENGTH) _uiState.update { it.copy(strength = value) }
        }

        fun onAboutMeChange(value: String) {
            _uiState.update { it.copy(aboutMe = value) }
        }

        fun onContactEmailChange(value: String) {
            _uiState.update { it.copy(contactEmail = value) }
        }

        fun onAddExperience() {
            _uiState.update { it.copy(experienceDrafts = it.experienceDrafts + ExperienceDraft()) }
        }

        fun onRemoveExperience(id: String) {
            _uiState.update { it.copy(experienceDrafts = it.experienceDrafts.filterNot { draft -> draft.id == id }) }
        }

        fun onExperienceStartDateChange(
            id: String,
            date: YearMonth
        ) = updateExperience(id) { it.copy(startDate = date) }

        fun onExperienceEndDateChange(
            id: String,
            date: YearMonth
        ) = updateExperience(id) { it.copy(endDate = date) }

        fun onExperienceTitleChange(
            id: String,
            value: String
        ) = updateExperience(id) { it.copy(title = value) }

        fun onExperienceOrganizationChange(
            id: String,
            value: String
        ) = updateExperience(id) { it.copy(organization = value) }

        fun onExperienceDescriptionChange(
            id: String,
            value: String
        ) = updateExperience(id) { it.copy(description = value) }

        private fun updateExperience(
            id: String,
            transform: (ExperienceDraft) -> ExperienceDraft
        ) {
            _uiState.update { state ->
                state.copy(experienceDrafts = state.experienceDrafts.map { if (it.id == id) transform(it) else it })
            }
        }

        fun onAddSkillTool() {
            _uiState.update { it.copy(skillToolDrafts = it.skillToolDrafts + SkillToolDraft()) }
        }

        fun onRemoveSkillTool(id: String) {
            _uiState.update { it.copy(skillToolDrafts = it.skillToolDrafts.filterNot { draft -> draft.id == id }) }
        }

        fun onSkillToolNameChange(
            id: String,
            value: String
        ) {
            _uiState.update { state ->
                state.copy(
                    skillToolDrafts =
                        state.skillToolDrafts.map {
                            if (it.id == id) it.copy(techStackName = value, isSelected = false) else it
                        }
                )
            }
        }

        fun onSkillToolSelect(
            id: String,
            techStack: TechStack
        ) {
            _uiState.update { state ->
                state.copy(
                    skillToolDrafts =
                        state.skillToolDrafts.map {
                            if (it.id == id) it.copy(techStackName = techStack.name, isSelected = true) else it
                        }
                )
            }
        }

        fun onSkillToolLevelChange(
            id: String,
            level: Int
        ) {
            _uiState.update { state ->
                state.copy(
                    skillToolDrafts = state.skillToolDrafts.map { if (it.id == id) it.copy(level = level) else it }
                )
            }
        }

        fun onAddLink() {
            _uiState.update { it.copy(linkDrafts = it.linkDrafts + LinkDraft()) }
        }

        fun onRemoveLink(id: String) {
            _uiState.update { it.copy(linkDrafts = it.linkDrafts.filterNot { draft -> draft.id == id }) }
        }

        fun onLinkCategorySelect(
            id: String,
            categoryName: String
        ) {
            _uiState.update { state ->
                state.copy(
                    linkDrafts =
                        state.linkDrafts.map {
                            if (it.id ==
                                id
                            ) {
                                it.copy(linkName = categoryName)
                            } else {
                                it
                            }
                        }
                )
            }
        }

        fun onLinkUrlChange(
            id: String,
            value: String
        ) {
            _uiState.update { state ->
                state.copy(linkDrafts = state.linkDrafts.map { if (it.id == id) it.copy(url = value) else it })
            }
        }

        fun onAddLicense() {
            _uiState.update { it.copy(licenseDrafts = it.licenseDrafts + LicenseDraft()) }
        }

        fun onRemoveLicense(id: String) {
            _uiState.update { it.copy(licenseDrafts = it.licenseDrafts.filterNot { draft -> draft.id == id }) }
        }

        fun onLicenseNameChange(
            id: String,
            value: String
        ) {
            _uiState.update { state ->
                state.copy(
                    licenseDrafts = state.licenseDrafts.map { if (it.id == id) it.copy(licenseName = value) else it }
                )
            }
        }

        fun onLicenseDateChange(
            id: String,
            date: YearMonth
        ) {
            _uiState.update { state ->
                state.copy(
                    licenseDrafts = state.licenseDrafts.map { if (it.id == id) it.copy(acquiredDate = date) else it }
                )
            }
        }

        fun onSaveClick(onSaved: () -> Unit) {
            val state = _uiState.value
            val university = state.selectedUniversity ?: return
            val academicStatus = state.academicStatus ?: return
            if (!state.isValid) return

            val input =
                UpdateProfileInput(
                    univId = university.id,
                    major = state.major,
                    academicStatus = academicStatus,
                    hope = state.hope.ifBlank { null },
                    strength = state.strength.ifBlank { null },
                    aboutMe = state.aboutMe.ifBlank { null },
                    contactEmail = state.contactEmail,
                    topicIds = state.selectedTopicIds.toList(),
                    skillTools =
                        state.skillToolDrafts.mapNotNull { draft ->
                            draft.techStackName.takeIf { it.isNotBlank() }?.let { SkillToolEntry(it, draft.level) }
                        },
                    licenses =
                        state.licenseDrafts.mapNotNull { draft ->
                            val date = draft.acquiredDate
                            if (draft.licenseName.isNotBlank() && date != null) {
                                LicenseEntry(draft.licenseName, date)
                            } else {
                                null
                            }
                        },
                    experiences =
                        state.experienceDrafts.mapNotNull { draft ->
                            val start = draft.startDate
                            if (draft.title.isNotBlank() && draft.organization.isNotBlank() && start != null) {
                                ExperienceEntry(
                                    startDate = start,
                                    endDate = draft.endDate,
                                    title = draft.title,
                                    organization = draft.organization,
                                    description = draft.description
                                )
                            } else {
                                null
                            }
                        },
                    additionalLinks =
                        state.linkDrafts.mapNotNull { draft ->
                            if (draft.linkName.isNotBlank() && draft.url.isNotBlank()) {
                                AdditionalLinkEntry(draft.linkName, draft.url)
                            } else {
                                null
                            }
                        }
                )

            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            viewModelScope.launch {
                profileRepository
                    .updateProfile(input)
                    .onSuccess {
                        _uiState.update { it.copy(isSaving = false) }
                        onSaved()
                    }.onFailure { error ->
                        _uiState.update { it.copy(isSaving = false, errorMessage = error.message) }
                    }
            }
        }
    }
