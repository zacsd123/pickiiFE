@file:OptIn(ExperimentalTime::class)

package com.example.pickii.ui.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pickii.domain.model.MeetingPollDetail
import com.example.pickii.domain.model.MeetingPollSlot
import com.example.pickii.domain.model.MeetingPollStatus
import com.example.pickii.shared.generated.resources.Res
import com.example.pickii.shared.generated.resources.ic_chevron_down
import com.example.pickii.shared.generated.resources.ic_chevron_up
import com.example.pickii.ui.theme.PickiiBorderGray
import com.example.pickii.ui.theme.PickiiGray400
import com.example.pickii.ui.theme.PickiiGray650
import com.example.pickii.ui.theme.PickiiInk
import kotlinx.coroutines.delay
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.DayOfWeekNames
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.painterResource
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * 회의 조율 하나(pollId 기준)의 진행 상황을 카드 하나(노란 라운드 박스)로 보여준다. 예전엔 등록공지/응답폼/
 * 집계/확정 각각이 자기 컨테이너를 가진 별도 카드라 단계가 지날수록 카드가 아래로 쌓였는데, 이제 박스는
 * [MeetingCardContainer] 하나뿐이고 안의 내용만 단계에 따라 바뀐다. 마감 카운트다운/집계 시점 계산도 예전엔
 * 카드마다(그리고 [ChatRoomScreen]에서도) 따로 돌던 걸 여기 하나로 합쳤다 — 어차피 한 번에 본문 하나만
 * 보이니 타이머도 하나면 된다.
 */
@Suppress("LongParameterList")
@Composable
internal fun MeetingProgressCard(
    meetingNotice: MeetingNoticeUiModel,
    pollDetail: MeetingPollDetail?,
    isAcknowledged: Boolean,
    myPollSelection: Set<Long>,
    confirmedMeeting: MeetingConfirmedUiModel?,
    participantNames: List<String>,
    isCurrentUserLeader: Boolean,
    isSaved: Boolean,
    onAcknowledgeClick: () -> Unit,
    onToggleSlot: (Long) -> Unit,
    onToggleNoneAvailable: () -> Unit,
    onSubmitClick: () -> Unit,
    onCancelClick: () -> Unit,
    onConfirmClick: (Long) -> Unit,
    onSaveClick: () -> Unit
) {
    val status = pollDetail?.status
    var nowMillis by remember(meetingNotice.pollId) { mutableStateOf(System.currentTimeMillis()) }
    val currentStatus by rememberUpdatedState(status)

    LaunchedEffect(meetingNotice.pollId, meetingNotice.deadlineMillis) {
        while (currentStatus == null || currentStatus == MeetingPollStatus.COLLECTING) {
            nowMillis = System.currentTimeMillis()
            if (nowMillis >= meetingNotice.deadlineMillis) break
            delay(1000)
        }
    }

    val isAggregationReady =
        pollDetail != null &&
            (pollDetail.respondedCount >= pollDetail.totalMembers || nowMillis >= meetingNotice.deadlineMillis)
    val remainingTime = remainingTimeText(meetingNotice.deadlineMillis, nowMillis)

    MeetingCardContainer {
        when {
            pollDetail == null ||
                (status == MeetingPollStatus.COLLECTING && !(isAcknowledged || pollDetail.myResponded)) -> {
                RegistrationBody(
                    meetingNotice = meetingNotice,
                    isAcknowledged = isAcknowledged,
                    remainingTime = remainingTime,
                    onAcknowledgeClick = onAcknowledgeClick
                )
            }

            status == MeetingPollStatus.CANCELLED ->
                MeetingStatusLineBody(title = "[회의 조율 취소]", message = "이 조율은 취소됐어요.")

            confirmedMeeting != null -> {
                ConfirmedBody(
                    meetingConfirmed = confirmedMeeting,
                    participantNames = participantNames,
                    isSaved = isSaved,
                    onSaveClick = onSaveClick
                )
            }

            // 히스토리 페이지네이션이 옛 확정 브로드캐스트 메시지를 아직 안 불러온 극히 드문 경우의 대비.
            status == MeetingPollStatus.CONFIRMED ->
                MeetingStatusLineBody(title = "[회의 일정 확정]", message = "회의 일정이 확정됐어요.")

            !isAggregationReady -> {
                ResponseBody(
                    poll = pollDetail,
                    remainingTime = remainingTime,
                    mySelection = myPollSelection,
                    isCurrentUserLeader = isCurrentUserLeader,
                    onToggleSlot = onToggleSlot,
                    onToggleNoneAvailable = onToggleNoneAvailable,
                    onSubmitClick = onSubmitClick,
                    onCancelClick = onCancelClick
                )
            }

            else -> {
                AggregationBody(
                    poll = pollDetail,
                    isCurrentUserLeader = isCurrentUserLeader,
                    onConfirmClick = onConfirmClick
                )
            }
        }
    }
}

