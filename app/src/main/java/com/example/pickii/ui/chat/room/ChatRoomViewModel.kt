package com.example.pickii.ui.chat

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * 채팅방의 메시지 및 사용자 동작 상태를 관리한다.
 */
@HiltViewModel
class ChatRoomViewModel
    @Inject
    constructor() : ViewModel() {
        private val _uiState = MutableStateFlow(ChatRoomUiState())
        val uiState: StateFlow<ChatRoomUiState> = _uiState.asStateFlow()

        /**
         * 진입한 채팅방의 기본 정보를 설정한다.
         */
        fun initializeRoom(
            roomId: Long,
            roomTitle: String,
            roomType: ChatRoomType
        ) {
            if (_uiState.value.roomId == roomId) {
                return
            }

            val members =
                createMockMembers(
                    roomType = roomType,
                    roomTitle = roomTitle
                )

            val leader =
                members.firstOrNull { member ->
                    member.isLeader
                }

            _uiState.value =
                ChatRoomUiState(
                    roomId = roomId,
                    roomTitle = roomTitle,
                    roomType = roomType,
                    memberCount = members.size,
                    members = members,
                    leaderName = leader?.name.orEmpty(),
                    personalChatMemberName =
                        if (roomType == ChatRoomType.PERSONAL) {
                            roomTitle
                        } else {
                            ""
                        },
                    isNotificationEnabled = false,
                    projectStatus = ProjectStatus.IN_PROGRESS,
                    // 현재 로그인 사용자가 김민서라고 가정한 Mock 상태
                    isCurrentUserLeader = roomType == ChatRoomType.GROUP,
                    messages = createMockMessages(),
                    meetings = createMockMeetings()
                )
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

        /**
         * 작성한 일반 메시지를 채팅방에 추가한다.
         */
        fun sendMessage() {
            val currentState = _uiState.value
            val content = currentState.messageInput.trim()

            if (content.isBlank()) {
                return
            }

            val unreadCount =
                when (currentState.roomType) {
                    ChatRoomType.PERSONAL -> 1
                    ChatRoomType.GROUP -> {
                        (currentState.memberCount - 1).coerceAtLeast(0)
                    }
                }

            val newMessage =
                ChatMessageUiModel(
                    id = System.currentTimeMillis(),
                    content = content,
                    sentAt = getCurrentTimeText(),
                    isMine = true,
                    unreadCount = unreadCount,
                    type = ChatMessageType.TEXT,
                    meetingNotice = null
                )

            _uiState.update { state ->
                state.copy(
                    messages = state.messages + newMessage,
                    messageInput = "",
                    isActionMenuExpanded = false
                )
            }

            simulateMessageRead(
                messageId = newMessage.id
            )
        }

        /**
         * 갤러리에서 고르거나 카메라로 찍은 사진을 채팅방에 이미지 메시지로 추가한다. 사진마다 별도 메시지로 전송된다.
         */
        fun sendImageMessages(uris: List<Uri>) {
            if (uris.isEmpty()) {
                return
            }

            val currentState = _uiState.value

            val unreadCount =
                when (currentState.roomType) {
                    ChatRoomType.PERSONAL -> 1
                    ChatRoomType.GROUP -> {
                        (currentState.memberCount - 1).coerceAtLeast(0)
                    }
                }

            val newMessages =
                uris.mapIndexed { index, uri ->
                    ChatMessageUiModel(
                        id = System.currentTimeMillis() + index,
                        content = "",
                        sentAt = getCurrentTimeText(),
                        isMine = true,
                        unreadCount = unreadCount,
                        type = ChatMessageType.IMAGE,
                        imageUri = uri.toString()
                    )
                }

            _uiState.update { state ->
                state.copy(
                    messages = state.messages + newMessages,
                    isActionMenuExpanded = false
                )
            }

            newMessages.forEach { message ->
                simulateMessageRead(
                    messageId = message.id
                )
            }
        }

        /**
         * 회의 등록 공지를 채팅방 메시지 목록에 추가한다.
         */
        fun sendMeetingNotice(meeting: QuickMeetingForm) {
            val meetingMessage =
                ChatMessageUiModel(
                    id = System.currentTimeMillis(),
                    content = "",
                    sentAt = getCurrentTimeText(),
                    isMine = false,
                    unreadCount = 0,
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
         * 선택한 회의를 예정된 회의 목록에서 삭제한다.
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

        /**
         * 시연을 위해 잠시 후 메시지를 읽은 상태로 변경한다.
         *
         * 실제 서버 연결 이후에는 WebSocket 읽음 이벤트로 대체한다.
         */
        private fun simulateMessageRead(messageId: Long) {
            viewModelScope.launch {
                delay(2_000L)

                _uiState.update { currentState ->
                    currentState.copy(
                        messages =
                            currentState.messages.map { message ->
                                if (message.id == messageId) {
                                    message.copy(
                                        unreadCount = 0
                                    )
                                } else {
                                    message
                                }
                            }
                    )
                }
            }
        }

        /**
         * 현재 시간을 시와 분 형식으로 반환한다.
         */
        private fun getCurrentTimeText(): String =
            SimpleDateFormat(
                "HH:mm",
                Locale.getDefault()
            ).format(Date())

        /**
         * 채팅 화면 확인용 임시 메시지를 생성한다.
         */
        private fun createMockMessages(): List<ChatMessageUiModel> =
            listOf(
                ChatMessageUiModel(
                    id = 1L,
                    content = "안녕하세요! 이번 프로젝트 일정 확인하셨나요?",
                    sentAt = "12:45",
                    isMine = false,
                    unreadCount = 0,
                    type = ChatMessageType.TEXT,
                    meetingNotice = null
                ),
                ChatMessageUiModel(
                    id = 2L,
                    content = "네, 확인했습니다. 이번 주 안으로 기획 정리해둘게요.",
                    sentAt = "12:46",
                    isMine = true,
                    unreadCount = 0,
                    type = ChatMessageType.TEXT,
                    meetingNotice = null
                ),
                ChatMessageUiModel(
                    id = 3L,
                    content = "좋아요! 정리되면 채팅방에 공유 부탁드립니다.",
                    sentAt = "12:50",
                    isMine = false,
                    unreadCount = 0,
                    type = ChatMessageType.TEXT,
                    meetingNotice = null
                )
            )

        fun registerNotice(content: String) {
            if (content.isBlank()) return

            val registeredAt =
                SimpleDateFormat(
                    "yyyy-MM-dd HH:mm",
                    Locale.getDefault()
                ).format(Date())

            _uiState.update { currentState ->
                currentState.copy(
                    noticeContent = content.trim(),
                    // 현재는 임시 사용자 이름
                    noticeWriter = "김민서",
                    noticeRegisteredAt = registeredAt,
                    isNoticeExpanded = false,
                    isActionMenuExpanded = false
                )
            }
        }

        /**
         * 채팅방 정보 화면 확인용 임시 팀원 목록을 생성한다.
         */
        private fun createMockMembers(
            roomType: ChatRoomType,
            roomTitle: String
        ): List<ChatRoomMemberUiModel> =
            when (roomType) {
                ChatRoomType.PERSONAL -> {
                    listOf(
                        ChatRoomMemberUiModel(
                            memberId = 1L,
                            name = "나",
                            isLeader = false
                        ),
                        ChatRoomMemberUiModel(
                            memberId = 2L,
                            name = roomTitle,
                            isLeader = false
                        )
                    )
                }

                ChatRoomType.GROUP -> {
                    listOf(
                        ChatRoomMemberUiModel(
                            memberId = 1L,
                            name = "민준",
                            isLeader = true
                        ),
                        ChatRoomMemberUiModel(
                            memberId = 2L,
                            name = "지은",
                            isLeader = false
                        ),
                        ChatRoomMemberUiModel(
                            memberId = 3L,
                            name = "현우",
                            isLeader = false
                        ),
                        ChatRoomMemberUiModel(
                            memberId = 4L,
                            name = "소연",
                            isLeader = false
                        ),
                        ChatRoomMemberUiModel(
                            memberId = 5L,
                            name = "태호",
                            isLeader = false
                        )
                    )
                }
            }

        /**
         * 선택한 팀원에게 팀장 권한을 위임한다.
         */
        fun delegateLeader(memberId: Long) {
            val selectedMember =
                uiState.value.members
                    .find { member ->
                        member.memberId == memberId
                    }
                    ?: return

            _uiState.update { currentState ->
                val updatedMembers =
                    currentState.members.map { member ->
                        member.copy(
                            isLeader = member.memberId == memberId
                        )
                    }

                currentState.copy(
                    members = updatedMembers,
                    leaderName = selectedMember.name,
                    projectInfo =
                        currentState.projectInfo.copy(
                            leaderName = selectedMember.name
                        ),
                    isCurrentUserLeader = false
                )
            }
        }

        /**
         * 선택한 팀원을 채팅방에서 내보낸다.
         */
        fun removeMember(memberId: Long) {
            _uiState.update { currentState ->
                val memberToRemove =
                    currentState.members.find { member ->
                        member.memberId == memberId
                    } ?: return@update currentState

                if (memberToRemove.isLeader) {
                    return@update currentState
                }

                val updatedMembers =
                    currentState.members.filterNot { member ->
                        member.memberId == memberId
                    }

                currentState.copy(
                    members = updatedMembers,
                    memberCount = updatedMembers.size,
                    projectInfo =
                        currentState.projectInfo.copy(
                            memberCount = updatedMembers.size
                        )
                )
            }
        }

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
