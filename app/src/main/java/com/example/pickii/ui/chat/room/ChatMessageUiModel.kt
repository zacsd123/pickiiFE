package com.example.pickii.ui.chat

import java.time.LocalDateTime

/**
 * 채팅 메시지 화면 표시 모델이다.
 *
 * @property id 메시지 식별자
 * @property senderId 작성자 회원 식별자
 * @property senderNickname 메시지 작성자 닉네임
 * @property content 메시지 내용
 * @property createdAt 메시지 전송 시각
 * @property isMine 현재 사용자가 보낸 메시지인지 여부
 * @property isReadByCounterpart 상대가 이 메시지를 읽었는지 여부(내가 보낸 메시지에서만 의미 있음)
 * @property imageUri 이미지 메시지일 때 표시할 사진의 URI(로컬 선택 URI 또는 서버 imageUrl)
 */
data class ChatMessageUiModel(
    val id: String,
    val senderId: Long = 0L,
    val senderNickname: String = "",
    val content: String,
    val createdAt: LocalDateTime,
    val isMine: Boolean,
    val isReadByCounterpart: Boolean = false,
    val type: ChatMessageType = ChatMessageType.TEXT,
    val meetingNotice: MeetingNoticeUiModel? = null,
    val meetingConfirmed: MeetingConfirmedUiModel? = null,
    val imageUri: String? = null
)

enum class ChatMessageType {
    TEXT,
    MEETING_NOTICE,
    MEETING_CONFIRMED,
    IMAGE
}

data class MeetingNoticeUiModel(
    val meetingTitle: String,
    val requesterName: String,
    val pollId: Long,
    val deadlineMillis: Long
)

data class MeetingConfirmedUiModel(
    val meetingTitle: String,
    val pollId: Long,
    val slotStartMillis: Long,
    val slotEndMillis: Long,
    val scheduleId: Long
)
