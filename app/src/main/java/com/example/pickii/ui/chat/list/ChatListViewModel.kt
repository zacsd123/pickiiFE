package com.example.pickii.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pickii.R
import com.example.pickii.domain.model.ChatRoomSummary
import com.example.pickii.domain.repository.ChatRepository
import com.example.pickii.domain.repository.NotificationRepository
import com.example.pickii.ui.common.RecruitUiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val CHAT_LIST_PAGE_SIZE = 10

/**
 * 채팅 목록 화면의 상태와 사용자 동작을 관리한다.
 */
@HiltViewModel
class ChatListViewModel
    @Inject
    constructor(
        private val chatRepository: ChatRepository,
        private val notificationRepository: NotificationRepository
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(ChatListUiState())
        val uiState: StateFlow<ChatListUiState> = _uiState.asStateFlow()

        private val _events = Channel<RecruitUiEvent>(Channel.BUFFERED)
        val events: Flow<RecruitUiEvent> = _events.receiveAsFlow()

        private val _navigationEvents = Channel<ChatNavigationEvent>(Channel.BUFFERED)
        val navigationEvents: Flow<ChatNavigationEvent> = _navigationEvents.receiveAsFlow()

        /** 두 탭의 첫 페이지를 모두 새로 불러온다. 목록 화면에 진입할 때마다 호출한다. */
        fun refreshAll() {
            refresh(ChatListTab.GROUP)
            refresh(ChatListTab.DIRECT)
            loadNotificationCount()
        }

        /** 상단 헤더에 표시할 미읽음 알림 수를 불러온다(예: 알림 화면을 봤다 돌아왔을 때). */
        fun loadNotificationCount() {
            viewModelScope.launch {
                notificationRepository.getUnreadCount().onSuccess { count ->
                    _uiState.update { it.copy(notificationCount = count) }
                }
            }
        }

        /**
         * 사용자가 선택한 채팅 탭으로 변경한다.
         *
         * @param tab 새로 선택한 탭
         */
        fun selectTab(tab: ChatListTab) {
            _uiState.update { currentState ->
                currentState.copy(selectedTab = tab)
            }
        }

        /** [tab]의 첫 페이지를 서버에서 새로 불러와 기존 목록을 대체한다. */
        private fun refresh(tab: ChatListTab) {
            _uiState.update { it.withLoading(tab, isLoading = true) }
            viewModelScope.launch {
                chatRepository
                    .getChatRooms(type = tab.toChatRoomType(), page = 0, size = CHAT_LIST_PAGE_SIZE)
                    .onSuccess { page ->
                        _uiState.update { state ->
                            state
                                .withRooms(tab, page.rooms.map { it.toPreviewUiModel() }.distinctBy { it.id })
                                .withPageInfo(tab, currentPage = page.currentPage, hasNext = page.hasNext)
                                .withLoading(tab, isLoading = false)
                        }
                    }.onFailure {
                        _uiState.update { state -> state.withLoading(tab, isLoading = false) }
                        emitEvent(RecruitUiEvent.ShowToast(R.string.chat_list_toast_load_failed))
                    }
            }
        }

        /** [tab]에 다음 페이지가 있으면 이어서 불러와 기존 목록 뒤에 덧붙인다. */
        fun loadNextPage(tab: ChatListTab) {
            val state = _uiState.value
            val hasNext = if (tab == ChatListTab.GROUP) state.groupHasNext else state.directHasNext
            val isLoadingMore = if (tab == ChatListTab.GROUP) state.isGroupLoadingMore else state.isDirectLoadingMore
            if (!hasNext || isLoadingMore) return

            val nextPage = (if (tab == ChatListTab.GROUP) state.groupPage else state.directPage) + 1
            _uiState.update { it.withLoadingMore(tab, isLoadingMore = true) }

            viewModelScope.launch {
                chatRepository
                    .getChatRooms(type = tab.toChatRoomType(), page = nextPage, size = CHAT_LIST_PAGE_SIZE)
                    .onSuccess { page ->
                        _uiState.update { current ->
                            val appended =
                                (current.roomsOf(tab) + page.rooms.map { it.toPreviewUiModel() })
                                    .distinctBy { it.id }
                            current
                                .withRooms(tab, appended)
                                .withPageInfo(tab, currentPage = page.currentPage, hasNext = page.hasNext)
                                .withLoadingMore(tab, isLoadingMore = false)
                        }
                    }.onFailure {
                        _uiState.update { current -> current.withLoadingMore(tab, isLoadingMore = false) }
                        emitEvent(RecruitUiEvent.ShowToast(R.string.chat_list_toast_load_more_failed))
                    }
            }
        }

        /** 특정 채팅방의 알림 설정을 반대로 변경한다(낙관적 갱신 후 서버 반영, 실패 시 되돌린다). */
        fun toggleNotification(chatRoomId: Long) {
            val currentlyEnabled =
                (_uiState.value.groupChatRooms + _uiState.value.directChatRooms)
                    .find { it.id == chatRoomId }
                    ?.isNotificationEnabled ?: return

            applyNotificationState(chatRoomId, enabled = !currentlyEnabled)

            viewModelScope.launch {
                chatRepository.updateNotification(chatRoomId, enabled = !currentlyEnabled).onFailure {
                    applyNotificationState(chatRoomId, enabled = currentlyEnabled)
                    emitEvent(RecruitUiEvent.ShowToast(R.string.chat_toast_generic_error))
                }
            }
        }

        private fun applyNotificationState(
            chatRoomId: Long,
            enabled: Boolean
        ) {
            _uiState.update { state ->
                state.copy(
                    groupChatRooms = state.groupChatRooms.withNotification(chatRoomId, enabled),
                    directChatRooms = state.directChatRooms.withNotification(chatRoomId, enabled)
                )
            }
        }

        /** 특정 채팅방을 읽음 상태로 표시한다(목록 화면 로컬 표시용 — 실제 읽음 처리는 채팅방 진입 시 서버에 반영된다). */
        fun markChatRoomAsRead(chatRoomId: Long) {
            _uiState.update { currentState ->
                currentState.copy(
                    groupChatRooms = currentState.groupChatRooms.markAsRead(chatRoomId),
                    directChatRooms = currentState.directChatRooms.markAsRead(chatRoomId)
                )
            }
        }

        private fun emitEvent(event: RecruitUiEvent) {
            viewModelScope.launch { _events.send(event) }
        }

        private fun List<ChatRoomPreviewUiModel>.withNotification(
            chatRoomId: Long,
            enabled: Boolean
        ): List<ChatRoomPreviewUiModel> =
            map { chatRoom ->
                if (chatRoom.id == chatRoomId) chatRoom.copy(isNotificationEnabled = enabled) else chatRoom
            }

        private fun List<ChatRoomPreviewUiModel>.markAsRead(chatRoomId: Long): List<ChatRoomPreviewUiModel> =
            map { chatRoom ->
                if (chatRoom.id == chatRoomId) chatRoom.copy(unreadCount = 0) else chatRoom
            }
    }

