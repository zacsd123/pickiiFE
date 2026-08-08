package com.example.pickii.data.remote.dto

import kotlinx.serialization.Serializable

/** `GET /chatrooms` 목록 카드 한 건(8-1). */
@Serializable
data class ChatRoomSummaryDto(
    val chatRoomId: Long,
    val type: String,
    val title: String,
    val lastMessage: String? = null,
    val lastMessageAt: String? = null,
    val unreadCount: Int = 0,
    val notiEnabled: Boolean = true
)

/** `GET /chatrooms/{chatRoomId}` 상세(8-2). GROUP 방일 때만 projectId/startDate/endDate/status/isLeader가 내려온다. */
@Serializable
data class ChatRoomDetailDto(
    val chatRoomId: Long,
    val title: String,
    val members: List<ChatRoomMemberDto> = emptyList(),
    val projectId: Long? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val status: String? = null,
    val isLeader: Boolean? = null
)

@Serializable
data class ChatRoomMemberDto(
    val memberId: Long,
    val nickname: String
)

/**
 * 메시지 한 건. `GET /chatrooms/{id}/messages` 응답 항목 및 `/sub/chatrooms/{id}` 실시간 수신 바디에 공용으로 쓴다.
 *
 * [senderId]/[senderNickname]은 회의 조율 개설 알림 같은 시스템 메시지에서는 보내는 사람이 없어 응답에서
 * 아예 빠질 수 있다(0.4 Null Policy). 필수로 선언하면 그런 메시지를 받을 때마다 역직렬화가 실패해
 * 앱이 죽으므로(MissingFieldException) 선택 필드로 둔다.
 */
@Serializable
data class ChatMessageDto(
    val messageId: String,
    val senderId: Long? = null,
    val senderNickname: String? = null,
    val type: String,
    val message: String? = null,
    val imageUrl: String? = null,
    val createdAt: String
)

/** `{content, nextCursor, hasNext}` 커서 기반 페이지 응답. 채팅 메시지 히스토리(8-3) 전용 — 번호 기반인 [PageEnvelope]와 다르다. */
@Serializable
data class CursorPageDto<T>(
    val content: List<T>,
    val nextCursor: String? = null,
    val hasNext: Boolean = false
)

/** `POST /chatrooms/{chatRoomId}/images`(8-4) 응답. */
@Serializable
data class ChatImageUploadResponseDto(
    val imageUrl: String
)

/** `POST /chatrooms/direct`(8-5) 요청/응답. */
@Serializable
data class CreateDirectChatRoomRequest(
    val targetMemberId: Long
)

@Serializable
data class CreateDirectChatRoomResponseDto(
    val chatRoomId: Long
)

/** `PATCH /chatrooms/{chatRoomId}/read`(8-6) 요청. */
@Serializable
data class MarkChatRoomReadRequest(
    val lastReadMessageId: String
)

/** `PATCH /chatrooms/{chatRoomId}/notification`(8-8) 요청. */
@Serializable
data class UpdateChatRoomNotificationRequest(
    val enabled: Boolean
)

/**
 * (미확정/추정) `PATCH /chatrooms/{chatRoomId}/leader` 요청.
 *
 * 8번 API 명세서에 없는 엔드포인트다. `.../notification`, `.../read`의 PATCH 컨벤션을 따라 추정했으며,
 * 백엔드 확인 후 경로/바디가 바뀔 수 있다.
 */
@Serializable
data class DelegateChatRoomLeaderRequest(
    val newLeaderMemberId: Long
)

/** WebSocket `/pub/chatrooms/{chatRoomId}/messages` 발행 바디. */
@Serializable
data class PublishChatMessage(
    val type: String,
    val message: String? = null,
    val imageUrl: String? = null
)

/** WebSocket `/pub/chatrooms/{chatRoomId}/read` 발행 바디. */
@Serializable
data class PublishChatRead(
    val lastReadMessageId: String
)
