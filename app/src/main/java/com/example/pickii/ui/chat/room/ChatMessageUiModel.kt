package com.example.pickii.ui.chat

/**
 * 채팅 메시지 화면 표시 모델이다.
 *
 * @property id 메시지 식별자
 * @property senderName 메시지 작성자 이름
 * @property content 메시지 내용
 * @property sentAt 메시지 전송 시간
 * @property isMine 현재 사용자가 보낸 메시지인지 여부
 * @property unreadCount 아직 읽지 않은 참여자 수
 */
data class ChatMessageUiModel(
    val id: Long,
    val content: String,
    val sentAt: String,
    val isMine: Boolean,
    val unreadCount: Int = 0,
    val type: ChatMessageType = ChatMessageType.TEXT,
    val meetingNotice: MeetingNoticeUiModel? = null,
)

enum class ChatMessageType {
    TEXT,
    MEETING_NOTICE,
}

data class MeetingNoticeUiModel(
    val meetingTitle: String,
    val requesterName: String,
    val createdTimeMillis: Long,
    val isRegistered: Boolean,
)