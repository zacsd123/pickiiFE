package com.example.pickii.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pickii.shared.generated.resources.Res
import com.example.pickii.shared.generated.resources.ic_arrow_back
import org.jetbrains.compose.resources.painterResource

/**
 * 뒤로가기 아이콘 + 제목으로 구성된 화면 상단 헤더.
 *
 * 화면마다 제목 색상/크기, 아이콘-제목 간격, 아이콘 터치 영역, 오른쪽 트레일링 콘텐츠(메뉴/버튼 등)가
 * 조금씩 달라서 전부 파라미터로 뺐다. 기본값은 가장 흔한 형태(순수 검정 18sp, 12dp 간격, 터치영역 없는
 * 기본 아이콘 크기)를 따른다.
 *
 * @param iconTouchSize 지정하면 아이콘을 이 크기의 원형 터치 영역(Box, 중앙 정렬 24dp 아이콘)으로 감싼다.
 *   생략하면 아이콘 자체에 바로 클릭 리스너를 붙인다(기존 기본 동작).
 * @param trailingContent 제목 오른쪽에 배치할 콘텐츠(메뉴 아이콘/버튼 등). 있으면 제목에 `weight(1f)`이
 *   붙어 트레일링 콘텐츠가 오른쪽 끝으로 밀린다.
 */
@Composable
fun BackHeader(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    titleColor: Color = Color.Black,
    titleFontSize: TextUnit = 18.sp,
    spacing: Dp = 12.dp,
    iconTouchSize: Dp? = null,
    trailingContent: (@Composable RowScope.() -> Unit)? = null
) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        if (iconTouchSize != null) {
            Box(
                modifier = Modifier.size(iconTouchSize).clickable(onClick = onBackClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_arrow_back),
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
            }
        } else {
            Icon(
                painter = painterResource(Res.drawable.ic_arrow_back),
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.clickable(onClick = onBackClick)
            )
        }
        Spacer(modifier = Modifier.width(spacing))
        Text(
            text = title,
            color = titleColor,
            fontSize = titleFontSize,
            fontWeight = FontWeight.Bold,
            modifier = if (trailingContent != null) Modifier.weight(1f) else Modifier
        )
        trailingContent?.invoke(this)
    }
}
