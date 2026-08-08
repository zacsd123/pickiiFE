package com.example.pickii.ui.common

import androidx.compose.ui.graphics.Color
import com.example.pickii.domain.model.RecruitStatus
import com.example.pickii.ui.theme.PickiiPaletteGray
import com.example.pickii.ui.theme.PickiiPaletteGreen

/** 모집 상태(모집중/추가모집/마감)에 대응하는 배지 색상. */
fun recruitStatusColor(status: RecruitStatus): Color =
    when (status) {
        RecruitStatus.OPEN -> PickiiPaletteGreen
        RecruitStatus.CLOSED -> PickiiPaletteGray
        RecruitStatus.ADDITIONAL -> PickiiPaletteGreen
    }
