package com.example.pickii.ui.applicant

/**
 * 지원자 한 명의 정보를 표현하는 UI 모델이다.
 */
data class ApplicantUiModel(
    val id: Long,

    // 프로필
    val nickname: String,
    val major: String,
    val grade: Int,

    // 지원 정보
    val appliedDate: String,
    val applicationMessage: String,

    // 현재 지원 상태
    val status: ApplicantStatus,
)