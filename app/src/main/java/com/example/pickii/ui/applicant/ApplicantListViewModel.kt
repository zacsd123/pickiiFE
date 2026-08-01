package com.example.pickii.ui.applicant

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ApplicantListViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(
        ApplicantListUiState(
            applicants = createMockApplicants()
        )
    )
    val uiState: StateFlow<ApplicantListUiState> = _uiState.asStateFlow()

    /**
     * 지원자를 수락한다.
     */
    fun acceptApplicant(applicantId: Long) {
        updateApplicantStatus(
            applicantId = applicantId,
            status = ApplicantStatus.ACCEPTED
        )
    }

    /**
     * 지원자를 거절한다.
     */
    fun rejectApplicant(applicantId: Long) {
        updateApplicantStatus(
            applicantId = applicantId,
            status = ApplicantStatus.REJECTED
        )
    }

    /**
     * id로 지원자를 찾는다.
     */
    fun getApplicant(
        applicantId: Long,
    ): ApplicantUiModel? {
        return uiState.value.applicants.find { applicant ->
            applicant.id == applicantId
        }
    }

    /**
     * 상태를 변경한다.
     */
    private fun updateApplicantStatus(
        applicantId: Long,
        status: ApplicantStatus,
    ) {
        _uiState.value = _uiState.value.copy(
            applicants = _uiState.value.applicants.map { applicant ->
                if (applicant.id == applicantId) {
                    applicant.copy(status = status)
                } else {
                    applicant
                }
            }
        )
    }

    /**
     * 더미 데이터
     */
    private fun createMockApplicants(): List<ApplicantUiModel> {
        return listOf(
            ApplicantUiModel(
                id = 1,
                nickname = "김지원",
                major = "응용소프트웨어전공",
                grade = 3,
                appliedDate = "2026.07.20",
                applicationMessage = "Android 개발 경험이 있으며 프로젝트에 적극적으로 참여하겠습니다.",
                status = ApplicantStatus.PENDING
            ),
            ApplicantUiModel(
                id = 2,
                nickname = "박민수",
                major = "컴퓨터공학과",
                grade = 4,
                appliedDate = "2026.07.18",
                applicationMessage = "백엔드 개발 경험이 있습니다.",
                status = ApplicantStatus.ACCEPTED
            ),
            ApplicantUiModel(
                id = 3,
                nickname = "이서연",
                major = "정보통신공학과",
                grade = 2,
                appliedDate = "2026.07.22",
                applicationMessage = "UI 구현을 좋아합니다.",
                status = ApplicantStatus.REJECTED
            ),
            ApplicantUiModel(
                id = 4,
                nickname = "최현우",
                major = "응용소프트웨어전공",
                grade = 3,
                appliedDate = "2026.07.24",
                applicationMessage = "Compose를 공부하고 있습니다.",
                status = ApplicantStatus.PENDING
            ),
            ApplicantUiModel(
                id = 5,
                nickname = "정다은",
                major = "컴퓨터공학과",
                grade = 4,
                appliedDate = "2026.07.26",
                applicationMessage = "팀 프로젝트 경험이 많습니다.",
                status = ApplicantStatus.PENDING
            ),
        )
    }
}