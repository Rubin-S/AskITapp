package com.askit.app.profile

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.askit.app.R
import com.askit.app.auth.isSignupPhotoReadable
import com.askit.app.media.createCaptureImageUri
import com.askit.app.media.persistPhotoPickerReadAccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfilePhotoActions(val launchLibrary: () -> Unit, val launchCamera: () -> Unit)

@Composable
fun rememberProfilePhotoActions(onUri: (String) -> Unit): ProfilePhotoActions {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val latestUri by rememberUpdatedState(onUri)
    fun failure() {
        Toast.makeText(context, R.string.profile_photo_unavailable, Toast.LENGTH_LONG).show()
    }
    fun deliver(uri: Uri) {
        scope.launch {
            if (withContext(Dispatchers.IO) { isSignupPhotoReadable(context.contentResolver, uri.toString()) }) {
                latestUri(uri.toString())
            } else {
                failure()
            }
        }
    }
    var pendingCapture by rememberSaveable(stateSaver = CaptureUriSaver) {
        mutableStateOf<Uri?>(null)
    }
    val library = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        persistPhotoPickerReadAccess(context.contentResolver, listOf(uri))
        deliver(uri)
    }
    val camera = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
    ) { success ->
        val captured = pendingCapture
        pendingCapture = null
        if (success && captured != null) deliver(captured)
    }
    return ProfilePhotoActions(
        launchLibrary = {
            runCatching {
                library.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }.onFailure { failure() }
        },
        launchCamera = {
            runCatching {
                val uri = createCaptureImageUri(context)
                pendingCapture = uri
                camera.launch(uri)
            }.onFailure {
                pendingCapture = null
                failure()
            }
        },
    )
}

private val CaptureUriSaver = Saver<Uri?, String>(
    save = { it?.toString().orEmpty() },
    restore = { value -> value.takeIf { it.isNotEmpty() }?.let(Uri::parse) },
)
