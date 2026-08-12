package com.example.pickii.ui.chat

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pickii.BuildConfig
import com.example.pickii.R
import com.example.pickii.data.local.SavedMeetingScheduleStore
import com.example.pickii.data.notification.ActiveChatRoomTracker
import com.example.pickii.data.remote.dto.ApiException
import com.example.pickii.data.remote.dto.ChatMessageDto
import com.example.pickii.data.remote.dto.PublishChatMessage
import com.example.pickii.data.remote.socket.ChatConnectionState
import com.example.pickii.data.remote.socket.ChatStompClient
import com.example.pickii.domain.model.ChatMessageContentType
import com.example.pickii.domain.model.ChatRoomDetail
import com.example.pickii.domain.repository.CalendarRepository
import com.example.pickii.domain.repository.ChatRepository
import com.example.pickii.domain.repository.MeetingPollRepository
import com.example.pickii.domain.repository.ProjectRepository
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

/** 발신자 정보가 없는 시스템 메시지(예: 회의 조율 개설 알림)에 표시할 기본 발신자명. */
private const val SYSTEM_SENDER_NICKNAME = "시스템"
private const val ERROR_CODE_LEADER_CANNOT_LEAVE = "LEADER_CANNOT_LEAVE"

/**
 * 채팅방의 메시지 및 사용자 동작 상태를 관리한다.
 */
