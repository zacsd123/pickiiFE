package com.example.pickii.ui.chat

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pickii.domain.model.ChatRoomType
import com.example.pickii.shared.generated.resources.Res
import com.example.pickii.shared.generated.resources.chat_direct_tab
import com.example.pickii.shared.generated.resources.chat_empty_list
import com.example.pickii.shared.generated.resources.chat_group_tab
import com.example.pickii.shared.generated.resources.chat_room_no_message
import com.example.pickii.shared.generated.resources.chat_title
import com.example.pickii.shared.generated.resources.ic_notification_off
import com.example.pickii.shared.generated.resources.ic_notification_on
import com.example.pickii.shared.generated.resources.ic_profile
import com.example.pickii.ui.common.OneShotEventEffect
import com.example.pickii.ui.common.PickiiBottomNavOverlaySpacing
import com.example.pickii.ui.common.PickiiTopBar
import com.example.pickii.ui.common.RecruitUiEvent
import com.example.pickii.ui.theme.PickiiSurfaceGrayMuted
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.androidx.compose.koinViewModel

private val ChatBackgroundColor = Color(0xFFF9FCA8)
private val ChatPrimaryColor = Color(0xFF1B2130)
private val ChatSecondaryTextColor = Color(0xFF9BA1B1)
private val ChatProfileBackgroundColor = Color(0xFFE7E8ED)
private val ChatUnselectedTabColor = PickiiSurfaceGrayMuted
private const val LOAD_MORE_THRESHOLD = 3

/**
 * 채팅 목록 화면의 ViewModel 상태를 구독하고 사용자 동작을 전달한다.
 *
 * @param onChatRoomClick 채팅방 선택(또는 1:1 채팅방 생성 후 이동) 시 실행할 동작. 채팅방 식별자만 전달하며,
 * 방 제목/종류는 채팅방 화면이 상세 조회 API로 직접 가져온다.
 * @param viewModel 채팅 목록 화면 ViewModel
 */
@Composable
fun ChatListRoute(
    onChatRoomClick: (Long) -> Unit,
    onNotificationBellClick: () -> Unit,
    viewModel: ChatListViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.refreshAll()
    }

    OneShotEventEffect(flow = viewModel.events) { event ->
        when (event) {
            is RecruitUiEvent.ShowToast ->
                Toast.makeText(context, getString(event.messageRes), Toast.LENGTH_SHORT).show()
        }
    }

    OneShotEventEffect(flow = viewModel.navigationEvents) { event ->
        when (event) {
            is ChatNavigationEvent.OpenRoom -> onChatRoomClick(event.roomId)
        }
    }

    ChatListScreen(
        uiState = uiState,
        onTabSelected = viewModel::selectTab,
        onNotificationClick = viewModel::toggleNotification,
        onNotificationBellClick = onNotificationBellClick,
        onLoadNextPage = viewModel::loadNextPage,
        onChatRoomClick = { chatRoom ->
            viewModel.markChatRoomAsRead(chatRoom.id)
            onChatRoomClick(chatRoom.id)
        }
    )
}

/**
 * 그룹 및 개인 채팅방 목록을 표시한다.
 *
 * 상단 공통 헤더와 하단 내비게이션은 다른 화면에서 결합할 수 있도록 포함하지 않는다.
 *
 * @param uiState 채팅 목록 화면 상태
 * @param onTabSelected 탭 선택 동작
 * @param onNotificationClick 알림 아이콘 선택 동작
 * @param onLoadNextPage 목록 끝에 가까워졌을 때 다음 페이지를 불러오는 동작
 * @param onChatRoomClick 채팅방 선택 동작
 */
