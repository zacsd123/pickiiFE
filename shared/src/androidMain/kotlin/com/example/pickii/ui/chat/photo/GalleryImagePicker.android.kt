package com.example.pickii.ui.chat.photo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.pickii.ui.chat.ChatImageUploadPart
import com.example.pickii.ui.chat.GalleryPickerBottomSheet
import com.example.pickii.ui.chat.toChatImagePart

/**
 * `selectionLimit`는 안 쓴다 — 기존 [GalleryPickerBottomSheet]에 선택 개수 제한 로직이 원래
 * 없어서(Android 동작을 바꾸지 않기 위해 그대로 둠), 이 파라미터는 iOS 쪽에서만 의미가 있다.
 */
@Composable
actual fun rememberGalleryImagePickerLauncher(
    selectionLimit: Int,
    onResult: (List<ChatImageUploadPart>) -> Unit
): () -> Unit {
    val context = LocalContext.current
    var showPicker by remember { mutableStateOf(false) }

    if (showPicker) {
        GalleryPickerBottomSheet(
            onDismiss = { showPicker = false },
            onConfirm = { uris ->
                showPicker = false
                onResult(uris.map { context.toChatImagePart(it) })
            }
        )
    }

    return { showPicker = true }
}
