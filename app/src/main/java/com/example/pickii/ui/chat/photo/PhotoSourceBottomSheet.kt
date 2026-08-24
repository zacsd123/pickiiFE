package com.example.pickii.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pickii.shared.generated.resources.Res
import com.example.pickii.shared.generated.resources.ic_photo_camera
import com.example.pickii.shared.generated.resources.ic_photo_library
import com.example.pickii.ui.theme.PickiiBlackAlt
import com.example.pickii.ui.theme.PickiiGray700
import com.example.pickii.ui.theme.PickiiSlateDark
import com.example.pickii.ui.theme.PickiiSurfaceGraySoft
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/**
 * "사진/카메라" 항목을 눌렀을 때, 갤러리와 카메라 중 사진을 가져올 방법을 고르는 바텀시트다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoSourceBottomSheet(
    onGalleryClick: () -> Unit,
    onCameraClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState =
        rememberModalBottomSheetState(
            skipPartiallyExpanded = true
        )

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
                    .padding(
                        start = 24.dp,
                        end = 24.dp,
                        bottom = 12.dp
                    )
        ) {
            Text(
                text = "사진 보내기",
                color = PickiiBlackAlt,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(18.dp))

            PhotoSourceRow(
                icon = Res.drawable.ic_photo_library,
                label = "갤러리",
                onClick = onGalleryClick
            )

            Spacer(modifier = Modifier.height(8.dp))

            PhotoSourceRow(
                icon = Res.drawable.ic_photo_camera,
                label = "카메라",
                onClick = onCameraClick
            )

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

/**
 * 갤러리/카메라 한 줄을 표시한다.
 */
@Composable
private fun PhotoSourceRow(
    icon: DrawableResource,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .clickable(onClick = onClick)
                .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier =
                Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(PickiiSurfaceGraySoft),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = label,
                tint = PickiiGray700,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Text(
            text = label,
            color = PickiiSlateDark,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
