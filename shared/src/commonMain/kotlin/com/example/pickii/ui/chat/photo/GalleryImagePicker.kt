package com.example.pickii.ui.chat.photo

import androidx.compose.runtime.Composable
import com.example.pickii.ui.chat.ChatImageUploadPart

/**
 * 갤러리(사진 라이브러리)에서 이미지를 선택하는 런처를 컴포지션에서 얻는다. 반환된 함수를 호출하면
 * 플랫폼 네이티브 선택 UI가 뜨고, 선택이 끝나면 [onResult]가 결과를 들고 호출된다.
 *
 * Android는 기존 `GalleryPickerBottomSheet`(커스텀 그리드)를 그대로 감싼다 — 이 그리드는 선택
 * 개수 제한이 없어서(디바이스 사진 수만큼, 쿼리는 최대 300장까지만
 * 보여줌) [selectionLimit] 파라미터를 받지 않는다. iOS는 `PHPickerViewController`(시스템 시트)를
 * 쓰는데 여기서는 실제로 개수 제한을 걸 수 있어서, Android와 동작을 맞추기 위해 기본값을 0(무제한)
 * 으로 둔다.
 *
 * 로딩 실패(iCloud 미다운로드, 네트워크 오류 등)한 개별 항목은 결과 목록에서 조용히 빠진다 —
 * `ChatRoomViewModel.sendImageMessages`가 이미 항목 단위로 검증/실패를 처리하는 것과 같은 태도
 * (항목 하나 실패했다고 전체를 막지 않음).
 */
@Composable
expect fun rememberGalleryImagePickerLauncher(
    selectionLimit: Int = 0,
    onResult: (List<ChatImageUploadPart>) -> Unit
): () -> Unit
