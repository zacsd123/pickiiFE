package com.example.pickii.domain.model

/** 이메일 인증 목적(`POST /auth/email/send`, `/verify`의 `type`). */
enum class EmailPurpose {
    SIGNUP,
    PW_RESET,
    WITHDRAWAL
}
