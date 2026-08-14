package com.example.pickii.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/**
 * 화면 오른쪽에서 슬라이드인하는 사이드 패널의 뼈대(전체 화면 스크림 + 오른쪽 정렬 패널).
 * 채팅방 정보/알림설정/팀원목록/팀장위임/팀원내보내기/나가기 패널 등에서 공통으로 쓰는 구조다.
 *
 * @param onScrimClick 스크림(패널 바깥 영역) 클릭 시 콜백. 보통 패널 닫기.
 * @param panelWidthFraction 패널이 차지하는 화면 너비 비율.
 */
@Composable
fun SlideInSidePanelScaffold(
    onScrimClick: () -> Unit,
    modifier: Modifier = Modifier,
    scrimColor: Color = Color.Black.copy(alpha = 0.35f),
    panelBackgroundColor: Color = Color.White,
    panelWidthFraction: Float = 0.79f,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(scrimColor)
                    .clickable(onClick = onScrimClick)
        )

        Column(
            modifier =
                Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(panelWidthFraction)
                    .align(Alignment.CenterEnd)
                    .background(panelBackgroundColor),
            content = content
        )
    }
}
