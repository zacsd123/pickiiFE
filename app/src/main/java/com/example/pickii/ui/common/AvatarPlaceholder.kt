package com.example.pickii.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.pickii.ui.theme.PickiiFieldBackground
import com.example.pickii.ui.theme.PickiiTextGray

/** 프로필 이미지가 없을 때 쓰는 빈 원 + 사람 아이콘 placeholder. */
@Composable
fun AvatarPlaceholder(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp
) {
    Box(
        modifier =
            modifier
                .size(size)
                .clip(CircleShape)
                .background(PickiiFieldBackground),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Person,
            contentDescription = null,
            tint = PickiiTextGray,
            modifier = Modifier.size(size / 2)
        )
    }
}
