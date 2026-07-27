package com.example.pickii.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pickii.ui.theme.PickiiBlue
import com.example.pickii.ui.theme.PickiiDisabledGray

/** 칩의 모서리 둥글기. */
private val ChipCornerRadius = 20.dp

/**
 * 선택/비선택/비활성 상태를 갖는 공용 선택 칩. 카테고리·주제 선택 등 여러 화면에서 재사용한다.
 *
 * @param label 칩에 표시할 문구
 * @param selected 현재 선택된 상태인지 여부
 * @param enabled 선택 가능한 상태인지 여부(false면 클릭이 무시되고 회색으로 표시된다)
 * @param onClick 칩 클릭 콜백
 */
@Composable
fun SelectableChip(
    label: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier =
            Modifier
                .clip(RoundedCornerShape(ChipCornerRadius))
                .background(
                    when {
                        !enabled -> PickiiDisabledGray
                        selected -> PickiiBlue
                        else -> Color.Black
                    }
                ).then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier)
                .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Text(text = label, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}
