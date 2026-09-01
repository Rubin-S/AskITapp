package com.askit.app.story.capture

import android.content.Context
import android.net.Uri
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.compose.CameraXViewfinder
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Recording
import androidx.camera.video.Recorder
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.io.File

class StoryCameraController {
    internal var capturePhoto: (() -> Unit)? = null
    internal var startVideo: (() -> Unit)? = null
    internal var stopVideo: (() -> Unit)? = null

    fun takePhoto() {
        capturePhoto?.invoke()
    }

    fun startRecording() {
        startVideo?.invoke()
    }

    fun stopRecording() {
        stopVideo?.invoke()
    }
}

@Composable
fun StoryCamera(
    flashEnabled: Boolean,
    useFrontCamera: Boolean,
    useFakePreview: Boolean,
    enableAudio: Boolean,
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
    var videoCapture by remember { mutableStateOf<VideoCapture<Recorder>?>(null) }
    var cameraControl by remember { mutableStateOf<CameraControl?>(null) }
    var hasFlashUnit by remember { mutableStateOf(false) }
    var activeRecording by remember { mutableStateOf<Recording?>(null) }
    var recordingStartMs by remember { mutableStateOf(0L) }

    DisposableEffect(lifecycleOwner, useFrontCamera) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        val listener = Runnable {
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build()
            preview.setSurfaceProvider { request ->
                surfaceRequest = request
            }
            val imageCaptureUseCase = ImageCapture.Builder().build()
            val recorder = Recorder.Builder().build()
            val videoCaptureUseCase = VideoCapture.withOutput(recorder)
            imageCapture = imageCaptureUseCase
            videoCapture = videoCaptureUseCase
            val cameraSelector = if (useFrontCamera) {
                CameraSelector.DEFAULT_FRONT_CAMERA
            } else {
                CameraSelector.DEFAULT_BACK_CAMERA
            }
            runCatching {
                cameraProvider.unbindAll()
                val camera = cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCaptureUseCase,
                    videoCaptureUseCase,
                )
                hasFlashUnit = camera.cameraInfo.hasFlashUnit()
                cameraControl = camera.cameraControl
            }
        }
        cameraProviderFuture.addListener(listener, executor)
        onDispose {
            activeRecording?.stop()
            activeRecording = null
            cameraControl = null
            hasFlashUnit = false
            runCatching {
                if (cameraProviderFuture.isDone) {
                    cameraProviderFuture.get().unbindAll()
                }
            }
        }
    }

    LaunchedEffect(flashEnabled, cameraControl, hasFlashUnit, imageCapture) {
        val capture = imageCapture
        if (capture != null) {
            capture.flashMode = if (flashEnabled) {
                ImageCapture.FLASH_MODE_ON
            } else {
                ImageCapture.FLASH_MODE_OFF
            }
        }
        if (hasFlashUnit) {
            runCatching { cameraControl?.enableTorch(flashEnabled) }
        }
    }

    controller.capturePhoto = {
        imageCapture?.let { capture ->
            val outputFile = createCaptureFile(context, "story-photo", ".jpg")
            val outputOptions = ImageCapture.OutputFileOptions.Builder(outputFile).build()
            capture.takePicture(
                outputOptions,
                executor,
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                        onPhotoCaptured(Uri.fromFile(outputFile))
                    }

                    override fun onError(exception: ImageCaptureException) {
                        outputFile.delete()
                    }
                },
            )
        }
    }

    controller.startVideo = {
        val capture = videoCapture
        if (capture != null && activeRecording == null) {
            val outputFile = createCaptureFile(context, "story-video", ".mp4")
            val outputOptions = FileOutputOptions.Builder(outputFile).build()
            recordingStartMs = System.currentTimeMillis()
            val pending = capture.output.prepareRecording(context, outputOptions)
            val withMic = if (enableAudio) {
                pending.withAudioEnabled()
            } else {
                pending
            }
            activeRecording = withMic.start(executor) { event ->
                    when (event) {
                        is VideoRecordEvent.Finalize -> {
                            activeRecording = null
                            if (event.hasError()) {
                                outputFile.delete()
                            } else {
                                val duration = System.currentTimeMillis() - recordingStartMs
                                onVideoCaptured(Uri.fromFile(outputFile), duration)
                            }
                        }
                        else -> Unit
                    }
                }
        }
    }

    controller.stopVideo = {
        activeRecording?.stop()
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
    return File(directory, "$prefix-${System.currentTimeMillis()}$suffix")
}
