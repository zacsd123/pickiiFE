package com.example.pickii.data.repository

import android.content.Context
import android.net.Uri
import com.example.pickii.data.remote.api.ChatApiService
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
import com.example.pickii.domain.model.ChatMember
import com.example.pickii.domain.model.ChatMessage
import com.example.pickii.domain.model.ChatMessageContentType
import com.example.pickii.domain.model.ChatMessagePage
import com.example.pickii.domain.model.ChatRoomDetail
import com.example.pickii.domain.model.ChatRoomSummary
import com.example.pickii.domain.model.ChatRoomSummaryPage
import com.example.pickii.domain.model.DirectChatRoomResult
import com.example.pickii.domain.repository.ChatRepository
import com.example.pickii.domain.repository.ProjectRepository
import com.example.pickii.domain.repository.SessionRepository
import com.example.pickii.ui.chat.ChatImageValidationError
import com.example.pickii.ui.chat.ChatRoomType
import com.example.pickii.ui.chat.ProjectStatus
import com.example.pickii.ui.chat.toChatImagePart
import com.example.pickii.ui.chat.validateChatImage
import com.example.pickii.util.network.safeApiCall
import com.example.pickii.util.network.safeApiCallUnit
import com.example.pickii.util.parseIsoOffsetDateTime
import kotlinx.datetime.LocalDate

private const val ERROR_CODE_INVALID_FILE_TYPE = "INVALID_FILE_TYPE"
private const val ERROR_CODE_FILE_TOO_LARGE = "FILE_TOO_LARGE"

/** 발신자 정보가 없는 시스템 메시지(예: 회의 조율 개설 알림)에 표시할 기본 발신자명. */
private const val SYSTEM_SENDER_NICKNAME = "시스템"

