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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.example.pickii.ui.theme.PickiiBorderGray
import com.example.pickii.ui.theme.PickiiGray400
import com.example.pickii.ui.theme.PickiiGray650
import com.example.pickii.ui.theme.PickiiInk
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
internal fun MeetingRegistrationNoticeCard(
    meetingNotice: MeetingNoticeUiModel,
    isAcknowledged: Boolean,
    onAcknowledgeClick: () -> Unit
) {
    MeetingCardContainer {
        Text(
            text = "[회의 등록 공지]",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

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

        var remainingTime by remember {
            mutableStateOf("00:00:00")
        }

        LaunchedEffect(meetingNotice.deadlineMillis) {
            while (true) {
                val remain = meetingNotice.deadlineMillis - System.currentTimeMillis()

                if (remain <= 0L) {
                    remainingTime = "00:00:00"
                    break
                }

                val hour = remain / 1000 / 3600
                val minute = (remain / 1000 % 3600) / 60
                val second = remain / 1000 % 60

                remainingTime =
                    "%02d:%02d:%02d".format(hour, minute, second)

                delay(1000)
            }
        }

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
}

/** 카드1~3이 공유하는 노란 라운드 카드 컨테이너. */
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

/** 카드1~3이 공유하는 흰색 알약형 액션 버튼. 눌린 뒤에는(enabled = false) 옅게 표시한다. */
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

private val SlotDateFormatter = DateTimeFormatter.ofPattern("M월 d일 (E)", java.util.Locale.KOREAN)
private val SlotTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

/** 카드2/3이 공유하는 선택 가능한 옵션 행("회의 가능한 날짜 없음" 등). [selected]면 어두운 배경으로
 * 강조한다 — 개별 시간 슬롯의 "가능" 체크([MeetingPollTimeChip])와는 다른 의미라 색을 구분했다. */
@Composable
private fun MeetingPollOptionRow(
    label: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(if (selected) PickiiInk else Color.White)
                .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier)
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
 * 카드2에서 시간 슬롯 하나를 나타내는 칩. 미선택은 흰색, 선택("가능한 시간"으로 체크됨)은 하늘색으로
 * 바뀐다. 여러 개를 [FlowRow]에 나란히 배치해 30분 단위 슬롯이 많아도 세로로 길게 늘어지지 않게 한다.
 */
@Composable
private fun MeetingPollTimeChip(
    label: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier =
            Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(if (selected) MeetingSlotAvailableBlue else Color.White)
                .border(
                    width = 1.dp,
                    color = if (selected) MeetingSlotAvailableBlue else PickiiBorderGray,
                    shape = RoundedCornerShape(12.dp)
                ).then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier)
                .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else Color.Black,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

/** 카드2 시간 슬롯 칩이 선택됐을 때("가능한 시간"으로 체크됨) 쓰는 하늘색. 이 파일에서만 쓰여 로컬 상수로 둔다. */
private val MeetingSlotAvailableBlue = Color(0xFF4FC3F7)

/**
 * 카드2([회의 일정 조율]). 7-11의 전체 슬롯을 날짜별 그리드로 나열한다(API 문서가 "후보를 자르지
 * 않는다"고 명시함). 날짜는 기본 접힘 상태이고 탭해야 그 날짜의 시간 칩이 펼쳐진다(여러 날짜 동시에
 * 펼쳐도 됨) — 슬롯이 많은 다일 조율에서 스크롤이 끝없이 길어지지 않게 하기 위함. 기본은 전부
 * 미선택(흰색)이고, 탭해서 가능하다고 표시한 슬롯만 하늘색으로 강조한다("체크 = 가능한 시간" — 화면
 * 문구와 실제 제출 방향을 일치시킴).
 *
 * 아직 응답 전이고 조율이 진행 중일 때만([showForm]) 선택 그리드와 "제출하기" 버튼을 보여준다. 이미
 * 제출했거나 조율이 끝났으면(확정/취소) 그리드 대신 상단 안내 문구만 남긴다 — 제출 후 다시 들어와도
 * 매번 같은 입력 폼이 또 나타나 "내가 이미 했는지" 헷갈리던 문제를 없앴다. 상단엔 응답 현황(N/M명)과
 * 마감 카운트다운(진행 중일 때만)을 보여주고, 프로젝트장에게는 진행 중인 동안 "조율 취소"(7-14)를
 * 함께 노출한다.
 */
@Suppress("LongParameterList")
@Composable
internal fun MeetingPollResponseCard(
    poll: MeetingPollDetail,
    deadlineMillis: Long,
    mySelection: Set<Long>,
    isCurrentUserLeader: Boolean,
    onToggleSlot: (Long) -> Unit,
    onToggleNoneAvailable: () -> Unit,
    onSubmitClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    val isCollecting = poll.status == MeetingPollStatus.COLLECTING
    val showForm = isCollecting && !poll.myResponded
    val allSlotIds = poll.slots.map { it.slotId }.toSet()
    val isNoneAvailableSelected = allSlotIds.isNotEmpty() && mySelection.isEmpty()
    val slotsByDate = poll.slots.groupBy { it.startAt.toLocalDate() }.toSortedMap()
    var expandedDates by remember { mutableStateOf(setOf<LocalDate>()) }

    var remainingTime by remember { mutableStateOf("00:00:00") }
    LaunchedEffect(deadlineMillis) {
        while (true) {
            val remain = deadlineMillis - System.currentTimeMillis()
            if (remain <= 0L) {
                remainingTime = "00:00:00"
                break
            }
            val hour = remain / 1000 / 3600
            val minute = (remain / 1000 % 3600) / 60
            val second = remain / 1000 % 60
            remainingTime = "%02d:%02d:%02d".format(hour, minute, second)
            delay(1000)
        }
    }

    MeetingCardContainer {
        Text(text = "[회의 일정 조율]", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text =
                when {
                    showForm -> "가능한 시간을 모두 선택해 주세요.\n(복수 선택 가능)"
                    poll.status == MeetingPollStatus.CANCELLED -> "이 조율은 취소됐어요."
                    poll.status == MeetingPollStatus.CONFIRMED -> "회의 일정이 확정됐어요.\n아래 확정 안내를 확인해 주세요."
                    poll.myResponded -> "응답을 제출했어요."
                    else -> "이 조율은 마감됐어요."
                },
            fontSize = 13.sp,
            color = Color.Black,
            textAlign = TextAlign.Center
        )
        if (isCollecting) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "${poll.respondedCount}/${poll.totalMembers}명 응답 · 마감까지 $remainingTime",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF7486D8)
            )
        }
        if (isCurrentUserLeader && isCollecting) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "조율 취소",
                fontSize = 12.sp,
                color = PickiiGray400,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.clickable(onClick = onCancelClick)
            )
        }

        if (!showForm) return@MeetingCardContainer
        Spacer(modifier = Modifier.height(16.dp))

        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            MeetingPollOptionRow(
                label = "회의 가능한 날짜 없음",
                selected = isNoneAvailableSelected,
                enabled = isCollecting,
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
                                .then(
                                    if (isCollecting) {
                                        Modifier.clickable {
                                            expandedDates =
                                                if (isDateExpanded) expandedDates - date else expandedDates + date
                                        }
                                    } else {
                                        Modifier
                                    }
                                ),
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
                        Text(
                            text = if (isDateExpanded) "⌃" else "⌄",
                            color = PickiiGray400
                        )
                    }
                    AnimatedVisibility(visible = isDateExpanded) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            slots.forEach { slot ->
                                MeetingPollTimeChip(
                                    label = slot.startAt.format(SlotTimeFormatter),
                                    selected = slot.slotId in mySelection,
                                    enabled = isCollecting,
                                    onClick = { onToggleSlot(slot.slotId) }
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        MeetingCardButton(label = "제출하기", enabled = isCollecting, onClick = onSubmitClick)
    }
}

