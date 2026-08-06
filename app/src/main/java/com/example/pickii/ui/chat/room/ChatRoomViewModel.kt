package com.example.pickii.ui.chat

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pickii.R
import com.example.pickii.data.remote.dto.ApiException
import com.example.pickii.data.remote.dto.ChatMessageDto
import com.example.pickii.data.remote.dto.PublishChatMessage
import com.example.pickii.data.remote.socket.ChatConnectionState
import com.example.pickii.data.remote.socket.ChatStompClient
import com.example.pickii.domain.model.ChatMessageContentType
import com.example.pickii.domain.model.ChatRoomDetail
import com.example.pickii.domain.repository.ChatRepository
import com.example.pickii.domain.repository.SessionRepository
import com.example.pickii.ui.common.RecruitUiEvent
import com.example.pickii.util.toDisplayString
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import com.example.pickii.domain.model.ChatMessage as DomainChatMessage

private const val MESSAGE_PAGE_SIZE = 20
private const val ERROR_CODE_LEADER_CANNOT_LEAVE = "LEADER_CANNOT_LEAVE"

/**
 * 채팅방의 메시지 및 사용자 동작 상태를 관리한다.
 *
 * 회의(회의 등록/관리)·공지 기능은 8번 API 명세서에 없어 이번 연동 범위 밖이다 — [sendMeetingNotice],
 * [registerNotice], [deleteMeeting], [createMockMeetings]는 기존처럼 로컬 목업 그대로 둔다.
 */