/** 마감까지 남은 시간을 "HH:mm:ss"로 표시한다. 지나면 "00:00:00". */
private fun remainingTimeText(
    deadlineMillis: Long,
    nowMillis: Long
): String {
    val remain = deadlineMillis - nowMillis
    if (remain <= 0L) return "00:00:00"
    val hour = remain / 1000 / 3600
    val minute = (remain / 1000 % 3600) / 60
    val second = remain / 1000 % 60
    return "%02d:%02d:%02d".format(hour, minute, second)
}

/** 등록 공지 단계 본문. */
@Composable
private fun RegistrationBody(
    meetingNotice: MeetingNoticeUiModel,
    isAcknowledged: Boolean,
    remainingTime: String,
    onAcknowledgeClick: () -> Unit
) {
    Text(text = "[회의 등록 공지]", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)

    Spacer(modifier = Modifier.height(12.dp))

    Text(
        text = "팀장 (${meetingNotice.requesterName})이 ${meetingNotice.pollId} 회의를 요청했어요",
        fontSize = 14.sp,
        color = Color.Black
    )

    Spacer(modifier = Modifier.height(4.dp))

    Text(
        text = "캘린더에 개인 일정을 모두 등록해주세요",
        fontSize = 14.sp,
        color = Color.Black
    )

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = "남은 시간: $remainingTime",
        fontSize = 14.sp,
        color = Color(0xFF7486D8)
    )

    Spacer(modifier = Modifier.height(20.dp))

    MeetingCardButton(
        label = "등록했어요",
        enabled = !isAcknowledged,
        onClick = onAcknowledgeClick
    )
}

/** 진행 단계와 무관하게 짧은 안내 문구만 보여줄 때 쓰는 본문(취소됨/확정 데이터 미도착 대비). */
@Composable
private fun MeetingStatusLineBody(
    title: String,
    message: String
) {
    Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)

    Spacer(modifier = Modifier.height(12.dp))

    Text(
        text = message,
        fontSize = 13.sp,
        color = Color.Black,
        textAlign = TextAlign.Center
    )
}

/** 회의 진행 카드가 공유하는 노란 라운드 컨테이너. */
@Composable
private fun MeetingCardContainer(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFFF9FCA8))
                .padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        content = content
    )
}

/** 회의 진행 카드가 공유하는 흰색 알약형 액션 버튼. 눌린 뒤에는(enabled = false) 옅게 표시한다. */
@Composable
private fun MeetingCardButton(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White)
                .alpha(if (enabled) 1f else 0.5f)
                .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
    }
}

private val SlotDateFormatter =
    LocalDate.Format {
        monthNumber(padding = Padding.NONE)
        char('월')
        char(' ')
        day(padding = Padding.NONE)
        char('일')
        char(' ')
        char('(')
        dayOfWeek(DayOfWeekNames("월", "화", "수", "목", "금", "토", "일"))
        char(')')
    }
private val SlotTimeFormatter =
    LocalDateTime.Format {
        hour()
        char(':')
        minute()
    }

/** 응답/집계 단계가 공유하는 선택 가능한 옵션 행("회의 가능한 날짜 없음" 등). [selected]면 어두운 배경으로
 * 강조한다 — 개별 시간 슬롯의 "가능" 체크([MeetingPollTimeChip])와는 다른 의미라 색을 구분했다. */
