package com.askit.app.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.askit.app.media.createCaptureImageUri
import com.askit.app.media.persistPhotoPickerReadAccess

class ProfilePhotoActions(
    val launchLibrary: () -> Unit,
    val launchCamera: () -> Unit,
)

@Composable
fun rememberProfilePhotoActions(onUri: (String) -> Unit): ProfilePhotoActions {
    val context = LocalContext.current
    var pendingCapture by rememberSaveable(stateSaver = CaptureUriSaver) {
        mutableStateOf<Uri?>(null)
    }
    val library = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        persistPhotoPickerReadAccess(context.contentResolver, listOf(uri))
        onUri(uri.toString())
    }
    val camera = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
    ) { success ->
        val captured = pendingCapture
        pendingCapture = null
        if (success && captured != null) onUri(captured.toString())
    }
    return ProfilePhotoActions(
        launchLibrary = {
            library.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        },
        launchCamera = {
            val uri = createCaptureImageUri(context)
            pendingCapture = uri
            camera.launch(uri)
        },
    )
}

private val CaptureUriSaver = Saver<Uri?, String>(
    save = { it?.toString().orEmpty() },
    restore = { value -> value.takeIf { it.isNotEmpty() }?.let(Uri::parse) },
)