@HiltViewModel
class ChatRoomViewModel
    @Inject
    constructor(
        private val chatRepository: ChatRepository,
        private val chatStompClient: ChatStompClient,
        private val sessionRepository: SessionRepository
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(ChatRoomUiState())
        val uiState: StateFlow<ChatRoomUiState> = _uiState.asStateFlow()

        private val _events = Channel<RecruitUiEvent>(Channel.BUFFERED)
        val events: Flow<RecruitUiEvent> = _events.receiveAsFlow()

        private val _navigationEvents = Channel<ChatRoomNavigationEvent>(Channel.BUFFERED)
        val navigationEvents: Flow<ChatRoomNavigationEvent> = _navigationEvents.receiveAsFlow()

        private val currentMemberId: Long?
            get() =
                sessionRepository.currentUser.value
                    ?.id
                    ?.toLongOrNull()

        /**
         * 진입한 채팅방의 상세 정보와 최근 메시지를 서버에서 불러오고 실시간 연결을 시작한다.
         * 방 제목/종류도 항상 이 상세 조회로 채운다(목록 화면을 거치지 않고 딥링크로 들어와도 동작하도록).
         */
        fun initializeRoom(roomId: Long) {
            if (_uiState.value.roomId == roomId) return
            _uiState.value = ChatRoomUiState(roomId = roomId, isLoading = true)

            viewModelScope.launch {
                val detail =
                    chatRepository.getChatRoomDetail(roomId).getOrNull()
                if (detail == null) {
                    _uiState.update { it.copy(isLoading = false) }
                    emitEvent(RecruitUiEvent.ShowToast(R.string.chat_toast_generic_error))
                    return@launch
                }

                val historyPage =
                    chatRepository
                        .getMessages(
                            roomId,
                            cursor = null,
                            size = MESSAGE_PAGE_SIZE
                        ).getOrNull()
                val messages =
                    historyPage
                        ?.messages
                        .orEmpty()
                        .map { it.toUiModel() }
                        .sortedBy { it.createdAt }

                _uiState.update { state ->
                    state
                        .applyDetail(detail)
                        .copy(
                            messages = messages,
                            nextMessageCursor = historyPage?.nextCursor,
                            hasMoreMessages = historyPage?.hasNext == true,
                            isLoading = false,
                            meetings = createMockMeetings()
                        )
                }

                messages.lastOrNull()?.let { lastMessage ->
                    chatRepository.markAsRead(roomId, lastMessage.id)
                }

                connectSocket(roomId)
            }
        }

        /** 스크롤을 위로 올렸을 때 이전 메시지를 커서 기반으로 이어서 불러온다. */
        fun loadMoreMessages() {
            val state = _uiState.value
            val cursor = state.nextMessageCursor
            if (!state.hasMoreMessages || state.isLoadingMoreMessages || cursor == null) return

            _uiState.update { it.copy(isLoadingMoreMessages = true) }
            viewModelScope.launch {
                chatRepository
                    .getMessages(state.roomId, cursor = cursor, size = MESSAGE_PAGE_SIZE)
                    .onSuccess { page ->
                        _uiState.update { current ->
                            val merged =
                                (current.messages + page.messages.map { it.toUiModel() })
                                    .distinctBy { it.id }
                                    .sortedBy { it.createdAt }
                            current.copy(
                                messages = merged,
                                nextMessageCursor = page.nextCursor,
                                hasMoreMessages = page.hasNext,
                                isLoadingMoreMessages = false
                            )
                        }
                    }.onFailure {
                        _uiState.update { it.copy(isLoadingMoreMessages = false) }
                        emitEvent(RecruitUiEvent.ShowToast(R.string.chat_room_toast_load_more_failed))
                    }
            }
        }

        /**
         * 방에 연결하고, 새 메시지 수신 시 화면에 반영한다. 상대가 보낸 메시지를 보고 있는 동안 실시간으로
         * 읽음 처리를 발행한다(REST 8-6과 같은 로직, WebSocket 경로).
         */
        private fun connectSocket(roomId: Long) {
            viewModelScope.launch { chatStompClient.connect(roomId) }

            viewModelScope.launch {
                chatStompClient.connectionState.collect { state ->
                    if (state == ChatConnectionState.FAILED) {
                        emitEvent(RecruitUiEvent.ShowToast(R.string.chat_toast_connection_failed))
                    }
                }
            }

            viewModelScope.launch {
                chatStompClient.incomingMessages.collect { dto ->
                    if (_uiState.value.roomId != roomId) return@collect
                    val newMessage = dto.toUiModel()

                    _uiState.update { state ->
                        if (state.messages.any { it.id == newMessage.id }) {
                            state
                        } else {
                            state.copy(messages = (state.messages + newMessage).sortedBy { it.createdAt })
                        }
                    }

                    if (!newMessage.isMine) {
                        chatStompClient.sendRead(roomId, newMessage.id)
                    }
                }
            }

            viewModelScope.launch {
                chatStompClient.incomingErrors.collect {
                    emitEvent(RecruitUiEvent.ShowToast(R.string.chat_toast_generic_error))
                }
            }
        }

        /**
         * 메시지 입력값을 변경한다.
         */
        fun updateMessageInput(message: String) {
            _uiState.update { currentState ->
                currentState.copy(
                    messageInput = message
                )
            }
        }

        /**
         * 추가 기능 메뉴를 열거나 닫는다.
         */
        fun toggleActionMenu() {
            _uiState.update { currentState ->
                currentState.copy(
                    isActionMenuExpanded = !currentState.isActionMenuExpanded
                )
            }
        }

        /**
         * 채팅방 공지를 열거나 닫는다.
         */
        fun toggleNotice() {
            _uiState.update { currentState ->
                currentState.copy(
                    isNoticeExpanded = !currentState.isNoticeExpanded
                )
            }
        }

        /** 입력한 텍스트 메시지를 WebSocket으로 발행한다. 실제 말풍선은 서버가 되돌려주는 메시지로 렌더링된다. */
        fun sendMessage() {
            val roomId = _uiState.value.roomId
            val content = _uiState.value.messageInput.trim()
            if (content.isBlank()) return

            _uiState.update { it.copy(messageInput = "", isActionMenuExpanded = false) }

            viewModelScope.launch {
                chatStompClient.sendMessage(roomId, PublishChatMessage(type = "TEXT", message = content))
            }
        }

        /**
         * 갤러리에서 고르거나 카메라로 찍은 사진을 업로드한 뒤 WebSocket으로 이미지 메시지를 전송한다.
         * 허용되지 않는 형식이거나 10MB를 초과하면 안내 토스트를 보여준다(8-4 Validation).
         */
        fun sendImageMessages(uris: List<Uri>) {
            if (uris.isEmpty()) return
            val roomId = _uiState.value.roomId
            _uiState.update { it.copy(isActionMenuExpanded = false) }

            viewModelScope.launch {
                uris.forEach { uri ->
                    chatRepository
                        .uploadImage(roomId, uri)
                        .onSuccess { imageUrl ->
                            chatStompClient.sendMessage(roomId, PublishChatMessage(type = "IMAGE", imageUrl = imageUrl))
                        }.onFailure {
                            emitEvent(RecruitUiEvent.ShowToast(R.string.chat_toast_send_invalid_file))
                        }
                }
            }
        }

        /**
         * 회의 등록 공지를 채팅방 메시지 목록에 추가한다(로컬 목업 — 회의 기능은 이번 연동 범위 밖).
         */
        fun sendMeetingNotice(meeting: QuickMeetingForm) {
            val meetingMessage =
                ChatMessageUiModel(
                    id = System.currentTimeMillis().toString(),
                    content = "",
                    createdAt = LocalDateTime.now(),
                    isMine = false,
                    type = ChatMessageType.MEETING_NOTICE,
                    meetingNotice =
                        MeetingNoticeUiModel(
                            meetingTitle = meeting.title,
                            requesterName = "팀장 (닉네임)",
                            createdTimeMillis = System.currentTimeMillis(),
                            isRegistered = false
                        )
                )

            _uiState.update { currentState ->
                currentState.copy(
                    messages = currentState.messages + meetingMessage,
                    isActionMenuExpanded = false
                )
            }
        }

        /**
         * 선택한 회의를 예정된 회의 목록에서 삭제한다(로컬 목업).
         *
         * @param meetingId 삭제할 회의의 ID
         */
        fun deleteMeeting(meetingId: Long) {
            _uiState.value =
                _uiState.value.copy(
                    meetings =
                        _uiState.value.meetings.filterNot { meeting ->
                            meeting.id == meetingId
                        }
                )
        }

        fun registerNotice(content: String) {
            if (content.isBlank()) return

            val registeredAt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())

            _uiState.update { currentState ->
                currentState.copy(
                    noticeContent = content.trim(),
                    // 현재는 임시 사용자 이름(회의/공지 기능은 이번 연동 범위 밖)
                    noticeWriter = "김민서",
                    noticeRegisteredAt = registeredAt,
                    isNoticeExpanded = false,
                    isActionMenuExpanded = false
                )
            }
        }

        /**
         * (미확정) 선택한 팀원에게 팀장 권한을 위임한다.
         */
        fun delegateLeader(memberId: Long) {
            val roomId = _uiState.value.roomId
            viewModelScope.launch {
                chatRepository
                    .delegateLeader(roomId, memberId)
                    .onSuccess { refreshDetail(roomId) }
                    .onFailure { emitEvent(RecruitUiEvent.ShowToast(R.string.chat_toast_generic_error)) }
            }
        }

        /**
         * (미확정) 선택한 팀원을 채팅방에서 내보낸다.
         */
        fun removeMember(memberId: Long) {
            val roomId = _uiState.value.roomId
            viewModelScope.launch {
                chatRepository
                    .removeMember(roomId, memberId)
                    .onSuccess { refreshDetail(roomId) }
                    .onFailure { emitEvent(RecruitUiEvent.ShowToast(R.string.chat_toast_generic_error)) }
            }
        }

        /** 채팅방 나가기를 시도한다. 성공하면 [ChatRoomNavigationEvent.LeftRoom]을 발행해 목록으로 돌아가게 한다. */
        fun leaveChatRoom() {
            val roomId = _uiState.value.roomId
            viewModelScope.launch {
                chatRepository
                    .leaveChatRoom(roomId)
                    .onSuccess { _navigationEvents.send(ChatRoomNavigationEvent.LeftRoom) }
                    .onFailure { error ->
                        val messageRes =
                            if (error is ApiException && error.code == ERROR_CODE_LEADER_CANNOT_LEAVE) {
                                R.string.chat_toast_leader_must_delegate
                            } else {
                                R.string.chat_toast_generic_error
                            }
                        emitEvent(RecruitUiEvent.ShowToast(messageRes))
                    }
            }
        }

        /** 이 채팅방의 알림 수신 여부를 서버에 반영한다(낙관적 갱신 후 실패 시 되돌린다). */
        fun updateNotificationSetting(enabled: Boolean) {
            val roomId = _uiState.value.roomId
            val previous = _uiState.value.isNotificationEnabled
            _uiState.update { it.copy(isNotificationEnabled = enabled) }

            viewModelScope.launch {
                chatRepository.updateNotification(roomId, enabled).onFailure {
                    _uiState.update { it.copy(isNotificationEnabled = previous) }
                    emitEvent(RecruitUiEvent.ShowToast(R.string.chat_toast_generic_error))
                }
            }
        }

        override fun onCleared() {
            super.onCleared()
            chatStompClient.disconnectAsync()
        }

        private fun refreshDetail(roomId: Long) {
            viewModelScope.launch {
                chatRepository.getChatRoomDetail(roomId).onSuccess { detail ->
                    _uiState.update { it.applyDetail(detail) }
                }
            }
        }

        private fun emitEvent(event: RecruitUiEvent) {
            viewModelScope.launch { _events.send(event) }
        }

        /**
         * 채팅방 상세 조회 결과를 상태에 반영한다.
         *
         * 상세 응답(8-2)에는 방별 알림 설정(notiEnabled)이 내려오지 않아, 서버 문서상 기본값인 true를 쓴다
         * (알려진 API 제약 — 목록에서 들어온 경우와 값이 다를 수 있다).
         */
        private fun ChatRoomUiState.applyDetail(detail: ChatRoomDetail): ChatRoomUiState {
            val members =
                detail.members.map { member ->
                    ChatRoomMemberUiModel(
                        memberId = member.memberId,
                        name = member.nickname,
                        isLeader = member.isLeader
                    )
                }
            val leader = members.firstOrNull { it.isLeader }
            val counterpartName =
                if (detail.type == ChatRoomType.DIRECT) {
                    members.firstOrNull { it.memberId != currentMemberId }?.name.orEmpty()
                } else {
                    ""
                }

            return copy(
                roomTitle = detail.title,
                roomType = detail.type,
                memberCount = members.size,
                members = members,
                leaderName = leader?.name.orEmpty(),
                personalChatMemberName = counterpartName,
                isNotificationEnabled = true,
                isCurrentUserLeader = detail.isCurrentUserLeader,
                projectStatus = detail.projectStatus ?: projectStatus,
                projectInfo =
                    if (detail.type == ChatRoomType.GROUP) {
                        ChatProjectInfoUiModel(
                            projectTitle = detail.title,
                            startDate = detail.startDate?.toDisplayString().orEmpty(),
                            endDate = detail.endDate?.toDisplayString().orEmpty(),
                            memberCount = members.size,
                            leaderName = leader?.name.orEmpty(),
                            projectStatus = detail.projectStatus ?: ProjectStatus.IN_PROGRESS
                        )
                    } else {
                        projectInfo
                    }
            )
        }

        /** REST 히스토리 조회(8-3)로 얻은 메시지를 화면 표시 모델로 바꾼다. */
        private fun DomainChatMessage.toUiModel(): ChatMessageUiModel =
            ChatMessageUiModel(
                id = messageId,
                senderId = senderId,
                senderNickname = senderNickname,
                content = text.orEmpty(),
                createdAt = createdAt,
                isMine = senderId == currentMemberId,
                // 상대의 읽음 여부를 알려주는 신호가 REST/WebSocket 어디에도 문서화돼 있지 않아, 허위로
                // "읽음"을 표시하지 않기 위해 항상 false로 둔다(§0-4 알려진 제약).
                isReadByCounterpart = false,
                type = if (type == ChatMessageContentType.IMAGE) ChatMessageType.IMAGE else ChatMessageType.TEXT,
                imageUri = imageUrl
            )

        /** WebSocket으로 실시간 수신한 메시지를 화면 표시 모델로 바꾼다. */
        private fun ChatMessageDto.toUiModel(): ChatMessageUiModel =
            ChatMessageUiModel(
                id = messageId,
                senderId = senderId,
                senderNickname = senderNickname,
                content = message.orEmpty(),
                createdAt =
                    runCatching {
                        OffsetDateTime
                            .parse(
                                createdAt
                            ).toLocalDateTime()
                    }.getOrDefault(LocalDateTime.now()),
                isMine = senderId == currentMemberId,
                isReadByCounterpart = false,
                type = if (type == "IMAGE") ChatMessageType.IMAGE else ChatMessageType.TEXT,
                imageUri = imageUrl
            )

        /**
         * 채팅방 회의 관리 화면 확인용 임시 데이터를 생성한다(로컬 목업 — 회의 기능은 이번 연동 범위 밖).
         */
        private fun createMockMeetings(): List<ManagedMeetingUiModel> =
            listOf(
                ManagedMeetingUiModel(
                    id = 1L,
                    title = "회의1",
                    date = "2026.07.10",
                    startTime = "14:00",
                    endTime = "15:30",
                    participants =
                        listOf(
                            MeetingMemberUiModel(1L, "김승민"),
                            MeetingMemberUiModel(2L, "김은지")
                        ),
                    absentees =
                        listOf(
                            MeetingMemberUiModel(3L, "최윤진")
                        )
                ),
                ManagedMeetingUiModel(
                    id = 2L,
                    title = "회의2(최대 15자)",
                    date = "2026.07.10",
                    startTime = "14:00",
                    endTime = "15:30",
                    participants =
                        listOf(
                            MeetingMemberUiModel(4L, "김승민"),
                            MeetingMemberUiModel(5L, "김은지")
                        ),
                    absentees =
                        listOf(
                            MeetingMemberUiModel(6L, "조승완"),
                            MeetingMemberUiModel(7L, "김민서"),
                            MeetingMemberUiModel(8L, "최윤진")
                        )
                )
            )
    }
