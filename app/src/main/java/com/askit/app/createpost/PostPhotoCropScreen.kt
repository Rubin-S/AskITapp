package com.askit.app.createpost

import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.core.graphics.withSave
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.askit.app.R
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun PostPhotoCropScreen(
    session: PostCropSession,
    onCancel: () -> Unit,
    onCropped: (PostMediaDraft) -> Unit,
) {
    val context = LocalContext.current.applicationContext
    val scope = rememberCoroutineScope()
    var attempt by remember { mutableIntStateOf(0) }
    val loaded by produceState<Result<Bitmap>?>(null, session.source, attempt) {
        value = null
        value = withContext(Dispatchers.IO) {
            try { Result.success(loadCropBitmap(context, session.source)) }
            catch (e: CancellationException) { throw e }
            catch (e: Exception) { Result.failure(e) }
        }
    }
    var ratioName by rememberSaveable(session.source) { mutableStateOf((session.ratio ?: PostPhotoRatio.LANDSCAPE).name) }
    val ratio = session.ratio ?: PostPhotoRatio.valueOf(ratioName)
    var zoom by rememberSaveable(session.source) { mutableFloatStateOf(1f) }
    var centerX by rememberSaveable(session.source) { mutableFloatStateOf(0.5f) }
    var centerY by rememberSaveable(session.source) { mutableFloatStateOf(0.5f) }
    var saving by remember { mutableStateOf(false) }
    var saveFailed by remember { mutableStateOf(false) }
    val bitmap = loaded?.getOrNull()
    fun reset() { zoom = 1f; centerX = 0.5f; centerY = 0.5f }
    fun shift(dx: Float, dy: Float): Boolean {
        if (saving || bitmap == null) return false
        val window = cropWindow(bitmap, ratio, zoom, centerX + dx, centerY + dy)
        centerX = (window.left + window.width / 2) / bitmap.width
        centerY = (window.top + window.height / 2) / bitmap.height
        return true
    }
    BackHandler { if (!saving) onCancel() }
    val instructions = stringResource(R.string.post_crop_instructions)
    val moveLeft = stringResource(R.string.post_crop_left)
    val moveRight = stringResource(R.string.post_crop_right)
    val moveUp = stringResource(R.string.post_crop_up)
    val moveDown = stringResource(R.string.post_crop_down)

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        BoxWithConstraints(Modifier.fillMaxSize().safeDrawingPadding()) {
            val availableCropHeight = (maxHeight * 0.6f).coerceIn(160.dp, 520.dp)
            Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    TextButton(onClick = onCancel, enabled = !saving) { Text(stringResource(R.string.post_crop_cancel)) }
                    Spacer(Modifier.weight(1f))
                    Button(
                        enabled = bitmap != null && !saving,
                        onClick = {
                            if (saving) return@Button
                            val image = bitmap ?: return@Button
                            val window = cropWindow(image, ratio, zoom, centerX, centerY)
                            saving = true
                            saveFailed = false
                            scope.launch {
                                var output: PostMediaDraft? = null
                                var delivered = false
                                try {
                                    // Finish a small atomic write even if recreation cancels the UI scope.
                                    withContext(Dispatchers.IO + NonCancellable) { output = savePostCrop(context, image, ratio, window) }
                                    coroutineContext.ensureActive()
                                    onCropped(requireNotNull(output))
                                    delivered = true
                                } catch (e: CancellationException) { throw e }
                                catch (e: Exception) { saveFailed = true }
                                finally { if (!delivered) output?.let(::deletePendingCrop); saving = false }
                            }
                        },
                    ) { Text(stringResource(if (session.completed.size == session.sources.lastIndex) R.string.post_crop_done else R.string.post_crop_next)) }
                }
                Column(Modifier.weight(1f).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(stringResource(R.string.post_crop_title), style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(R.string.post_crop_position, session.completed.size + 1, session.sources.size), style = MaterialTheme.typography.labelLarge)
                    Text(instructions, style = MaterialTheme.typography.bodyMedium)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PostPhotoRatio.entries.forEach { choice ->
                            FilterChip(
                                selected = choice == ratio,
                                enabled = !saving && (session.ratio == null || session.ratio == choice),
                                onClick = { ratioName = choice.name; reset() },
                                label = { Text(stringResource(if (choice == PostPhotoRatio.LANDSCAPE) R.string.post_crop_landscape else R.string.post_crop_portrait)) },
                            )
                        }
                    }
                    if (session.ratio != null) Text(stringResource(R.string.post_crop_ratio_locked), style = MaterialTheme.typography.bodySmall)
                    BoxWithConstraints(Modifier.height(availableCropHeight).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        if (bitmap != null) {
                            val frameWidth = minOf(maxWidth, maxHeight * ratio.value)
                            Canvas(
                                Modifier.width(frameWidth).aspectRatio(ratio.value)
                                    .background(Color.Black)
                                    .semantics {
                                        contentDescription = instructions
                                        customActions = listOf(
                                            CustomAccessibilityAction(moveLeft) { shift(-0.05f, 0f) },
                                            CustomAccessibilityAction(moveRight) { shift(0.05f, 0f) },
                                            CustomAccessibilityAction(moveUp) { shift(0f, -0.05f) },
                                            CustomAccessibilityAction(moveDown) { shift(0f, 0.05f) },
                                        )
                                    }
                                    .pointerInput(bitmap, ratio, saving) {
                                        if (!saving) detectTransformGestures { _, pan, scale, _ ->
                                            val previous = cropWindow(bitmap, ratio, zoom, centerX, centerY)
                                            zoom = (zoom * scale).coerceIn(1f, 6f)
                                            val next = cropWindow(bitmap, ratio, zoom,
                                                (previous.left + previous.width / 2 - pan.x * previous.width / size.width) / bitmap.width,
                                                (previous.top + previous.height / 2 - pan.y * previous.height / size.height) / bitmap.height)
                                            centerX = (next.left + next.width / 2) / bitmap.width
                                            centerY = (next.top + next.height / 2) / bitmap.height
                                        }
                                    },
                            ) {
                                val window = cropWindow(bitmap, ratio, zoom, centerX, centerY)
                                drawContext.canvas.nativeCanvas.withSave {
                                    clipRect(0f, 0f, size.width, size.height)
                                    scale(size.width / window.width, size.height / window.height)
                                    translate(-window.left, -window.top)
                                    drawBitmap(bitmap, 0f, 0f, android.graphics.Paint(android.graphics.Paint.FILTER_BITMAP_FLAG))
                                }
                                for (i in 1..2) {
                                    drawLine(Color.White.copy(alpha = 0.5f), Offset(size.width * i / 3, 0f), Offset(size.width * i / 3, size.height), 1.dp.toPx())
                                    drawLine(Color.White.copy(alpha = 0.5f), Offset(0f, size.height * i / 3), Offset(size.width, size.height * i / 3), 1.dp.toPx())
                                }
                            }
                        } else if (loaded == null) {
                            CircularProgressIndicator()
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(stringResource(R.string.post_crop_load_failed))
                                TextButton(onClick = { attempt++ }) { Text(stringResource(R.string.post_crop_retry)) }
                            }
                        }
                    }
                }
                if (saveFailed) Text(stringResource(R.string.post_crop_save_failed), color = MaterialTheme.colorScheme.error)
                if (saving) LinearProgressIndicator(Modifier.fillMaxWidth())
                Text(stringResource(R.string.post_crop_zoom), style = MaterialTheme.typography.labelLarge)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Slider(value = zoom, onValueChange = { zoom = it }, valueRange = 1f..6f,
                        enabled = bitmap != null && !saving, modifier = Modifier.weight(1f).padding(horizontal = 10.dp))
                    TextButton(onClick = { reset() }, enabled = !saving) { Text(stringResource(R.string.post_crop_reset)) }
                }
            }
        }
    }
}
