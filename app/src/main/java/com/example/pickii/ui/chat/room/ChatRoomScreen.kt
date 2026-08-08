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
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.example.pickii.R
import com.example.pickii.domain.model.MeetingPollDetail
import com.example.pickii.domain.model.MeetingPollSlot
import com.example.pickii.domain.model.MeetingPollStatus
import com.example.pickii.ui.common.ConfirmDialog
import com.example.pickii.ui.common.OneShotEventEffect
import com.example.pickii.ui.common.RecruitUiEvent
import com.example.pickii.ui.theme.PickiiDivider
import com.example.pickii.ui.theme.PickiiGray400
import com.example.pickii.ui.theme.PickiiGray650
import com.example.pickii.ui.theme.PickiiGray700
import com.example.pickii.ui.theme.PickiiInk
import com.example.pickii.ui.theme.PickiiNavyTextDark
import com.example.pickii.ui.theme.PickiiSurfaceGraySoft
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val ChatBackgroundColor = Color(0xFFF8F9FB)
private val MyMessageColor = Color(0xFF111111)
private val OtherMessageColor = Color(0xFFF9FCA8)
private val InputBackgroundColor = Color(0xFFF1F2F5)
private val PickiiYellowColor = Color(0xFFF9FCA8)
private const val LOAD_MORE_MESSAGES_THRESHOLD = 3

/** 오른쪽에서 슬라이드인하는 채팅방 정보/설정 패널 종류(상호 배타적). */
private enum class ChatRoomPanel {
    NONE,
    INFO,
    NOTIFICATION_SETTING,
    MEMBER_LIST,
    PROJECT_INFO,
    LEADER_DELEGATION,
    MEMBER_REMOVAL,
    LEAVE
}

/** 아래에서 올라오는 채팅방 바텀시트 종류(상호 배타적). */
private sealed interface ChatRoomSheet {
    data object None : ChatRoomSheet

    data object QuickMeeting : ChatRoomSheet

    data object NoticeRegistration : ChatRoomSheet

    data class MeetingConfirm(
        val meeting: QuickMeetingForm
    ) : ChatRoomSheet

    data object MeetingManagement : ChatRoomSheet

    data object DirectRegister : ChatRoomSheet

    data object PhotoSource : ChatRoomSheet

