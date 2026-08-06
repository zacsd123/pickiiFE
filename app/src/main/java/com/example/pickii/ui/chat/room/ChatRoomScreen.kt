package com.example.pickii.ui.chat

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.example.pickii.R
import com.example.pickii.ui.common.OneShotEventEffect
import com.example.pickii.ui.common.RecruitUiEvent
import kotlinx.coroutines.delay

private val ChatBackgroundColor = Color(0xFFF8F9FB)
private val MyMessageColor = Color(0xFF111111)
private val OtherMessageColor = Color(0xFFF0F1F4)
private val InputBackgroundColor = Color(0xFFF1F2F5)
private val SecondaryTextColor = Color(0xFF9CA3AF)
private val PickiiYellowColor = Color(0xFFF9FCA8)
private const val MEETING_DURATION_MILLIS = 2 * 60 * 60 * 1000L
private const val LOAD_MORE_MESSAGES_THRESHOLD = 3

/**
 * 채팅방 상태와 화면을 연결한다.
 */
@Composable
fun ChatRoomRoute(
    roomId: Long,
    onBackClick: () -> Unit,
    onLeaveChatRoom: () -> Unit,
    onNavigateToMemberProfile: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChatRoomViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(roomId) {
        viewModel.initializeRoom(roomId = roomId)
    }

    OneShotEventEffect(flow = viewModel.events) { event ->
        when (event) {
            is RecruitUiEvent.ShowToast ->
                Toast.makeText(context, context.getString(event.messageRes), Toast.LENGTH_SHORT).show()
        }
    }

    OneShotEventEffect(flow = viewModel.navigationEvents) { event ->
        when (event) {
            ChatRoomNavigationEvent.LeftRoom -> onLeaveChatRoom()
        }
    }

    ChatRoomScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onMessageChange = viewModel::updateMessageInput,
        onSendClick = viewModel::sendMessage,
        onAddClick = viewModel::toggleActionMenu,
        onNoticeClick = viewModel::toggleNotice,
        onNoticeRegister = viewModel::registerNotice,
        onMeetingSend = viewModel::sendMeetingNotice,
        onSendImages = viewModel::sendImageMessages,
        onLoadMoreMessages = viewModel::loadMoreMessages,
        modifier = modifier,
        onDelegateLeader = viewModel::delegateLeader,
        onRemoveMember = viewModel::removeMember,
        onNotificationEnabledChange = viewModel::updateNotificationSetting,
        onLeaveChatRoomRequested = viewModel::leaveChatRoom,
        onDeleteMeeting = viewModel::deleteMeeting,
        onNavigateToMemberProfile = onNavigateToMemberProfile
    )
}

/**
 * 채팅방 화면을 표시한다.
 */
