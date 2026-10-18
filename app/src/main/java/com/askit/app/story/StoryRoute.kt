package com.askit.app.story

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.askit.app.R
import com.askit.app.story.capture.StoryCaptureScreen
import com.askit.app.story.create.StoryCreateCanvas
import com.askit.app.story.editor.StoryEditorScreen
import com.askit.app.story.share.StoryShareSheet
import com.askit.app.story.stickers.StoryStickerTray
import com.askit.designsystem.dialogs.AskITDiscardDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun StoryRoute(
    viewModel: StoryViewModel,
    onBack: () -> Unit,
    onCompleteDraft: (StoryDraft) -> Unit = {},
    onPublishDraft: ((StoryDraft) -> Boolean)? = null,
    onOpenCreateSheet: () -> Unit = {},
    useFakeCapturePreview: Boolean = false,
) {
    val state by viewModel.formState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showDiscardDialog by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var pendingDownload by remember { mutableStateOf<Pair<Uri, StoryMediaType?>?>(null) }

    fun download(target: Pair<Uri, StoryMediaType?>) {
        scope.launch {
            val saved = copyMediaToGallery(context, target.first, target.second)
            Toast.makeText(
                context,
                if (saved) R.string.story_download_saved else R.string.story_download_failed,
                Toast.LENGTH_SHORT,
            ).show()
        }
    }

    val storagePermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        val target = pendingDownload
        pendingDownload = null
        if (target == null) return@rememberLauncherForActivityResult
        if (granted) download(target) else Toast.makeText(context, R.string.story_download_permission_denied, Toast.LENGTH_LONG).show()
    }

    fun requestBack() {
        if (viewModel.isDirty) {
            showDiscardDialog = true
        } else {
            onBack()
        }
    }

    BackHandler {
        when {
            showDiscardDialog -> showDiscardDialog = false
            state.showShareSheet -> viewModel.closeShareSheet()
            state.showStickerTray -> viewModel.closeStickerTray()
            state.screenMode == StoryScreenMode.Editor -> viewModel.backToCapture()
            state.screenMode == StoryScreenMode.CreateText -> viewModel.backToCapture()
            else -> requestBack()
        }
    }

    when (state.screenMode) {
        StoryScreenMode.Capture -> {
            StoryCaptureScreen(
                viewModel = viewModel,
                flashEnabled = state.flashEnabled,
                useFrontCamera = state.useFrontCamera,
                galleryThumbUri = state.galleryThumbUri,
                useFakePreview = useFakeCapturePreview,
                onClose = ::requestBack,
                onOpenCreateSheet = onOpenCreateSheet,
                onOpenCreateText = viewModel::openCreateText,
                onMediaReady = viewModel::onGalleryMediaSelected,
            )
        }

        StoryScreenMode.CreateText -> {
            StoryCreateCanvas(
                viewModel = viewModel,
                backgroundArgb = viewModel.solidBackgroundArgb(),
                textDraft = state.createTextDraft,
                onBack = viewModel::backToCapture,
                onConfirm = viewModel::confirmCreateText,
            )
        }

        StoryScreenMode.Editor -> {
            StoryEditorScreen(
                viewModel = viewModel,
                state = state,
                onBack = viewModel::backToCapture,
                onNext = viewModel::openShareSheet,
                onDownload = {
                    val draft = viewModel.buildValidatedDraft()
                    if (draft?.mediaUri != null) {
                        val pending = draft.mediaUri.toUri() to draft.mediaType
                        val granted = !storyDownloadNeedsLegacyPermission(Build.VERSION.SDK_INT) ||
                            ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                            PackageManager.PERMISSION_GRANTED
                        if (granted) {
                            download(pending)
                        } else {
                            pendingDownload = pending
                            storagePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        }
                    }
                },
            )
        }
    }

    if (state.showStickerTray) {
        StoryStickerTray(
            viewModel = viewModel,
            onDismiss = viewModel::closeStickerTray,
        )
    }

    if (state.showShareSheet) {
        StoryShareSheet(
            viewModel = viewModel,
            state = state,
            solidBackgroundArgb = viewModel.solidBackgroundArgb(),
            onDismiss = viewModel::closeShareSheet,
            onShare = {
                val draft = viewModel.buildValidatedDraft()
                if (draft != null && onPublishDraft?.invoke(draft) != false) {
                    onCompleteDraft(draft)
                    viewModel.startNewDraft()
                    onBack()
                } else if (draft != null) {
                    android.widget.Toast.makeText(
                        context,
                        com.askit.app.R.string.story_action_sign_in,
                        android.widget.Toast.LENGTH_SHORT,
                    ).show()
                }
            },
        )
    }

    if (showDiscardDialog) {
        AskITDiscardDialog(
            onDismissRequest = { showDiscardDialog = false },
            onDiscard = {
                showDiscardDialog = false
                viewModel.startNewDraft()
                onBack()
            },
        )
    }
}

/** Before Android 10 (API 29), inserting into MediaStore needs WRITE_EXTERNAL_STORAGE. */
internal fun storyDownloadNeedsLegacyPermission(sdkInt: Int): Boolean = sdkInt < Build.VERSION_CODES.Q

/**
 * MIME type for the gallery copy. Camera captures are `file://` URIs for which the resolver
 * returns null, so fall back to the draft's media type instead of saving a video as JPEG.
 */
internal fun storyGalleryMimeType(resolvedType: String?, mediaType: StoryMediaType?): String = when {
    !resolvedType.isNullOrBlank() && (resolvedType.startsWith("image/") || resolvedType.startsWith("video/")) -> resolvedType
    mediaType == StoryMediaType.Video -> "video/mp4"
    else -> "image/jpeg"
}

/**
 * Copies the story media into the shared gallery. Runs on [Dispatchers.IO]; returns false instead
 * of throwing when the source is gone, a grant was revoked, or the write is refused, and removes
 * the half-written MediaStore row in that case.
 */
private suspend fun copyMediaToGallery(context: android.content.Context, uri: Uri, mediaType: StoryMediaType?): Boolean =
    withContext(Dispatchers.IO) {
        val resolver = context.contentResolver
        val mimeType = storyGalleryMimeType(runCatching { resolver.getType(uri) }.getOrNull(), mediaType)
        val isVideo = mimeType.startsWith("video")
        val collection = when {
            isVideo && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> MediaStore.Video.Media.getContentUri(
                MediaStore.VOLUME_EXTERNAL_PRIMARY,
            )

            isVideo -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)

            else -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            put(MediaStore.MediaColumns.DISPLAY_NAME, "AskIT_${System.currentTimeMillis()}.${if (isVideo) "mp4" else "jpg"}")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, if (isVideo) "Movies/AskIT" else "Pictures/AskIT")
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
        }
        var dest: Uri? = null
        try {
            val inserted = resolver.insert(collection, values) ?: return@withContext false
            dest = inserted
            val copied = resolver.openInputStream(uri)?.use { input ->
                resolver.openOutputStream(inserted)?.use { output ->
                    input.copyTo(output)
                    true
                }
            } ?: false
            if (!copied) error("Story media unavailable")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.clear()
                values.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(inserted, values, null, null)
            }
            true
        } catch (_: Exception) {
            // SecurityException (no permission / revoked grant), FileNotFoundException (evicted
            // cache file) or IllegalStateException above: never crash the editor.
            dest?.let { runCatching { resolver.delete(it, null, null) } }
            false
        }
    }
