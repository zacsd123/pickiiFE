package com.example.pickii.ui.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * 마이페이지 하위 화면(작성 공고, 지원 현황 등)의 2단 헤더. 상단 브랜드바([PickiiTopBar]) 아래에
 * 뒤로가기+제목([BackHeader])을 쌓는다.
 */
@Composable
fun MyPageSectionHeader(
    title: String,
    onBackClick: () -> Unit,
    onNotificationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        PickiiTopBar(notificationCount = 0, onNotificationClick = onNotificationClick)
        Spacer(modifier = Modifier.height(20.dp))
        BackHeader(title = title, onBackClick = onBackClick)
    }
}