@Composable
private fun ChatRoomScreen(
    uiState: ChatRoomUiState,
    onBackClick: () -> Unit,
    onSendClick: () -> Unit,
    onMessageChange: (String) -> Unit,
    onAddClick: () -> Unit,
    onNoticeClick: () -> Unit,
    onNoticeRegister: (String) -> Unit,
    onMeetingSend: (QuickMeetingForm) -> Unit,
    onSendImages: (List<Uri>) -> Unit,
    onLoadMoreMessages: () -> Unit,
    modifier: Modifier = Modifier,
    onDelegateLeader: (Long) -> Unit,
    onRemoveMember: (Long) -> Unit,
    onNotificationEnabledChange: (Boolean) -> Unit,
    onLeaveChatRoomRequested: () -> Unit,
    onDeleteMeeting: (Long) -> Unit,
    onNavigateToMemberProfile: (Long) -> Unit
) {
    val listState = rememberLazyListState()

    val shouldLoadMoreMessages by
        remember {
            derivedStateOf {
                val firstVisibleIndex =
                    listState.layoutInfo.visibleItemsInfo
                        .firstOrNull()
                        ?.index ?: 0
                firstVisibleIndex <= LOAD_MORE_MESSAGES_THRESHOLD
            }
        }

    LaunchedEffect(shouldLoadMoreMessages, uiState.hasMoreMessages) {
        if (shouldLoadMoreMessages && uiState.hasMoreMessages && uiState.messages.isNotEmpty()) {
            onLoadMoreMessages()
        }
    }

    var showQuickMeetingSheet by remember {
        mutableStateOf(false)
    }

    var showNoticeRegistrationSheet by remember {
        mutableStateOf(false)
    }

    var sentMeetingBanner by remember {
        mutableStateOf<QuickMeetingForm?>(null)
    }

    var showMeetingConfirmSheet by remember {
        mutableStateOf(false)
    }

    var meetingToConfirm by remember {
        mutableStateOf<QuickMeetingForm?>(null)
    }

    var isChatRoomInfoPanelVisible by rememberSaveable {
        mutableStateOf(false)
    }

    var isNotificationSettingPanelVisible by rememberSaveable {
        mutableStateOf(false)
    }

    var isMemberListPanelVisible by rememberSaveable {
        mutableStateOf(false)
    }

    var isProjectInfoPanelVisible by rememberSaveable {
        mutableStateOf(false)
    }

    var isLeaderDelegationPanelVisible by rememberSaveable {
        mutableStateOf(false)
    }

    var isMemberRemovalPanelVisible by rememberSaveable {
        mutableStateOf(false)
    }

    var isLeavePanelVisible by rememberSaveable {
        mutableStateOf(false)
    }

    var showMeetingManagementSheet by rememberSaveable {
        mutableStateOf(false)
    }

    var showPhotoSourceSheet by remember {
        mutableStateOf(false)
    }

    var showGalleryPickerSheet by remember {
        mutableStateOf(false)
    }

    var pendingCameraUri by remember {
        mutableStateOf<Uri?>(null)
    }

    LaunchedEffect(sentMeetingBanner) {
        if (sentMeetingBanner != null) {
            delay(3000)
            sentMeetingBanner = null
        }
    }

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.lastIndex)
        }
    }

    val context = LocalContext.current

    val cameraLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.TakePicture()
        ) { isSaved ->
            val capturedUri = pendingCameraUri
            pendingCameraUri = null

            if (isSaved && capturedUri != null) {
                onSendImages(listOf(capturedUri))
            }
        }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(ChatBackgroundColor)
                    .imePadding()
        ) {
            ChatRoomHeader(
                roomTitle = uiState.roomTitle,
                roomType = uiState.roomType,
                onBackClick = onBackClick,
                onMenuClick = {
                    isChatRoomInfoPanelVisible = true
                }
            )

            ChatNotice(
                noticeContent = uiState.noticeContent,
                noticeWriter = uiState.noticeWriter,
                noticeRegisteredAt = uiState.noticeRegisteredAt,
                isExpanded = uiState.isNoticeExpanded,
                onClick = onNoticeClick
            )

            LazyColumn(
                state = listState,
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    count = uiState.messages.size,
                    key = { index -> uiState.messages[index].id }
                ) { index ->
                    val message = uiState.messages[index]
                    val previousMessage = uiState.messages.getOrNull(index - 1)

                    if (previousMessage == null ||
                        previousMessage.createdAt.toLocalDate() != message.createdAt.toLocalDate()
                    ) {
                        ChatDateDivider(dateText = message.createdAt.toChatDateDividerText())
                    }

                    ChatMessageItem(
                        message = message,
                        isLastOfRun = uiState.messages.isLastOfConsecutiveRun(index)
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            AnimatedVisibility(
                visible = uiState.isActionMenuExpanded
            ) {
                ChatActionMenu(
                    onPhotoClick = {
                        showPhotoSourceSheet = true
                    },
                    onNoticeRegisterClick = {
                        showNoticeRegistrationSheet = true
                    },
                    onQuickMeetingClick = {
                        showQuickMeetingSheet = true
                    },
                    onMeetingManagementClick = {
                        showMeetingManagementSheet = true
                    }
                )
            }

            ChatMessageInput(
                message = uiState.messageInput,
                isActionMenuExpanded = uiState.isActionMenuExpanded,
                onMessageChange = onMessageChange,
                onAddClick = onAddClick,
                onSendClick = onSendClick
            )
        }

        if (isChatRoomInfoPanelVisible) {
            ChatRoomInfoPanel(
                uiState = uiState,
                onCloseClick = {
                    isChatRoomInfoPanelVisible = false
                },
                onNotificationSettingClick = {
                    isChatRoomInfoPanelVisible = false
                    isNotificationSettingPanelVisible = true
                },
                onMemberListClick = {
                    isChatRoomInfoPanelVisible = false
                    isMemberListPanelVisible = true
                },
                onProjectInfoClick = {
                    isChatRoomInfoPanelVisible = false
                    isProjectInfoPanelVisible = true
                },
                onLeaderDelegationClick = {
                    isChatRoomInfoPanelVisible = false
                    isLeaderDelegationPanelVisible = true
                },
                onMemberRemovalClick = {
                    isChatRoomInfoPanelVisible = false
                    isMemberRemovalPanelVisible = true
                },
                onLeaveChatRoomClick = {
                    isChatRoomInfoPanelVisible = false
                    isLeavePanelVisible = true
                }
            )
        }

        if (isNotificationSettingPanelVisible) {
            ChatNotificationSettingPanel(
                initialNotificationEnabled = uiState.isNotificationEnabled,
                onEnabledChange = onNotificationEnabledChange,
                onBackClick = {
                    isNotificationSettingPanelVisible = false
                    isChatRoomInfoPanelVisible = true
                }
            )
        }

        if (isMemberListPanelVisible) {
            ChatMemberListPanel(
                members = uiState.members,
                onBackClick = {
                    isMemberListPanelVisible = false
                    isChatRoomInfoPanelVisible = true
                },
                onMemberClick = onNavigateToMemberProfile
            )
        }

        if (isProjectInfoPanelVisible) {
            ChatProjectInfoPanel(
                projectInfo = uiState.projectInfo,
                onBackClick = {
                    isProjectInfoPanelVisible = false
                    isChatRoomInfoPanelVisible = true
                }
            )
        }

        if (isLeaderDelegationPanelVisible) {
            ChatLeaderDelegationPanel(
                members = uiState.members,
                onBackClick = {
                    isLeaderDelegationPanelVisible = false
                    isChatRoomInfoPanelVisible = true
                },
                onDelegateClick = { memberId ->
                    onDelegateLeader(memberId)
                    isLeaderDelegationPanelVisible = false
                    isChatRoomInfoPanelVisible = true
                }
            )
        }

        if (isMemberRemovalPanelVisible) {
            ChatMemberRemovalPanel(
                members = uiState.members,
                onBackClick = {
                    isMemberRemovalPanelVisible = false
                    isChatRoomInfoPanelVisible = true
                },
                onRemoveClick = { memberId ->
                    onRemoveMember(memberId)
                    isMemberRemovalPanelVisible = false
                    isChatRoomInfoPanelVisible = true
                }
            )
        }

        if (isLeavePanelVisible) {
            ChatRoomLeavePanel(
                isCurrentUserLeader = uiState.isCurrentUserLeader,
                onBackClick = {
                    isLeavePanelVisible = false
                    isChatRoomInfoPanelVisible = true
                },
                onLeaveClick = {
                    isLeavePanelVisible = false
                    onLeaveChatRoomRequested()
                },
                onDelegateLeaderClick = {
                    isLeavePanelVisible = false
                    isLeaderDelegationPanelVisible = true
                }
            )
        }

        if (showMeetingManagementSheet) {
            MeetingManagementBottomSheet(
                meetings = uiState.meetings,
                onDismiss = {
                    showMeetingManagementSheet = false
                },
                onDeleteMeeting = onDeleteMeeting,
                onMoveToAbsent = { _, _ ->
                    // TODO: 참여자를 불참으로 변경
                },
                onMoveToParticipant = { _, _ ->
                    // TODO: 불참자를 참여로 변경
                }
            )
        }

        if (showQuickMeetingSheet) {
            MeetingRegistrationBottomSheet(
                onDismiss = {
                    showQuickMeetingSheet = false
                },
                onNextClick = { meeting ->
                    meetingToConfirm = meeting
                    showQuickMeetingSheet = false
                    showMeetingConfirmSheet = true
                }
            )
        }

        if (showNoticeRegistrationSheet) {
            NoticeRegistrationBottomSheet(
                initialContent = uiState.noticeContent,
                onDismiss = {
                    showNoticeRegistrationSheet = false
                },
                onCompleteClick = { content ->
                    onNoticeRegister(content)
                    showNoticeRegistrationSheet = false
                }
            )
        }
        meetingToConfirm?.let { meeting ->
            if (showMeetingConfirmSheet) {
                MeetingConfirmBottomSheet(
                    meeting = meeting,
                    // < 버튼을 눌렀을 때
                    onPreviousClick = {
                        showMeetingConfirmSheet = false
                        showQuickMeetingSheet = true
                    },
                    // 취소 버튼 또는 바텀시트 바깥을 눌렀을 때
                    onCancelClick = {
                        showMeetingConfirmSheet = false
                        showQuickMeetingSheet = false
                        meetingToConfirm = null
                    },
                    // 팀원에게 전송하기 버튼을 눌렀을 때
                    onSendClick = {
                        onMeetingSend(meeting)

                        showMeetingConfirmSheet = false
                        showQuickMeetingSheet = false
                        meetingToConfirm = null
                        sentMeetingBanner = meeting
                    }
                )
            }
        }

        if (showPhotoSourceSheet) {
            PhotoSourceBottomSheet(
                onGalleryClick = {
                    showPhotoSourceSheet = false
                    showGalleryPickerSheet = true
                },
                onCameraClick = {
                    showPhotoSourceSheet = false
                    val captureUri = createImageCaptureUri(context)
                    pendingCameraUri = captureUri
                    cameraLauncher.launch(captureUri)
                },
                onDismiss = {
                    showPhotoSourceSheet = false
                }
            )
        }

        if (showGalleryPickerSheet) {
            GalleryPickerBottomSheet(
                onDismiss = {
                    showGalleryPickerSheet = false
                },
                onConfirm = { uris ->
                    showGalleryPickerSheet = false
                    onSendImages(uris)
                }
            )
        }
    }
}

