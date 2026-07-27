package com.example.pickii.domain.model

/** 현재 로그인한 사용자. [hasProfile]이 false면 프로필(Resume)을 아직 만들지 않은 상태다. */
data class CurrentUser(
    val id: String,
    val nickname: String,
    val experience: Int,
    val hasProfile: Boolean
)
