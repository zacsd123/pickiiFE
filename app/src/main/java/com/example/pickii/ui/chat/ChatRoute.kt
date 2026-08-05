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
 */
@Composable
fun ChatRoute(
    onTopLevelScreenChange: (Boolean) -> Unit,
    initialRoomId: Long? = null,
    modifier: Modifier = Modifier
) {
    var currentScreen by rememberSaveable {
        mutableStateOf(if (initialRoomId != null) ChatScreenType.ROOM.name else ChatScreenType.LIST.name)
    }

    var selectedRoomId by rememberSaveable {
        mutableStateOf(initialRoomId ?: 0L)
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
                }
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
                modifier = modifier
            )
        }
    }
}