private fun ChatListTab.toChatRoomType(): ChatRoomType =
    when (this) {
        ChatListTab.GROUP -> ChatRoomType.GROUP
        ChatListTab.DIRECT -> ChatRoomType.DIRECT
    }

/**
 * 8-1 목록 응답의 `lastMessage`는 마지막 메시지가 사진(IMAGE)이면 본문 텍스트가 없어 null로 내려온다
 * (서버가 이미지용 미리보기 문구를 따로 안 만들어줌 — 직접 확인함). `lastMessageAt`은 있는데
 * `lastMessage`만 비어 있으면 메시지가 없는 방이 아니라 마지막 메시지가 사진이라는 뜻이므로 클라이언트가
 * 대체 문구를 채운다.
 */
private fun ChatRoomSummary.toPreviewUiModel(): ChatRoomPreviewUiModel =
    ChatRoomPreviewUiModel(
        id = chatRoomId,
        type = type,
        roomName = title,
        lastMessage =
            when {
                !lastMessage.isNullOrBlank() -> lastMessage.truncateForChatListPreview()
                lastMessageAt != null -> "사진을 보냈습니다."
                else -> ""
            },
        lastMessageTime = lastMessageAt?.toChatListPreviewTimeText().orEmpty(),
        participantSummary = null,
        unreadCount = unreadCount,
        isNotificationEnabled = isNotificationEnabled
    )

private fun ChatListUiState.roomsOf(tab: ChatListTab): List<ChatRoomPreviewUiModel> =
    if (tab == ChatListTab.GROUP) groupChatRooms else directChatRooms

private fun ChatListUiState.withRooms(
    tab: ChatListTab,
    rooms: List<ChatRoomPreviewUiModel>
): ChatListUiState = if (tab == ChatListTab.GROUP) copy(groupChatRooms = rooms) else copy(directChatRooms = rooms)

private fun ChatListUiState.withPageInfo(
    tab: ChatListTab,
    currentPage: Int,
    hasNext: Boolean
): ChatListUiState =
    if (tab == ChatListTab.GROUP) {
        copy(groupPage = currentPage, groupHasNext = hasNext)
    } else {
        copy(directPage = currentPage, directHasNext = hasNext)
    }

private fun ChatListUiState.withLoading(
    tab: ChatListTab,
    isLoading: Boolean
): ChatListUiState =
    if (tab == ChatListTab.GROUP) copy(isGroupLoading = isLoading) else copy(isDirectLoading = isLoading)

private fun ChatListUiState.withLoadingMore(
    tab: ChatListTab,
    isLoadingMore: Boolean
): ChatListUiState =
    if (tab ==
        ChatListTab.GROUP
    ) {
        copy(isGroupLoadingMore = isLoadingMore)
    } else {
        copy(isDirectLoadingMore = isLoadingMore)
    }