    data object GalleryPicker : ChatRoomSheet
}

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
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(roomId) {
        viewModel.initializeRoom(roomId = roomId)
    }

    DisposableEffect(lifecycleOwner) {
        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    viewModel.refreshMeetings()
                }
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
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
        onMeetingSend = viewModel::createMeetingPoll,
        onSendImages = viewModel::sendImageMessages,
        onLoadMoreMessages = viewModel::loadMoreMessages,
        modifier = modifier,
        onDelegateLeader = viewModel::delegateLeader,
        onRemoveMember = viewModel::removeMember,
        onNotificationEnabledChange = viewModel::updateNotificationSetting,
        onLeaveChatRoomRequested = viewModel::leaveChatRoom,
        onDeleteMeeting = viewModel::deleteMeeting,
        onAttendMeeting = { meetingId -> viewModel.updateMeetingAttendance(meetingId, attending = true) },
        onAbsentMeeting = { meetingId -> viewModel.updateMeetingAttendance(meetingId, attending = false) },
        onNavigateToMemberProfile = onNavigateToMemberProfile,
        onAcknowledgeMeetingNotice = viewModel::onAcknowledgeMeetingNotice,
        onToggleMeetingPollSlot = viewModel::toggleMeetingPollSlot,
        onToggleMeetingPollNoneAvailable = viewModel::toggleMeetingPollNoneAvailable,
        onSubmitMeetingPollResponse = viewModel::submitMeetingPollResponse,
        onConfirmSlotClick = viewModel::onConfirmSlotClick,
        onForceConfirmConfirm = viewModel::onForceConfirmConfirm,
        onForceConfirmDismiss = viewModel::onForceConfirmDismiss,
        onCancelMeetingPoll = viewModel::cancelMeetingPoll,
        onRegisterScheduleDirectly = viewModel::registerScheduleDirectly,
        onSelectProjectColor = viewModel::onSelectProjectColor,
        onCloseProjectClick = viewModel::closeProject
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
    onAttendMeeting: (Long) -> Unit,
    onAbsentMeeting: (Long) -> Unit,
    onNavigateToMemberProfile: (Long) -> Unit,
    onAcknowledgeMeetingNotice: (Long) -> Unit,
    onToggleMeetingPollSlot: (Long, Long) -> Unit,
    onToggleMeetingPollNoneAvailable: (Long) -> Unit,
    onSubmitMeetingPollResponse: (Long) -> Unit,
    onConfirmSlotClick: (Long, Long) -> Unit,
    onForceConfirmConfirm: () -> Unit,
    onForceConfirmDismiss: () -> Unit,
    onCancelMeetingPoll: (Long) -> Unit,
    onRegisterScheduleDirectly: (String, LocalDate, LocalTime, LocalTime) -> Unit,
    onSelectProjectColor: (Long) -> Unit,
    onCloseProjectClick: () -> Unit
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

    var sentMeetingBanner by remember {
        mutableStateOf<QuickMeetingForm?>(null)
    }

    /** 오른쪽에서 슬라이드인하는 정보/설정 패널 중 현재 열린 것(상호 배타적, 하나만 열린다). */
    var activePanel by rememberSaveable { mutableStateOf(ChatRoomPanel.NONE) }

    /** 아래에서 올라오는 바텀시트 중 현재 열린 것(상호 배타적, 하나만 열린다). */
    var activeSheet by remember { mutableStateOf<ChatRoomSheet>(ChatRoomSheet.None) }

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
                    activePanel = ChatRoomPanel.INFO
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
                        isLastOfRun = uiState.messages.isLastOfConsecutiveRun(index),
                        pollDetail = uiState.pollDetails[message.meetingNotice?.pollId],
                        isAcknowledged = message.meetingNotice?.pollId in uiState.acknowledgedPollIds,
                        myPollSelection = uiState.myPollSelections[message.meetingNotice?.pollId].orEmpty(),
                        isCurrentUserLeader = uiState.isCurrentUserLeader,
                        participantNames = uiState.members.map { it.name },
                        onAcknowledgeMeetingNotice = onAcknowledgeMeetingNotice,
                        onToggleMeetingPollSlot = onToggleMeetingPollSlot,
                        onToggleMeetingPollNoneAvailable = onToggleMeetingPollNoneAvailable,
                        onSubmitMeetingPollResponse = onSubmitMeetingPollResponse,
                        onConfirmSlotClick = onConfirmSlotClick,
                        onCancelMeetingPoll = onCancelMeetingPoll
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
                    isCurrentUserLeader = uiState.isCurrentUserLeader,
                    onPhotoClick = {
                        activeSheet = ChatRoomSheet.PhotoSource
                    },
                    onNoticeRegisterClick = {
                        activeSheet = ChatRoomSheet.NoticeRegistration
                    },
                    onQuickMeetingClick = {
                        activeSheet = ChatRoomSheet.QuickMeeting
                    },
                    onMeetingManagementClick = {
                        activeSheet = ChatRoomSheet.MeetingManagement
                    },
                    onDirectRegisterClick = {
                        activeSheet = ChatRoomSheet.DirectRegister
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

        if (activePanel == ChatRoomPanel.INFO) {
            ChatRoomInfoPanel(
                uiState = uiState,
                onCloseClick = {
                    activePanel = ChatRoomPanel.NONE
                },
                onNotificationSettingClick = {
                    activePanel = ChatRoomPanel.NOTIFICATION_SETTING
                },
                onMemberListClick = {
                    activePanel = ChatRoomPanel.MEMBER_LIST
                },
                onProjectInfoClick = {
                    activePanel = ChatRoomPanel.PROJECT_INFO
                },
                onLeaderDelegationClick = {
                    activePanel = ChatRoomPanel.LEADER_DELEGATION
                },
                onMemberRemovalClick = {
                    activePanel = ChatRoomPanel.MEMBER_REMOVAL
                },
                onLeaveChatRoomClick = {
                    activePanel = ChatRoomPanel.LEAVE
                }
            )
        }

        if (activePanel == ChatRoomPanel.NOTIFICATION_SETTING) {
            ChatNotificationSettingPanel(
                initialNotificationEnabled = uiState.isNotificationEnabled,
                onEnabledChange = onNotificationEnabledChange,
                onBackClick = {
                    activePanel = ChatRoomPanel.INFO
                }
            )
        }

        if (activePanel == ChatRoomPanel.MEMBER_LIST) {
            ChatMemberListPanel(
                members = uiState.members,
                onBackClick = {
                    activePanel = ChatRoomPanel.INFO
                },
                onMemberClick = onNavigateToMemberProfile
            )
        }

        if (activePanel == ChatRoomPanel.PROJECT_INFO) {
            ChatProjectInfoPanel(
                projectInfo = uiState.projectInfo,
                scheduleCategories = uiState.scheduleCategories,
                selectedCategoryId = uiState.selectedProjectCategoryId,
                onBackClick = {
                    activePanel = ChatRoomPanel.INFO
                },
                onSelectColor = onSelectProjectColor,
                isCurrentUserLeader = uiState.isCurrentUserLeader,
                onCloseProjectClick = onCloseProjectClick
            )
        }

        if (activePanel == ChatRoomPanel.LEADER_DELEGATION) {
            ChatLeaderDelegationPanel(
                members = uiState.members,
                onBackClick = {
                    activePanel = ChatRoomPanel.INFO
                },
                onDelegateClick = { memberId ->
                    onDelegateLeader(memberId)
                    activePanel = ChatRoomPanel.INFO
                }
            )
        }

        if (activePanel == ChatRoomPanel.MEMBER_REMOVAL) {
            ChatMemberRemovalPanel(
                members = uiState.members,
                onBackClick = {
                    activePanel = ChatRoomPanel.INFO
                },
                onRemoveClick = { memberId ->
                    onRemoveMember(memberId)
                    activePanel = ChatRoomPanel.INFO
                }
            )
        }

        if (activePanel == ChatRoomPanel.LEAVE) {
            ChatRoomLeavePanel(
                isCurrentUserLeader = uiState.isCurrentUserLeader,
                onBackClick = {
                    activePanel = ChatRoomPanel.INFO
                },
                onLeaveClick = {
                    activePanel = ChatRoomPanel.NONE
                    onLeaveChatRoomRequested()
                },
                onDelegateLeaderClick = {
                    activePanel = ChatRoomPanel.LEADER_DELEGATION
                }
            )
        }

        if (activeSheet == ChatRoomSheet.MeetingManagement) {
            MeetingManagementBottomSheet(
                meetings = uiState.meetings,
                onDismiss = {
                    activeSheet = ChatRoomSheet.None
                },
                onDeleteMeeting = onDeleteMeeting,
                onAttendClick = onAttendMeeting,
                onAbsentClick = onAbsentMeeting
            )
        }

        if (activeSheet == ChatRoomSheet.QuickMeeting) {
            MeetingRegistrationBottomSheet(
                members = uiState.members,
                onDismiss = {
                    activeSheet = ChatRoomSheet.None
                },
                onNextClick = { meeting ->
                    activeSheet = ChatRoomSheet.MeetingConfirm(meeting)
                }
            )
        }

        if (activeSheet == ChatRoomSheet.DirectRegister) {
            MeetingDirectRegisterBottomSheet(
                onDismiss = { activeSheet = ChatRoomSheet.None },
                onRegisterClick = { title, date, startTime, endTime ->
                    onRegisterScheduleDirectly(title, date, startTime, endTime)
                    activeSheet = ChatRoomSheet.None
                }
            )
        }

        if (activeSheet == ChatRoomSheet.NoticeRegistration) {
            NoticeRegistrationBottomSheet(
                initialContent = uiState.noticeContent,
                onDismiss = {
                    activeSheet = ChatRoomSheet.None
                },
                onCompleteClick = { content ->
                    onNoticeRegister(content)
                    activeSheet = ChatRoomSheet.None
                }
            )
        }
        (activeSheet as? ChatRoomSheet.MeetingConfirm)?.let { sheet ->
            MeetingConfirmBottomSheet(
                meeting = sheet.meeting,
                totalMemberCount = uiState.members.size,
                // < 버튼을 눌렀을 때
                onPreviousClick = {
                    activeSheet = ChatRoomSheet.QuickMeeting
                },
                // 취소 버튼 또는 바텀시트 바깥을 눌렀을 때
                onCancelClick = {
                    activeSheet = ChatRoomSheet.None
                },
                // 팀원에게 전송하기 버튼을 눌렀을 때
                onSendClick = {
                    onMeetingSend(sheet.meeting)

                    activeSheet = ChatRoomSheet.None
                    sentMeetingBanner = sheet.meeting
                }
            )
        }

        if (activeSheet == ChatRoomSheet.PhotoSource) {
            PhotoSourceBottomSheet(
                onGalleryClick = {
                    activeSheet = ChatRoomSheet.GalleryPicker
                },
                onCameraClick = {
                    activeSheet = ChatRoomSheet.None
                    val captureUri = createImageCaptureUri(context)
                    pendingCameraUri = captureUri
                    cameraLauncher.launch(captureUri)
                },
                onDismiss = {
                    activeSheet = ChatRoomSheet.None
                }
            )
        }

        if (activeSheet == ChatRoomSheet.GalleryPicker) {
            GalleryPickerBottomSheet(
                onDismiss = {
                    activeSheet = ChatRoomSheet.None
                },
                onConfirm = { uris ->
                    activeSheet = ChatRoomSheet.None
                    onSendImages(uris)
                }
            )
        }
    }

    if (uiState.pendingForceConfirm != null) {
        ConfirmDialog(
            title = "아직 응답하지 않은 팀원이 있어요",
            body = "그래도 이 시간으로 확정할까요?",
            confirmLabel = "확정",
            dismissLabel = "취소",
            onConfirm = onForceConfirmConfirm,
            onDismiss = onForceConfirmDismiss
        )
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
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    tint = Color.Black
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            Box(
                modifier =
                    Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(PickiiDivider),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (roomType == ChatRoomType.GROUP) "G" else "D",
                    color = PickiiGray650,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = roomTitle,
                modifier = Modifier.weight(1f),
                color = PickiiNavyTextDark,
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

        HorizontalDivider(color = PickiiDivider)
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
            color = PickiiDivider
        )

        Text(
            text = dateText,
            modifier = Modifier.padding(horizontal = 14.dp),
            color = PickiiGray400,
            fontSize = 11.sp
        )

        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = PickiiDivider
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
                color = PickiiGray700
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
                color = PickiiGray650,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = if (isExpanded) "⌃" else "⌄",
                color = PickiiGray400
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
                    color = PickiiGray400,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "등록 일자: $noticeRegisteredAt",
                    color = PickiiGray400,
                    fontSize = 11.sp
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(12.dp))
}

