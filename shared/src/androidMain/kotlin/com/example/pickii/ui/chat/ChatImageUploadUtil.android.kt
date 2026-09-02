package com.example.pickii.ui.chat

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap

private const val DEFAULT_IMAGE_EXTENSION = "jpg"

/** [uri]를 Ktor 멀티파트 업로드에 필요한 [ChatImageUploadPart]로 변환한다(검증은 [validate] 참고). */
fun Context.toChatImagePart(uri: Uri): ChatImageUploadPart {
    val mimeType = contentResolver.getType(uri)
    val extension = mimeType?.let { MimeTypeMap.getSingleton().getExtensionFromMimeType(it) } ?: DEFAULT_IMAGE_EXTENSION
    val fileName = "chat_image.$extension"
    val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: ByteArray(0)

    return ChatImageUploadPart(fileName = fileName, contentType = mimeType, bytes = bytes)
}
