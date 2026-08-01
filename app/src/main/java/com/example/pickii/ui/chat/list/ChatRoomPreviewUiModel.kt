package com.example.pickii.ui.chat

/**
 * 채팅방의 유형을 나타낸다.
 */
//enum class ChatRoomType {
//    GROUP,
//    DIRECT,
//}

/**
 * 채팅 목록에 표시되는 채팅방 미리보기 정보다.
 *
 * @property id 채팅방 식별자
 * @property type 채팅방 유형
 * @property roomName 채팅방 또는 상대방 이름
 * @property senderName 마지막 메시지를 보낸 사람 이름
 * @property lastMessage 마지막 메시지 내용
 * @property lastMessageTime 마지막 메시지 전송 시간
 * @property recruitTitle 연결된 모집 글 제목
 * @property participantSummary 참여자 정보
 * @property unreadCount 읽지 않은 메시지 개수
 * @property isNotificationEnabled 채팅방 알림 활성화 여부
 */
data class ChatRoomPreviewUiModel(
    val id: Long,
    val type: ChatRoomType,
    val roomName: String,
    val senderName: String,
    val lastMessage: String,
    val lastMessageTime: String,
    val recruitTitle: String,
    val participantSummary: String? = null,
    val unreadCount: Int = 0,
    val isNotificationEnabled: Boolean = true,
)