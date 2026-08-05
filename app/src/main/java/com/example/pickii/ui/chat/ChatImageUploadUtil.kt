package com.example.pickii.ui.chat

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

private val ALLOWED_IMAGE_MIME_TYPES = setOf("image/jpeg", "image/png", "image/gif", "image/webp")
private const val MAX_IMAGE_BYTES = 10L * 1024 * 1024
private const val IMAGE_PART_NAME = "image"
private const val DEFAULT_IMAGE_EXTENSION = "jpg"

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

/** 검증을 통과한 [uri]를 `"image"` 파트 이름의 [MultipartBody.Part]로 변환한다. */
fun Context.toChatImagePart(uri: Uri): MultipartBody.Part {
    val mimeType = contentResolver.getType(uri).orEmpty()
    val mediaType = mimeType.toMediaTypeOrNull()
    val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: DEFAULT_IMAGE_EXTENSION
    val fileName = "chat_image.$extension"

    val cachedFile = File(cacheDir, fileName)
    contentResolver.openInputStream(uri)?.use { input ->
        cachedFile.outputStream().use { output -> input.copyTo(output) }
    }

    val requestBody =
        if (cachedFile.exists()) {
            cachedFile.asRequestBody(mediaType)
        } else {
            ByteArray(0).toRequestBody(mediaType)
        }

    return MultipartBody.Part.createFormData(IMAGE_PART_NAME, fileName, requestBody)
}
