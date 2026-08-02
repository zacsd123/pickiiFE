package com.example.pickii.domain.model

/** 소셜 계정 연동 상태 한 건(`GET /users/me/social-accounts`, 1-13). 현재는 KAKAO만 지원한다. */
data class SocialAccount(
    val provider: String,
    val linked: Boolean
)
