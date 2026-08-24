package com.example.pickii.data.remote.api

import com.example.pickii.data.remote.dto.CreateDirectChatRoomRequest
import com.example.pickii.data.remote.dto.DelegateChatRoomLeaderRequest
import com.example.pickii.data.remote.dto.MarkChatRoomReadRequest
import com.example.pickii.data.remote.dto.UpdateChatRoomNotificationRequest
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.append

/** [ChatApiService]를 Ktor [HttpClient]로 구현한다. */
class KtorChatApiService(
    private val client: HttpClient
) : ChatApiService {
    override suspend fun getChatRooms(
        type: String,
        page: Int,
        size: Int
    ): HttpResponse =
        client.get("chatrooms") {
            parameter("type", type)
            parameter("page", page)
            parameter("size", size)
        }

    override suspend fun getChatRoomDetail(chatRoomId: Long): HttpResponse = client.get("chatrooms/$chatRoomId")

    override suspend fun getMessages(
        chatRoomId: Long,
        cursor: String?,
        size: Int
    ): HttpResponse =
        client.get("chatrooms/$chatRoomId/messages") {
            parameter("cursor", cursor)
            parameter("size", size)
        }

    override suspend fun uploadImage(
        chatRoomId: Long,
        fileName: String,
        contentType: String?,
        imageBytes: ByteArray
    ): HttpResponse =
        client.submitFormWithBinaryData(
            url = "chatrooms/$chatRoomId/images",
            formData =
                formData {
                    append(
                        "image",
                        imageBytes,
                        Headers.build {
                            append(HttpHeaders.ContentType, contentType ?: "application/octet-stream")
                            append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                        }
                    )
                }
        )

    override suspend fun createDirectChatRoom(request: CreateDirectChatRoomRequest): HttpResponse =
        client.post("chatrooms/direct") {
            setBody(request)
        }

    override suspend fun markAsRead(
        chatRoomId: Long,
        request: MarkChatRoomReadRequest
    ): HttpResponse =
        client.patch("chatrooms/$chatRoomId/read") {
            setBody(request)
        }

    override suspend fun leaveChatRoom(chatRoomId: Long): HttpResponse = client.delete("chatrooms/$chatRoomId/members/me")

    override suspend fun updateNotification(
        chatRoomId: Long,
        request: UpdateChatRoomNotificationRequest
    ): HttpResponse =
        client.patch("chatrooms/$chatRoomId/notification") {
            setBody(request)
        }

    override suspend fun delegateLeader(
        projectId: Long,
        request: DelegateChatRoomLeaderRequest
    ): HttpResponse =
        client.patch("projects/$projectId/leader") {
            setBody(request)
        }

    override suspend fun removeMember(
        projectId: Long,
        memberId: Long
    ): HttpResponse = client.delete("projects/$projectId/members/$memberId")
}
