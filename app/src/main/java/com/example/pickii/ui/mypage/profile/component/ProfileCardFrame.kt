package com.example.pickii.ui.mypage.profile.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pickii.shared.generated.resources.Res
import com.example.pickii.shared.generated.resources.img_mascot_preparing
import com.example.pickii.shared.generated.resources.mypage_profile_card_empty_message
import com.example.pickii.ui.theme.PickiiProfileCardBlack
import com.example.pickii.ui.theme.PickiiProfileCardGoldBright
import com.example.pickii.ui.theme.PickiiProfileCardGoldDim
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * 프로필 확인 화면 카드 6장이 공통으로 쓰는 검정+골드 카드 껍데기.
 *
 * [title]이 null이면 헤더/구분선 없이 [content]만 채운다(캐릭터 카드 전용).
 */
@Composable
fun ProfileCardFrame(
    modifier: Modifier = Modifier,
    headerIcon: ImageVector? = null,
    title: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier =
            modifier
                .clip(RoundedCornerShape(24.dp))
                .background(PickiiProfileCardBlack)
                .border(1.dp, PickiiProfileCardGoldDim, RoundedCornerShape(24.dp))
                .padding(20.dp)
    ) {
        if (title != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (headerIcon != null) {
                    Icon(
                        imageVector = headerIcon,
                        contentDescription = null,
                        tint = PickiiProfileCardGoldBright,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = title,
                    color = PickiiProfileCardGoldBright,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            DashedDivider(color = PickiiProfileCardGoldDim, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(14.dp))
        }
        content()
    }
}

/**
 * 데이터가 비어있는 카드(Skill&Tool/자격증/경력)에서 보여줄 마스코트 + 안내 문구.
 */
@Composable
fun ProfileCardEmptyState(
    modifier: Modifier = Modifier,
    message: String = stringResource(Res.string.mypage_profile_card_empty_message)
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(Res.drawable.img_mascot_preparing),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(160.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = message,
                color = PickiiProfileCardGoldBright,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun DashedDivider(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.height(1.dp)) {
        drawLine(
            color = color,
            start = Offset(0f, 0f),
            end = Offset(size.width, 0f),
            strokeWidth = 2f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
        )
    }
}
