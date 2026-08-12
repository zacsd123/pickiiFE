package com.example.pickii.ui.chat

private const val MEETING_NOTICE_PREFIX = "PICKII_MEETING_POLL"

/** 정규 텍스트에 나타나지 않는 유니코드 구분자(Control Picture "UNIT SEPARATOR"). */
private const val FIELD_SEPARATOR = "␟"

/**
 * 회의 조율 개설(7-10) 시 서버가 그룹 채팅방에 보내는 SYSTEM 메시지에는 pollId가 없다(알려진 API 제약 —
 * 응답할 poll을 찾을 방법이 없어짐). 그래서 개설한 클라이언트가 이 형식으로 인코딩한 "일반" 채팅 메시지를
 * 대신 전송해 모든 팀원에게 정상적인 채팅 브로드캐스트 경로로 pollId를 전달한다. 수신 측은
 * [decodeMeetingNoticeMessage]로 복원해 "회의 조율 등록" 카드로 렌더링한다(원문 텍스트는 화면에 보이지 않음).
 */
fun encodeMeetingNoticeMessage(
    pollId: Long,
    title: String,
    deadlineMillis: Long
): String = "$MEETING_NOTICE_PREFIX$pollId$FIELD_SEPARATOR$title$FIELD_SEPARATOR$deadlineMillis"

data class DecodedMeetingNotice(
    val pollId: Long,
    val title: String,
    val deadlineMillis: Long
)

fun decodeMeetingNoticeMessage(content: String): DecodedMeetingNotice? {
    if (!content.startsWith(MEETING_NOTICE_PREFIX)) return null
    val parts = content.removePrefix(MEETING_NOTICE_PREFIX).split(FIELD_SEPARATOR)
    if (parts.size != 3) return null
    val pollId = parts[0].toLongOrNull() ?: return null
    val deadlineMillis = parts[2].toLongOrNull() ?: return null
    return DecodedMeetingNotice(pollId = pollId, title = parts[1], deadlineMillis = deadlineMillis)
}

private const val MEETING_CONFIRMED_PREFIX = "PICKII_MEETING_CONFIRMED"

/**
 * 회의 최종 확정(7-13) 시 서버가 보내는 SYSTEM 메시지도 [MEETING_NOTICE_PREFIX]와 같은 이유로 구조화된
 * 정보(확정 슬롯 시각 등)가 없다. 확정을 호출한 클라이언트가 이 형식으로 인코딩해 전송하고, 수신 측은
 * [decodeMeetingConfirmedMessage]로 복원해 "회의 일정 확정" 카드로 렌더링한다. 참여자 명단은 페이로드에
 * 넣지 않는다 — 렌더링 시점에 그 채팅방의 현재 멤버 목록을 그대로 쓴다(정확한 참석자 API가 없어서의 근사치).
 */
fun encodeMeetingConfirmedMessage(
    pollId: Long,
    title: String,
    slotStartMillis: Long,
    slotEndMillis: Long,
    scheduleId: Long
): String =
    "$MEETING_CONFIRMED_PREFIX$pollId$FIELD_SEPARATOR$title$FIELD_SEPARATOR" +
        "$slotStartMillis$FIELD_SEPARATOR$slotEndMillis$FIELD_SEPARATOR$scheduleId"

data class DecodedMeetingConfirmed(
    val pollId: Long,
    val title: String,
    val slotStartMillis: Long,
    val slotEndMillis: Long,
    val scheduleId: Long
)

fun decodeMeetingConfirmedMessage(content: String): DecodedMeetingConfirmed? {
    if (!content.startsWith(MEETING_CONFIRMED_PREFIX)) return null
    val parts = content.removePrefix(MEETING_CONFIRMED_PREFIX).split(FIELD_SEPARATOR)
    if (parts.size != 5) return null
    val pollId = parts[0].toLongOrNull() ?: return null
    val slotStartMillis = parts[2].toLongOrNull() ?: return null
    val slotEndMillis = parts[3].toLongOrNull() ?: return null
    val scheduleId = parts[4].toLongOrNull() ?: return null
    return DecodedMeetingConfirmed(
        pollId = pollId,
        title = parts[1],
        slotStartMillis = slotStartMillis,
        slotEndMillis = slotEndMillis,
        scheduleId = scheduleId
    )
}

private const val DIRECT_MEETING_PREFIX = "PICKII_DIRECT_MEETING"

/**
 * 조율 없이 팀장이 바로 등록한 팀 일정(7-16)도 구조화된 정보(확정 슬롯 시각 등)가 필요해 같은 방식으로
 * 인코딩해 보낸다. 실제 poll이 없어 pollId는 필요 없다 — 받는 즉시 그 자체로 카드 하나만 뜬다
 * ([MeetingConfirmedUiModel]과 같은 화면 모델을 재사용하되 [ChatMessageType.DIRECT_MEETING]으로 구분한다).
 */
fun encodeDirectMeetingMessage(
    title: String,
    slotStartMillis: Long,
    slotEndMillis: Long,
    scheduleId: Long
): String =
    "$DIRECT_MEETING_PREFIX$title$FIELD_SEPARATOR$slotStartMillis$FIELD_SEPARATOR" +
        "$slotEndMillis$FIELD_SEPARATOR$scheduleId"

data class DecodedDirectMeeting(
    val title: String,
    val slotStartMillis: Long,
    val slotEndMillis: Long,
    val scheduleId: Long
)

fun decodeDirectMeetingMessage(content: String): DecodedDirectMeeting? {
    if (!content.startsWith(DIRECT_MEETING_PREFIX)) return null
    val parts = content.removePrefix(DIRECT_MEETING_PREFIX).split(FIELD_SEPARATOR)
    if (parts.size != 4) return null
    val slotStartMillis = parts[1].toLongOrNull() ?: return null
    val slotEndMillis = parts[2].toLongOrNull() ?: return null
    val scheduleId = parts[3].toLongOrNull() ?: return null
    return DecodedDirectMeeting(
        title = parts[0],
        slotStartMillis = slotStartMillis,
        slotEndMillis = slotEndMillis,
        scheduleId = scheduleId
    )
}
