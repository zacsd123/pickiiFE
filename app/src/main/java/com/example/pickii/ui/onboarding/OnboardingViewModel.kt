package com.example.pickii.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pickii.domain.model.AcademicStatus
import com.example.pickii.domain.model.AdditionalLinkEntry
import com.example.pickii.domain.model.CreateProfileInput
import com.example.pickii.domain.model.ExperienceEntry
import com.example.pickii.domain.model.LicenseEntry
import com.example.pickii.domain.model.LicenseOption
import com.example.pickii.domain.model.LinkCategory
import com.example.pickii.domain.model.RecruitTopic
import com.example.pickii.domain.model.SkillToolEntry
import com.example.pickii.domain.model.TechStack
import com.example.pickii.domain.model.University
import com.example.pickii.domain.repository.MasterDataRepository
import com.example.pickii.domain.repository.ProfileRepository
import com.example.pickii.ui.common.AiDialogState
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

/** 온보딩 전체 단계 수. */
internal const val ONBOARDING_TOTAL_STEPS = 8

/** 희망 진로 최대 글자 수. */
internal const val MAX_HOPE_LENGTH = 100

/** 장점 최대 글자 수. */
internal const val MAX_STRENGTH_LENGTH = 300

/** 관심 분야는 최대 이 개수까지만 선택할 수 있다. */
internal const val MAX_TOPIC_SELECTION = 3

/** 학교 검색 디바운스 간격(ms). */
private const val UNIVERSITY_SEARCH_DEBOUNCE_MS = 300L

/** [OnboardingScreen]에 표시되는 상태. */
data class OnboardingUiState(
    val step: Int = 1,
    val isSkipDialogVisible: Boolean = false,
    val aiDialogState: AiDialogState = AiDialogState.Hidden,
    val masterDataErrorMessage: String? = null,
    // 1단계: 학교 정보
    val academicStatus: AcademicStatus? = null,
    val universityQuery: String = "",
    val universitySuggestions: List<University> = emptyList(),
    val selectedUniversity: University? = null,
    val major: String = "",
    // 2단계: 관심 분야
    val availableTopics: List<RecruitTopic> = emptyList(),
    val selectedTopicIds: Set<Int> = emptySet(),
    // 3단계: 희망 진로
    val hope: String = "",
    // 4단계: 장점
    val strength: String = "",
    // 5단계: 수상 및 경험
    val experienceDrafts: List<ExperienceDraft> = emptyList(),
    // 6단계: Skill & Tool
    val availableTechStacks: List<TechStack> = emptyList(),
    val skillToolDrafts: List<SkillToolDraft> = emptyList(),
    // 7단계: 외부 링크
    val availableLinkCategories: List<LinkCategory> = emptyList(),
    val linkDrafts: List<LinkDraft> = emptyList(),
    // 8단계: 자격증
    val availableLicenseOptions: List<LicenseOption> = emptyList(),
    val licenseDrafts: List<LicenseDraft> = emptyList()
) {
    /** 1단계(학적상태/학교/전공)는 서버 필수 필드라, 다음 단계로 넘어가기 전에 검증한다. */
    val isStep1Valid: Boolean
        get() = academicStatus != null && selectedUniversity != null && major.length in 2..50

    /** 6단계(Skill & Tool)는 마스터 데이터 목록에서 선택된 이름만 서버에 전달 가능하다. */
    val isStep6Valid: Boolean
        get() = skillToolDrafts.all { it.techStackName.isBlank() || it.isSelected }

    val isLastStep: Boolean
        get() = step == ONBOARDING_TOTAL_STEPS
}

/**
 * 온보딩 8단계(학교/전공 → 관심분야 → 희망진로 → 장점 → 수상및경험 → Skill&Tool → 외부링크 → 자격증)의
 * 입력 상태를 누적하고, 마지막 단계에서 [ProfileRepository.createProfile]로 한 번에 제출한다.
 */