/**
 * 채팅방 상단 영역을 표시한다.
 */
@Composable
private fun ChatRoomHeader(
    roomTitle: String,
    roomType: ChatRoomType,
    onBackClick: () -> Unit,
    onMenuClick: () -> Unit
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(Color.White)
    ) {
        // 헤더 위쪽 여백 (필요하면 20~40.dp 사이에서 조절)
        Spacer(modifier = Modifier.height(28.dp))

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 8.dp,
                        end = 18.dp,
                        top = 14.dp,
                        bottom = 14.dp
                    ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier =
                    Modifier
                        .size(40.dp)
                        .clickable(onClick = onBackClick),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "‹",
                    fontSize = 34.sp,
                    color = Color(0xFF4B5563)
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            Box(
                modifier =
                    Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE5E7EB)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (roomType == ChatRoomType.GROUP) "G" else "D",
                    color = Color(0xFF6B7280),
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = roomTitle,
                modifier = Modifier.weight(1f),
                color = Color(0xFF111827),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Box(
                modifier =
                    Modifier
                        .size(40.dp)
                        .clickable(onClick = onMenuClick),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter =
                        painterResource(
                            id = R.drawable.ic_chat_room_menu
                        ),
                    contentDescription = "채팅방 정보 열기",
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        HorizontalDivider(color = Color(0xFFE5E7EB))
    }
}

