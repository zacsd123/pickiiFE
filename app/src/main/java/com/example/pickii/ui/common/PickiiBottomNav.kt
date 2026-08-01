package com.example.pickii.ui.common

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.pickii.R
import com.example.pickii.ui.theme.PickiiNavYellow

/** 캡슐 모양 배경/인디케이터의 모서리 둥글기. */
private val NavCapsuleCornerRadius = 20.dp

/** 앱 전반에서 공유하는 하단 탭 내비게이션 바의 탭. 화면이 아직 없는 탭은 선택 상태만 바뀐다. */
enum class PickiiBottomNavTab {
    HOME,
    CALENDAR,
    CHAT,
    MY_PAGE
}

/** 탭의 위치/크기를 [PickiiBottomNav]의 슬라이딩 인디케이터 애니메이션에 사용하기 위해 기록해둔 값. */
private data class NavTabBounds(
    val x: Dp,
    val width: Dp,
    val height: Dp
)

/**
 * 인디케이터 이동/크기 변화에 사용하는 스프링 애니메이션.
 *
 * 탭 아이템 자체는 크기 애니메이션을 갖지 않아(라벨이 없어 아이콘만 표시) 인디케이터가
 * 쫓아갈 목표(탭의 위치/크기)가 매 프레임 흔들리지 않는다. 그래도 `tween`이 아닌 `spring`을 쓰는 건
 * 애니메이션 도중 다른 탭을 연속으로 눌러도(목표가 또 바뀌어도) 속도가 자연스럽게 이어지기 때문이다.
 * `StiffnessMedium`은 누르자마자 바로 반응하는 느낌을 주기 위해 기존 `StiffnessMediumLow`보다 높였다.
 */
private val NavIndicatorAnimationSpec =
    spring<Dp>(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium)

/** 탭과 아이콘 목록. */
private val PickiiBottomNavItems: List<Pair<PickiiBottomNavTab, ImageVector>> =
    listOf(
        PickiiBottomNavTab.HOME to Icons.Filled.Home,
        PickiiBottomNavTab.CALENDAR to Icons.Filled.DateRange,
        PickiiBottomNavTab.CHAT to Icons.Filled.ChatBubbleOutline,
        PickiiBottomNavTab.MY_PAGE to Icons.Filled.Person
    )

/** 탭에 표시할 라벨 문자열 리소스. */
private fun pickiiBottomNavLabelRes(tab: PickiiBottomNavTab): Int =
    when (tab) {
        PickiiBottomNavTab.HOME -> R.string.nav_home
        PickiiBottomNavTab.CALENDAR -> R.string.nav_calendar
        PickiiBottomNavTab.CHAT -> R.string.nav_chat
        PickiiBottomNavTab.MY_PAGE -> R.string.nav_mypage
    }

/**
 * 여러 화면에서 공유하는 하단 탭 내비게이션 바.
 *
 * 화면이 아직 없는 탭은 선택된 모양만 바뀌고 실제 이동은 하지 않는다.
 * 선택된 탭이 바뀌면 노란색 인디케이터가 이전 위치에서 새 위치로 부드럽게 슬라이딩한다.
 *
 * @param selectedTab 현재 선택된 탭
 * @param onTabSelect 탭 클릭 콜백
 */
@Composable
fun PickiiBottomNav(
    selectedTab: PickiiBottomNavTab,
    onTabSelect: (PickiiBottomNavTab) -> Unit
) {
    val density = LocalDensity.current
    val tabBounds = remember { mutableStateMapOf<PickiiBottomNavTab, NavTabBounds>() }
    val selectedBounds = tabBounds[selectedTab]

    val indicatorX by animateDpAsState(
        targetValue = selectedBounds?.x ?: 0.dp,
        animationSpec = NavIndicatorAnimationSpec,
        label = "bottomNavIndicatorX"
    )
    val indicatorWidth by animateDpAsState(
        targetValue = selectedBounds?.width ?: 0.dp,
        animationSpec = NavIndicatorAnimationSpec,
        label = "bottomNavIndicatorWidth"
    )
    val indicatorHeight by animateDpAsState(
        targetValue = selectedBounds?.height ?: 0.dp,
        animationSpec = NavIndicatorAnimationSpec,
        label = "bottomNavIndicatorHeight"
    )

    Box(
        modifier =
            Modifier
                .padding(vertical = 16.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(Color.Black)
                .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Box(
            modifier =
                Modifier
                    .offset(x = indicatorX)
                    .width(indicatorWidth)
                    .height(indicatorHeight)
                    .clip(RoundedCornerShape(NavCapsuleCornerRadius))
                    .background(PickiiNavYellow)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PickiiBottomNavItems.forEach { (tab, icon) ->
                val isSelected = tab == selectedTab
                val contentColor by animateColorAsState(
                    targetValue = if (isSelected) Color.Black else Color.Gray,
                    label = "bottomNavContentColor"
                )

                Row(
                    modifier =
                        Modifier
                            .onGloballyPositioned { coordinates ->
                                // onGloballyPositioned는 값이 그대로여도 레이아웃이 지나갈 때마다 다시 불린다.
                                // 매번 SnapshotStateMap에 새로 쓰면 좌표가 안 바뀌어도 리컴포지션을 유발해서
                                // 리플/인디케이터 애니메이션 중에 프레임이 끊기므로, 실제로 달라졌을 때만 쓴다.
                                val bounds =
                                    with(density) {
                                        NavTabBounds(
                                            x = coordinates.positionInParent().x.toDp(),
                                            width = coordinates.size.width.toDp(),
                                            height = coordinates.size.height.toDp()
                                        )
                                    }
                                if (tabBounds[tab] != bounds) {
                                    tabBounds[tab] = bounds
                                }
                            }.clip(RoundedCornerShape(NavCapsuleCornerRadius))
                            .clickable(onClick = { onTabSelect(tab) })
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(18.dp)
                    )
//                    글자가 없는게 더 깔끔해보임
//                    if (isSelected) {
//                        Spacer(modifier = Modifier.width(6.dp))
//                        Text(
//                            text = stringResource(pickiiBottomNavLabelRes(tab)),
//                            color = contentColor,
//                            fontSize = 13.sp,
//                            fontWeight = FontWeight.Medium
//                        )
//                    }
                }
            }
        }
    }
}