/**
 * 메시지 작성자에 따라 말풍선을 좌우로 배치한다. 회의 조율 관련 타입은 [ChatRoomUiState.pollDetails] 등
 * 라이브 poll 상태를 받아 등록 공지(카드1) 아래에 응답 카드(카드2)/집계 카드(카드3)를 이어서 그린다.
 */
@Suppress("LongParameterList")
@Composable
private fun ChatMessageItem(
    message: ChatMessageUiModel,
    isLastOfRun: Boolean,
    pollDetail: MeetingPollDetail?,
    isAcknowledged: Boolean,
    myPollSelection: Set<Long>,
    isCurrentUserLeader: Boolean,
    participantNames: List<String>,
    onAcknowledgeMeetingNotice: (Long) -> Unit,
    onToggleMeetingPollSlot: (Long, Long) -> Unit,
    onToggleMeetingPollNoneAvailable: (Long) -> Unit,
    onSubmitMeetingPollResponse: (Long) -> Unit,
    onConfirmSlotClick: (Long, Long) -> Unit,
    onCancelMeetingPoll: (Long) -> Unit
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
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    MeetingRegistrationNoticeCard(
                        meetingNotice = notice,
                        isAcknowledged = isAcknowledged,
                        onAcknowledgeClick = { onAcknowledgeMeetingNotice(notice.pollId) }
                    )

                    if (isAcknowledged && pollDetail != null) {
                        // 3단계(집계)는 전원 응답 또는 마감 시각 도달 시 자동으로 열린다. 서버엔 이 상태를
                        // 별도로 표시하는 필드가 없어(status는 COLLECTING/CONFIRMED/CANCELLED뿐) 클라이언트가
                        // 매초 다시 계산한다.
                        var nowMillis by remember(notice.pollId) { mutableStateOf(System.currentTimeMillis()) }
                        LaunchedEffect(notice.pollId, notice.deadlineMillis) {
                            while (true) {
                                nowMillis = System.currentTimeMillis()
                                if (nowMillis >= notice.deadlineMillis) break
                                delay(1000)
                            }
                        }
                        val isAggregationReady =
                            pollDetail.respondedCount >= pollDetail.totalMembers || nowMillis >= notice.deadlineMillis

                        MeetingPollResponseCard(
                            poll = pollDetail,
                            deadlineMillis = notice.deadlineMillis,
                            mySelection = myPollSelection,
                            isCurrentUserLeader = isCurrentUserLeader,
                            onToggleSlot = { slotId -> onToggleMeetingPollSlot(notice.pollId, slotId) },
                            onToggleNoneAvailable = { onToggleMeetingPollNoneAvailable(notice.pollId) },
                            onSubmitClick = { onSubmitMeetingPollResponse(notice.pollId) },
                            onCancelClick = { onCancelMeetingPoll(notice.pollId) }
                        )

                        if (isAggregationReady) {
                            MeetingPollAggregationCard(
                                poll = pollDetail,
                                isCurrentUserLeader = isCurrentUserLeader,
                                onConfirmClick = { slotId -> onConfirmSlotClick(notice.pollId, slotId) }
                            )
                        }
                    }
                }
            }
        }

        ChatMessageType.MEETING_CONFIRMED -> {
            message.meetingConfirmed?.let { confirmed ->
                MeetingConfirmedNoticeCard(meetingConfirmed = confirmed, participantNames = participantNames)
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
                    color = PickiiGray400,
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
                color = PickiiGray400,
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
                .background(PickiiDivider),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = nickname.firstOrNull()?.toString().orEmpty(),
            color = PickiiGray400,
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
                    color = PickiiGray400,
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
                color = PickiiGray400,
                fontSize = 10.sp
            )
        }
    }
}

