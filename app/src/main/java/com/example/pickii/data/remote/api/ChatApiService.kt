package com.example.pickii.data.remote.api

import com.example.pickii.data.remote.dto.ApiEnvelope
import com.example.pickii.data.remote.dto.ChatImageUploadResponseDto
import com.example.pickii.data.remote.dto.ChatMessageDto
import com.example.pickii.data.remote.dto.ChatRoomDetailDto
import com.example.pickii.data.remote.dto.ChatRoomSummaryDto
import com.example.pickii.data.remote.dto.CreateDirectChatRoomRequest
import com.example.pickii.data.remote.dto.CreateDirectChatRoomResponseDto
import com.example.pickii.data.remote.dto.CursorPageDto
import com.example.pickii.data.remote.dto.DelegateChatRoomLeaderRequest
import com.example.pickii.data.remote.dto.MarkChatRoomReadRequest
import com.example.pickii.data.remote.dto.PageEnvelope
import com.example.pickii.data.remote.dto.UpdateChatRoomNotificationRequest
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

/** `8. Chat` 문서의 REST 엔드포인트(8-1 ~ 8-8). 실시간 메시지 송수신은 WebSocket(STOMP)으로 별도 처리한다. */
interface ChatApiService {
    @GET("chatrooms")
    suspend fun getChatRooms(
        @Query("type") type: String,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Response<ApiEnvelope<PageEnvelope<ChatRoomSummaryDto>>>

    @GET("chatrooms/{chatRoomId}")
    suspend fun getChatRoomDetail(
        @Path("chatRoomId") chatRoomId: Long
    ): Response<ApiEnvelope<ChatRoomDetailDto>>

    @GET("chatrooms/{chatRoomId}/messages")
    suspend fun getMessages(
        @Path("chatRoomId") chatRoomId: Long,
        @Query("cursor") cursor: String?,
        @Query("size") size: Int
    ): Response<ApiEnvelope<CursorPageDto<ChatMessageDto>>>

    @Multipart
    @POST("chatrooms/{chatRoomId}/images")
    suspend fun uploadImage(
        @Path("chatRoomId") chatRoomId: Long,
        @Part image: MultipartBody.Part
    ): Response<ApiEnvelope<ChatImageUploadResponseDto>>

    @POST("chatrooms/direct")
    suspend fun createDirectChatRoom(
        @Body request: CreateDirectChatRoomRequest
    ): Response<ApiEnvelope<CreateDirectChatRoomResponseDto>>

    @PATCH("chatrooms/{chatRoomId}/read")
    suspend fun markAsRead(
        @Path("chatRoomId") chatRoomId: Long,
        @Body request: MarkChatRoomReadRequest
    ): Response<Unit>

    @DELETE("chatrooms/{chatRoomId}/members/me")
    suspend fun leaveChatRoom(
        @Path("chatRoomId") chatRoomId: Long
    ): Response<Unit>

    @PATCH("chatrooms/{chatRoomId}/notification")
    suspend fun updateNotification(
        @Path("chatRoomId") chatRoomId: Long,
        @Body request: UpdateChatRoomNotificationRequest
    ): Response<Unit>

    // 아래 두 엔드포인트는 8번 문서에 없는 추정 경로다. .../notification, .../members/me 컨벤션을 따랐으며
    // 백엔드 확인 전까지 실제 경로가 다를 수 있다. ChatRepository 뒤에 격리돼 있어 변경 시 이 파일과
    // ChatApiRepository만 고치면 된다.

    @PATCH("projects/{projectId}/leader")
    suspend fun delegateLeader(
        @Path("projectId") projectId: Long,
        @Body request: DelegateChatRoomLeaderRequest
    ): Response<Unit>

    @DELETE("projects/{projectId}/members/{memberId}")
    suspend fun removeMember(
        @Path("projectId") projectId: Long,
        @Path("memberId") memberId: Long
    ): Response<Unit>
}
