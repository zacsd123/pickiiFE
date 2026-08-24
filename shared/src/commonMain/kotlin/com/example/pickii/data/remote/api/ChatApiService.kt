package com.example.pickii.data.remote.api

import com.example.pickii.data.remote.dto.CreateDirectChatRoomRequest
import com.example.pickii.data.remote.dto.DelegateChatRoomLeaderRequest
import com.example.pickii.data.remote.dto.MarkChatRoomReadRequest
import com.example.pickii.data.remote.dto.UpdateChatRoomNotificationRequest
import io.ktor.client.statement.HttpResponse

/** `8. Chat` 문서의 REST 엔드포인트(8-1 ~ 8-8). 실시간 메시지 송수신은 WebSocket(STOMP)으로 별도 처리한다. */
interface ChatApiService {
    suspend fun getChatRooms(
        type: String,
        page: Int,
        size: Int
    ): HttpResponse

    suspend fun getChatRoomDetail(chatRoomId: Long): HttpResponse

    suspend fun getMessages(
        chatRoomId: Long,
        cursor: String?,
        size: Int
    ): HttpResponse

    suspend fun uploadImage(
        chatRoomId: Long,
        fileName: String,
        contentType: String?,
        imageBytes: ByteArray
    ): HttpResponse

    suspend fun createDirectChatRoom(request: CreateDirectChatRoomRequest): HttpResponse

    suspend fun markAsRead(
        chatRoomId: Long,
        request: MarkChatRoomReadRequest
    ): HttpResponse

    suspend fun leaveChatRoom(chatRoomId: Long): HttpResponse

    suspend fun updateNotification(
        chatRoomId: Long,
        request: UpdateChatRoomNotificationRequest
    ): HttpResponse

    // 아래 두 엔드포인트는 8번 문서에 없는 추정 경로다. .../notification, .../members/me 컨벤션을 따랐으며
    // 백엔드 확인 전까지 실제 경로가 다를 수 있다. ChatRepository 뒤에 격리돼 있어 변경 시 이 파일과
    // ChatApiRepository만 고치면 된다.

    suspend fun delegateLeader(
        projectId: Long,
        request: DelegateChatRoomLeaderRequest
    ): HttpResponse

    suspend fun removeMember(
        projectId: Long,
        memberId: Long
    ): HttpResponse
}
