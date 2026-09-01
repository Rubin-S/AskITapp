package com.askit.app.story.capture

import android.Manifest
import android.net.Uri
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.askit.app.R
import com.askit.app.media.persistPhotoPickerReadAccess
import com.askit.app.story.STORY_MAX_DURATION_MS
import com.askit.app.story.StoryMediaType
import com.askit.app.story.StoryViewModel
import kotlinx.coroutines.delay
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
) {
    val context = LocalContext.current
    val cameraController = remember { StoryCameraController() }
    var hasCameraPermission by remember { mutableStateOf(false) }
    var hasAudioPermission by remember { mutableStateOf(false) }
    var isRecording by remember { mutableStateOf(false) }
    var recordingProgress by remember { mutableFloatStateOf(0f) }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCameraPermission = granted },
    )
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasAudioPermission = granted },
    )

    LaunchedEffect(Unit) {
        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri == null) return@rememberLauncherForActivityResult
            persistPhotoPickerReadAccess(context.contentResolver, listOf(uri))
            val type = context.contentResolver.getType(uri)
            val mediaType = if (type?.startsWith("video") == true) {
                StoryMediaType.Video
            } else {
                StoryMediaType.Photo
            }
            onMediaReady(uri.toString(), mediaType, null)
        },
    )

    LaunchedEffect(isRecording) {
        if (!isRecording) {
            recordingProgress = 0f
            return@LaunchedEffect
        }
        val start = System.currentTimeMillis()
        while (isRecording) {
            val elapsed = System.currentTimeMillis() - start
            recordingProgress = (elapsed.toFloat() / STORY_MAX_DURATION_MS).coerceIn(0f, 1f)
            if (elapsed >= STORY_MAX_DURATION_MS) {
                cameraController.stopRecording()
                isRecording = false
                break
            }
            delay(50L)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("story_capture"),
    ) {
        StoryCamera(
            flashEnabled = flashEnabled,
            useFrontCamera = useFrontCamera,
            useFakePreview = useFakePreview || !hasCameraPermission,
            enableAudio = hasAudioPermission,
            controller = cameraController,
            modifier = Modifier.fillMaxSize(),
            onPhotoCaptured = { uri ->
                onMediaReady(uri.toString(), StoryMediaType.Photo, null)
            },
            onVideoCaptured = { uri, duration ->
                onMediaReady(uri.toString(), StoryMediaType.Video, duration)
            },
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
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
                IconButton(onClick = { viewModel.toggleFlash() }) {
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
                modifier = Modifier.semantics {
                    contentDescription = context.getString(R.string.story_create_text)
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
                modifier = Modifier.semantics {
                    contentDescription = context.getString(R.string.story_more_create)
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
                    onClick = {
                        galleryLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo),
                        )
                    },
                    modifier = Modifier.semantics {
                        contentDescription = context.getString(R.string.story_gallery)
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
                                contentDescription = context.getString(R.string.story_shutter)
                            }
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onPress = {
                                        if (isRecording) return@detectTapGestures
                                        val releasedQuickly = withTimeoutOrNull(HOLD_TO_RECORD_MS) {
                                            tryAwaitRelease()
                                            true
                                        } == true
                                        if (releasedQuickly) {
                                            cameraController.takePhoto()
                                            return@detectTapGestures
                                        }
                                        isRecording = true
                                        cameraController.startRecording()
                                        tryAwaitRelease()
                                        isRecording = false
                                        cameraController.stopRecording()
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

                IconButton(onClick = { viewModel.toggleCameraFacing() }) {
                    Icon(
                        imageVector = Icons.Default.Cameraswitch,
                        contentDescription = stringResource(R.string.story_flip_camera),
                        tint = Color.White,
                    )
                }
            }
        }
    }
}
