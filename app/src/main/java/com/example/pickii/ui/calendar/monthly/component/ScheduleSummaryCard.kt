package com.example.pickii.ui.calendar.monthly.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pickii.ui.calendar.monthly.MonthlyScheduleUiModel
import com.example.pickii.domain.model.ScheduleColorType
import com.example.pickii.ui.common.toComposeColor
import com.example.pickii.ui.theme.PickiiInk
import com.example.pickii.ui.theme.PickiiPaletteRed
import com.example.pickii.ui.theme.PickiiSurfaceGrayMuted
import java.time.format.DateTimeFormatter

private val SummaryCardBackgroundColor = Color(0xFFFAFAF3)
private val SummaryTitleColor = PickiiInk
private val SummaryLabelColor = Color(0xFF99999A)
private val SummaryDescriptionColor = Color(0xFF43434A)
private val SummaryArrowBackgroundColor = Color(0xFFFFFFFF)
private val SummaryDividerColor = Color(0xFFE5E5DE)
private val SummaryIconBackgroundColor = PickiiSurfaceGrayMuted

private val EditButtonBackgroundColor = PickiiSurfaceGrayMuted
private val EditButtonTextColor = Color(0xFF202330)

private val ScheduleDateFormatter =
    DateTimeFormatter.ofPattern(
        "yyyy.MM.dd"
    )

/**
 * 선택한 날짜에 포함된 일정 카드를 표시한다.
 *
 * 카드를 누르면 같은 카드 내부에서 상세 내용이 펼쳐진다.
 */
@Composable
fun ScheduleSummaryCard(
    schedule: MonthlyScheduleUiModel,
    isExpanded: Boolean,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(
                    color = SummaryCardBackgroundColor,
                    shape = RoundedCornerShape(20.dp)
                ).clickable(onClick = onClick)
                .animateContentSize()
                .padding(
                    horizontal = 18.dp,
                    vertical = 16.dp
                )
    ) {
        ScheduleSummaryHeader(
            schedule = schedule,
            isExpanded = isExpanded
        )

        AnimatedVisibility(
            visible = isExpanded,
            enter =
                expandVertically(
                    expandFrom = Alignment.Top
                ) + fadeIn(),
            exit =
                shrinkVertically(
                    shrinkTowards = Alignment.Top
                ) + fadeOut()
        ) {
            ScheduleExpandedContent(
                schedule = schedule,
                onEditClick = onEditClick,
                onDeleteClick = onDeleteClick
            )
        }
    }
}

/**
 * 일정 카드 상단에 일정 색상, 제목, 펼치기 아이콘을 표시한다.
 */
@Composable
private fun ScheduleSummaryHeader(
    schedule: MonthlyScheduleUiModel,
    isExpanded: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier =
                Modifier
                    .size(11.dp)
                    .background(
                        color = schedule.categoryColor.toComposeColor(),
                        shape = CircleShape
                    )
        )

        Spacer(
            modifier = Modifier.width(14.dp)
        )

        Text(
            text = schedule.title,
            modifier = Modifier.weight(1f),
            color = SummaryTitleColor,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(
            modifier = Modifier.width(12.dp)
        )

        Box(
            modifier =
                Modifier
                    .size(38.dp)
                    .background(
                        color = SummaryArrowBackgroundColor,
                        shape = CircleShape
                    ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isExpanded) "⌃" else "⌄",
                color = SummaryTitleColor,
                fontSize = 22.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 24.sp
            )
        }
    }
}

/**
 * 펼쳐진 일정의 태그, 시간, 위치, 반복, 메모를 표시한다.
 */
