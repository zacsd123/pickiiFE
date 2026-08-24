package com.example.pickii.ui.chat

import com.example.pickii.domain.model.ChatRoomType

/**
 * 채팅 목록에 표시되는 채팅방 미리보기 정보다.
 *
 * @property id 채팅방 식별자
 * @property type 채팅방 유형
 * @property roomName 채팅방 또는 상대방 이름
 * @property lastMessage 마지막 메시지 내용
 * @property lastMessageTime 마지막 메시지 전송 시간
 * @property participantSummary 참여자 정보. 목록 조회 API 응답에 참여자 닉네임이 내려오지 않아 항상 null이다
 * (백엔드에 필드 추가되기 전까지의 알려진 제약 — [com.example.pickii.data.repository.ChatApiRepository] 참고).
 * @property unreadCount 읽지 않은 메시지 개수
 * @property isNotificationEnabled 채팅방 알림 활성화 여부
 */
data class ChatRoomPreviewUiModel(
    val id: Long,
    val type: ChatRoomType,
    val roomName: String,
    val lastMessage: String,
    val lastMessageTime: String,
    val participantSummary: String? = null,
    val unreadCount: Int = 0,
    val isNotificationEnabled: Boolean = true
)
