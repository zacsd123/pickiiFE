package com.example.pickii.domain.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

/**
 * 채팅방 종류를 나타낸다.
 */
enum class ChatRoomType {
    GROUP,
    DIRECT
}

/**
 * 프로젝트 진행 상태를 나타낸다. 서버는 IN_PROGRESS/END 두 가지만 쓴다(API 6-4).
 */
enum class ProjectStatus {
    IN_PROGRESS,
    END
}

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
 * 채팅방 참여자. [isLeader]는 `GET /projects/{projectId}`(6-2)의 `leaderId`와 비교해 계산한다 —
 * `GET /chatrooms/{chatRoomId}`(8-2) 자체의 `isLeader` 필드는 본인 여부만 알려줘서(알려진 API 제약)
 * 다른 참여자 판정에는 쓸 수 없다. 6-2 호출이 실패하면 본인 여부만 신뢰 가능한 규칙으로 내려간다
 * ([com.example.pickii.data.repository.ChatApiRepository] 참고).
 */
data class ChatMember(
    val memberId: Long,
    val nickname: String,
    val isLeader: Boolean,
    val exp: Int = 0
)

/** `POST /chatrooms/direct`(8-5) 결과. */
data class DirectChatRoomResult(
    val chatRoomId: Long
)
