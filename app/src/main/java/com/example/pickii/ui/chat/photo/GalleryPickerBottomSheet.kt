package com.example.pickii.ui.chat

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil3.compose.AsyncImage
import com.example.pickii.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val GALLERY_QUERY_LIMIT = 300
private val GalleryGridHeight = 420.dp

/**
 * 기기 사진을 그리드로 보여주고 여러 장을 골라 채팅으로 보낼 수 있는 바텀시트다.
 *
 * 우상단 버튼은 선택된 사진이 없으면 닫기(X), 하나 이상 선택되면 완료(체크)로 바뀐다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryPickerBottomSheet(
    onDismiss: () -> Unit,
    onConfirm: (List<Uri>) -> Unit
) {
    val context = LocalContext.current
    val sheetState =
        rememberModalBottomSheetState(
            skipPartiallyExpanded = true
        )

    var hasPermission by remember {
        mutableStateOf(hasGalleryReadPermission(context))
    }

    var permissionDenied by remember {
        mutableStateOf(false)
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { granted ->
            hasPermission = granted
            if (!granted) {
                permissionDenied = true
            }
        }

    var photoUris by remember {
        mutableStateOf<List<Uri>>(emptyList())
    }

    var isLoading by remember {
        mutableStateOf(false)
    }

    val selectedUris =
        remember {
            mutableStateListOf<Uri>()
        }

    LaunchedEffect(hasPermission) {
        if (hasPermission) {
            isLoading = true
            photoUris =
                withContext(Dispatchers.IO) {
                    queryDeviceImages(context)
                }
            isLoading = false
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        dragHandle = {
            BottomSheetDefaults.DragHandle(
                width = 36.dp,
                height = 4.dp
            )
        },
        shape =
            RoundedCornerShape(
                topStart = 28.dp,
                topEnd = 28.dp
            )
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = 16.dp)
        ) {
            GalleryPickerHeader(
                selectedCount = selectedUris.size,
                onCloseClick = onDismiss,
                onConfirmClick = {
                    onConfirm(selectedUris.toList())
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            when {
                !hasPermission -> {
                    GalleryPermissionRequest(
                        denied = permissionDenied,
                        onRequestClick = {
                            permissionLauncher.launch(galleryReadPermission())
                        }
                    )
                }

                isLoading -> {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(GalleryGridHeight),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF171714))
                    }
                }

                photoUris.isEmpty() -> {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(GalleryGridHeight),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "표시할 사진이 없습니다",
                            color = Color(0xFF9CA3AF),
                            fontSize = 14.sp
                        )
                    }
                }

                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(GalleryGridHeight),
                        contentPadding = PaddingValues(2.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        items(
                            items = photoUris,
                            key = { it.toString() }
                        ) { uri ->
                            GalleryPhotoCell(
                                uri = uri,
                                selected = uri in selectedUris,
                                onClick = {
                                    if (uri in selectedUris) {
                                        selectedUris.remove(uri)
                                    } else {
                                        selectedUris.add(uri)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 갤러리 시트 상단의 제목과, 선택 여부에 따라 닫기(X)/완료(체크)로 바뀌는 버튼이다.
 */
@Composable
private fun GalleryPickerHeader(
    selectedCount: Int,
    onCloseClick: () -> Unit,
    onConfirmClick: () -> Unit
) {
    val hasSelection = selectedCount > 0

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    start = 24.dp,
                    end = 24.dp,
                    top = 4.dp
                ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "사진 선택",
            modifier = Modifier.weight(1f),
            color = Color(0xFF18181B),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Box(
            modifier =
                Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        if (hasSelection) {
                            Color(0xFF171714)
                        } else {
                            Color(0xFFF2F3F6)
                        }
                    ).clickable(
                        onClick = if (hasSelection) onConfirmClick else onCloseClick
                    ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter =
                    painterResource(
                        id = if (hasSelection) R.drawable.ic_check else R.drawable.ic_close
                    ),
                contentDescription = if (hasSelection) "완료" else "닫기",
                tint = if (hasSelection) Color.White else Color(0xFF4B5563),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

/**
 * 사진 접근 권한이 없을 때 요청 버튼을 보여준다.
 */
@Composable
private fun GalleryPermissionRequest(
    denied: Boolean,
    onRequestClick: () -> Unit
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(GalleryGridHeight)
                .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text =
                if (denied) {
                    "설정에서 사진 접근 권한을 허용해주세요"
                } else {
                    "사진을 보려면 접근 권한이 필요해요"
                },
            color = Color(0xFF667085),
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onRequestClick,
            shape = RoundedCornerShape(14.dp),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF171714),
                    contentColor = Color.White
                )
        ) {
            Text(text = "권한 허용하기")
        }
    }
}

/**
 * 사진 한 장의 썸네일. 선택되면 어두운 오버레이와 체크 배지가 표시된다.
 */
@Composable
private fun GalleryPhotoCell(
    uri: Uri,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier =
            Modifier
                .aspectRatio(1f)
                .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = uri,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        if (selected) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.35f))
            )
        }

        Box(
            modifier =
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(
                        if (selected) Color(0xFF171714) else Color.White.copy(alpha = 0.7f)
                    ),
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_check),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

/**
 * SDK 버전에 맞는 사진 읽기 권한 문자열을 반환한다.
 */
private fun galleryReadPermission(): String =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

private fun hasGalleryReadPermission(context: Context): Boolean =
    ContextCompat.checkSelfPermission(
        context,
        galleryReadPermission()
    ) == PackageManager.PERMISSION_GRANTED

/**
 * MediaStore에서 기기에 저장된 사진을 최신순으로 최대 [GALLERY_QUERY_LIMIT]장까지 가져온다.
 */
private fun queryDeviceImages(context: Context): List<Uri> {
    val uris = mutableListOf<Uri>()
    val collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    val projection = arrayOf(MediaStore.Images.Media._ID)
    val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

    context.contentResolver
        .query(
            collection,
            projection,
            null,
            null,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)

            while (cursor.moveToNext() && uris.size < GALLERY_QUERY_LIMIT) {
                val id = cursor.getLong(idColumn)
                uris.add(ContentUris.withAppendedId(collection, id))
            }
        }

    return uris
}