/**
 * 파일 및 회의 관련 추가 기능을 표시한다.
 */
private data class ChatActionItem(
    @DrawableRes val iconRes: Int,
    val label: String,
    val onClick: () -> Unit
)

@Composable
private fun ChatActionMenu(
    isCurrentUserLeader: Boolean,
    onPhotoClick: () -> Unit,
    onNoticeRegisterClick: () -> Unit,
    onQuickMeetingClick: () -> Unit,
    onMeetingManagementClick: () -> Unit,
    onDirectRegisterClick: () -> Unit
) {
    val actionItems =
        buildList {
            add(ChatActionItem(iconRes = R.drawable.ic_file, label = "사진/카메라", onClick = onPhotoClick))
            add(ChatActionItem(iconRes = R.drawable.ic_notice, label = "공지 등록", onClick = onNoticeRegisterClick))
            add(
                ChatActionItem(
                    iconRes = R.drawable.ic_meeting_schedule,
                    label = "회의 일정 잡기",
                    onClick = onQuickMeetingClick
                )
            )
            add(
                ChatActionItem(
                    iconRes = R.drawable.ic_meeting_manage,
                    label = "회의 관리",
                    onClick = onMeetingManagementClick
                )
            )
            if (isCurrentUserLeader) {
                add(
                    ChatActionItem(
                        iconRes = R.drawable.ic_meeting_schedule,
                        label = "회의 직접 등록",
                        onClick = onDirectRegisterClick
                    )
                )
            }
        }

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
        actionItems.forEach { item ->
            Column(
                modifier =
                    Modifier
                        .weight(1f)
                        .clickable(onClick = item.onClick),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(PickiiSurfaceGraySoft),
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
                    color = PickiiGray650,
                    fontSize = 10.sp,
                    maxLines = 1
                )
            }
        }
    }

    HorizontalDivider(color = PickiiDivider)
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
                color = PickiiGray700,
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
                    color = PickiiNavyTextDark,
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
                        color = PickiiGray400,
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
    isAcknowledged: Boolean,
    onAcknowledgeClick: () -> Unit
) {
    MeetingCardContainer {
        Text(
            text = "[회의 등록 공지]",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "팀장 (${meetingNotice.requesterName})이 ${meetingNotice.pollId} 회의를 요청했어요",
            fontSize = 14.sp,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "캘린더에 개인 일정을 모두 등록해주세요",
            fontSize = 14.sp,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(16.dp))

        var remainingTime by remember {
            mutableStateOf("00:00:00")
        }

        LaunchedEffect(meetingNotice.deadlineMillis) {
            while (true) {
                val remain = meetingNotice.deadlineMillis - System.currentTimeMillis()

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

        Spacer(modifier = Modifier.height(20.dp))

        MeetingCardButton(
            label = "등록했어요",
            enabled = !isAcknowledged,
            onClick = onAcknowledgeClick
        )
    }
}

/** 카드1~3이 공유하는 노란 라운드 카드 컨테이너. */
@Composable
private fun MeetingCardContainer(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFFF9FCA8))
                .padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        content = content
    )
}