class OnboardingViewModel
    constructor(
        private val masterDataRepository: MasterDataRepository,
        private val profileRepository: ProfileRepository
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(OnboardingUiState())
        val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

        private var universitySearchJob: Job? = null

        init {
            loadMasterData()
        }

        /** 온보딩 각 단계의 목록(관심분야/기술스택/링크카테고리/자격증)을 불러온다. 실패한 항목이 있으면
         * 배너로 알리고 [onRetryLoadMasterDataClick]으로 다시 시도할 수 있게 한다 — 예전엔 실패를 조용히
         * 삼켜서 목록이 그냥 빈 채로 안 뜨는 것처럼 보였다. */
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
            _uiState.update { it.copy(masterDataErrorMessage = "목록을 불러오지 못했어요. 다시 시도해주세요.") }
        }

        /** 목록 로드 실패 배너의 "다시 시도" 버튼을 클릭한다. */
        fun onRetryLoadMasterDataClick() {
            _uiState.update { it.copy(masterDataErrorMessage = null) }
            loadMasterData()
        }

        // ---------- 1단계: 학교 정보 ----------

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
            _uiState.update { it.copy(major = value.take(MAX_MAJOR_LENGTH)) }
        }

        // ---------- 2단계: 관심 분야 ----------

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

        // ---------- 3/4단계: 희망 진로 / 장점 ----------

        fun onHopeChange(value: String) {
            if (value.length <= MAX_HOPE_LENGTH) _uiState.update { it.copy(hope = value) }
        }

        fun onStrengthChange(value: String) {
            if (value.length <= MAX_STRENGTH_LENGTH) _uiState.update { it.copy(strength = value) }
        }

        // ---------- 5단계: 수상 및 경험 ----------

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

        // ---------- 6단계: Skill & Tool ----------

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

        // ---------- 7단계: 외부 링크 ----------

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
                    linkDrafts = state.linkDrafts.map { if (it.id == id) it.copy(linkName = categoryName) else it }
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

        // ---------- 8단계: 자격증 ----------

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

        // ---------- 단계 이동 / 제출 / 건너뛰기 ----------

        /** 다음 단계로 이동한다. 마지막 단계면 실제로 제출한다. 1단계는 필수값을 채우지 않으면 넘어가지 않는다. */
        fun onNextClick(onCompleted: () -> Unit) {
            val state = _uiState.value
            if (state.step == 1 && !state.isStep1Valid) return
            if (state.step == 6 && !state.isStep6Valid) return

            if (state.isLastStep) {
                submit(onCompleted)
            } else {
                _uiState.update { it.copy(step = it.step + 1) }
            }
        }

        fun onPreviousClick() {
            _uiState.update { it.copy(step = (it.step - 1).coerceAtLeast(1)) }
        }

        fun onSkipRequested() {
            _uiState.update { it.copy(isSkipDialogVisible = true) }
        }

        fun onDismissSkipDialog() {
            _uiState.update { it.copy(isSkipDialogVisible = false) }
        }

        /** 온보딩 전체를 중단한다. 이력서를 만들지 않은 채 홈으로 이동한다(교내 공고를 볼 수 없음). */
        fun onConfirmSkip(onSkipped: () -> Unit) {
            _uiState.update { it.copy(isSkipDialogVisible = false) }
            onSkipped()
        }

        fun onAiDialogDismiss() {
            _uiState.update { it.copy(aiDialogState = AiDialogState.Hidden) }
        }

        fun onAiRetryClick(onCompleted: () -> Unit) {
            submit(onCompleted)
        }

        private fun submit(onCompleted: () -> Unit) {
            val state = _uiState.value
            val university = state.selectedUniversity ?: return
            val academicStatus = state.academicStatus ?: return

            val input =
                CreateProfileInput(
                    univId = university.id,
                    major = state.major,
                    academicStatus = academicStatus,
                    hope = state.hope.ifBlank { null },
                    strength = state.strength.ifBlank { null },
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

            _uiState.update { it.copy(aiDialogState = AiDialogState.Loading) }
            viewModelScope.launch {
                profileRepository
                    .createProfile(input)
                    .onSuccess {
                        _uiState.update { it.copy(aiDialogState = AiDialogState.Hidden) }
                        onCompleted()
                    }.onFailure {
                        _uiState.update { it.copy(aiDialogState = AiDialogState.Failed) }
                    }
            }
        }
    }

/** 전공 최대 글자 수(0.7 공통 Validation 규칙). */
private const val MAX_MAJOR_LENGTH = 50
