package com.example.pickii.ui.chat

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap

private val ALLOWED_IMAGE_MIME_TYPES = setOf("image/jpeg", "image/png", "image/gif", "image/webp")
private const val MAX_IMAGE_BYTES = 10L * 1024 * 1024
private const val DEFAULT_IMAGE_EXTENSION = "jpg"

/** [Context.toChatImagePart]가 만드는, Ktor 멀티파트 업로드에 필요한 최소 정보. */
data class ChatImageUploadPart(
    val fileName: String,
    val contentType: String?,
    val bytes: ByteArray
)

/** 채팅 이미지 업로드 실패 사유(8-4 Validation). */
sealed interface ChatImageValidationError {
    data object InvalidFileType : ChatImageValidationError

    data object FileTooLarge : ChatImageValidationError
}

/** 업로드 전 [uri]의 확장자(jpg/jpeg/png/gif/webp)와 크기(최대 10MB)를 검사한다. 문제없으면 null을 반환한다. */
fun Context.validateChatImage(uri: Uri): ChatImageValidationError? {
    val mimeType = contentResolver.getType(uri)
    if (mimeType !in ALLOWED_IMAGE_MIME_TYPES) {
        return ChatImageValidationError.InvalidFileType
    }

    val sizeBytes =
        runCatching {
            contentResolver.openAssetFileDescriptor(uri, "r")?.use { it.length }
        }.getOrNull()

    if (sizeBytes != null && sizeBytes > MAX_IMAGE_BYTES) {
        return ChatImageValidationError.FileTooLarge
    }

    return null
}

/** 검증을 통과한 [uri]를 Ktor 멀티파트 업로드에 필요한 [ChatImageUploadPart]로 변환한다. */
fun Context.toChatImagePart(uri: Uri): ChatImageUploadPart {
    val mimeType = contentResolver.getType(uri)
    val extension = mimeType?.let { MimeTypeMap.getSingleton().getExtensionFromMimeType(it) } ?: DEFAULT_IMAGE_EXTENSION
    val fileName = "chat_image.$extension"
    val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: ByteArray(0)

    return ChatImageUploadPart(fileName = fileName, contentType = mimeType, bytes = bytes)
}
