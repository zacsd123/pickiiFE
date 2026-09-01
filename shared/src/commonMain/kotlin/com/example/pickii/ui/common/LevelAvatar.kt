package com.example.pickii.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.pickii.shared.generated.resources.Res
import com.example.pickii.shared.generated.resources.img_level_cat_1
import com.example.pickii.shared.generated.resources.img_level_cat_2
import com.example.pickii.shared.generated.resources.img_level_cat_3
import com.example.pickii.shared.generated.resources.img_level_cat_4
import com.example.pickii.util.calculateLevel
import org.jetbrains.compose.resources.painterResource

/** 프로필 사진이 아직 없어, 그 자리에 [exp]로 계산한 레벨의 고양이 캐릭터를 보여주는 placeholder. */
@Composable
fun LevelAvatar(
    exp: Int,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp
) {
    val level = calculateLevel(exp).level
    val avatarRes =
        when (level) {
            1 -> Res.drawable.img_level_cat_1
            2 -> Res.drawable.img_level_cat_2
            3 -> Res.drawable.img_level_cat_3
            else -> Res.drawable.img_level_cat_4
        }

    Image(
        painter = painterResource(resource = avatarRes),
        contentDescription = null,
        contentScale = ContentScale.Fit,
        modifier = modifier.size(size)
    )
}