/** `8. Chat` REST API로 [ChatRepository]를 구현한다. DTO ↔ 도메인 매핑을 전담한다. */
class ChatApiRepository(
    private val context: Context,
    private val chatApiService: ChatApiService,
    private val sessionRepository: SessionRepository,
    private val projectRepository: ProjectRepository
) : ChatRepository {
    override suspend fun getChatRooms(
        type: ChatRoomType,
        page: Int,
        size: Int
    ): Result<ChatRoomSummaryPage> =
        safeApiCall<ApiEnvelope<PageEnvelope<ChatRoomSummaryDto>>> {
            chatApiService.getChatRooms(type.name, page, size)
        }.map { envelope ->
            val pageEnvelope = envelope.data
            ChatRoomSummaryPage(
                rooms = pageEnvelope.content.map { it.toDomain() },
                currentPage = pageEnvelope.pageInfo.currentPage,
                totalPages = pageEnvelope.pageInfo.totalPages,
                hasNext = pageEnvelope.pageInfo.hasNext
            )
        }

    override suspend fun getChatRoomDetail(chatRoomId: Long): Result<ChatRoomDetail> {
        val currentMemberId =
            sessionRepository.currentUser.value
                ?.id
                ?.toLongOrNull()
        return safeApiCall<ApiEnvelope<ChatRoomDetailDto>> { chatApiService.getChatRoomDetail(chatRoomId) }
            .map { envelope ->
                val dto = envelope.data
                // GROUP 방이면 6-2로 진짜 leaderId를 받아와 팀원 전체의 팀장 여부를 정확히 계산한다.
                // 실패하면 본인만 신뢰 가능한 기존 규칙으로 내려간다(아래 toDomain 참고).
                val leaderId = dto.projectId?.let { projectRepository.getProjectLeaderId(it).getOrNull() }
                dto.toDomain(currentMemberId, leaderId)
            }
    }

    override suspend fun getMessages(
        chatRoomId: Long,
        cursor: String?,
        size: Int
    ): Result<ChatMessagePage> =
        safeApiCall<ApiEnvelope<CursorPageDto<ChatMessageDto>>> { chatApiService.getMessages(chatRoomId, cursor, size) }
            .map { envelope ->
                val page = envelope.data
                ChatMessagePage(
                    messages = page.content.map { it.toDomain() },
                    nextCursor = page.nextCursor,
                    hasNext = page.hasNext
                )
            }

    override suspend fun uploadImage(
        chatRoomId: Long,
        imageUri: Uri
    ): Result<String> {
        when (context.validateChatImage(imageUri)) {
            ChatImageValidationError.InvalidFileType ->
                return Result.failure(chatValidationException(ERROR_CODE_INVALID_FILE_TYPE, "허용되지 않는 파일 형식입니다."))
            ChatImageValidationError.FileTooLarge ->
                return Result.failure(chatValidationException(ERROR_CODE_FILE_TOO_LARGE, "파일 크기가 10MB를 초과했습니다."))
            null -> Unit
        }

        val part = context.toChatImagePart(imageUri)
        return safeApiCall<ApiEnvelope<ChatImageUploadResponseDto>> {
            chatApiService.uploadImage(chatRoomId, part.fileName, part.contentType, part.bytes)
        }.map { it.data.imageUrl }
    }

    override suspend fun createDirectChatRoom(targetMemberId: Long): Result<DirectChatRoomResult> =
        safeApiCall<ApiEnvelope<CreateDirectChatRoomResponseDto>> {
            chatApiService.createDirectChatRoom(CreateDirectChatRoomRequest(targetMemberId))
        }.map { DirectChatRoomResult(chatRoomId = it.data.chatRoomId) }

    override suspend fun markAsRead(
        chatRoomId: Long,
        lastReadMessageId: String
    ): Result<Unit> =
        safeApiCallUnit {
            chatApiService.markAsRead(chatRoomId, MarkChatRoomReadRequest(lastReadMessageId))
        }

    override suspend fun leaveChatRoom(chatRoomId: Long): Result<Unit> =
        safeApiCallUnit { chatApiService.leaveChatRoom(chatRoomId) }

    override suspend fun updateNotification(
        chatRoomId: Long,
        enabled: Boolean
    ): Result<Unit> =
        safeApiCallUnit {
            chatApiService.updateNotification(chatRoomId, UpdateChatRoomNotificationRequest(enabled))
        }

    override suspend fun delegateLeader(
        projectId: Long,
        newLeaderMemberId: Long
    ): Result<Unit> =
        safeApiCallUnit {
            chatApiService.delegateLeader(projectId, DelegateChatRoomLeaderRequest(newLeaderMemberId))
        }

    override suspend fun removeMember(
        projectId: Long,
        memberId: Long
    ): Result<Unit> = safeApiCallUnit { chatApiService.removeMember(projectId, memberId) }

    private fun ChatRoomSummaryDto.toDomain(): ChatRoomSummary =
        ChatRoomSummary(
            chatRoomId = chatRoomId,
            type = type.toChatRoomType(),
            title = title,
            lastMessage = lastMessage,
            lastMessageAt = lastMessageAt?.let(::parseIsoOffsetDateTime),
            unreadCount = unreadCount,
            isNotificationEnabled = notiEnabled
        )

    /**
     * 팀장 여부는 6-2(`GET /projects/{projectId}`)의 `leaderId`로 정확히 계산한다.
     * 그 호출이 실패한 경우(네트워크 오류 등)에만 본인 여부만 신뢰 가능한 예전 규칙으로 내려간다
     * (`GET /chatrooms/{chatRoomId}`의 `isLeader`는 본인 것만 알려준다 — 알려진 API 제약).
     */
    private fun ChatRoomDetailDto.toDomain(
        currentMemberId: Long?,
        leaderId: Long?
    ): ChatRoomDetail {
        val amLeader = isLeader == true
        return ChatRoomDetail(
            chatRoomId = chatRoomId,
            type = if (projectId != null) ChatRoomType.GROUP else ChatRoomType.DIRECT,
            title = title,
            members =
                members.map { member ->
                    ChatMember(
                        memberId = member.memberId,
                        nickname = member.nickname,
                        exp = member.exp,
                        isLeader =
                            if (leaderId != null) {
                                member.memberId == leaderId
                            } else {
                                amLeader && member.memberId == currentMemberId
                            }
                    )
                },
            projectId = projectId,
            startDate = startDate?.let(LocalDate::parse),
            endDate = endDate?.let(LocalDate::parse),
            projectStatus = status?.toProjectStatus(),
            isCurrentUserLeader = amLeader
        )
    }

    private fun ChatMessageDto.toDomain(): ChatMessage =
        ChatMessage(
            messageId = messageId,
            senderId = senderId ?: 0L,
            senderNickname = senderNickname ?: SYSTEM_SENDER_NICKNAME,
            senderExp = senderExp ?: 0,
            type = type.toChatMessageContentType(),
            text = message,
            imageUrl = imageUrl,
            createdAt = parseIsoOffsetDateTime(createdAt)
        )
}

private fun chatValidationException(
    code: String,
    message: String
) = com.example.pickii.data.remote.dto
    .ApiException(code, message)

private fun String.toChatRoomType(): ChatRoomType =
    runCatching {
        ChatRoomType.valueOf(this)
    }.getOrDefault(ChatRoomType.DIRECT)

private fun String.toProjectStatus(): ProjectStatus? = runCatching { ProjectStatus.valueOf(this) }.getOrNull()

private fun String.toChatMessageContentType(): ChatMessageContentType =
    runCatching { ChatMessageContentType.valueOf(this) }.getOrDefault(ChatMessageContentType.TEXT)
