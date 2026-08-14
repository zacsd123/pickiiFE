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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pickii.R
import com.example.pickii.domain.model.MeetingPollDetail
import com.example.pickii.ui.common.ConfirmDialog
import com.example.pickii.ui.common.OneShotEventEffect
import com.example.pickii.ui.common.RecruitUiEvent
import com.example.pickii.ui.theme.PickiiDivider
import com.example.pickii.ui.theme.PickiiGray400
import com.example.pickii.ui.theme.PickiiGray650
import com.example.pickii.ui.theme.PickiiGray700
import com.example.pickii.ui.theme.PickiiNavyTextDark
import com.example.pickii.ui.theme.PickiiSurfaceGraySoft
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalTime
import androidx.compose.material3.Icon
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.size


private val ChatBackgroundColor = Color(0xFFF8F9FB)
private val InputBackgroundColor = Color(0xFFF1F2F5)
private val PickiiYellowColor = Color(0xFFF9FCA8)
private const val LOAD_MORE_MESSAGES_THRESHOLD = 3
private const val NEAR_BOTTOM_THRESHOLD = 2

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
        onCloseProjectClick = viewModel::closeProject,
        onSaveMeetingToMyCalendar = viewModel::onSaveMeetingToMyCalendar,
        onQuickMeetingSheetOpen = viewModel::loadMyCalendarSchedulesForMeetingPicker
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
    onSelectProjectColor: (Long?) -> Unit,
    onCloseProjectClick: () -> Unit,
    onSaveMeetingToMyCalendar: (MeetingConfirmedUiModel) -> Unit,
    onQuickMeetingSheetOpen: () -> Unit
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
        if (
            shouldLoadMoreMessages &&
            uiState.hasMoreMessages &&
            !uiState.isLoadingMoreMessages &&
            uiState.messages.isNotEmpty()
        ) {
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

    // 회의 조율 날짜 선택기가 이미 등록된 날짜를 비활성화할 수 있도록, 시트가 열릴 때(최초 진입/"이전" 되돌아오기
    // 둘 다) 내 캘린더 일정을 미리 불러온다.
    LaunchedEffect(activeSheet) {
        if (activeSheet == ChatRoomSheet.QuickMeeting) {
            onQuickMeetingSheetOpen()
        }
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

    // messages.size 변화 하나만으로는 과거 로그 prepend(loadMoreMessages)와 새 메시지 append(소켓 수신,
    // 내가 보낸 메시지 echo 포함)를 구분할 수 없다. 이전 프레임의 첫/마지막 메시지 id를 기억해뒀다가 비교해서
    // 셋을 구분한다: prepend면 스크롤하지 않는다(LazyColumn이 key = { messages[index].id }로 안정적 키를
    // 쓰고 있어서 위에 아이템이 추가돼도 보던 위치가 자동 유지된다) — 예전엔 여기서 무조건 맨 아래로
    // 스크롤시켜서, 위로 스크롤해 과거 로그를 불러올 때마다 다시 맨 아래로 튕기고 그게 "맨 위 근처" 로드
    // 조건을 재충족시켜 전체 히스토리를 연쇄로 다 불러와버리는 버그가 있었다.
    var previousFirstMessageId by remember { mutableStateOf<String?>(null) }
    var previousLastMessageId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(uiState.messages.size) {
        val messages = uiState.messages
        if (messages.isEmpty()) {
            previousFirstMessageId = null
            previousLastMessageId = null
            return@LaunchedEffect
        }

        val newFirstId = messages.first().id
        val newLastId = messages.last().id
        val isInitialLoad = previousFirstMessageId == null
        val isTailAppend =
            !isInitialLoad && newFirstId == previousFirstMessageId && newLastId != previousLastMessageId

        when {
            isInitialLoad -> listState.scrollToItem(messages.lastIndex)

            isTailAppend -> {
                val appendedMessage = messages.last()
                val lastVisibleIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                val wasNearBottom = messages.lastIndex - lastVisibleIndex <= NEAR_BOTTOM_THRESHOLD
                if (appendedMessage.isMine || wasNearBottom) {
                    listState.animateScrollToItem(messages.lastIndex)
                }
            }

            // 과거 로그가 앞에 붙은 경우(또는 첫/마지막이 동시에 바뀐 모호한 경우) — 스크롤하지 않는다.
            else -> Unit
        }

        previousFirstMessageId = newFirstId
        previousLastMessageId = newLastId
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
                personalChatMemberName = uiState.personalChatMemberName,
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
                        isFirstOfRun = uiState.messages.isFirstOfConsecutiveRun(index),
                        isLastOfRun = uiState.messages.isLastOfConsecutiveRun(index),
                        pollDetail = uiState.pollDetails[message.meetingNotice?.pollId],
                        confirmedMeeting = uiState.confirmedMeetings[message.meetingNotice?.pollId],
                        isAcknowledged = message.meetingNotice?.pollId in uiState.acknowledgedPollIds,
                        myPollSelection = uiState.myPollSelections[message.meetingNotice?.pollId].orEmpty(),
                        isCurrentUserLeader = uiState.isCurrentUserLeader,
                        participantNames = uiState.members.map { it.name },
                        savedMeetingScheduleIds = uiState.savedMeetingScheduleIds,
                        onAcknowledgeMeetingNotice = onAcknowledgeMeetingNotice,
                        onToggleMeetingPollSlot = onToggleMeetingPollSlot,
                        onToggleMeetingPollNoneAvailable = onToggleMeetingPollNoneAvailable,
                        onSubmitMeetingPollResponse = onSubmitMeetingPollResponse,
                        onConfirmSlotClick = onConfirmSlotClick,
                        onCancelMeetingPoll = onCancelMeetingPoll,
                        onSaveMeetingToMyCalendar = onSaveMeetingToMyCalendar
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            val isChatWriteDisabled =
                uiState.roomType == ChatRoomType.GROUP && uiState.projectStatus == ProjectStatus.END

            if (isChatWriteDisabled) {
                ChatMessageInputDisabled()
            } else {
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
                isCurrentUserLeader = uiState.isCurrentUserLeader,
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
                registeredSchedules = uiState.myCalendarSchedules,
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
    personalChatMemberName: String,
    onBackClick: () -> Unit,
    onMenuClick: () -> Unit
) {
    val displayTitle = if (roomType == ChatRoomType.DIRECT && personalChatMemberName.isNotBlank()) {
        personalChatMemberName
    } else {
        roomTitle
    }
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
                text = displayTitle,
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

            Icon(
                painter = painterResource(
                    id = if (isExpanded) {
                        R.drawable.ic_chevron_up
                    } else {
                        R.drawable.ic_chevron_down
                    }
                ),
                contentDescription = if (isExpanded) "접기" else "펼치기",
                tint = PickiiGray400,
                modifier = Modifier.size(20.dp)
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
 * 메시지 작성자에 따라 말풍선을 좌우로 배치한다. 회의 조율 관련 타입([ChatMessageType.MEETING_NOTICE])은
 * [ChatRoomUiState.pollDetails]/[ChatRoomUiState.confirmedMeetings] 등 라이브 상태를 받아 카드 하나
 * ([MeetingProgressCard])의 내부 내용을 등록공지→응답→집계→확정(또는 취소)으로 갱신한다.
 */
@Suppress("LongParameterList")
@Composable
private fun ChatMessageItem(
    message: ChatMessageUiModel,
    isFirstOfRun: Boolean,
    isLastOfRun: Boolean,
    pollDetail: MeetingPollDetail?,
    confirmedMeeting: MeetingConfirmedUiModel?,
    isAcknowledged: Boolean,
    myPollSelection: Set<Long>,
    isCurrentUserLeader: Boolean,
    participantNames: List<String>,
    savedMeetingScheduleIds: Set<Long>,
    onAcknowledgeMeetingNotice: (Long) -> Unit,
    onToggleMeetingPollSlot: (Long, Long) -> Unit,
    onToggleMeetingPollNoneAvailable: (Long) -> Unit,
    onSubmitMeetingPollResponse: (Long) -> Unit,
    onConfirmSlotClick: (Long, Long) -> Unit,
    onCancelMeetingPoll: (Long) -> Unit,
    onSaveMeetingToMyCalendar: (MeetingConfirmedUiModel) -> Unit
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
                    isFirstOfRun = isFirstOfRun,
                    isLastOfRun = isLastOfRun
                )
            }
        }

        ChatMessageType.MEETING_NOTICE -> {
            message.meetingNotice?.let { notice ->
                MeetingProgressCard(
                    meetingNotice = notice,
                    pollDetail = pollDetail,
                    isAcknowledged = isAcknowledged,
                    myPollSelection = myPollSelection,
                    confirmedMeeting = confirmedMeeting,
                    participantNames = participantNames,
                    isCurrentUserLeader = isCurrentUserLeader,
                    isSaved = confirmedMeeting?.scheduleId?.let { it in savedMeetingScheduleIds } == true,
                    onAcknowledgeClick = { onAcknowledgeMeetingNotice(notice.pollId) },
                    onToggleSlot = { slotId -> onToggleMeetingPollSlot(notice.pollId, slotId) },
                    onToggleNoneAvailable = { onToggleMeetingPollNoneAvailable(notice.pollId) },
                    onSubmitClick = { onSubmitMeetingPollResponse(notice.pollId) },
                    onCancelClick = { onCancelMeetingPoll(notice.pollId) },
                    onConfirmClick = { slotId -> onConfirmSlotClick(notice.pollId, slotId) },
                    onSaveClick = { confirmedMeeting?.let(onSaveMeetingToMyCalendar) }
                )
            }
        }

        // ChatRoomViewModel이 이 타입 메시지를 uiState.messages에 절대 넣지 않는다(confirmedMeetings로 흡수).
        ChatMessageType.MEETING_CONFIRMED -> Unit

        ChatMessageType.DIRECT_MEETING -> {
            message.meetingConfirmed?.let { confirmed ->
                DirectMeetingCard(
                    meetingConfirmed = confirmed,
                    participantNames = participantNames,
                    isSaved = confirmed.scheduleId in savedMeetingScheduleIds,
                    onSaveClick = { onSaveMeetingToMyCalendar(confirmed) }
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
                        isFirstOfRun = isFirstOfRun,
                        isLastOfRun = isLastOfRun
                    )
                }
            }
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
                    iconRes = R.drawable.ic_meeting_manage,
                    label = "회의 관리",
                    onClick = onMeetingManagementClick
                )
            )
            // 회의 조율 개설(7-10)·직접 등록(7-16)은 서버가 프로젝트장 전용으로 강제한다(비-리더가 호출하면
            // 403). 버튼 자체를 숨겨서 팀원이 눌렀다가 애매한 에러 토스트만 보는 일이 없게 한다.
            if (isCurrentUserLeader) {
                add(
                    ChatActionItem(
                        iconRes = R.drawable.ic_meeting_schedule,
                        label = "회의 일정 잡기",
                        onClick = onQuickMeetingClick
                    )
                )
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
 * 종료된 프로젝트의 채팅방에서 입력창 대신 보여주는 안내 바. 더 이상 채팅을 작성할 수 없음을 알린다.
 */
@Composable
private fun ChatMessageInputDisabled() {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(
                    horizontal = 16.dp,
                    vertical = 16.dp
                ),
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = "종료된 프로젝트예요. 더 이상 채팅을 보낼 수 없어요.",
            color = PickiiGray400,
            fontSize = 14.sp
        )
    }
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
