package com.example.pickii.ui.chat

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format
import kotlinx.datetime.format.char

private val ChatRoomBubbleTimeFormat =
    LocalDateTime.Format {
        hour()
        char(':')
        minute()
    }

/** 채팅방 말풍선 시각. 24시간 "HH:mm"(스펙+데모 화면 모두 일치). */
fun LocalDateTime.toChatRoomBubbleTimeText(): String = format(ChatRoomBubbleTimeFormat)

/** 날짜 구분선에 쓸 "YYYY-MM-DD". */
fun LocalDateTime.toChatDateDividerText(): String = date.toString()

/**
 * [index]번 메시지가 "연속된 같은 발신자 묶음"의 마지막인지 여부. 시간/읽음 표시는 묶음의 마지막 메시지에만 노출한다.
 * MEETING_NOTICE는 발신자 개념이 없는 시스템 카드라 항상 묶음의 끝으로 취급한다(앞뒤 묶음도 끊는다).
 */
fun List<ChatMessageUiModel>.isLastOfConsecutiveRun(index: Int): Boolean {
    val current = this[index]
    val next = getOrNull(index + 1) ?: return true
    if (current.type == ChatMessageType.MEETING_NOTICE || next.type == ChatMessageType.MEETING_NOTICE) return true
    return next.senderId != current.senderId || next.isMine != current.isMine
}

/**
 * [index]번 메시지가 "연속된 같은 발신자 묶음"의 처음인지 여부. 상대방 발신자 이름은 묶음의 첫 메시지에만 노출한다.
 */
fun List<ChatMessageUiModel>.isFirstOfConsecutiveRun(index: Int): Boolean {
    val current = this[index]
    val previous = getOrNull(index - 1) ?: return true
    if (current.type == ChatMessageType.MEETING_NOTICE || previous.type == ChatMessageType.MEETING_NOTICE) return true
    return previous.senderId != current.senderId || previous.isMine != current.isMine
}
