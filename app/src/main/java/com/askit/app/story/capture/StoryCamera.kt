package com.askit.app.story.capture

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.camera.compose.CameraXViewfinder
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraSelector
import androidx.camera.core.CameraState
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.Observer
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.askit.app.R
import com.askit.app.story.STORY_MAX_DURATION_MS
import java.io.File

class StoryCameraController {
    internal var capturePhoto: (() -> Unit)? = null
    internal var startVideo: ((Boolean) -> Unit)? = null
    internal var stopVideo: (() -> Unit)? = null
    var isReady by mutableStateOf(false)
        internal set
    var isBusy by mutableStateOf(false)
        internal set
    var isRecording by mutableStateOf(false)
        internal set
    var recordedDurationMs by mutableLongStateOf(0L)
        internal set
    var hasFlash by mutableStateOf(false)
        internal set
    var canFlip by mutableStateOf(false)
        internal set
    var cameraUnavailable by mutableStateOf(false)
        internal set
    internal var error by mutableStateOf<Int?>(null)
    internal var retryKey by mutableIntStateOf(0)

    fun takePhoto() {
        if (isReady && !isBusy) capturePhoto?.invoke()
    }

    fun startRecording(enableAudio: Boolean) {
        if (isReady && !isBusy) startVideo?.invoke(enableAudio)
    }

    fun stopRecording() {
        stopVideo?.invoke()
    }

    fun retry() {
        retryKey++
    }
}

