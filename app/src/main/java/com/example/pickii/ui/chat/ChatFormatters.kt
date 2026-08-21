package com.example.pickii.ui.chat

import com.example.pickii.util.nowDateTime
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char

private val ChatListSameDayTimeFormat =
    LocalDateTime.Format {
        amPmMarker("오전", "오후")
        char(' ')
        amPmHour(padding = Padding.NONE)
        char(':')
        minute()
    }
private val ChatRoomBubbleTimeFormat =
    LocalDateTime.Format {
        hour()
        char(':')
        minute()
    }
private const val LIST_PREVIEW_MAX_LENGTH = 20

/** 목록 카드의 마지막 메시지 시각. 오늘이면 "오전/오후 h:mm", 아니면 "YYYY-MM-DD"(스펙 8-1 원문 표기). */
fun LocalDateTime.toChatListPreviewTimeText(now: LocalDateTime = nowDateTime()): String =
    if (date == now.date) format(ChatListSameDayTimeFormat) else date.toString()

/** 채팅방 말풍선 시각. 24시간 "HH:mm"(스펙+데모 화면 모두 일치). */
fun LocalDateTime.toChatRoomBubbleTimeText(): String = format(ChatRoomBubbleTimeFormat)

/** 날짜 구분선에 쓸 "YYYY-MM-DD". */
fun LocalDateTime.toChatDateDividerText(): String = date.toString()

/** 목록 카드 미리보기용 20자 초과분 말줄임(8-1 문서 5번). 채팅방 화면 내부 메시지에는 적용하지 않는다. */
fun String.truncateForChatListPreview(maxLength: Int = LIST_PREVIEW_MAX_LENGTH): String =
    if (length > maxLength) take(maxLength) + "..." else this

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
