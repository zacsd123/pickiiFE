package com.example.pickii.ui.chat

private val ALLOWED_IMAGE_MIME_TYPES = setOf("image/jpeg", "image/png", "image/gif", "image/webp")
private const val MAX_IMAGE_BYTES = 10L * 1024 * 1024

/** 갤러리에서 고르거나 카메라로 찍은 사진을 Ktor 멀티파트 업로드용으로 변환한 결과. */
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

/**
 * [ChatImageUploadPart]의 확장자(jpg/jpeg/png/gif/webp)와 크기(최대 10MB)를 검사한다. 문제없으면
 * null을 반환한다. 플랫폼 API가 필요 없는 순수 함수라 Android/iOS 양쪽에서 그대로 재사용한다.
 */
fun ChatImageUploadPart.validate(): ChatImageValidationError? {
    if (contentType !in ALLOWED_IMAGE_MIME_TYPES) {
        return ChatImageValidationError.InvalidFileType
    }

    if (bytes.size > MAX_IMAGE_BYTES) {
        return ChatImageValidationError.FileTooLarge
    }

    return null
}
