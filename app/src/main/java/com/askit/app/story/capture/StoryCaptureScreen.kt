package com.askit.app.story.capture

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaMetadataRetriever
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import coil3.compose.AsyncImage
import com.askit.app.R
import com.askit.app.media.persistPhotoPickerReadAccess
import com.askit.app.story.STORY_MAX_DURATION_MS
import com.askit.app.story.StoryMediaType
import com.askit.app.story.StoryViewModel
import com.askit.app.story.editor.StoryStage
import com.askit.designsystem.dialogs.AskITAlertDialog
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

private const val HOLD_TO_RECORD_MS = 400L

@Composable
fun StoryCaptureScreen(
    viewModel: StoryViewModel,
    flashEnabled: Boolean,
    useFrontCamera: Boolean,
    galleryThumbUri: String?,
    useFakePreview: Boolean,
    onClose: () -> Unit,
    onOpenCreateSheet: () -> Unit,
    onOpenCreateText: () -> Unit,
    onMediaReady: (String, StoryMediaType, Long?) -> Unit,
    modifier: Modifier = Modifier,
    cameraController: StoryCameraController = remember { StoryCameraController() },
) {
    val context = LocalContext.current
    val activity = LocalActivity.current
    val createTextLabel = stringResource(R.string.story_create_text)
    val moreCreateLabel = stringResource(R.string.story_more_create)
    val galleryLabel = stringResource(R.string.story_gallery)
    val shutterLabel = stringResource(R.string.story_shutter)
    val recordLabel = stringResource(R.string.story_record_video)
    val stopLabel = stringResource(R.string.story_stop_recording)
    var hasCameraPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    var hasAudioPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)
    }
    var cameraRequested by rememberSaveable { mutableStateOf(false) }
    var cameraNeedsSettings by rememberSaveable { mutableStateOf(false) }
    var audioRequested by rememberSaveable { mutableStateOf(false) }
    var showAudioExplanation by rememberSaveable { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var isLoadingMedia by remember { mutableStateOf(false) }
    val isBusy = cameraController.isBusy || isLoadingMedia
    val isRecording = cameraController.isRecording
    val recordingProgress = (cameraController.recordedDurationMs.toFloat() / STORY_MAX_DURATION_MS).coerceIn(0f, 1f)
    val latestMediaReady by rememberUpdatedState(onMediaReady)

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
            cameraNeedsSettings = !granted && activity?.shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) == false
        },
    )
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasAudioPermission = granted
            cameraController.error = if (granted) R.string.story_hold_again else R.string.story_video_without_sound
        },
    )

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        hasCameraPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        hasAudioPermission =
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
    }

    LaunchedEffect(useFakePreview) {
        if (!useFakePreview && !hasCameraPermission && !cameraRequested) {
            cameraRequested = true
            if (activity?.shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) != true) {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri == null) return@rememberLauncherForActivityResult
            scope.launch {
                isLoadingMedia = true
                val (mediaType, durationMs) = try {
                    withContext(Dispatchers.IO) {
                        val type = context.contentResolver.getType(uri)
                        val mediaType = when {
                            type?.startsWith("video/") == true -> StoryMediaType.Video
                            type?.startsWith("image/") == true -> StoryMediaType.Photo
                            else -> throw java.io.IOException("Unsupported media type")
                        }
                        context.contentResolver.openAssetFileDescriptor(uri, "r")?.use { }
                            ?: throw java.io.FileNotFoundException("Selected media is unavailable")
                        val duration = if (mediaType == StoryMediaType.Video) {
                            val retriever = MediaMetadataRetriever()
                            try {
                                retriever.setDataSource(context, uri)
                                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                                    ?.toLongOrNull()?.takeIf { it > 0 }
                                    ?: throw java.io.IOException("Video duration is unavailable")
                            } finally {
                                retriever.release()
                            }
                        } else {
                            null
                        }
                        persistPhotoPickerReadAccess(context.contentResolver, listOf(uri))
                        mediaType to duration
                    }
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    cameraController.error = R.string.story_media_unavailable
                    return@launch
                } finally {
                    isLoadingMedia = false
                }
                latestMediaReady(uri.toString(), mediaType, durationMs)
            }
        },
    )

    val errorMessage = cameraController.error?.let { stringResource(it) }
    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            snackbarHostState.showSnackbar(errorMessage, withDismissAction = true)
            cameraController.error = null
        }
    }

    val startRecording by rememberUpdatedState {
        if (cameraController.isReady && !isBusy) {
            hasAudioPermission =
                ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
            if (!hasAudioPermission && !audioRequested) {
                showAudioExplanation = true
            } else {
                cameraController.startRecording(hasAudioPermission)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("story_capture"),
    ) {
        if (hasCameraPermission || useFakePreview) {
            StoryStage {
                StoryCamera(
                    flashEnabled = flashEnabled,
                    useFrontCamera = useFrontCamera,
                    useFakePreview = useFakePreview,
                    controller = cameraController,
                    modifier = Modifier.fillMaxSize(),
                    onPhotoCaptured = { uri ->
                        latestMediaReady(uri.toString(), StoryMediaType.Photo, null)
                    },
                    onVideoCaptured = { uri, duration ->
                        latestMediaReady(uri.toString(), StoryMediaType.Video, duration)
                    },
                )
            }
        }

        if (!useFakePreview && (!hasCameraPermission || cameraController.cameraUnavailable)) {
            Column(
                modifier = Modifier.align(Alignment.Center).padding(horizontal = 72.dp, vertical = 120.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = stringResource(if (hasCameraPermission) R.string.story_camera_unavailable else R.string.story_camera_permission),
                    color = Color.White,
                )
                Button(onClick = {
                    when {
                        hasCameraPermission -> cameraController.retry()

                        cameraNeedsSettings -> {
                            try {
                                context.startActivity(
                                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, "package:${context.packageName}".toUri()),
                                )
                            } catch (_: ActivityNotFoundException) {
                                cameraController.error = R.string.story_settings_unavailable
                            }
                        }

                        else -> {
                            cameraRequested = true
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    }
                }) {
                    Text(
                        stringResource(
                            when {
                                hasCameraPermission -> R.string.explore_retry
                                cameraNeedsSettings -> R.string.story_open_settings
                                else -> R.string.story_allow_camera
                            },
                        ),
                    )
                }
            }
        } else if (isLoadingMedia || (!useFakePreview && !cameraController.isReady)) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color.White)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 12.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.story_close),
                    tint = Color.White,
                )
            }
            Row {
                IconButton(
                    onClick = { viewModel.toggleFlash() },
                    enabled = cameraController.isReady && cameraController.hasFlash && !isBusy,
                ) {
                    Icon(
                        imageVector = if (flashEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                        contentDescription = stringResource(R.string.story_flash),
                        tint = Color.White,
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            IconButton(
                onClick = onOpenCreateText,
                enabled = !isBusy,
                modifier = Modifier.semantics {
                    contentDescription = createTextLabel
                },
            ) {
                Text(
                    text = "Aa",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                )
            }
            IconButton(
                onClick = onOpenCreateSheet,
                enabled = !isBusy,
                modifier = Modifier.semantics {
                    contentDescription = moreCreateLabel
                },
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .border(1.5.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    enabled = !isBusy,
                    onClick = {
                        galleryLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo),
                        )
                    },
                    modifier = Modifier.semantics {
                        contentDescription = galleryLabel
                    },
                ) {
                    if (galleryThumbUri != null) {
                        AsyncImage(
                            model = galleryThumbUri,
                            contentDescription = null,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop,
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Photo,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(32.dp),
                        )
                    }
                }

                Box(contentAlignment = Alignment.Center) {
                    if (isRecording) {
                        CircularProgressIndicator(
                            progress = { recordingProgress },
                            modifier = Modifier.size(84.dp),
                            color = MaterialTheme.colorScheme.secondary,
                            trackColor = Color.White.copy(alpha = 0.3f),
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.25f))
                            .semantics {
                                role = Role.Button
                                contentDescription = if (isRecording) stopLabel else shutterLabel
                                if (!cameraController.isReady || (isBusy && !isRecording)) disabled()
                                onClick {
                                    if (cameraController.isRecording) {
                                        cameraController.stopRecording()
                                    } else if (!isLoadingMedia) {
                                        cameraController.takePhoto()
                                    }
                                    true
                                }
                                onLongClick(label = recordLabel) {
                                    startRecording()
                                    true
                                }
                            }
                            .pointerInput(cameraController) {
                                detectTapGestures(
                                    onPress = {
                                        if (cameraController.isRecording) {
                                            cameraController.stopRecording()
                                            return@detectTapGestures
                                        }
                                        if (!cameraController.isReady || cameraController.isBusy || isLoadingMedia) return@detectTapGestures
                                        val released = withTimeoutOrNull(HOLD_TO_RECORD_MS) { tryAwaitRelease() }
                                        when (released) {
                                            true -> cameraController.takePhoto()

                                            false -> Unit

                                            null -> {
                                                startRecording()
                                                try {
                                                    tryAwaitRelease()
                                                } finally {
                                                    cameraController.stopRecording()
                                                }
                                            }
                                        }
                                    },
                                )
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(if (isRecording) 32.dp else 56.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isRecording) MaterialTheme.colorScheme.error else Color.White,
                                ),
                        )
                    }
                }

                IconButton(
                    onClick = { viewModel.toggleCameraFacing() },
                    enabled = cameraController.canFlip && !isBusy,
                ) {
                    Icon(
                        imageVector = Icons.Default.Cameraswitch,
                        contentDescription = stringResource(R.string.story_flip_camera),
                        tint = Color.White,
                    )
                }
            }
            if (audioRequested && !hasAudioPermission) {
                Text(stringResource(R.string.story_video_without_sound), color = Color.White)
            }
        }
        SnackbarHost(snackbarHostState, Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 160.dp))
    }

    if (showAudioExplanation) {
        AskITAlertDialog(
            onDismissRequest = { showAudioExplanation = false },
            title = { Text(stringResource(R.string.story_microphone_title)) },
            text = { Text(stringResource(R.string.story_microphone_explanation)) },
            confirmButton = {
                TextButton(onClick = {
                    showAudioExplanation = false
                    audioRequested = true
                    audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }) { Text(stringResource(R.string.story_allow_microphone)) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showAudioExplanation = false
                    audioRequested = true
                    cameraController.error = R.string.story_hold_again
                }) { Text(stringResource(R.string.story_without_sound)) }
            },
        )
    }
}
