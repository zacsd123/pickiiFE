package com.example.pickii.ui.chat

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * 카메라로 촬영한 사진을 저장할 임시 파일을 만들고, [FileProvider]를 통해 접근 가능한 [Uri]로 반환한다.
 */
fun createImageCaptureUri(context: Context): Uri {
    val imagesDir = File(context.cacheDir, "images").apply { mkdirs() }

    val fileName = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(java.util.Date())
    val file = File(imagesDir, "camera_$fileName.jpg")

    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file,
    )
}