@HiltViewModel
class ChatRoomViewModel
    @Inject
    constructor(
        private val chatRepository: ChatRepository,
        internal val chatStompClient: ChatStompClient,
        private val sessionRepository: SessionRepository,
        internal val meetingPollRepository: MeetingPollRepository,
        internal val calendarRepository: CalendarRepository,
        private val projectRepository: ProjectRepository,
        private val activeChatRoomTracker: ActiveChatRoomTracker,
        internal val savedMeetingScheduleStore: SavedMeetingScheduleStore
    ) : ViewModel() {
        internal val _uiState = MutableStateFlow(ChatRoomUiState())
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
            activeChatRoomTracker.onRoomEntered(roomId)
            _uiState.value = ChatRoomUiState(roomId = roomId, isLoading = true)

            // 되읽기 API가 없어(ChatRoomUiState.savedMeetingScheduleIds 참고) 기기에 저장해둔 목록으로 채워야
            // 채팅방을 나갔다 다시 들어와도 이미 저장한 일정에 "저장" 버튼이 다시 뜨지 않는다.
            viewModelScope.launch {
                val savedIds = savedMeetingScheduleStore.getSavedIds()
                _uiState.update { it.copy(savedMeetingScheduleIds = it.savedMeetingScheduleIds + savedIds) }
            }

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
                val allFetchedMessages = historyPage?.messages.orEmpty().map { it.toUiModel() }
                val (messages, confirmedMeetings) =
                    allFetchedMessages
                        .filterNot { it.isMeetingPollServerNotice() }
                        .sortedBy { it.createdAt }
                        .partitionConfirmedMeetings()

                _uiState.update { state ->
                    state
                        .applyDetail(detail)
                        .copy(
                            messages = messages,
                            confirmedMeetings = state.confirmedMeetings + confirmedMeetings,
                            nextMessageCursor = historyPage?.nextCursor,
                            hasMoreMessages = historyPage?.hasNext == true,
                            isLoading = false
                        )
                }
                ensurePollDetailsLoaded(messages)

                // 읽음 처리(8-6)는 화면에 보이는 필터링된 messages가 아니라 실제로 가장 최근인 메시지
                // 기준으로 보내야 한다. 회의 조율 시작/확정 시 서버가 자동으로 보내는 SYSTEM 문구는 화면엔
                // 안 보이게 걸러내지만(isMeetingPollServerNotice) 그게 방의 진짜 마지막 메시지인 경우,
                // 필터링된 목록의 마지막 항목으로 읽음 처리하면 그 SYSTEM 메시지 하나가 서버 기준으로는
                // 영원히 안읽음으로 남아 채팅 목록 뱃지가 0으로 안 내려가는 버그가 있었다.
                allFetchedMessages.maxByOrNull { it.createdAt }?.let { latestMessage ->
                    chatRepository.markAsRead(roomId, latestMessage.id)
                }

                loadMeetings()
                loadScheduleCategories()
                connectSocket(roomId)
            }
        }

        /** 화면이 다시 보일 때(ON_RESUME) 확정된 회의 목록을 최신 상태로 갱신한다. */
        fun refreshMeetings() {
            if (_uiState.value.projectId != null) loadMeetings()
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
                        val (newMessages, newConfirmedMeetings) =
                            page.messages
                                .map { it.toUiModel() }
                                .filterNot { it.isMeetingPollServerNotice() }
                                .partitionConfirmedMeetings()
                        _uiState.update { current ->
                            val merged =
                                (current.messages + newMessages)
                                    .distinctBy { it.id }
                                    .sortedBy { it.createdAt }
                            current.copy(
                                messages = merged,
                                confirmedMeetings = current.confirmedMeetings + newConfirmedMeetings,
                                nextMessageCursor = page.nextCursor,
                                hasMoreMessages = page.hasNext,
                                isLoadingMoreMessages = false
                            )
                        }
                        ensurePollDetailsLoaded(newMessages)
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

                    // 서버가 poll 개설/확정 시 자동으로 보내는 안내 문구는 이미 [MeetingProgressCard]와 내용이
                    // 겹쳐서 화면에 중복으로 보이지 않게 걸러낸다. 읽음 처리는 그대로 해서 안 보이는 메시지
                    // 때문에 안 읽음 배지가 남지 않게 한다.
                    if (!newMessage.isMeetingPollServerNotice()) {
                        val confirmed = newMessage.meetingConfirmed
                        if (newMessage.type == ChatMessageType.MEETING_CONFIRMED && confirmed != null) {
                            // 확정 브로드캐스트는 새 메시지로 쌓지 않고 원래 등록공지 카드가 쓸 상태로만 흡수한다.
                            _uiState.update { state ->
                                state.copy(confirmedMeetings = state.confirmedMeetings + (confirmed.pollId to confirmed))
                            }
                        } else {
                            _uiState.update { state ->
                                if (state.messages.any { it.id == newMessage.id }) {
                                    state
                                } else {
                                    state.copy(messages = (state.messages + newMessage).sortedBy { it.createdAt })
                                }
                            }
                            ensurePollDetailsLoaded(listOf(newMessage))
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

        fun registerNotice(content: String) {
            if (content.isBlank()) return

            val registeredAt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())

            _uiState.update { currentState ->
                currentState.copy(
                    noticeContent = content.trim(),
                    noticeWriter =
                        sessionRepository.currentUser.value
                            ?.nickname
                            .orEmpty(),
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
            val projectId = _uiState.value.projectId ?: return
            viewModelScope.launch {
                chatRepository
                    .delegateLeader(projectId, memberId)
                    .onSuccess { refreshDetail(roomId) }
                    .onFailure { emitEvent(RecruitUiEvent.ShowToast(R.string.chat_toast_generic_error)) }
            }
        }

        /**
         * (미확정) 선택한 팀원을 채팅방에서 내보낸다.
         */
        fun removeMember(memberId: Long) {
            val roomId = _uiState.value.roomId
            val projectId = _uiState.value.projectId ?: return
            viewModelScope.launch {
                chatRepository
                    .removeMember(projectId, memberId)
                    .onSuccess { refreshDetail(roomId) }
                    .onFailure { emitEvent(RecruitUiEvent.ShowToast(R.string.chat_toast_generic_error)) }
            }
        }

        /** 프로젝트를 종료한다(6-4). 성공하면 방 상세를 다시 불러와 상태 뱃지("종료")를 반영한다. */
        fun closeProject() {
            val roomId = _uiState.value.roomId
            val projectId = _uiState.value.projectId ?: return
            viewModelScope.launch {
                projectRepository
                    .closeProject(projectId)
                    .onSuccess {
                        refreshDetail(roomId)
                        emitEvent(RecruitUiEvent.ShowToast(R.string.chat_toast_project_closed))
                    }.onFailure { emitEvent(RecruitUiEvent.ShowToast(R.string.chat_toast_generic_error)) }
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
            activeChatRoomTracker.onRoomExited(_uiState.value.roomId)
        }

        private fun refreshDetail(roomId: Long) {
            viewModelScope.launch {
                chatRepository.getChatRoomDetail(roomId).onSuccess { detail ->
                    _uiState.update { it.applyDetail(detail) }
                }
            }
        }

        internal fun emitEvent(event: RecruitUiEvent) {
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
                        isLeader = member.isLeader,
                        exp = member.exp
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
                projectId = detail.projectId,
                roomTitle = detail.title,
                roomType = detail.type,
                memberCount = members.size,
                members = members,
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

        /**
         * 서버가 poll 개설(7-10)/확정(7-13) 시 그룹 채팅방에 자동으로 보내는 SYSTEM 문구인지 판별한다.
         * 이 문구엔 pollId 등 구조화된 정보가 없어([MeetingNoticeMessageCodec] 문서 참고) 클라이언트가
         * 직접 브로드캐스트하는 카드([MeetingProgressCard])로 대체했고, 서버 쪽 원문은 내용이 겹쳐 화면에서
         * 숨긴다. 참석/불참 등 다른 SYSTEM 메시지는 그대로 보여준다.
         */
        private fun ChatMessageUiModel.isMeetingPollServerNotice(): Boolean =
            type == ChatMessageType.TEXT &&
                (content.contains("회의 시간 조율이 시작되었습니다") || content.contains("확정되었습니다"))

        /**
         * [MEETING_CONFIRMED] 타입 메시지를 리스트에서 걷어내고, 그 안의 확정 정보만 pollId 기준으로 뽑아낸다.
         * 확정 안내는 이제 별도 카드가 아니라 원래 회의 등록 카드([MeetingProgressCard])의 내부 상태로 합쳐서
         * 보여주므로, 화면에 보일 리스트에는 남기지 않는다.
         */
        private fun List<ChatMessageUiModel>.partitionConfirmedMeetings():
            Pair<List<ChatMessageUiModel>, Map<Long, MeetingConfirmedUiModel>> {
            val confirmed = mapNotNull { it.meetingConfirmed }.associateBy { it.pollId }
            val remaining = filterNot { it.type == ChatMessageType.MEETING_CONFIRMED }
            return remaining to confirmed
        }

        /** REST 히스토리 조회(8-3)로 얻은 메시지를 화면 표시 모델로 바꾼다. */
        private fun DomainChatMessage.toUiModel(): ChatMessageUiModel {
            val rawContent = text.orEmpty()
            val meetingNotice = decodeMeetingNoticeMessage(rawContent)?.toUiModel(senderNickname)
            val meetingConfirmed = decodeMeetingConfirmedMessage(rawContent)?.toUiModel()
            val directMeeting = decodeDirectMeetingMessage(rawContent)?.toUiModel()
            val isEncoded = meetingNotice != null || meetingConfirmed != null || directMeeting != null
            return ChatMessageUiModel(
                id = messageId,
                senderId = senderId,
                senderNickname = senderNickname,
                senderExp = senderExp,
                content = if (isEncoded) "" else rawContent,
                createdAt = createdAt,
                isMine = senderId == currentMemberId,
                // 상대의 읽음 여부를 알려주는 신호가 REST/WebSocket 어디에도 문서화돼 있지 않아, 허위로
                // "읽음"을 표시하지 않기 위해 항상 false로 둔다(§0-4 알려진 제약).
                isReadByCounterpart = false,
                type =
                    when {
                        meetingNotice != null -> ChatMessageType.MEETING_NOTICE
                        meetingConfirmed != null -> ChatMessageType.MEETING_CONFIRMED
                        directMeeting != null -> ChatMessageType.DIRECT_MEETING
                        type == ChatMessageContentType.IMAGE -> ChatMessageType.IMAGE
                        else -> ChatMessageType.TEXT
                    },
                meetingNotice = meetingNotice,
                meetingConfirmed = meetingConfirmed ?: directMeeting,
                imageUri = imageUrl?.toAbsoluteImageUrl()
            )
        }

        /** WebSocket으로 실시간 수신한 메시지를 화면 표시 모델로 바꾼다. */
        private fun ChatMessageDto.toUiModel(): ChatMessageUiModel {
            val nickname = senderNickname ?: SYSTEM_SENDER_NICKNAME
            val rawContent = message.orEmpty()
            val meetingNotice = decodeMeetingNoticeMessage(rawContent)?.toUiModel(nickname)
            val meetingConfirmed = decodeMeetingConfirmedMessage(rawContent)?.toUiModel()
            val directMeeting = decodeDirectMeetingMessage(rawContent)?.toUiModel()
            val isEncoded = meetingNotice != null || meetingConfirmed != null || directMeeting != null
            return ChatMessageUiModel(
                id = messageId,
                senderId = senderId ?: 0L,
                senderNickname = nickname,
                senderExp = senderExp ?: 0,
                content = if (isEncoded) "" else rawContent,
                createdAt =
                    runCatching {
                        OffsetDateTime
                            .parse(
                                createdAt
                            ).toLocalDateTime()
                    }.getOrDefault(LocalDateTime.now()),
                isMine = senderId == currentMemberId,
                isReadByCounterpart = false,
                type =
                    when {
                        meetingNotice != null -> ChatMessageType.MEETING_NOTICE
                        meetingConfirmed != null -> ChatMessageType.MEETING_CONFIRMED
                        directMeeting != null -> ChatMessageType.DIRECT_MEETING
                        type == "IMAGE" -> ChatMessageType.IMAGE
                        else -> ChatMessageType.TEXT
                    },
                meetingNotice = meetingNotice,
                meetingConfirmed = meetingConfirmed ?: directMeeting,
                imageUri = imageUrl?.toAbsoluteImageUrl()
            )
        }

        private fun DecodedMeetingNotice.toUiModel(requesterName: String): MeetingNoticeUiModel =
            MeetingNoticeUiModel(
                meetingTitle = title,
                requesterName = requesterName,
                pollId = pollId,
                deadlineMillis = deadlineMillis
            )

        private fun DecodedMeetingConfirmed.toUiModel(): MeetingConfirmedUiModel =
            MeetingConfirmedUiModel(
                meetingTitle = title,
                pollId = pollId,
                slotStartMillis = slotStartMillis,
                slotEndMillis = slotEndMillis,
                scheduleId = scheduleId
            )

        // 실제 poll이 없어 pollId가 의미 없다 — DIRECT_MEETING 타입만 보고 다르게 렌더링되므로
        // scheduleId를 채워 넣어도(그 자체로 유일값) 무해하다.
        private fun DecodedDirectMeeting.toUiModel(): MeetingConfirmedUiModel =
            MeetingConfirmedUiModel(
                meetingTitle = title,
                pollId = scheduleId,
                slotStartMillis = slotStartMillis,
                slotEndMillis = slotEndMillis,
                scheduleId = scheduleId
            )
    }

/** 이미지 업로드(8-4) 응답의 `imageUrl`이 `/static-uploads/...`처럼 API_BASE_URL(`.../api/v1/`) 기준
 * 상대경로라서(실제 파일은 `.../api/v1/static-uploads/...`에 있음, origin 바로 아래가 아니다 — 직접
 * curl로 확인함), 실제 로드하려면 API_BASE_URL을 그대로 앞에 붙여야 한다. */
private val ApiBaseUrlNoTrailingSlash: String by lazy { BuildConfig.API_BASE_URL.trimEnd('/') }

/** 서버가 내려준 이미지 경로를 실제 로드 가능한 절대 URL로 바꾼다. 이미 절대 URL이면 그대로 둔다. */
private fun String.toAbsoluteImageUrl(): String =
    if (startsWith("http://") || startsWith("https://")) this else ApiBaseUrlNoTrailingSlash + this