@Composable
private fun MeetingPollOptionRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(if (selected) PickiiInk else Color.White)
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else Color.Black,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * 응답 단계에서 시간 슬롯 하나를 나타내는 칩. 미선택은 흰색, 선택("가능한 시간"으로 체크됨)은 하늘색으로
 * 바뀐다. 여러 개를 [FlowRow]에 나란히 배치해 30분 단위 슬롯이 많아도 세로로 길게 늘어지지 않게 한다.
 */
@Composable
private fun MeetingPollTimeChip(
    label: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor =
        when {
            selected -> MeetingSlotAvailableBlue
            !enabled -> PickiiBorderGray
            else -> Color.White
        }
    Box(
        modifier =
            Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(backgroundColor)
                .border(
                    width = 1.dp,
                    color = if (selected) MeetingSlotAvailableBlue else PickiiBorderGray,
                    shape = RoundedCornerShape(12.dp)
                ).then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier)
                .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Text(
            text = label,
            color =
                if (selected) {
                    Color.White
                } else if (enabled) {
                    Color.Black
                } else {
                    PickiiGray400
                },
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

/** 시간 슬롯 칩이 선택됐을 때("가능한 시간"으로 체크됨) 쓰는 하늘색. 이 파일에서만 쓰여 로컬 상수로 둔다. */
private val MeetingSlotAvailableBlue = Color(0xFF4FC3F7)

/**
 * 응답 단계([회의 일정 조율]) 본문. 7-11의 전체 슬롯을 날짜별 그리드로 나열한다(API 문서가 "후보를 자르지
 * 않는다"고 명시함). 날짜는 기본 접힘 상태이고 탭해야 그 날짜의 시간 칩이 펼쳐진다(여러 날짜 동시에
 * 펼쳐도 됨) — 슬롯이 많은 다일 조율에서 스크롤이 끝없이 길어지지 않게 하기 위함. 기본은 전부
 * 미선택(흰색)이고, 탭해서 가능하다고 표시한 슬롯만 하늘색으로 강조한다("체크 = 가능한 시간" — 화면
 * 문구와 실제 제출 방향을 일치시킴).
 *
 * [MeetingProgressCard]가 이 본문을 고를 때는 이미 poll이 COLLECTING이고("취소/확정" 갈래는 그 앞에서
 * 걸러짐) "등록했어요"를 눌렀거나 이미 응답한 상태만 남아 있어, 여기서는 그 두 조건을 다시 검사하지 않는다.
 * 아직 응답 전이면([showForm]) 선택 그리드와 "제출하기" 버튼을, 이미 제출했으면 상단 안내 문구만 보여준다.
 */
@Suppress("LongParameterList")
@Composable
private fun ResponseBody(
    poll: MeetingPollDetail,
    remainingTime: String,
    mySelection: Set<Long>,
    isCurrentUserLeader: Boolean,
    onToggleSlot: (Long) -> Unit,
    onToggleNoneAvailable: () -> Unit,
    onSubmitClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    val showForm = !poll.myResponded
    val allSlotIds = poll.slots.map { it.slotId }.toSet()
    val isNoneAvailableSelected = allSlotIds.isNotEmpty() && mySelection.isEmpty()
    val slotsByDate = poll.slots.groupBy { it.startAt.date }.toSortedMap()
    var expandedDates by remember { mutableStateOf(setOf<LocalDate>()) }

    Text(text = "[회의 일정 조율]", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
    Spacer(modifier = Modifier.height(12.dp))
    Text(
        text = if (showForm) "가능한 시간을 모두 선택해 주세요.\n(복수 선택 가능)" else "응답을 제출했어요.",
        fontSize = 13.sp,
        color = Color.Black,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(12.dp))
    Text(
        text = "${poll.respondedCount}/${poll.totalMembers}명 응답 · 마감까지 $remainingTime",
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        color = Color(0xFF7486D8)
    )

    if (isCurrentUserLeader) {
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "조율 취소",
            fontSize = 12.sp,
            color = PickiiGray400,
            textDecoration = TextDecoration.Underline,
            modifier = Modifier.clickable(onClick = onCancelClick)
        )
    }

    if (!showForm) return
    Spacer(modifier = Modifier.height(16.dp))

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        MeetingPollOptionRow(
            label = "회의 가능한 날짜 없음",
            selected = isNoneAvailableSelected,
            onClick = onToggleNoneAvailable
        )

        slotsByDate.forEach { (date, slots) ->
            val isDateExpanded = date in expandedDates
            val selectedCountForDate = slots.count { it.slotId in mySelection }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clickable {
                                expandedDates =
                                    if (isDateExpanded) expandedDates - date else expandedDates + date
                            },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = date.format(SlotDateFormatter),
                        color = PickiiGray650,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    if (selectedCountForDate > 0) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${selectedCountForDate}개 선택",
                            color = MeetingSlotAvailableBlue,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        painter =
                            painterResource(
                                resource =
                                    if (isDateExpanded) Res.drawable.ic_chevron_up else Res.drawable.ic_chevron_down
                            ),
                        contentDescription = if (isDateExpanded) "접기" else "펼치기",
                        tint = PickiiGray400,
                        modifier = Modifier.size(16.dp)
                    )
                }
                AnimatedVisibility(visible = isDateExpanded) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        slots.forEach { slot ->
                            MeetingPollTimeChip(
                                label =
                                    slot.startAt.format(SlotTimeFormatter) +
                                        if (slot.prefilledByCalendar) " (일정 있음)" else "",
                                selected = slot.slotId in mySelection,
                                enabled = !slot.prefilledByCalendar,
                                onClick = { onToggleSlot(slot.slotId) }
                            )
                        }
                    }
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))
    MeetingCardButton(label = "제출하기", enabled = true, onClick = onSubmitClick)
}

