package com.askit.app.story

import android.content.ContentValues
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.askit.app.story.capture.StoryCaptureScreen
import com.askit.app.story.create.StoryCreateCanvas
import com.askit.app.story.editor.StoryEditorScreen
import com.askit.app.story.share.StoryShareSheet
import com.askit.app.story.stickers.StoryStickerTray
import com.askit.designsystem.dialogs.AskITDiscardDialog

@Composable
fun StoryRoute(
    viewModel: StoryViewModel,
    onBack: () -> Unit,
    onCompleteDraft: (StoryDraft) -> Unit = {},
    onOpenCreateSheet: () -> Unit = {},
    useFakeCapturePreview: Boolean = false,
) {
    val state by viewModel.formState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showDiscardDialog by rememberSaveable { mutableStateOf(false) }

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
                        copyMediaToGallery(context, Uri.parse(draft.mediaUri))
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
                if (draft != null) {
                    onCompleteDraft(draft)
                    viewModel.startNewDraft()
                    onBack()
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

private fun copyMediaToGallery(context: android.content.Context, uri: Uri) {
    val resolver = context.contentResolver
    val mimeType = resolver.getType(uri).orEmpty()
    val collection = if (mimeType.startsWith("video")) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }
    } else {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }
    }
    val values = ContentValues().apply {
        put(MediaStore.MediaColumns.MIME_TYPE, mimeType.ifBlank { "image/jpeg" })
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }
    }
  val dest = resolver.insert(collection, values) ?: return
    resolver.openInputStream(uri)?.use { input ->
        resolver.openOutputStream(dest)?.use { output ->
            input.copyTo(output)
        }
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        values.clear()
        values.put(MediaStore.MediaColumns.IS_PENDING, 0)
        resolver.update(dest, values, null, null)
    }
}