/**
 * 채팅 날짜 구분선을 표시한다.
 */
@Composable
private fun ChatDateDivider(dateText: String) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 20.dp,
                    vertical = 10.dp
                ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = Color(0xFFE5E7EB)
        )

        Text(
            text = dateText,
            modifier = Modifier.padding(horizontal = 14.dp),
            color = SecondaryTextColor,
            fontSize = 11.sp
        )

        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = Color(0xFFE5E7EB)
        )
    }
}

/**
 * 채팅방 공지를 표시한다.
 */
@Composable
private fun ChatNotice(
    noticeContent: String,
    noticeWriter: String,
    noticeRegisteredAt: String,
    isExpanded: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White)
                .clickable(onClick = onClick)
                .padding(
                    horizontal = 12.dp,
                    vertical = 10.dp
                )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.ic_notice),
                contentDescription = "공지",
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "공지",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4B5563)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text =
                    if (noticeContent.isBlank()) {
                        "등록된 공지가 없습니다."
                    } else {
                        noticeContent
                    },
                modifier = Modifier.weight(1f),
                fontSize = 12.sp,
                color = Color(0xFF6B7280),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = if (isExpanded) "⌃" else "⌄",
                color = SecondaryTextColor
            )
        }

        AnimatedVisibility(
            visible = isExpanded && noticeContent.isNotBlank()
        ) {
            Column(
                modifier =
                    Modifier.padding(
                        start = 40.dp,
                        top = 10.dp,
                        end = 8.dp
                    )
            ) {
                Text(
                    text = noticeContent,
                    color = Color(0xFF374151),
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "작성자: $noticeWriter",
                    color = SecondaryTextColor,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "등록 일자: $noticeRegisteredAt",
                    color = SecondaryTextColor,
                    fontSize = 11.sp
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(12.dp))
}

/**
 * 메시지 작성자에 따라 말풍선을 좌우로 배치한다.
 */
@Composable
private fun ChatMessageItem(
    message: ChatMessageUiModel,
    isLastOfRun: Boolean
) {
    when (message.type) {
        ChatMessageType.TEXT -> {
            if (message.isMine) {
                MyChatMessage(
                    message = message,
                    isLastOfRun = isLastOfRun
                )
            } else {
                OtherChatMessage(
                    message = message,
                    isLastOfRun = isLastOfRun
                )
            }
        }

        ChatMessageType.MEETING_NOTICE -> {
            message.meetingNotice?.let { notice ->
                MeetingRegistrationNoticeCard(
                    meetingNotice = notice,
                    onRegisterClick = {
                        // TODO
                    }
                )
            }
        }

        ChatMessageType.IMAGE -> {
            message.imageUri?.let { uri ->
                if (message.isMine) {
                    MyImageMessage(
                        message = message,
                        imageUri = uri,
                        isLastOfRun = isLastOfRun
                    )
                } else {
                    OtherImageMessage(
                        message = message,
                        imageUri = uri,
                        isLastOfRun = isLastOfRun
                    )
                }
            }
        }
    }
}

/**
 * 현재 사용자가 보낸 메시지를 표시한다. 시각/읽음 표시는 연속된 내 메시지 묶음의 마지막에만 보여준다.
 */
@Composable
private fun MyChatMessage(
    message: ChatMessageUiModel,
    isLastOfRun: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.Bottom
    ) {
        if (isLastOfRun) {
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = if (message.isReadByCounterpart) "읽음" else "읽기 전",
                    color = Color(0xFFB4B868),
                    fontSize = 10.sp
                )

                Text(
                    text = message.createdAt.toChatRoomBubbleTimeText(),
                    color = SecondaryTextColor,
                    fontSize = 10.sp
                )
            }

            Spacer(modifier = Modifier.width(6.dp))
        }

        Text(
            text = message.content,
            modifier =
                Modifier
                    .fillMaxWidth(0.72f)
                    .clip(
                        RoundedCornerShape(
                            topStart = 18.dp,
                            topEnd = 18.dp,
                            bottomStart = 18.dp,
                            bottomEnd = 4.dp
                        )
                    ).background(MyMessageColor)
                    .padding(
                        horizontal = 14.dp,
                        vertical = 11.dp
                    ),
            color = Color.White,
            fontSize = 14.sp,
            lineHeight = 19.sp
        )
    }
}

