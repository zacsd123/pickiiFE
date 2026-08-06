package com.example.pickii.ui.applicant

/**
 * 지원자 한 명의 정보를 표현하는 UI 모델이다.
 */
data class ApplicantUiModel(
    val id: Long,
    val memberId: Long,
    // 프로필
    val nickname: String,
    // 지원 정보
    val appliedDate: String,
    val applicationMessage: String,
    val keywords: List<String>,
    // 현재 지원 상태
    val status: ApplicantStatus
)
