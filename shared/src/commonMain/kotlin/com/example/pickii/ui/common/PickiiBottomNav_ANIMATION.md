# PickiiBottomNav 애니메이션 동작 원리

`PickiiBottomNav.kt`의 노란 캡슐 인디케이터가 탭 전환 시 부드럽게 슬라이딩하는 로직 설명.

## 1. 개요

탭 선택 시 노란 인디케이터가 이전 탭 위치에서 새 탭 위치로 슬라이딩. 선택된 탭에만 라벨 텍스트가 나타나며, 그로 인한 크기 변화도 같이 애니메이션 처리됨.

## 2. 위치/크기 추적 — `NavTabBounds`, `tabBounds`

```kotlin
private data class NavTabBounds(val x: Dp, val width: Dp, val height: Dp)
val tabBounds = remember { mutableStateMapOf<PickiiBottomNavTab, NavTabBounds>() }
```

각 탭 `Row`에 `onGloballyPositioned` 붙여서 실제 레이아웃 완료 후 좌표 읽음 (line 165-173):

- `coordinates.positionInParent().x` : 부모(바깥 `Row`) 기준 x 좌표. px 단위라 `with(density) { .toDp() }`로 변환
- `coordinates.size` : 탭의 실제 measure된 width/height

`mutableStateMapOf`는 Compose State라서, 탭 위치가 바뀌어 맵이 갱신되면 그 값을 읽는 쪽(`selectedBounds`)이 자동으로 recomposition됨.

## 3. 선택된 탭 좌표 추출

```kotlin
val selectedBounds = tabBounds[selectedTab]
```

현재 선택된 탭의 저장된 좌표를 조회. 아직 한 번도 measure 안 된 탭(맵에 값 없음)이면 null → 인디케이터 좌표는 `?: 0.dp`로 폴백.

## 4. 인디케이터 애니메이션 — `animateDpAsState` x3

```kotlin
val indicatorX by animateDpAsState(selectedBounds?.x ?: 0.dp, NavIndicatorAnimationSpec, ...)
val indicatorWidth by animateDpAsState(selectedBounds?.width ?: 0.dp, NavIndicatorAnimationSpec, ...)
val indicatorHeight by animateDpAsState(selectedBounds?.height ?: 0.dp, NavIndicatorAnimationSpec, ...)
```

`selectedTab`이 바뀌면 `selectedBounds`가 바뀌고, 세 `animateDpAsState`의 `targetValue`가 바뀌면서 현재 값에서 새 목표로 자동 보간 시작. 실제 렌더링은 인디케이터 `Box`에 그대로 반영 (line 141-149):

```kotlin
Modifier.offset(x = indicatorX).width(indicatorWidth).height(indicatorHeight)
```

## 5. 왜 `spring`이고 `tween`이 아닌지 — `NavIndicatorAnimationSpec`

```kotlin
private val NavIndicatorAnimationSpec =
    spring<Dp>(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)
```

핵심 이유: 라벨 텍스트가 나타나거나 사라질 때 탭 자체 너비가 `animateContentSize`로 프레임마다 조금씩 변함. 탭 너비가 바뀌면 `onGloballyPositioned`가 매 프레임 다시 호출되어 `selectedBounds`도 계속 미세하게 갱신됨 → 인디케이터의 `targetValue`가 애니메이션 도중 계속 이동하는 상황.

- `tween`은 목표가 바뀔 때마다 처음부터 다시 선형/이징 보간을 시작 → 목표가 계속 바뀌면 매 프레임 리셋되어 끊겨 보임
- `spring`은 속도 기반 물리 모델이라 목표가 계속 바뀌어도 현재 속도를 유지한 채 자연스럽게 새 목표를 따라감 → 끊김 없이 이어짐

파라미터:
- `dampingRatio = DampingRatioNoBouncy` : 통통 튀는 오버슈트 없음
- `stiffness = StiffnessMediumLow` : 너무 느리지도 빠르지도 않은 속도

## 6. 크기 변화 애니메이션 — `animateContentSize`, `NavSizeAnimationSpec`

```kotlin
private val NavSizeAnimationSpec =
    spring<IntSize>(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)
```

바깥 캡슐 `Box`(line 138)와 각 탭 `Row`(line 176) 양쪽에 적용. 선택된 탭에 라벨 `Text`가 추가/제거되며 content 크기가 바뀔 때, 그 크기 변화 자체를 애니메이션 처리. 인디케이터와 같은 damping/stiffness 값을 써서 두 애니메이션이 같은 리듬으로 동시에 움직여 자연스러워 보임.

## 7. 색상 애니메이션 — `animateColorAsState`

```kotlin
val contentColor by animateColorAsState(if (isSelected) Color.Black else Color.Gray, ...)
```

아이콘/텍스트 색: 선택 시 검정, 비선택 시 회색으로 서서히 전환 (line 157-160). 기본 스펙 사용, spring 계열 아님.

## 8. 전체 흐름 요약

```
탭 클릭
  → onTabSelect 콜백으로 selectedTab 상태 변경
  → recomposition
  → selectedBounds 값 바뀜
  → animateDpAsState 3개(X/Width/Height)가 새 목표로 spring 애니메이션 시작
  → 동시에 새로 선택된 탭엔 라벨 Text 추가되어 animateContentSize로 탭/캡슐 크기 애니메이션
  → 인디케이터가 새 위치/크기로 부드럽게 이동하며 라벨 나타남
```