/**
 * 집계 단계([최종 일정 조율]) 본문. 전원 응답 또는 마감 시각 도달 시 자동으로 나타난다(트리거는
 * [MeetingProgressCard]에서 계산). 응답 단계와 동일하게 날짜별로 접고 펼치는 구조를 쓰고, 각 슬롯을 참여 가능
 * 인원 비율로 배경 농도가 다른 칩으로 보여준다(진할수록 가능 인원 많음). 프로젝트장에게는 칩 자체가
 * 클릭 가능해 탭하면 바로 그 슬롯으로 확정(7-13)된다 — 실제 팀 투표 API는 없고 자동 확정도 하지 않는다.
 *
 * 이 본문이 선택될 때는 이미 poll이 COLLECTING으로 확정돼 있어([MeetingProgressCard]의 분기 순서) 별도로
 * 상태를 다시 검사하지 않는다.
 */
@Composable
private fun AggregationBody(
    poll: MeetingPollDetail,
    isCurrentUserLeader: Boolean,
    onConfirmClick: (Long) -> Unit
) {
    val slotsByDate = poll.slots.groupBy { it.startAt.date }.toSortedMap()
    var expandedDates by remember { mutableStateOf(setOf<LocalDate>()) }
    val canConfirm = isCurrentUserLeader

    Text(text = "[최종 일정 조율]", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
    Spacer(modifier = Modifier.height(12.dp))
    Text(
        text =
            if (canConfirm) {
                "응답이 마감됐어요. 진할수록 가능한 인원이 많아요.\n칩을 눌러 확정해 주세요."
            } else {
                "응답이 마감됐어요. 참여 가능한 인원이 많을수록 진하게 표시돼요."
            },
        fontSize = 13.sp,
        color = Color.Black,
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(16.dp))

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        slotsByDate.forEach { (date, slots) ->
            val isDateExpanded = date in expandedDates
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clickable {
                                expandedDates =
                                    if (isDateExpanded) expandedDates - date else expandedDates + date
                            },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = date.format(SlotDateFormatter),
                        color = PickiiGray650,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        painter =
                            painterResource(
                                resource =
                                    if (isDateExpanded) Res.drawable.ic_chevron_up else Res.drawable.ic_chevron_down
                            ),
                        contentDescription = if (isDateExpanded) "접기" else "펼치기",
                        tint = PickiiGray400,
                        modifier = Modifier.size(16.dp)
                    )
                }
                AnimatedVisibility(visible = isDateExpanded) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        slots.forEach { slot ->
                            MeetingPollHeatmapChip(
                                slot = slot,
                                totalMembers = poll.totalMembers,
                                enabled = canConfirm,
                                onClick = { onConfirmClick(slot.slotId) }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 집계 단계에서 시간 슬롯 하나를 나타내는 칩. [MeetingPollSlot.availableCount]가 [totalMembers]에서 차지하는
 * 비율로 배경 농도가 진해진다(응답 단계의 [MeetingPollTimeChip]과 같은 칩 형태를 재사용해 시각 언어를
 * 통일했다). 프로젝트장에게만 클릭 가능해 탭하면 그 슬롯으로 바로 확정한다.
 */
@Composable
private fun MeetingPollHeatmapChip(
    slot: MeetingPollSlot,
    totalMembers: Int,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val ratio = if (totalMembers > 0) (slot.availableCount.toFloat() / totalMembers).coerceIn(0f, 1f) else 0f
    val backgroundColor = lerp(Color.White, Color(0xFF4C6FFF), ratio)
    val textColor = if (ratio > 0.55f) Color.White else Color.Black

    Column(
        modifier =
            Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(backgroundColor)
                .border(
                    width = 1.dp,
                    color = if (ratio > 0f) backgroundColor else PickiiBorderGray,
                    shape = RoundedCornerShape(12.dp)
                ).then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier)
                .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "${slot.startAt.format(SlotTimeFormatter)}~${slot.endAt.format(SlotTimeFormatter)}",
            color = textColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = "가능 ${slot.availableCount} · 미응답 ${slot.unansweredCount}",
            color = textColor,
            fontSize = 10.sp
        )
    }
}

/**
 * 조율 없이 팀장이 바로 등록한 팀 일정(7-16) 카드. 등록되는 순간 이미 확정 상태라 별도 단계 없이
 * [ConfirmedBody]를 그대로 재사용한다 — 화면상 문구("확정됐어요")도 실제 상태와 맞다(회의 관리에서 바로
 * 수정 가능).
 */
@Composable
internal fun DirectMeetingCard(
    meetingConfirmed: MeetingConfirmedUiModel,
    participantNames: List<String>,
    isSaved: Boolean,
    onSaveClick: () -> Unit
) {
    MeetingCardContainer {
        ConfirmedBody(
            meetingConfirmed = meetingConfirmed,
            participantNames = participantNames,
            isSaved = isSaved,
            onSaveClick = onSaveClick
        )
    }
}

/**
 * 확정 단계([회의 일정 확정]) 본문. 확정된 슬롯 시각과 채팅방 멤버 전체 이름을 보여주고, "내 캘린더에 저장"
 * 버튼으로 이 일정을 본인 개인 캘린더에 저장할 수 있다. 다른 멤버 대신 저장해줄 API가 없어(권한상
 * 본인 캘린더만 쓸 수 있음) 각자 이 버튼을 눌러야 한다 — 알려진 제약.
 */
@Composable
private fun ConfirmedBody(
    meetingConfirmed: MeetingConfirmedUiModel,
    participantNames: List<String>,
    isSaved: Boolean,
    onSaveClick: () -> Unit
) {
    val start =
        Instant.fromEpochMilliseconds(meetingConfirmed.slotStartMillis).toLocalDateTime(TimeZone.currentSystemDefault())
    val end =
        Instant.fromEpochMilliseconds(meetingConfirmed.slotEndMillis).toLocalDateTime(TimeZone.currentSystemDefault())

    Text(text = "[회의 일정 확정]", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
    Spacer(modifier = Modifier.height(12.dp))
    Text(
        text = "회의 일정이 확정되었어요. 일정을 수정할 경우, 회의 관리에서 할 수 있어요.",
        fontSize = 13.sp,
        color = Color.Black,
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(16.dp))

    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(PickiiInk)
                .padding(horizontal = 16.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text =
                    "${start.date.format(SlotDateFormatter)} " +
                        "${start.format(SlotTimeFormatter)}~${end.format(SlotTimeFormatter)}",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            if (participantNames.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "참여자: ${participantNames.joinToString(", ")}",
                    color = Color(0xFFB0B0AA),
                    fontSize = 12.sp
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))
    MeetingCardButton(
        label = if (isSaved) "내 캘린더에 저장됨" else "내 캘린더에 저장",
        enabled = !isSaved,
        onClick = onSaveClick
    )
}
