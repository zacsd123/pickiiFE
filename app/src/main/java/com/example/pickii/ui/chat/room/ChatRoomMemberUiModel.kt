package com.example.pickii.ui.chat

/**
 * 채팅방 참여자 정보를 나타낸다.
 *
 * @property memberId 참여자 식별자
 * @property name 참여자 이름
 * @property isLeader 채팅방 팀장 여부
 */
data class ChatRoomMemberUiModel(
    val memberId: Long,
    val name: String,
    val isLeader: Boolean = false
)