@Composable
fun StoryCamera(
    flashEnabled: Boolean,
    useFrontCamera: Boolean,
    useFakePreview: Boolean,
    controller: StoryCameraController,
    modifier: Modifier = Modifier,
    onPhotoCaptured: (Uri) -> Unit,
    onVideoCaptured: (Uri, Long) -> Unit,
) {
    if (useFakePreview) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.secondaryContainer),
        )
        return
    }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val executor = remember { ContextCompat.getMainExecutor(context) }
    var surfaceRequest by remember { mutableStateOf<androidx.camera.core.SurfaceRequest?>(null) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var cameraControl by remember { mutableStateOf<CameraControl?>(null) }
    val latestPhotoCaptured by rememberUpdatedState(onPhotoCaptured)
    val latestVideoCaptured by rememberUpdatedState(onVideoCaptured)

    DisposableEffect(lifecycleOwner, useFrontCamera, controller, controller.retryKey) {
        var active = true
        var generation = 0
        var provider: ProcessCameraProvider? = null
        var activeRecording: Recording? = null
        var stopRequested = false
        val preview = Preview.Builder().build()
        val photo = ImageCapture.Builder().build()
        val video = VideoCapture.withOutput(Recorder.Builder().build())
        var cameraStates: androidx.lifecycle.LiveData<CameraState>? = null
        controller.isReady = false
        controller.cameraUnavailable = false
        controller.error = null

        val cameraObserver = Observer<CameraState> { state ->
            if (active) {
                controller.isReady = state.type == CameraState.Type.OPEN && state.error == null
                controller.cameraUnavailable = state.error != null
            }
        }
        val lifecycleObserver = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                generation++
                stopRequested = true
                activeRecording?.stop()
                controller.isRecording = false
                controller.isReady = false
            }
        }
        lifecycleOwner.lifecycle.addObserver(lifecycleObserver)
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        val listener = Runnable {
            // Provider initialization is shared; cancelling its future does not cancel this listener.
            if (!active) return@Runnable
            try {
                val cameraProvider = cameraProviderFuture.get()
                provider = cameraProvider
                preview.setSurfaceProvider(executor) { request ->
                    if (active) surfaceRequest = request else request.willNotProvideSurface()
                }
                controller.canFlip = cameraProvider.hasCamera(
                    if (useFrontCamera) CameraSelector.DEFAULT_BACK_CAMERA else CameraSelector.DEFAULT_FRONT_CAMERA,
                )
                val camera = cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    if (useFrontCamera) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    photo,
                    video,
                )
                imageCapture = photo
                controller.hasFlash = camera.cameraInfo.hasFlashUnit()
                cameraControl = camera.cameraControl
                cameraStates = camera.cameraInfo.cameraState
                cameraStates.observe(lifecycleOwner, cameraObserver)
            } catch (_: Exception) {
                controller.isReady = false
                controller.cameraUnavailable = true
            }
        }
        cameraProviderFuture.addListener(listener, executor)

        controller.capturePhoto = {
            val captureGeneration = generation
            var outputFile: File? = null
            try {
                controller.isBusy = true
                val file = createCaptureFile(context, "story-photo", ".jpg")
                outputFile = file
                photo.takePicture(
                    ImageCapture.OutputFileOptions.Builder(file).build(),
                    executor,
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                            if (active) controller.isBusy = false
                            if (active && captureGeneration == generation) {
                                latestPhotoCaptured(Uri.fromFile(file))
                            } else {
                                file.delete()
                            }
                        }

                        override fun onError(exception: ImageCaptureException) {
                            file.delete()
                            if (active) {
                                controller.isBusy = false
                                if (captureGeneration == generation) controller.error = R.string.story_photo_failed
                            }
                        }
                    },
                )
            } catch (_: Exception) {
                outputFile?.delete()
                controller.isBusy = false
                controller.error = R.string.story_photo_failed
            }
        }

        controller.startVideo = { enableAudio ->
            val captureGeneration = generation
            var outputFile: File? = null
            try {
                controller.isBusy = true
                controller.recordedDurationMs = 0L
                stopRequested = false
                val file = createCaptureFile(context, "story-video", ".mp4")
                outputFile = file
                val pending = video.output.prepareRecording(
                    context,
                    FileOutputOptions.Builder(file).setDurationLimitMillis(STORY_MAX_DURATION_MS).build(),
                )
                if (enableAudio) {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) !=
                        PackageManager.PERMISSION_GRANTED
                    ) {
                        throw SecurityException("Microphone permission was revoked")
                    }
                    pending.withAudioEnabled()
                }
                activeRecording = pending.start(executor) { event ->
                    when (event) {
                        is VideoRecordEvent.Start -> {
                            if (active && captureGeneration == generation && !stopRequested) {
                                controller.isRecording = true
                            }
                        }

                        is VideoRecordEvent.Status -> {
                            if (active && captureGeneration == generation) {
                                controller.recordedDurationMs = event.recordingStats.recordedDurationNanos / 1_000_000
                            }
                        }

                        is VideoRecordEvent.Finalize -> {
                            activeRecording = null
                            if (active) {
                                controller.isBusy = false
                                controller.isRecording = false
                            }
                            if (!active || captureGeneration != generation) {
                                file.delete()
                            } else if (!event.hasError() || event.error == VideoRecordEvent.Finalize.ERROR_DURATION_LIMIT_REACHED) {
                                latestVideoCaptured(Uri.fromFile(file), event.recordingStats.recordedDurationNanos / 1_000_000)
                            } else {
                                file.delete()
                                controller.error = R.string.story_video_failed
                            }
                        }

                        else -> Unit
                    }
                }
            } catch (_: Exception) {
                outputFile?.delete()
                controller.isBusy = false
                controller.isRecording = false
                controller.error = R.string.story_video_failed
            }
        }

        controller.stopVideo = {
            stopRequested = true
            activeRecording?.stop()
            controller.isRecording = false
        }

        onDispose {
            active = false
            controller.capturePhoto = null
            controller.startVideo = null
            controller.stopVideo = null
            controller.isReady = false
            controller.isBusy = false
            controller.isRecording = false
            controller.hasFlash = false
            controller.canFlip = false
            activeRecording?.stop()
            lifecycleOwner.lifecycle.removeObserver(lifecycleObserver)
            cameraStates?.removeObserver(cameraObserver)
            provider?.unbind(preview, photo, video)
            surfaceRequest = null
            imageCapture = null
            cameraControl = null
        }
    }

    DisposableEffect(flashEnabled, cameraControl, controller.hasFlash, imageCapture) {
        var active = true
        imageCapture?.flashMode = if (flashEnabled && controller.hasFlash) {
            ImageCapture.FLASH_MODE_ON
        } else {
            ImageCapture.FLASH_MODE_OFF
        }
        if (controller.hasFlash) {
            val torch = cameraControl?.enableTorch(flashEnabled)
            torch?.addListener({
                if (active) {
                    try {
                        torch.get()
                    } catch (_: Exception) {
                        controller.error = R.string.story_flash_failed
                    }
                }
            }, executor)
        }
        onDispose { active = false }
    }

    Box(modifier = modifier.fillMaxSize()) {
        val request = surfaceRequest
        if (request != null) {
            CameraXViewfinder(
                surfaceRequest = request,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            )
        }
    }
}

private fun createCaptureFile(context: Context, prefix: String, suffix: String): File {
    val directory = File(context.cacheDir, "story-captures").apply { mkdirs() }
    return File.createTempFile(prefix, suffix, directory)
}
