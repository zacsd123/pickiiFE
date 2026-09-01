package com.example.pickii.ui.common

import androidx.compose.ui.graphics.Color
import com.example.pickii.domain.model.ScheduleColorType

/**
 * 일정 카테고리 색상을 실제 표시 색상으로 변환한다.
 * 월간/일간 캘린더, 카테고리 설정 화면이 전부 이 매핑을 공유해서 같은 카테고리는 어디서나 같은 색으로 보인다.
 */
fun ScheduleColorType.toComposeColor(): Color =
    when (this) {
        ScheduleColorType.RED -> Color(0xFFE86F73)
        ScheduleColorType.ORANGE -> Color(0xFFED9A53)
        ScheduleColorType.YELLOW -> Color(0xFFF1D354)
        ScheduleColorType.GREEN -> Color(0xFF84C976)
        ScheduleColorType.BLUE -> Color(0xFF6D9EEB)
        ScheduleColorType.PURPLE -> Color(0xFFAC8AFA)
        ScheduleColorType.PINK -> Color(0xFFE38AB0)
        ScheduleColorType.GRAY -> Color(0xFFBDBDBD)
        ScheduleColorType.BLACK -> Color(0xFF1C1C1C)
    }
