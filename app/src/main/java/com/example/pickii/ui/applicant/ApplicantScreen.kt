package com.example.pickii.ui.applicant

sealed interface ApplicantScreen {

    data object List : ApplicantScreen

    data class Detail(
        val applicantId: Long,
    ) : ApplicantScreen
}