@Composable
private fun ScheduleExpandedContent(
    schedule: MonthlyScheduleUiModel,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        ScheduleDivider()

        ScheduleDetailRow(
            icon = "◇",
            label = "태그"
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(8.dp)
                            .background(
                                color = schedule.categoryColor.toComposeColor(),
                                shape = CircleShape
                            )
                )

                Text(
                    text =
                        schedule.categoryName.ifBlank {
                            "없음"
                        },
                    color = SummaryDescriptionColor,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        ScheduleDivider()

        ScheduleDetailRow(
            icon = "◷"
        ) {
            ScheduleDateTimeContent(
                schedule = schedule
            )
        }

        ScheduleDivider()

        ScheduleDetailRow(
            icon = "⌖",
            label = "위치"
        ) {
            Text(
                text =
                    schedule.location.ifBlank {
                        "없음"
                    },
                color = SummaryDescriptionColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        ScheduleDivider()

        ScheduleDetailRow(
            icon = "⟳",
            label = "반복"
        ) {
            Text(
                text =
                    schedule.repeatText.ifBlank {
                        "없음"
                    },
                color = SummaryDescriptionColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        ScheduleDivider()

        ScheduleDetailRow(
            icon = "▤",
            label = "메모"
        ) {
            Text(
                text =
                    schedule.memo.ifBlank {
                        "없음"
                    },
                color = SummaryDescriptionColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 22.sp
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier =
                    Modifier
                        .weight(1f)
                        .background(
                            color = EditButtonBackgroundColor,
                            shape = RoundedCornerShape(16.dp)
                        ).clickable(onClick = onEditClick)
                        .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "일정 수정하기",
                    color = EditButtonTextColor,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier =
                    Modifier
                        .weight(1f)
                        .background(
                            color = EditButtonBackgroundColor,
                            shape = RoundedCornerShape(16.dp)
                        ).clickable(onClick = onDeleteClick)
                        .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "삭제",
                    color = PickiiPaletteRed,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * 일정 시작 날짜와 종료 날짜 및 시간을 표시한다.
 */
@Composable
private fun ScheduleDateTimeContent(
    schedule: MonthlyScheduleUiModel,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(28.dp)
    ) {
        ScheduleDateTimeColumn(
            label = "시작",
            date =
                schedule.startDate.format(
                    ScheduleDateFormatter
                ),
            time = createStartTimeText(schedule),
            modifier = Modifier.weight(1f)
        )

        ScheduleDateTimeColumn(
            label = "종료",
            date =
                schedule.endDate.format(
                    ScheduleDateFormatter
                ),
            time = createEndTimeText(schedule),
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * 날짜와 시간 한 묶음을 표시한다.
 */
@Composable
private fun ScheduleDateTimeColumn(
    label: String,
    date: String,
    time: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(
            text = label,
            color = SummaryLabelColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )

        Text(
            text = date,
            color = SummaryDescriptionColor,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )

        Text(
            text = time,
            color = SummaryLabelColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * 일정 상세 항목의 아이콘과 내용을 한 줄로 표시한다.
 */
@Composable
private fun ScheduleDetailRow(
    icon: String,
    modifier: Modifier = Modifier,
    label: String? = null,
    content: @Composable () -> Unit
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier =
                Modifier
                    .size(30.dp)
                    .background(
                        color = SummaryIconBackgroundColor,
                        shape = CircleShape
                    ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = icon,
                color = SummaryLabelColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(
            modifier = Modifier.width(12.dp)
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (label != null) {
                Text(
                    text = label,
                    color = SummaryLabelColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            content()
        }
    }
}

/**
 * 일정 상세 항목 사이 구분선을 표시한다.
 */
@Composable
private fun ScheduleDivider() {
    HorizontalDivider(
        modifier =
            Modifier.padding(
                start = 42.dp
            ),
        color = SummaryDividerColor,
        thickness = 1.dp
    )
}

/**
 * 일정 시작 시간을 표시할 문자열로 변환한다.
 */
private fun createStartTimeText(schedule: MonthlyScheduleUiModel): String {
    if (schedule.isAllDay) {
        return "하루 종일"
    }

    return schedule.startTime.ifBlank {
        "시간 미정"
    }
}

/**
 * 일정 종료 시간을 표시할 문자열로 변환한다.
 */
private fun createEndTimeText(schedule: MonthlyScheduleUiModel): String {
    if (schedule.isAllDay) {
        return "하루 종일"
    }

    return schedule.endTime.ifBlank {
        "시간 미정"
    }
}

/**
 * 일정 카테고리 색상을 Compose 색상으로 변환한다.
 */
