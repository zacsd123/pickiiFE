package com.example.pickii.ui.applicant

/**
 * 지원자 목록 화면의 상태를 관리한다.
 */
data class ApplicantListUiState(
    val applicants: List<ApplicantUiModel> = emptyList(),
    val isLoading: Boolean = false,
) {

    val pendingCount: Int
        get() = applicants.count { it.status == ApplicantStatus.PENDING }

    val acceptedCount: Int
        get() = applicants.count { it.status == ApplicantStatus.ACCEPTED }

    val rejectedCount: Int
        get() = applicants.count { it.status == ApplicantStatus.REJECTED }

    val hasAcceptedApplicant: Boolean
        get() = acceptedCount > 0
}