package com.example.pickii.ui.applicant

/**
 * 지원자의 현재 지원 상태다.
 */
enum class ApplicantStatus(
    val displayName: String,
) {
    PENDING(
        displayName = "대기",
    ),
    ACCEPTED(
        displayName = "수락",
    ),
    REJECTED(
        displayName = "거절",
    ),
}