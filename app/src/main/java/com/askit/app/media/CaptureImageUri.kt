package com.askit.app.media

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

internal fun createCaptureImageUri(context: Context): Uri {
    val file = File(context.cacheDir, "askit_capture_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}
