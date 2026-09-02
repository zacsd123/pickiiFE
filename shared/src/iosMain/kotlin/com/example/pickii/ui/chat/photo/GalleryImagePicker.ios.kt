package com.example.pickii.ui.chat.photo

import androidx.compose.runtime.Composable
import com.example.pickii.ui.chat.ChatImageUploadPart
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSItemProvider
import platform.PhotosUI.PHPickerConfiguration
import platform.PhotosUI.PHPickerFilter
import platform.PhotosUI.PHPickerResult
import platform.PhotosUI.PHPickerViewController
import platform.PhotosUI.PHPickerViewControllerDelegateProtocol
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController
import platform.darwin.NSObject
import platform.posix.memcpy

/** [ChatImageUploadPart.validate]가 허용하는 MIME 타입과 짝을 맞춘 UTI 식별자 매핑. */
private val IMAGE_TYPE_IDENTIFIER_TO_MIME =
    mapOf(
        "public.jpeg" to "image/jpeg",
        "public.png" to "image/png",
        "com.compuserve.gif" to "image/gif",
        "public.webp" to "image/webp",
        "org.webmproject.webp" to "image/webp"
    )

private const val DEFAULT_IMAGE_FILE_NAME = "chat_image.jpg"

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
    val size = length.toInt()
    if (size == 0) return ByteArray(0)

    val out = ByteArray(size)
    out.usePinned { pinned ->
        memcpy(pinned.addressOf(0), bytes, length)
    }
    return out
}

/** 지금 화면 맨 위에 떠 있는(가장 최근에 present된) 뷰 컨트롤러를 찾는다 — 여기서 피커를 띄운다. */
private fun topmostViewController(): UIViewController? {
    var controller = UIApplication.sharedApplication.keyWindow?.rootViewController
    while (controller?.presentedViewController != null) {
        controller = controller.presentedViewController
    }
    return controller
}

/**
 * [itemProvider]에서 이미지 데이터를 비동기로 읽어 [ChatImageUploadPart]로 변환한다. iCloud
 * 미다운로드 상태거나 로딩 자체가 실패하면(네트워크 오류 등) [onLoaded]에 null을 넘긴다 — 이
 * 항목만 결과에서 조용히 빠지고 나머지 선택 항목은 그대로 전송된다.
 */
private fun loadImagePart(
    itemProvider: NSItemProvider,
    onLoaded: (ChatImageUploadPart?) -> Unit
) {
    @Suppress("UNCHECKED_CAST")
    val typeIdentifiers = itemProvider.registeredTypeIdentifiers as List<String>
    val typeIdentifier =
        typeIdentifiers.firstOrNull { it in IMAGE_TYPE_IDENTIFIER_TO_MIME } ?: typeIdentifiers.firstOrNull()

    if (typeIdentifier == null) {
        onLoaded(null)
        return
    }

    itemProvider.loadDataRepresentationForTypeIdentifier(typeIdentifier) { data, error ->
        if (error != null || data == null) {
            onLoaded(null)
        } else {
            onLoaded(
                ChatImageUploadPart(
                    fileName = DEFAULT_IMAGE_FILE_NAME,
                    contentType = IMAGE_TYPE_IDENTIFIER_TO_MIME[typeIdentifier],
                    bytes = data.toByteArray()
                )
            )
        }
    }
}

private class GalleryPickerDelegate(
    private val onResult: (List<ChatImageUploadPart>) -> Unit
) : NSObject(), PHPickerViewControllerDelegateProtocol {
    override fun picker(
        picker: PHPickerViewController,
        didFinishPicking: List<*>
    ) {
        picker.dismissViewControllerAnimated(true, completion = null)

        @Suppress("UNCHECKED_CAST")
        val results = didFinishPicking as List<PHPickerResult>
        if (results.isEmpty()) {
            onResult(emptyList())
            return
        }

        val loaded = arrayOfNulls<ChatImageUploadPart>(results.size)
        var remaining = results.size

        results.forEachIndexed { index, result ->
            loadImagePart(result.itemProvider) { part ->
                loaded[index] = part
                remaining--
                if (remaining == 0) {
                    onResult(loaded.filterNotNull())
                }
            }
        }
    }
}

@Composable
actual fun rememberGalleryImagePickerLauncher(
    selectionLimit: Int,
    onResult: (List<ChatImageUploadPart>) -> Unit
): () -> Unit =
    {
        val configuration = PHPickerConfiguration()
        configuration.selectionLimit = selectionLimit.toLong()
        configuration.filter = PHPickerFilter.imagesFilter()

        val picker = PHPickerViewController(configuration = configuration)
        picker.delegate = GalleryPickerDelegate(onResult)

        topmostViewController()?.presentViewController(picker, animated = true, completion = null)
    }
