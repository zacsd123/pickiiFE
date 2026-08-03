package com.example.pickii.ui.mypage.profile.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.compose.ui.zIndex
import com.example.pickii.ui.theme.PickiiProfileCardBlack
import com.example.pickii.ui.theme.PickiiProfileCardGoldDim
import kotlinx.coroutines.launch

private const val TRANSITION_DURATION_MS = 360

/**
 * 카드가 한 장씩 위로 쌓이는 캐러셀. [pageCount]장을 화살표 버튼으로 넘긴다.
 *
 * 처음엔 카드 0번 한 장만 있고, → 를 누를 때마다 다음 카드가 위에서 내려와 쌓인다.
 * ← 를 누르면 맨 위 카드가 들려 빠지고 그 아래 있던 카드가 다시 앞으로 온다.
 * 양 끝(첫 장 / 다 쌓인 상태)에서는 해당 방향 화살표가 비활성화된다(순환하지 않음).
 */
@Composable
fun ProfileCardStack(
    pageCount: Int,
    modifier: Modifier = Modifier,
    page: @Composable (index: Int) -> Unit
) {
    // TODO: 드래그 스와이프 제스처 지원 추가 (pointerInput/detectHorizontalDragGestures)
    var currentIndex by rememberSaveable { mutableStateOf(0) }
    var direction by remember { mutableStateOf(0) }
    val progress = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    val isAnimating = direction != 0

    fun navigate(delta: Int) {
        val target = currentIndex + delta
        if (isAnimating || target !in 0 until pageCount) return
        direction = delta
        scope.launch {
            progress.animateTo(1f, animationSpec = tween(TRANSITION_DURATION_MS, easing = FastOutSlowInEasing))
            currentIndex = target
            progress.snapTo(0f)
            direction = 0
        }
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth(0.82f)
                    .aspectRatio(0.75f)
        ) {
            val oldTop = currentIndex
            val newTop = currentIndex + direction
            val lastIndex = maxOf(oldTop, newTop)
            val t = progress.value

            for (j in 0..lastIndex) {
                val leanDirection = if (j % 2 == 0) -1f else 1f
                val oldDepth = if (j <= oldTop) (oldTop - j).toFloat() else -1f
                val newDepth = if (j <= newTop) (newTop - j).toFloat() else -1f
                val depth = lerp(oldDepth, newDepth, t)

                StackedCard(depth = depth, leanDirection = leanDirection) {
                    page(j)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(28.dp)) {
            ArrowButton(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                enabled = !isAnimating && currentIndex > 0,
                onClick = { navigate(-1) }
            )
            ArrowButton(
                icon = Icons.AutoMirrored.Filled.ArrowForward,
                enabled = !isAnimating && currentIndex < pageCount - 1,
                onClick = { navigate(1) }
            )
        }
    }
}

/**
 * 스택 안에서 한 장의 카드를 [depth](0 = 맨 앞, 양수 = 뒤로 쌓인 정도, 음수 = 아직 스택에
 * 들어오지 않았거나 막 빠져나가는 중)에 맞는 위치/회전/크기로 그린다.
 */
@Composable
private fun StackedCard(
    depth: Float,
    leanDirection: Float,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val offsetXPx = with(density) { offsetXForDepth(depth, leanDirection).dp.toPx() }
    val offsetYPx = with(density) { offsetYForDepth(depth).dp.toPx() }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .zIndex(-depth)
                .graphicsLayer {
                    translationX = offsetXPx
                    translationY = offsetYPx
                    rotationZ = rotationForDepth(depth, leanDirection)
                    val scale = scaleForDepth(depth)
                    scaleX = scale
                    scaleY = scale
                    alpha = alphaForDepth(depth)
                }
    ) {
        content()
    }
}

/** depth<0이면 아직 스택에 안 들어왔거나(진입) 막 빠져나가는(이탈) 카드다. */
private fun scaleForDepth(depth: Float): Float {
    val coefficient = if (depth < 0f) 0.15f else 0.045f
    return (1f - depth * coefficient).coerceIn(0.7f, 1.15f)
}

private fun rotationForDepth(
    depth: Float,
    leanDirection: Float
): Float {
    val coefficient = if (depth < 0f) 20f else 6f
    return leanDirection * depth * coefficient
}

private fun offsetXForDepth(
    depth: Float,
    leanDirection: Float
): Float {
    if (depth < 0f) return 0f
    return leanDirection * depth * 10f
}

private fun offsetYForDepth(depth: Float): Float = if (depth < 0f) depth * 50f else -depth * 7f

/** 진입/이탈 구간(depth -1~0)에서만 서서히 나타나고 사라진다. 스택에 머무는 동안은 항상 불투명. */
private fun alphaForDepth(depth: Float): Float = (depth + 1f).coerceIn(0f, 1f)

@Composable
private fun ArrowButton(
    icon: ImageVector,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier =
            Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(PickiiProfileCardBlack)
                .border(1.dp, PickiiProfileCardGoldDim, CircleShape)
                .clickable(enabled = enabled, onClick = onClick)
                .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (enabled) Color.White else Color.White.copy(alpha = 0.3f)
        )
    }
}
