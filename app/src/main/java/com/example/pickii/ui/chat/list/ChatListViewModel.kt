package com.example.pickii.ui.chat

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/**
 * 채팅 목록 화면의 상태와 사용자 동작을 관리한다.
 */
@HiltViewModel
class ChatListViewModel
    @Inject
    constructor() : ViewModel() {
        private val _uiState =
            MutableStateFlow(
                ChatListUiState(
                    selectedTab = ChatListTab.DIRECT,
                    groupChatRooms = createGroupChatRooms(),
                    directChatRooms = createDirectChatRooms()
                )
            )

        val uiState = _uiState.asStateFlow()

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

        /**
         * 특정 채팅방의 알림 설정을 반대로 변경한다.
         *
         * @param chatRoomId 알림 설정을 변경할 채팅방 식별자
         */
        fun toggleNotification(chatRoomId: Long) {
            _uiState.update { currentState ->
                currentState.copy(
                    groupChatRooms = currentState.groupChatRooms.toggleNotification(chatRoomId),
                    directChatRooms = currentState.directChatRooms.toggleNotification(chatRoomId)
                )
            }
        }

        /**
         * 특정 채팅방의 읽지 않은 메시지 개수를 0으로 변경한다.
         *
         * 추후 실제 API가 연결되면 채팅방 진입 시 서버의 읽음 처리 API를 호출하도록 교체한다.
         *
         * @param chatRoomId 읽음 처리할 채팅방 식별자
         */
        fun markChatRoomAsRead(chatRoomId: Long) {
            _uiState.update { currentState ->
                currentState.copy(
                    groupChatRooms = currentState.groupChatRooms.markAsRead(chatRoomId),
                    directChatRooms = currentState.directChatRooms.markAsRead(chatRoomId)
                )
            }
        }

        /**
         * 특정 채팅방의 알림 상태를 변경한 새 목록을 반환한다.
         *
         * @param chatRoomId 변경할 채팅방 식별자
         */
        private fun List<ChatRoomPreviewUiModel>.toggleNotification(chatRoomId: Long): List<ChatRoomPreviewUiModel> =
            map { chatRoom ->
                if (chatRoom.id == chatRoomId) {
                    chatRoom.copy(
                        isNotificationEnabled = !chatRoom.isNotificationEnabled
                    )
                } else {
                    chatRoom
                }
            }

        /**
         * 특정 채팅방을 읽음 상태로 변경한 새 목록을 반환한다.
         *
         * @param chatRoomId 읽음 처리할 채팅방 식별자
         */
        private fun List<ChatRoomPreviewUiModel>.markAsRead(chatRoomId: Long): List<ChatRoomPreviewUiModel> =
            map { chatRoom ->
                if (chatRoom.id == chatRoomId) {
                    chatRoom.copy(unreadCount = 0)
                } else {
                    chatRoom
                }
            }

        /**
         * 개인 채팅방 화면 확인을 위한 임시 데이터를 생성한다.
         */
        private fun createDirectChatRooms(): List<ChatRoomPreviewUiModel> =
            listOf(
                ChatRoomPreviewUiModel(
                    id = 1L,
                    type = ChatRoomType.PERSONAL,
                    roomName = "김민준",
                    senderName = "김민준",
                    lastMessage = "안녕하세요! 연락 주셔서 감사합니다",
                    lastMessageTime = "오후 3:20",
                    recruitTitle = "React 개발자 모집",
                    unreadCount = 2,
                    isNotificationEnabled = true
                ),
                ChatRoomPreviewUiModel(
                    id = 2L,
                    type = ChatRoomType.PERSONAL,
                    roomName = "이서연",
                    senderName = "이서연",
                    lastMessage = "내일 미팅 참석 가능하신가요?",
                    lastMessageTime = "오후 12:00",
                    recruitTitle = "UI/UX 디자이너 모집",
                    unreadCount = 0,
                    isNotificationEnabled = true
                )
            )

        /**
         * 그룹 채팅방 화면 확인을 위한 임시 데이터를 생성한다.
         */
        private fun createGroupChatRooms(): List<ChatRoomPreviewUiModel> =
            listOf(
                ChatRoomPreviewUiModel(
                    id = 101L,
                    type = ChatRoomType.GROUP,
                    roomName = "Pickii 앱 개발 프로젝트",
                    senderName = "김민서",
                    lastMessage = "화면 구현 완료되면 공유 부탁드려요!",
                    lastMessageTime = "오후 4:10",
                    recruitTitle = "React Native 개발자 모집",
                    participantSummary = "김민서 님 외 3명",
                    unreadCount = 5,
                    isNotificationEnabled = true
                ),
                ChatRoomPreviewUiModel(
                    id = 102L,
                    type = ChatRoomType.GROUP,
                    roomName = "교내 해커톤 준비팀",
                    senderName = "조승완",
                    lastMessage = "회의 일정 투표가 등록됐어요.",
                    lastMessageTime = "어제",
                    recruitTitle = "교내 해커톤 팀원 모집",
                    participantSummary = "조승완 님 외 4명",
                    unreadCount = 1,
                    isNotificationEnabled = false
                )
            )

        /**
         * 채팅 목록에서 특정 채팅방을 제거한다.
         */
        fun removeChatRoom(roomId: Long) {
            _uiState.update { currentState ->
                currentState.copy(
                    groupChatRooms =
                        currentState.groupChatRooms.filterNot { chatRoom ->
                            chatRoom.id == roomId
                        },
                    directChatRooms =
                        currentState.directChatRooms.filterNot { chatRoom ->
                            chatRoom.id == roomId
                        }
                )
            }
        }
    }
