package com.example.pickii.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pickii.ui.theme.PickiiTextGray

/** 목록 화면 하단의 숫자 페이지네이션. 현재 페이지는 검은 원으로 강조되고, 첫/마지막 페이지에서는 이전/다음 버튼이 비활성화된다. */
@Composable
fun PaginationRow(
    currentPage: Int,
    totalPages: Int,
    visiblePageNumbers: List<Int>,
    onPageClick: (Int) -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasPreviousPage = currentPage > 1
    val hasNextPage = currentPage < totalPages

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "<",
            color = if (hasPreviousPage) Color.Black else PickiiTextGray,
            fontSize = 14.sp,
            modifier = Modifier.clickable(enabled = hasPreviousPage, onClick = onPreviousClick)
        )

        Spacer(modifier = Modifier.width(10.dp))

        visiblePageNumbers.forEach { page ->
            val isSelected = page == currentPage
            Box(
                modifier =
                    Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) Color.Black else Color.Transparent)
                        .clickable(enabled = !isSelected) { onPageClick(page) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = page.toString(),
                    color = if (isSelected) Color.White else PickiiTextGray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Text(
            text = ">",
            color = if (hasNextPage) Color.Black else PickiiTextGray,
            fontSize = 14.sp,
            modifier = Modifier.clickable(enabled = hasNextPage, onClick = onNextClick)
        )
    }
}
