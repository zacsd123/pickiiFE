package com.example.pickii.domain.model

/** 홈 화면의 모집 글 카드 하나를 표현하는 모델. */
data class RecruitPost(
    val id: String,
    val title: String,
    val authorName: String,
    val date: String,
    val currentParticipants: Int,
    val maxParticipants: Int
)
