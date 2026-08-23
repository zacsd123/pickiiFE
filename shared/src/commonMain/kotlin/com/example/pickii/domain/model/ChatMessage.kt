package com.example.pickii.domain.model

import kotlinx.datetime.LocalDateTime

/** 채팅 메시지 한 건(히스토리 조회 + WebSocket 실시간 수신 공용). */
data class ChatMessage(
    val messageId: String,
    val senderId: Long,
    val senderNickname: String,
    val senderExp: Int = 0,
    val type: ChatMessageContentType,
    val text: String?,
    val imageUrl: String?,
    val createdAt: LocalDateTime
)

enum class ChatMessageContentType {
    TEXT,
    IMAGE
}

/** 커서 기반 메시지 페이지(`GET /chatrooms/{id}/messages`). */
data class ChatMessagePage(
    val messages: List<ChatMessage>,
    val nextCursor: String?,
    val hasNext: Boolean
)
