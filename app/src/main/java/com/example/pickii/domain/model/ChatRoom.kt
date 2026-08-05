package com.example.pickii.domain.model

import com.example.pickii.ui.chat.ChatRoomType
import com.example.pickii.ui.chat.ProjectStatus
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * 채팅방 목록 카드 한 건(`GET /chatrooms`).
 *
 * 서버 응답에 참여자 요약/연결된 모집글 제목/마지막 발신자 필드가 없어 여기에도 없다(알려진 API 제약).
 */
data class ChatRoomSummary(
    val chatRoomId: Long,
    val type: ChatRoomType,
    val title: String,
    val lastMessage: String?,
    val lastMessageAt: LocalDateTime?,
    val unreadCount: Int,
    val isNotificationEnabled: Boolean
)

data class ChatRoomSummaryPage(
    val rooms: List<ChatRoomSummary>,
    val currentPage: Int,
    val totalPages: Int,
    val hasNext: Boolean
)

/**
 * 채팅방 상세(`GET /chatrooms/{id}`). GROUP 전용 필드는 DIRECT 방일 때 모두 null이다.
 *
 * 응답에 채팅방 종류를 나타내는 필드가 따로 없어, [projectId] 존재 여부로 [type]을 추론한다
 * (GROUP 전용 필드는 GROUP일 때만 채워진다는 8-2 문서 규칙에 근거).
 */
data class ChatRoomDetail(
    val chatRoomId: Long,
    val type: ChatRoomType,
    val title: String,
    val members: List<ChatMember>,
    val projectId: Long?,
    val startDate: LocalDate?,
    val endDate: LocalDate?,
    val projectStatus: ProjectStatus?,
    val isCurrentUserLeader: Boolean
)

/**
 * 채팅방 참여자. [isLeader]는 본인(memberId가 로그인 사용자와 일치)일 때만 신뢰할 수 있다 —
 * 서버 응답에 다른 참여자의 팀장 여부가 내려오지 않아, 본인이 아닌 행은 항상 false로 채워진다.
 */
data class ChatMember(
    val memberId: Long,
    val nickname: String,
    val isLeader: Boolean
)

/** `POST /chatrooms/direct`(8-5) 결과. */
data class DirectChatRoomResult(
    val chatRoomId: Long,
    val isNew: Boolean
)