/**
 * 상대방이 보낸 메시지를 표시한다. 시각은 연속된 상대 메시지 묶음의 마지막에만 보여준다.
 */
@Composable
private fun OtherChatMessage(
    message: ChatMessageUiModel,
    isLastOfRun: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom
    ) {
        ChatSenderAvatar(nickname = message.senderNickname)

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = message.content,
            modifier =
                Modifier
                    .fillMaxWidth(0.72f)
                    .clip(
                        RoundedCornerShape(
                            topStart = 18.dp,
                            topEnd = 18.dp,
                            bottomStart = 4.dp,
                            bottomEnd = 18.dp
                        )
                    ).background(OtherMessageColor)
                    .padding(
                        horizontal = 14.dp,
                        vertical = 11.dp
                    ),
            color = Color(0xFF374151),
            fontSize = 14.sp,
            lineHeight = 19.sp
        )

        if (isLastOfRun) {
            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = message.createdAt.toChatRoomBubbleTimeText(),
                color = SecondaryTextColor,
                fontSize = 10.sp
            )
        }
    }
}

/** 상대방 메시지 왼쪽에 보여줄 발신자 아바타. 닉네임 첫 글자를 표시한다. */
@Composable
private fun ChatSenderAvatar(nickname: String) {
    Box(
        modifier =
            Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Color(0xFFE5E7EB)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = nickname.firstOrNull()?.toString().orEmpty(),
            color = Color(0xFF9CA3AF),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

private val ChatImageBubbleSize = 160.dp

/**
 * 현재 사용자가 보낸 사진 메시지를 표시한다.
 */
@Composable
private fun MyImageMessage(
    message: ChatMessageUiModel,
    imageUri: String,
    isLastOfRun: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.Bottom
    ) {
        if (isLastOfRun) {
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = if (message.isReadByCounterpart) "읽음" else "읽기 전",
                    color = Color(0xFFB4B868),
                    fontSize = 10.sp
                )

                Text(
                    text = message.createdAt.toChatRoomBubbleTimeText(),
                    color = SecondaryTextColor,
                    fontSize = 10.sp
                )
            }

            Spacer(modifier = Modifier.width(6.dp))
        }

        AsyncImage(
            model = imageUri,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier =
                Modifier
                    .size(ChatImageBubbleSize)
                    .clip(
                        RoundedCornerShape(
                            topStart = 18.dp,
                            topEnd = 18.dp,
                            bottomStart = 18.dp,
                            bottomEnd = 4.dp
                        )
                    )
        )
    }
}