/**
 * 카드3(집계, [회의 일정 조율] 다음 단계). 전원 응답 또는 마감 시각 도달 시 자동으로 나타난다(트리거는
 * [ChatMessageItem]에서 계산). 카드2와 동일하게 날짜별로 접고 펼치는 구조를 쓰고, 각 슬롯을 참여 가능
 * 인원 비율로 배경 농도가 다른 칩으로 보여준다(진할수록 가능 인원 많음). 프로젝트장에게는 칩 자체가
 * 클릭 가능해 탭하면 바로 그 슬롯으로 확정(7-13)된다 — 실제 팀 투표 API는 없고 자동 확정도 하지 않는다.
 */
@Composable
internal fun MeetingPollAggregationCard(
    poll: MeetingPollDetail,
    isCurrentUserLeader: Boolean,
    onConfirmClick: (Long) -> Unit
) {
    val slotsByDate = poll.slots.groupBy { it.startAt.toLocalDate() }.toSortedMap()
    var expandedDates by remember { mutableStateOf(setOf<LocalDate>()) }
    val canConfirm = isCurrentUserLeader && poll.status == MeetingPollStatus.COLLECTING

    MeetingCardContainer {
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
                        Text(
                            text = if (isDateExpanded) "⌃" else "⌄",
                            color = PickiiGray400
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
}

/**
 * 카드3에서 시간 슬롯 하나를 나타내는 칩. [MeetingPollSlot.availableCount]가 [totalMembers]에서 차지하는
 * 비율로 배경 농도가 진해진다(카드2의 [MeetingPollTimeChip]과 같은 칩 형태를 재사용해 시각 언어를
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
 * 카드4([회의 일정 확정]). 확정된 슬롯 시각과 채팅방 멤버 전체 이름을 보여주고, "내 캘린더에 저장"
 * 버튼으로 이 일정을 본인 개인 캘린더에 저장할 수 있다. 다른 멤버 대신 저장해줄 API가 없어(권한상
 * 본인 캘린더만 쓸 수 있음) 각자 이 버튼을 눌러야 한다 — 알려진 제약.
 */
@Composable
internal fun MeetingConfirmedNoticeCard(
    meetingConfirmed: MeetingConfirmedUiModel,
    participantNames: List<String>,
    isSaved: Boolean,
    onSaveClick: () -> Unit
) {
    val start =
        Instant.ofEpochMilli(meetingConfirmed.slotStartMillis).atZone(ZoneId.systemDefault()).toLocalDateTime()
    val end =
        Instant.ofEpochMilli(meetingConfirmed.slotEndMillis).atZone(ZoneId.systemDefault()).toLocalDateTime()

    MeetingCardContainer {
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
                        "${start.toLocalDate().format(SlotDateFormatter)} " +
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
}
