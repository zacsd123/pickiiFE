package com.example.pickii.domain.model

/** 학적 상태. */
enum class AcademicStatus(
    val label: String
) {
    ENROLLED("재학"),
    LEAVE_OF_ABSENCE("휴학"),
    GRADUATION_DEFERRED("졸업유예"),
    GRADUATED("졸업")
}