/**
 * 상대방이 보낸 사진 메시지를 표시한다.
 */
@Composable
private fun OtherImageMessage(
    message: ChatMessageUiModel,
    imageUri: String,
    isLastOfRun: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom
    ) {
        ChatSenderAvatar(nickname = message.senderNickname)

        Spacer(modifier = Modifier.width(8.dp))

        AsyncImage(
            model = imageUri,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier =
                Modifier
                    .size(ChatImageBubbleSize)
                    .clip(
                        RoundedCornerShape(
                            topStart = 18.dp,
                            topEnd = 18.dp,
                            bottomStart = 4.dp,
                            bottomEnd = 18.dp
                        )
                    )
        )

        if (isLastOfRun) {
            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = message.createdAt.toChatRoomBubbleTimeText(),
                color = SecondaryTextColor,
                fontSize = 10.sp
            )
        }
    }
}

/**
 * 파일 및 회의 관련 추가 기능을 표시한다.
 */
data class ChatActionItem(
    @DrawableRes val iconRes: Int,
    val label: String
)

@Composable
private fun ChatActionMenu(
    onPhotoClick: () -> Unit,
    onNoticeRegisterClick: () -> Unit,
    onQuickMeetingClick: () -> Unit,
    onMeetingManagementClick: () -> Unit
) {
    val actionItems =
        listOf(
            ChatActionItem(
                iconRes = R.drawable.ic_file,
                label = "사진/카메라"
            ),
            ChatActionItem(
                iconRes = R.drawable.ic_notice,
                label = "공지 등록"
            ),
            ChatActionItem(
                iconRes = R.drawable.ic_quick_meeting,
                label = "빠른 회의"
            ),
            ChatActionItem(
                iconRes = R.drawable.ic_meeting_schedule,
                label = "회의 조율"
            ),
            ChatActionItem(
                iconRes = R.drawable.ic_meeting_manage,
                label = "회의 관리"
            )
        )

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(
                    horizontal = 16.dp,
                    vertical = 14.dp
                ),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        actionItems.forEachIndexed { index, item ->
            Column(
                modifier =
                    Modifier
                        .weight(1f)
                        .clickable {
                            when (index) {
                                0 -> onPhotoClick()
                                1 -> onNoticeRegisterClick()
                                2 -> onQuickMeetingClick()
                                3 -> println("회의 조율 클릭")
                                4 -> onMeetingManagementClick()
                            }
                        },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFF2F3F6)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = item.iconRes),
                        contentDescription = item.label,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.height(7.dp))

                Text(
                    text = item.label,
                    color = Color(0xFF6B7280),
                    fontSize = 10.sp,
                    maxLines = 1
                )
            }
        }
    }

    HorizontalDivider(color = Color(0xFFE5E7EB))
}