/** 카드1~3이 공유하는 흰색 알약형 액션 버튼. 눌린 뒤에는(enabled = false) 옅게 표시한다. */
@Composable
private fun MeetingCardButton(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White)
                .alpha(if (enabled) 1f else 0.5f)
                .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
    }
}

private val SlotDateFormatter = DateTimeFormatter.ofPattern("M월 d일 (E)", java.util.Locale.KOREAN)
private val SlotTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

/** 카드3(집계)에서 프로젝트장에게 "확정" 후보로 보여줄 상위 슬롯 개수(가능 인원 많은 순). */
private const val MAX_CONFIRM_CANDIDATES = 2

/** 카드2/3이 공유하는 선택 가능한 옵션 행. [selected]면 어두운 배경으로 강조한다. */
@Composable
private fun MeetingPollOptionRow(
    label: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(if (selected) PickiiInk else Color.White)
                .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier)
                .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else Color.Black,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * 카드2([회의 일정 조율]). 7-11의 전체 슬롯 그리드를 날짜별로 세로 나열한다(API 문서가 "후보를 자르지
 * 않는다"고 명시함). 기본은 전부 가능(흰색)이고, 탭해서 불가로 표시한 슬롯만 어둡게 강조한다. 상단에
 * 응답 현황(N/M명)과 마감 카운트다운을 보여주고, 프로젝트장에게는 "조율 취소"(7-14)를 함께 노출한다.
 */
@Suppress("LongParameterList")
@Composable
private fun MeetingPollResponseCard(
    poll: MeetingPollDetail,
    deadlineMillis: Long,
    mySelection: Set<Long>,
    isCurrentUserLeader: Boolean,
    onToggleSlot: (Long) -> Unit,
    onToggleNoneAvailable: () -> Unit,
    onSubmitClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    val isCollecting = poll.status == MeetingPollStatus.COLLECTING
    val allSlotIds = poll.slots.map { it.slotId }.toSet()
    val isNoneAvailableSelected = allSlotIds.isNotEmpty() && mySelection == allSlotIds
    val slotsByDate = poll.slots.groupBy { it.startAt.toLocalDate() }.toSortedMap()

    var remainingTime by remember { mutableStateOf("00:00:00") }
    LaunchedEffect(deadlineMillis) {
        while (true) {
            val remain = deadlineMillis - System.currentTimeMillis()
            if (remain <= 0L) {
                remainingTime = "00:00:00"
                break
            }
            val hour = remain / 1000 / 3600
            val minute = (remain / 1000 % 3600) / 60
            val second = remain / 1000 % 60
            remainingTime = "%02d:%02d:%02d".format(hour, minute, second)
            delay(1000)
        }
    }

    MeetingCardContainer {
        Text(text = "[회의 일정 조율]", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "가능한 시간을 모두 선택해 주세요.\n(복수 선택 가능)",
            fontSize = 13.sp,
            color = Color.Black,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "${poll.respondedCount}/${poll.totalMembers}명 응답 · 마감까지 $remainingTime",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF7486D8)
        )
        if (isCurrentUserLeader && isCollecting) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "조율 취소",
                fontSize = 12.sp,
                color = PickiiGray400,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.clickable(onClick = onCancelClick)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            MeetingPollOptionRow(
                label = "회의 가능한 날짜 없음",
                selected = isNoneAvailableSelected,
                enabled = isCollecting,
                onClick = onToggleNoneAvailable
            )

            slotsByDate.forEach { (date, slots) ->
                Text(
                    text = date.format(SlotDateFormatter),
                    color = PickiiGray650,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                slots.forEach { slot ->
                    MeetingPollOptionRow(
                        label =
                            "${slot.startAt.format(SlotTimeFormatter)}~${slot.endAt.format(SlotTimeFormatter)} " +
                                "· ${slot.availableCount}명",
                        selected = slot.slotId in mySelection,
                        enabled = isCollecting,
                        onClick = { onToggleSlot(slot.slotId) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        MeetingCardButton(label = "제출하기", enabled = isCollecting, onClick = onSubmitClick)
    }
}

/**
 * 카드3(집계, [회의 일정 조율] 다음 단계). 전원 응답 또는 마감 시각 도달 시 자동으로 나타난다(트리거는
 * [ChatMessageItem]에서 계산). 전체 슬롯을 참여 가능 인원 비율로 히트맵 표시하고(진할수록 가능 인원 많음),
 * 칸마다 "가능 N / 미응답 M"을 보여준다. 프로젝트장에게만 가능 인원 많은 순 후보 + "확정"(7-13) 버튼을
 * 추가로 보여준다 — 실제 팀 투표 API는 없고(7-13은 프로젝트장 단독 확정만 지원) 자동 확정도 하지 않는다.
 */
@Composable
private fun MeetingPollAggregationCard(
    poll: MeetingPollDetail,
    isCurrentUserLeader: Boolean,
    onConfirmClick: (Long) -> Unit
) {
    val slotsByDate = poll.slots.groupBy { it.startAt.toLocalDate() }.toSortedMap()
    val rankedCandidates =
        poll.slots
            .sortedWith(compareByDescending<MeetingPollSlot> { it.availableCount }.thenBy { it.unansweredCount })
            .take(MAX_CONFIRM_CANDIDATES)

    MeetingCardContainer {
        Text(text = "[최종 일정 조율]", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "응답이 마감됐어요. 참여 가능한 인원이 많을수록 진하게 표시돼요.",
            fontSize = 13.sp,
            color = Color.Black,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))

        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            slotsByDate.forEach { (date, slots) ->
                Text(
                    text = date.format(SlotDateFormatter),
                    color = PickiiGray650,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                slots.forEach { slot ->
                    MeetingPollHeatmapRow(slot = slot, totalMembers = poll.totalMembers)
                }
            }
        }

        if (isCurrentUserLeader && poll.status == MeetingPollStatus.COLLECTING && rankedCandidates.isNotEmpty()) {
            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = Color(0x33000000))
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "가능 인원 많은 순 — 하나를 골라 확정하세요",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(12.dp))
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                rankedCandidates.forEach { slot ->
                    MeetingPollConfirmRow(slot = slot, onConfirmClick = { onConfirmClick(slot.slotId) })
                }
            }
        }
    }
}

/** 히트맵 한 칸. [totalMembers] 대비 [MeetingPollSlot.availableCount] 비율로 배경 농도를 정한다. */
@Composable
private fun MeetingPollHeatmapRow(
    slot: MeetingPollSlot,
    totalMembers: Int
) {
    val ratio = if (totalMembers > 0) (slot.availableCount.toFloat() / totalMembers).coerceIn(0f, 1f) else 0f
    val backgroundColor = lerp(Color.White, Color(0xFF4C6FFF), ratio)
    val textColor = if (ratio > 0.55f) Color.White else Color.Black

    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(backgroundColor)
                .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Text(
            text =
                "${slot.startAt.format(SlotTimeFormatter)}~${slot.endAt.format(SlotTimeFormatter)} " +
                    "· 가능 ${slot.availableCount} / 미응답 ${slot.unansweredCount}",
            color = textColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

/** 프로젝트장 전용 확정 후보 행. */
@Composable
private fun MeetingPollConfirmRow(
    slot: MeetingPollSlot,
    onConfirmClick: () -> Unit
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text =
                    "${slot.startAt.toLocalDate().format(SlotDateFormatter)} " +
                        "${slot.startAt.format(SlotTimeFormatter)}~${slot.endAt.format(SlotTimeFormatter)}",
                color = Color.Black,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "가능 ${slot.availableCount}명 · 미응답 ${slot.unansweredCount}명",
                color = PickiiGray650,
                fontSize = 11.sp
            )
        }

        Box(
            modifier =
                Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(PickiiInk)
                    .clickable(onClick = onConfirmClick)
                    .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Text(text = "확정", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

/** 카드4([회의 일정 확정]). 읽기 전용 — 확정된 슬롯 시각과 채팅방 멤버 전체 이름을 보여준다. */
@Composable
private fun MeetingConfirmedNoticeCard(
    meetingConfirmed: MeetingConfirmedUiModel,
    participantNames: List<String>
) {
    val start =
        Instant.ofEpochMilli(meetingConfirmed.slotStartMillis).atZone(ZoneId.systemDefault()).toLocalDateTime()
    val end =
        Instant.ofEpochMilli(meetingConfirmed.slotEndMillis).atZone(ZoneId.systemDefault()).toLocalDateTime()

    MeetingCardContainer {
        Text(text = "[회의 일정 확정]", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "회의 일정이 확정되었어요. 일정을 수정할 경우, 회의 관리에서 할 수 있어요.",
            fontSize = 13.sp,
            color = Color.Black,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(PickiiInk)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text =
                        "${start.toLocalDate().format(SlotDateFormatter)} " +
                            "${start.format(SlotTimeFormatter)}~${end.format(SlotTimeFormatter)}",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                if (participantNames.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "참여자: ${participantNames.joinToString(", ")}",
                        color = Color(0xFFB0B0AA),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