@Composable
private fun ChatListScreen(
    uiState: ChatListUiState,
    onTabSelected: (ChatListTab) -> Unit,
    onNotificationClick: (Long) -> Unit,
    onNotificationBellClick: () -> Unit,
    onLoadNextPage: (ChatListTab) -> Unit,
    onChatRoomClick: (ChatRoomPreviewUiModel) -> Unit
) {
    val chatRooms =
        when (uiState.selectedTab) {
            ChatListTab.GROUP -> uiState.groupChatRooms
            ChatListTab.DIRECT -> uiState.directChatRooms
        }

    val listState = rememberLazyListState()

    val shouldLoadMore by
        remember(uiState.selectedTab) {
            derivedStateOf {
                val lastVisibleIndex =
                    listState.layoutInfo.visibleItemsInfo
                        .lastOrNull()
                        ?.index ?: 0
                lastVisibleIndex >= chatRooms.size - LOAD_MORE_THRESHOLD
            }
        }

    LaunchedEffect(shouldLoadMore, uiState.selectedTab) {
        if (shouldLoadMore && chatRooms.isNotEmpty()) {
            onLoadNextPage(uiState.selectedTab)
        }
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(ChatBackgroundColor)
                .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        PickiiTopBar(
            notificationCount = uiState.notificationCount,
            onNotificationClick = onNotificationBellClick
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(Res.string.chat_title),
            color = ChatPrimaryColor,
            fontSize = 52.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        ChatTabSelector(
            selectedTab = uiState.selectedTab,
            onTabSelected = onTabSelected
        )

        Spacer(modifier = Modifier.height(20.dp))

        val isInitialLoading =
            when (uiState.selectedTab) {
                ChatListTab.GROUP -> uiState.isGroupLoading
                ChatListTab.DIRECT -> uiState.isDirectLoading
            }

        if (chatRooms.isEmpty()) {
            if (!isInitialLoading) {
                EmptyChatContent()
            }
        } else {
            LazyColumn(
                state = listState,
                verticalArrangement = Arrangement.spacedBy(18.dp),
                contentPadding = PaddingValues(bottom = PickiiBottomNavOverlaySpacing)
            ) {
                items(
                    items = chatRooms,
                    key = ChatRoomPreviewUiModel::id
                ) { chatRoom ->
                    ChatRoomPreviewCard(
                        chatRoom = chatRoom,
                        onNotificationClick = {
                            onNotificationClick(chatRoom.id)
                        },
                        onChatRoomClick = {
                            onChatRoomClick(chatRoom)
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

/**
 * 그룹과 개인 채팅 목록을 전환하는 탭을 표시한다.
 *
 * @param selectedTab 현재 선택된 탭
 * @param onTabSelected 탭 선택 동작
 */
@Composable
private fun ChatTabSelector(
    selectedTab: ChatListTab,
    onTabSelected: (ChatListTab) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ChatTabButton(
            text = stringResource(Res.string.chat_group_tab),
            isSelected = selectedTab == ChatListTab.GROUP,
            onClick = {
                onTabSelected(ChatListTab.GROUP)
            }
        )

        ChatTabButton(
            text = stringResource(Res.string.chat_direct_tab),
            isSelected = selectedTab == ChatListTab.DIRECT,
            onClick = {
                onTabSelected(ChatListTab.DIRECT)
            }
        )
    }
}

/**
 * 채팅 목록의 단일 탭 버튼을 표시한다.
 *
 * @param text 탭에 표시할 문구
 * @param isSelected 선택 여부
 * @param onClick 탭 선택 동작
 */
@Composable
private fun ChatTabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier =
            Modifier
                .background(
                    color =
                        if (isSelected) {
                            ChatPrimaryColor
                        } else {
                            ChatUnselectedTabColor
                        },
                    shape = RoundedCornerShape(30.dp)
                ).clickable(onClick = onClick)
                .padding(
                    horizontal = 26.dp,
                    vertical = 13.dp
                ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color =
                if (isSelected) {
                    Color.White
                } else {
                    ChatSecondaryTextColor
                },
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * 하나의 채팅방 미리보기 카드를 표시한다.
 *
 * @param chatRoom 채팅방 미리보기 정보
 * @param onNotificationClick 알림 아이콘 선택 동작
 * @param onChatRoomClick 채팅방 선택 동작
 */
@Composable
private fun ChatRoomPreviewCard(
    chatRoom: ChatRoomPreviewUiModel,
    onNotificationClick: () -> Unit,
    onChatRoomClick: () -> Unit
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 3.dp,
                    shape = RoundedCornerShape(24.dp),
                    clip = false
                ).clickable(onClick = onChatRoomClick),
        shape = RoundedCornerShape(24.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = Color.White
            )
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 18.dp,
                        vertical = 14.dp
                    ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ChatProfileImage(
                roomType = chatRoom.type
            )

            Spacer(
                modifier = Modifier.width(14.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = chatRoom.roomName,
                        color = ChatPrimaryColor,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(
                        modifier = Modifier.width(8.dp)
                    )

                    Text(
                        text = chatRoom.lastMessageTime,
                        color = ChatSecondaryTextColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(
                    modifier = Modifier.height(5.dp)
                )

                Text(
                    text = chatRoom.lastMessage.ifBlank { stringResource(Res.string.chat_room_no_message) },
                    color = ChatSecondaryTextColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                chatRoom.participantSummary?.let { summary ->
                    Spacer(
                        modifier = Modifier.height(13.dp)
                    )

                    Text(
                        text = summary,
                        color = ChatSecondaryTextColor,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(
                modifier = Modifier.width(10.dp)
            )

            Column(
                modifier = Modifier.height(68.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Image(
                    painter =
                        painterResource(
                            resource =
                                if (chatRoom.isNotificationEnabled) {
                                    Res.drawable.ic_notification_on
                                } else {
                                    Res.drawable.ic_notification_off
                                }
                        ),
                    contentDescription = null,
                    modifier =
                        Modifier
                            .size(18.dp)
                            .clickable(onClick = onNotificationClick)
                )

                if (chatRoom.unreadCount > 0) {
                    UnreadMessageBadge(
                        unreadCount = chatRoom.unreadCount
                    )
                }
            }
        }
    }
}

/**
 * 채팅 카드의 기본 프로필 이미지를 표시한다.
 */
@Composable
private fun ChatProfileImage(roomType: ChatRoomType) {
    when (roomType) {
        ChatRoomType.GROUP -> {
            GroupChatProfileImage()
        }

        else -> {
            DirectChatProfileImage()
        }
    }
}

// 개인채팅
@Composable
private fun DirectChatProfileImage() {
    Box(
        modifier =
            Modifier
                .size(48.dp)
                .background(
                    color = ChatProfileBackgroundColor,
                    shape = CircleShape
                ),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter =
                painterResource(
                    resource = Res.drawable.ic_profile
                ),
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
    }
}

// 그룹채팅
@Composable
private fun GroupChatProfileImage() {
    Box(
        modifier =
            Modifier.size(
                width = 52.dp,
                height = 48.dp
            )
    ) {
        ChatProfileCircle(
            modifier =
                Modifier
                    .align(Alignment.TopStart)
                    .size(32.dp)
        )

        ChatProfileCircle(
            modifier =
                Modifier
                    .align(Alignment.BottomEnd)
                    .size(32.dp)
        )
    }
}

// 공통 프로필 원
@Composable
private fun ChatProfileCircle(modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier
                .background(
                    color = ChatProfileBackgroundColor,
                    shape = CircleShape
                ).border(
                    width = 2.dp,
                    color = Color.White,
                    shape = CircleShape
                ),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter =
                painterResource(
                    resource = Res.drawable.ic_profile
                ),
            contentDescription = null,
            modifier = Modifier.size(16.dp)
        )
    }
}

/**
 * 읽지 않은 메시지 개수를 원형 배지로 표시한다. 0개일 때는 호출하지 않는다(스펙 8-1 6번: 미확인 메시지가
 * 없으면 배지를 표시하지 않는다).
 *
 * @param unreadCount 읽지 않은 메시지 개수
 */
@Composable
private fun UnreadMessageBadge(unreadCount: Int) {
    Box(
        modifier =
            Modifier
                .size(26.dp)
                .background(
                    color = ChatPrimaryColor,
                    shape = CircleShape
                ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = unreadCount.toString(),
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * 표시할 채팅방이 없을 때 빈 화면을 표시한다.
 */
@Composable
private fun EmptyChatContent() {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(top = 100.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(Res.string.chat_empty_list),
            color = ChatSecondaryTextColor,
            fontSize = 16.sp,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
