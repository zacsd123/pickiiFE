package com.example.pickii.ui.mypage.profile.edit

import com.example.pickii.domain.model.AcademicStatus
import com.example.pickii.domain.model.LicenseOption
import com.example.pickii.domain.model.LinkCategory
import com.example.pickii.domain.model.RecruitTopic
import com.example.pickii.domain.model.TechStack
import com.example.pickii.domain.model.University
import com.example.pickii.ui.common.entrydraft.ExperienceDraft
import com.example.pickii.ui.common.entrydraft.LicenseDraft
import com.example.pickii.ui.common.entrydraft.LinkDraft
import com.example.pickii.ui.common.entrydraft.SkillToolDraft

/** 전공 최대 글자 수(0.7 공통 Validation 규칙). */
internal const val PROFILE_EDIT_MAX_MAJOR_LENGTH = 50

/** 희망 진로 최대 글자 수. */
internal const val PROFILE_EDIT_MAX_HOPE_LENGTH = 100

/** 장점 최대 글자 수. */
internal const val PROFILE_EDIT_MAX_STRENGTH_LENGTH = 300

/** [ProfileEditScreen]에 표시되는 상태. 온보딩과 같은 필드를 한 화면 스크롤 폼으로 보여준다. */
data class ProfileEditUiState(
    val isLoading: Boolean = true,
    val academicStatus: AcademicStatus? = null,
    val universityQuery: String = "",
    val universitySuggestions: List<University> = emptyList(),
    val selectedUniversity: University? = null,
    val major: String = "",
    val availableTopics: List<RecruitTopic> = emptyList(),
    val selectedTopicIds: Set<Int> = emptySet(),
    val hope: String = "",
    val strength: String = "",
    val experienceDrafts: List<ExperienceDraft> = emptyList(),
    val availableTechStacks: List<TechStack> = emptyList(),
    val skillToolDrafts: List<SkillToolDraft> = emptyList(),
    val availableLinkCategories: List<LinkCategory> = emptyList(),
    val linkDrafts: List<LinkDraft> = emptyList(),
    val availableLicenseOptions: List<LicenseOption> = emptyList(),
    val licenseDrafts: List<LicenseDraft> = emptyList(),
    val aboutMe: String = "",
    val contactEmail: String = "",
    val isSaving: Boolean = false,
    val errorMessage: String? = null
) {
    val isValid: Boolean
        get() =
            academicStatus != null &&
                selectedUniversity != null &&
                major.length in 2..PROFILE_EDIT_MAX_MAJOR_LENGTH &&
                contactEmail.isNotBlank() &&
                skillToolDrafts.all { it.techStackName.isBlank() || it.isSelected }
}
