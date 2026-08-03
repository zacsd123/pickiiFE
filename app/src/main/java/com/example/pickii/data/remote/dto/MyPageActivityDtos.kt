package com.example.pickii.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * `GET /users/me/applies`(4-4) 목록 항목.
 *
 * 문서에 응답 필드 예시가 없어 추정한 형태다 — 실제 백엔드 응답과 다를 수 있다.
 */
@Serializable
data class MyApplyDto(
    val applyId: Long,
    val recruitId: Long,
    val recruitTitle: String,
    val status: String,
    val appliedAt: String,
    val chatRoomId: Long? = null
)

/**
 * `GET /users/me/comments`(4-6) 목록 항목. 문서에 JSON 예시가 있어 필드명이 확정적이다.
 */
@Serializable
data class MyCommentDto(
    val commentId: Long,
    val recruitId: Long,
    val recruitTitle: String,
    val recruitStatus: String,
    val content: String,
    val createdAt: String
)

/**
 * `GET /users/me/scraps`(3-16) 목록 항목. 문서에 JSON 예시가 있어 필드명이 확정적이다(`scrappedAt` 철자 그대로).
 */
@Serializable
data class MyScrapDto(
    val recruitId: Long,
    val title: String,
    val authorNickname: String,
    val onCampus: Boolean = false,
    val status: String,
    val maxMembers: Int = 0,
    val availableSlots: Int = 0,
    val scrappedAt: String
)
