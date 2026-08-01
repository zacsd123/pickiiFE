package com.example.pickii.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pickii.domain.model.CampusScope
import com.example.pickii.ui.theme.PickiiFieldBackground
import com.example.pickii.ui.theme.PickiiTextGray

/** 토글의 모서리 둥글기. */
private val ToggleCornerRadius = 20.dp

/**
 * 교내/교외 범위를 고르는 세그먼트 토글. 홈 필터와 공고 등록/수정 폼에서 공용으로 사용한다.
 *
 * @param selected 현재 선택된 범위
 * @param onSelect 범위 선택 콜백
 */
@Composable
fun CampusScopeToggle(
    selected: CampusScope,
    onSelect: (CampusScope) -> Unit
) {
    Row(
        modifier =
            Modifier
                .clip(RoundedCornerShape(ToggleCornerRadius))
                .background(PickiiFieldBackground)
                .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CampusScope.entries.forEach { scope ->
            val isSelected = scope == selected
            Box(
                modifier =
                    Modifier
                        .clip(RoundedCornerShape(ToggleCornerRadius))
                        .background(if (isSelected) Color.Black else Color.Transparent)
                        .clickable { onSelect(scope) }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = scope.label,
                    color = if (isSelected) Color.White else PickiiTextGray,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
