package com.example.pickii.ui.chat

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

private enum class ChatScreenType {
    LIST,
    ROOM
}

/**
 * 채팅 내부 화면 전환을 관리한다.
 *
 * @param onTopLevelScreenChange 채팅 목록 화면(최상위)인지 여부가 바뀔 때마다 호출된다. 바깥의 공유 바텀 내비게이션을
 * 언제 보여줄지 결정하는 데 쓰인다.
 * @param initialRoomId 지정하면 목록을 거치지 않고 바로 해당 채팅방으로 진입한다(예: "지원 현황"의 채팅방 바로가기).
 * @param onInitialRoomConsumed [initialRoomId] 반영을 마쳤을 때 호출된다. 앱 레벨에서 이 값을 다시 null로
 * 돌려놓는 데 쓴다 — 그러지 않으면 채팅 탭이 매번 새로 컴포즈될 때(다른 탭에 갔다 돌아올 때 등) 사용자가
 * 그동안 직접 목록/다른 방으로 옮겨갔어도 다시 이 방으로 튕겨간다.
 */
@Composable
fun ChatRoute(
    onTopLevelScreenChange: (Boolean) -> Unit,
    onNavigateToMemberProfile: (Long) -> Unit,
    onNotificationBellClick: () -> Unit,
    initialRoomId: Long? = null,
    onInitialRoomConsumed: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var currentScreen by rememberSaveable {
        mutableStateOf(if (initialRoomId != null) ChatScreenType.ROOM.name else ChatScreenType.LIST.name)
    }

    var selectedRoomId by rememberSaveable {
        mutableStateOf(initialRoomId ?: 0L)
    }

    // 채팅 탭은 하단 내비게이션에서 상태가 저장/복원되므로(navigateToTab의 restoreState),
    // 위 rememberSaveable 초기값은 이 화면에 처음 진입할 때만 반영된다. 이미 채팅 탭을 방문한 적이
    // 있는 상태에서 새로 특정 방으로 이동시키려면(지원자 채팅 개설, 알림 클릭 등) initialRoomId가
    // 바뀔 때마다 명시적으로 화면을 전환해야 한다.
    LaunchedEffect(initialRoomId) {
        if (initialRoomId != null) {
            selectedRoomId = initialRoomId
            currentScreen = ChatScreenType.ROOM.name
            onInitialRoomConsumed()
        }
    }

    LaunchedEffect(currentScreen) {
        onTopLevelScreenChange(currentScreen == ChatScreenType.LIST.name)
    }

    when (
        ChatScreenType.valueOf(currentScreen)
    ) {
        ChatScreenType.LIST -> {
            ChatListRoute(
                onChatRoomClick = { roomId ->
                    selectedRoomId = roomId
                    currentScreen = ChatScreenType.ROOM.name
                },
                onNotificationBellClick = onNotificationBellClick
            )
        }

        ChatScreenType.ROOM -> {
            ChatRoomRoute(
                roomId = selectedRoomId,
                onBackClick = {
                    currentScreen = ChatScreenType.LIST.name
                },
                onLeaveChatRoom = {
                    currentScreen = ChatScreenType.LIST.name
                },
                onNavigateToMemberProfile = onNavigateToMemberProfile,
                modifier = modifier
            )
        }
    }
}