/**
 * 메시지 입력 및 전송 영역을 표시한다.
 */
@Composable
private fun ChatMessageInput(
    message: String,
    isActionMenuExpanded: Boolean,
    onMessageChange: (String) -> Unit,
    onAddClick: () -> Unit,
    onSendClick: () -> Unit
) {
    val isSendEnabled = message.isNotBlank()

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(
                    horizontal = 16.dp,
                    vertical = 12.dp
                ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier =
                Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(
                        if (isActionMenuExpanded) {
                            PickiiYellowColor
                        } else {
                            Color.White
                        }
                    ).clickable(onClick = onAddClick),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isActionMenuExpanded) "−" else "+",
                color = Color(0xFF4B5563),
                fontSize = 25.sp
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        BasicTextField(
            value = message,
            onValueChange = onMessageChange,
            modifier =
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(InputBackgroundColor)
                    .padding(
                        horizontal = 16.dp,
                        vertical = 12.dp
                    ),
            singleLine = true,
            textStyle =
                androidx.compose.ui.text.TextStyle(
                    color = Color(0xFF111827),
                    fontSize = 14.sp
                ),
            keyboardOptions =
                KeyboardOptions(
                    imeAction = ImeAction.Send
                ),
            keyboardActions =
                KeyboardActions(
                    onSend = {
                        if (isSendEnabled) {
                            onSendClick()
                        }
                    }
                ),
            decorationBox = { innerTextField ->
                if (message.isEmpty()) {
                    Text(
                        text = "메시지를 입력하세요",
                        color = SecondaryTextColor,
                        fontSize = 14.sp
                    )
                }

                innerTextField()
            }
        )

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier =
                Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSendEnabled) {
                            Color(0xFF202428)
                        } else {
                            Color(0xFFD1D5DB)
                        }
                    ).clickable(
                        enabled = isSendEnabled,
                        onClick = onSendClick
                    ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "↑",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun MeetingRegistrationNoticeCard(
    meetingNotice: MeetingNoticeUiModel,
    onRegisterClick: () -> Unit
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFFF9FCA8))
                .padding(
                    horizontal = 24.dp,
                    vertical = 24.dp
                ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "[회의 등록 공지]",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        Text(
            text = "${meetingNotice.requesterName}님이 회의를 요청했어요.",
            fontSize = 14.sp,
            color = Color.Black
        )

        Spacer(
            modifier = Modifier.height(4.dp)
        )

        Text(
            text = "캘린더에 개인 일정을 등록해 주세요.",
            fontSize = 14.sp,
            color = Color.Black
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        var remainingTime by remember {
            mutableStateOf("02:00:00")
        }

        LaunchedEffect(meetingNotice.createdTimeMillis) {
            while (true) {
                val remain =
                    MEETING_DURATION_MILLIS -
                        (System.currentTimeMillis() - meetingNotice.createdTimeMillis)

                if (remain <= 0L) {
                    remainingTime = "00:00:00"
                    break
                }

                val hour = remain / 1000 / 3600
                val minute = (remain / 1000 % 3600) / 60
                val second = remain / 1000 % 60

                remainingTime =
                    "%02d:%02d:%02d".format(hour, minute, second)

                delay(1000)
            }
        }

        Text(
            text = "남은 시간: $remainingTime",
            fontSize = 14.sp,
            color = Color(0xFF7486D8)
        )

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .clickable {
                        onRegisterClick()
                    },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text =
                    if (meetingNotice.isRegistered) {
                        "등록 완료"
                    } else {
                        "등록했어요"
                    },
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
    }
}
