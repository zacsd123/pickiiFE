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
private const val LIST_PREVIEW_MAX_LENGTH = 20

/** 목록 카드의 마지막 메시지 시각. 오늘이면 "오전/오후 h:mm", 아니면 "YYYY-MM-DD"(스펙 8-1 원문 표기). */
fun LocalDateTime.toChatListPreviewTimeText(now: LocalDateTime = nowDateTime()): String =
    if (date == now.date) format(ChatListSameDayTimeFormat) else date.toString()

/** 목록 카드 미리보기용 20자 초과분 말줄임(8-1 문서 5번). 채팅방 화면 내부 메시지에는 적용하지 않는다. */
fun String.truncateForChatListPreview(maxLength: Int = LIST_PREVIEW_MAX_LENGTH): String =
    if (length > maxLength) take(maxLength) + "..." else this
