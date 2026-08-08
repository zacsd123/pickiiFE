package com.example.pickii.ui.calendar.editor.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pickii.domain.model.ScheduleCategory
import com.example.pickii.ui.common.toComposeColor
import com.example.pickii.ui.theme.PickiiGray600
import com.example.pickii.ui.theme.PickiiInk

private val CategorySectionBackgroundColor = Color(0xFFFFFFFF)
private val CategoryTitleColor = PickiiGray600

private val SelectedCategoryBackgroundColor = PickiiInk
private val SelectedCategoryTextColor = Color(0xFFFFFFFF)

private val UnselectedCategoryBackgroundColor = Color(0xFFF1F1F1)
private val UnselectedCategoryTextColor = PickiiGray600

/**
 * 일정 태그 선택 영역이다.
 *
 * 선택 가능한 태그와 태그 관리 버튼을 표시한다.
 */
@Composable
fun ScheduleCategorySection(
    categories: List<ScheduleCategory>,
    selectedCategoryId: Long?,
    onCategoryClick: (Long?) -> Unit,
    onManageClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(
                    color = CategorySectionBackgroundColor,
                    shape = RoundedCornerShape(18.dp)
                ).padding(
                    horizontal = 20.dp,
                    vertical = 18.dp
                ),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "◇",
                color = CategoryTitleColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(
                modifier = Modifier.width(8.dp)
            )

            Text(
                text = "태그",
                color = CategoryTitleColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
        }

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ScheduleCategoryChip(
                text = "없음",
                color = null,
                isSelected = selectedCategoryId == null,
                onClick = {
                    onCategoryClick(null)
                }
            )

            categories.forEach { category ->
                ScheduleCategoryChip(
                    text = category.name,
                    color = category.color.toComposeColor(),
                    isSelected = selectedCategoryId == category.id,
                    onClick = {
                        onCategoryClick(category.id)
                    }
                )
            }

            ScheduleCategoryChip(
                text = "+ 관리",
                color = null,
                isSelected = false,
                onClick = onManageClick
            )
        }
    }
}

/**
 * 태그 하나를 둥근 칩 형태로 표시한다.
 */
@Composable
private fun ScheduleCategoryChip(
    text: String,
    color: Color?,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isNoneChip = color == null
    val chipShape = RoundedCornerShape(50)

    Row(
        modifier =
            modifier
                .background(
                    color =
                        when {
                            isSelected && isNoneChip -> SelectedCategoryBackgroundColor
                            isSelected -> Color.White
                            else -> UnselectedCategoryBackgroundColor
                        },
                    shape = chipShape
                ).let {
                    if (isSelected && color != null) {
                        it.border(1.5.dp, color, chipShape)
                    } else {
                        it
                    }
                }.clickable(
                    onClick = onClick
                ).padding(
                    horizontal = 14.dp,
                    vertical = 9.dp
                ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (color != null) {
            Box(
                modifier =
                    Modifier
                        .size(8.dp)
                        .background(
                            color = color,
                            shape = CircleShape
                        )
            )

            Spacer(
                modifier = Modifier.width(7.dp)
            )
        }

        Text(
            text = text,
            color =
                when {
                    isSelected && isNoneChip -> SelectedCategoryTextColor
                    isSelected && color != null -> color
                    else -> UnselectedCategoryTextColor
                },
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * 도메인 색상 타입을 Compose 색상으로 변환한다.
 */
