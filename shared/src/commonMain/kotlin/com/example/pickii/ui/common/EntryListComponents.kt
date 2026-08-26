package com.example.pickii.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pickii.shared.generated.resources.Res
import com.example.pickii.shared.generated.resources.ic_add
import com.example.pickii.shared.generated.resources.ic_close_filled
import com.example.pickii.ui.theme.PickiiFieldBackground
import com.example.pickii.ui.theme.PickiiTextGray
import org.jetbrains.compose.resources.painterResource

/** 반복 입력 카드/버튼에 공통으로 쓰는 모서리 둥글기. */
private val EntryCornerRadius = 14.dp

/**
 * "+ 추가" 버튼. 수상 및 경험/Skill&Tool/외부 링크/자격증처럼 항목을 여러 개 입력받는 온보딩 단계에서 공용으로 쓴다.
 */
@Composable
fun AddEntryButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(EntryCornerRadius))
                .border(1.dp, PickiiTextGray, RoundedCornerShape(EntryCornerRadius))
                .clickable(onClick = onClick)
                .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_add),
            contentDescription = null,
            tint = PickiiTextGray,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = label, color = PickiiTextGray, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

/** 삭제(X) 버튼이 우상단에 붙은 반복 입력 항목 카드. [content]에 실제 입력 필드들을 배치한다. */
@Composable
fun RemovableEntryCard(
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(EntryCornerRadius))
                .background(PickiiFieldBackground)
                .padding(14.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(end = 28.dp)) {
            content()
        }
        IconButton(onClick = onRemove, modifier = Modifier.align(Alignment.TopEnd).size(28.dp)) {
            Icon(
                painter = painterResource(Res.drawable.ic_close_filled),
                contentDescription = null,
                tint = PickiiTextGray,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
