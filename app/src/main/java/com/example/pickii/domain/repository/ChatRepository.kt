package com.example.pickii.domain.repository

import android.net.Uri
import com.example.pickii.domain.model.ChatMessagePage
import com.example.pickii.domain.model.ChatRoomDetail
import com.example.pickii.domain.model.ChatRoomSummaryPage
import com.example.pickii.domain.model.DirectChatRoomResult
import com.example.pickii.ui.chat.ChatRoomType

/**
 * `8. Chat` 문서의 REST 엔드포인트를 담당한다(실시간 송수신은 `ChatStompClient`가 별도로 담당한다).
 *
 * [delegateLeader]/[removeMember]는 8번 문서에 없는 추정 엔드포인트를 호출한다. 이 인터페이스 뒤에
 * 격리해 두어, 실제 경로가 확정되면 [com.example.pickii.data.repository.ChatApiRepository]와
 * [com.example.pickii.data.remote.api.ChatApiService]만 고치면 되게 한다.
 */
interface ChatRepository {
    suspend fun getChatRooms(
        type: ChatRoomType,
        page: Int,
        size: Int
    ): Result<ChatRoomSummaryPage>

    suspend fun getChatRoomDetail(chatRoomId: Long): Result<ChatRoomDetail>

    suspend fun getMessages(
        chatRoomId: Long,
        cursor: String?,
        size: Int
    ): Result<ChatMessagePage>

    /** [imageUri]를 검증·업로드하고 서버가 저장한 이미지 URL을 반환한다. 실제 메시지 전송은 WebSocket으로 별도 발행해야 한다. */
    suspend fun uploadImage(
        chatRoomId: Long,
        imageUri: Uri
    ): Result<String>

    suspend fun createDirectChatRoom(targetMemberId: Long): Result<DirectChatRoomResult>

    suspend fun markAsRead(
        chatRoomId: Long,
        lastReadMessageId: String
    ): Result<Unit>

    suspend fun leaveChatRoom(chatRoomId: Long): Result<Unit>

    suspend fun updateNotification(
        chatRoomId: Long,
        enabled: Boolean
    ): Result<Unit>

    /** (미확정) 팀장 위임. `PATCH /projects/{projectId}/leader`라 채팅방 id가 아니라 프로젝트 id를 받는다. */
    suspend fun delegateLeader(
        projectId: Long,
        newLeaderMemberId: Long
    ): Result<Unit>

    /** (미확정) 팀원 내보내기. `DELETE /projects/{projectId}/members/{memberId}`라 채팅방 id가 아니라 프로젝트 id를 받는다. */
    suspend fun removeMember(
        projectId: Long,
        memberId: Long
    ): Result<Unit>
}
