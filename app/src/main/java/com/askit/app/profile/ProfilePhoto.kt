package com.askit.app.profile

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.askit.app.R
import com.askit.app.auth.isSignupPhotoReadable
import com.askit.app.media.createCaptureImageUri
import com.askit.app.media.persistPhotoPickerReadAccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Opens the system photo picker; a readable image is passed to [onPick]. Returns the launcher. */
@Composable
internal fun rememberPhotoLibrary(onPick: (String) -> Unit): () -> Unit {
    val context = LocalContext.current
    val deliver = rememberPhotoDelivery(onPick)
    val library = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            persistPhotoPickerReadAccess(context.contentResolver, listOf(uri))
            deliver(uri)
        }
    }
    return {
        runCatching { library.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
            .onFailure { photoUnavailable(context) }
    }
}

/**
 * Photo source sheet (camera, library, remove). Stays composed while hidden so the camera and picker
 * results still arrive after the sheet closes. The camera asks for CAMERA first, as the manifest
 * declares it; a permanent denial offers the app's system settings instead of failing silently.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ProfilePhotoSource(
    visible: Boolean,
    hasPhoto: Boolean,
    onDismiss: () -> Unit,
    onPick: (String) -> Unit,
    onRemove: () -> Unit,
) {
    val context = LocalContext.current
    val activity = LocalActivity.current
    val deliver = rememberPhotoDelivery(onPick)
    val openLibrary = rememberPhotoLibrary(onPick)
    var pendingCapture by rememberSaveable(stateSaver = UriSaver) { mutableStateOf<Uri?>(null) }
    var showSettingsPrompt by rememberSaveable { mutableStateOf(false) }
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        val captured = pendingCapture
        pendingCapture = null
        if (success && captured != null) deliver(captured)
    }
    fun capture() {
        runCatching {
            val uri = createCaptureImageUri(context)
            pendingCapture = uri
            camera.launch(uri)
        }.onFailure {
            pendingCapture = null
            photoUnavailable(context)
        }
    }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        val canAskAgain = activity?.shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) != false
        when (cameraPermissionOutcome(granted, canAskAgain)) {
            CameraPermissionOutcome.Capture -> capture()
            CameraPermissionOutcome.Explain -> Toast.makeText(context, R.string.profile_camera_denied, Toast.LENGTH_LONG).show()
            CameraPermissionOutcome.OpenSettings -> showSettingsPrompt = true
        }
    }
    if (visible) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ) {
            Column(Modifier.fillMaxWidth().padding(bottom = 16.dp).testTag("profile_photo_sheet")) {
                SheetRow(R.string.profile_photo_camera, "profile_photo_camera") {
                    onDismiss()
                    val granted =
                        ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                    if (granted) capture() else permission.launch(Manifest.permission.CAMERA)
                }
                SheetRow(R.string.profile_photo_library, "profile_photo_library") {
                    onDismiss()
                    openLibrary()
                }
                if (hasPhoto) {
                    SheetRow(R.string.profile_photo_remove, "profile_photo_remove") {
                        onDismiss()
                        onRemove()
                    }
                }
            }
        }
    }
    if (showSettingsPrompt) {
        AlertDialog(
            onDismissRequest = { showSettingsPrompt = false },
            title = { Text(stringResource(R.string.profile_camera_off_title)) },
            text = { Text(stringResource(R.string.profile_camera_off_body)) },
            confirmButton = {
                TextButton(onClick = {
                    showSettingsPrompt = false
                    openAppSettings(context)
                }) { Text(stringResource(R.string.profile_open_settings)) }
            },
            dismissButton = { TextButton(onClick = { showSettingsPrompt = false }) { Text(stringResource(R.string.profile_cancel)) } },
        )
    }
}

@Composable
private fun SheetRow(label: Int, tag: String, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(stringResource(label)) },
        modifier = Modifier.clickable(role = Role.Button, onClick = onClick).heightIn(min = 48.dp).testTag(tag),
    )
}

/** Checks the image is readable off the main thread before handing it over. */
@Composable
private fun rememberPhotoDelivery(onPick: (String) -> Unit): (Uri) -> Unit {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val latest by rememberUpdatedState(onPick)
    return { uri ->
        scope.launch {
            if (withContext(Dispatchers.IO) { isSignupPhotoReadable(context.contentResolver, uri.toString()) }) {
                latest(uri.toString())
            } else {
                photoUnavailable(context)
            }
        }
    }
}

internal enum class CameraPermissionOutcome { Capture, Explain, OpenSettings }

/** After a denial, "no rationale" means the system won't ask again, so only Settings can grant it. */
internal fun cameraPermissionOutcome(granted: Boolean, canAskAgain: Boolean): CameraPermissionOutcome = when {
    granted -> CameraPermissionOutcome.Capture
    canAskAgain -> CameraPermissionOutcome.Explain
    else -> CameraPermissionOutcome.OpenSettings
}

private fun photoUnavailable(context: Context) = Toast.makeText(context, R.string.profile_photo_unavailable, Toast.LENGTH_LONG).show()

private fun openAppSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
    runCatching { context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }.onFailure { photoUnavailable(context) }
}

private val UriSaver = Saver<Uri?, String>(
    save = { it?.toString().orEmpty() },
    restore = { value -> value.takeIf { it.isNotEmpty() }?.let(Uri::parse) },
)
