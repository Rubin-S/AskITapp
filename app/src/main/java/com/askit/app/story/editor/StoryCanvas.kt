package com.askit.app.story.editor

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.askit.app.R
import com.askit.app.story.StoryLayer
import com.askit.app.story.StoryMediaType
import com.askit.app.story.StoryTextAlignment
import com.askit.app.story.StoryTransform
import kotlin.math.roundToInt

@Composable
fun StoryCanvas(
    mediaUri: String?,
    mediaType: StoryMediaType?,
    solidBackgroundArgb: Long?,
    durationMs: Long?,
    transform: StoryTransform,
    layers: List<StoryLayer>,
    selectedLayerId: String?,
    drawMode: Boolean,
    onTransformChanged: (Float, Float, Float) -> Unit,
    onLayerTransformChanged: (String, Float, Float, Float, Float) -> Unit,
    onLayerSelected: (String?) -> Unit,
    onDrawPoint: (Float, Float) -> Unit,
    onDrawFinished: () -> Unit,
    editingTextLayerId: String? = null,
    onTextChanged: (String, String) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier,
) {
    var mediaPanX by remember { mutableFloatStateOf(transform.panX) }
  var mediaPanY by remember { mutableFloatStateOf(transform.panY) }
  var mediaZoom by remember { mutableFloatStateOf(transform.zoom) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationX = mediaPanX
                    translationY = mediaPanY
                    scaleX = mediaZoom
                    scaleY = mediaZoom
                }
                .pointerInput(drawMode, mediaType, editingTextLayerId) {
                    if (!drawMode &&
                        editingTextLayerId == null &&
                        mediaType != StoryMediaType.SolidBackground
                    ) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            mediaPanX += pan.x
                            mediaPanY += pan.y
                            mediaZoom = (mediaZoom * zoom).coerceIn(0.5f, 3f)
                            onTransformChanged(mediaPanX, mediaPanY, mediaZoom)
                        }
                    }
                },
        ) {
            when (mediaType) {
                StoryMediaType.Photo -> {
                    AsyncImage(
                        model = mediaUri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                }
                StoryMediaType.Video -> {
                    AsyncImage(
                        model = mediaUri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                    if (durationMs != null) {
                        Text(
                            text = formatDurationBadge(durationMs),
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .background(
                                    Color.Black.copy(alpha = 0.6f),
                                    RoundedCornerShape(4.dp),
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }
                StoryMediaType.SolidBackground -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(solidBackgroundArgb ?: 0xFF000000)),
                    )
                }
                null -> Unit
            }
        }

        layers.sortedBy(StoryLayer::zIndex).forEach { layer ->
            when (layer) {
                is StoryLayer.Text -> StoryTextLayer(
                    layer = layer,
                    isSelected = layer.id == selectedLayerId,
                    isEditing = layer.id == editingTextLayerId,
                    onSelected = { onLayerSelected(layer.id) },
                    onTransformChanged = onLayerTransformChanged,
                    onTextChanged = { onTextChanged(layer.id, it) },
                )
                is StoryLayer.Sticker -> StoryStickerLayer(
                    layer = layer,
                    isSelected = layer.id == selectedLayerId,
                    onSelected = { onLayerSelected(layer.id) },
                    onTransformChanged = onLayerTransformChanged,
                )
                is StoryLayer.Draw -> StoryDrawLayer(
                    layer = layer,
                    isSelected = layer.id == selectedLayerId,
                )
            }
        }

        if (drawMode) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                onDrawPoint(offset.x, offset.y)
                            },
                            onDrag = { change, _ ->
                                onDrawPoint(change.position.x, change.position.y)
                            },
                            onDragEnd = { onDrawFinished() },
                            onDragCancel = { onDrawFinished() },
                        )
                    },
            )
        }
    }
}

@Composable
private fun StoryTextLayer(
    layer: StoryLayer.Text,
    isSelected: Boolean,
    isEditing: Boolean,
    onSelected: () -> Unit,
    onTransformChanged: (String, Float, Float, Float, Float) -> Unit,
    onTextChanged: (String) -> Unit,
) {
    var offsetX by remember(layer.id) { mutableFloatStateOf(layer.offsetX) }
    var offsetY by remember(layer.id) { mutableFloatStateOf(layer.offsetY) }
    var rotation by remember(layer.id) { mutableFloatStateOf(layer.rotation) }
    var scale by remember(layer.id) { mutableFloatStateOf(layer.scale) }
    val focusRequester = remember { FocusRequester() }
    val textColor = Color(layer.colorArgb)
    val textAlign = when (layer.alignment) {
        StoryTextAlignment.Start -> TextAlign.Start
        StoryTextAlignment.Center -> TextAlign.Center
        StoryTextAlignment.End -> TextAlign.End
    }
    val textStyle = MaterialTheme.typography.titleLarge.copy(
        color = textColor,
        fontSize = layer.textSize.sp,
        fontFamily = storyFontFamily(layer.fontFamily),
        textAlign = textAlign,
    )

    LaunchedEffect(isEditing) {
        if (isEditing) focusRequester.requestFocus()
    }

    Box(
        modifier = Modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .graphicsLayer {
                rotationZ = rotation
                scaleX = scale
                scaleY = scale
            }
            .then(
                if (isEditing) {
                    Modifier
                } else {
                    Modifier.pointerInput(layer.id) {
                        detectTransformGestures { _, pan, zoom, rotationChange ->
                            offsetX += pan.x
                            offsetY += pan.y
                            scale = (scale * zoom).coerceIn(0.5f, 3f)
                            rotation += rotationChange
                            onTransformChanged(layer.id, offsetX, offsetY, rotation, scale)
                            onSelected()
                        }
                    }
                },
            )
            .then(
                if (isSelected && !isEditing) {
                    Modifier.border(1.dp, Color.White, RoundedCornerShape(4.dp))
                } else {
                    Modifier
                },
            )
            .padding(8.dp),
    ) {
        val textModifier = Modifier
            .background(
                if (layer.hasBackground) Color(layer.backgroundArgb) else Color.Transparent,
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
        if (isEditing) {
            BasicTextField(
                value = layer.text,
                onValueChange = onTextChanged,
                textStyle = textStyle,
                cursorBrush = SolidColor(textColor),
                modifier = textModifier.focusRequester(focusRequester),
                decorationBox = { inner ->
                    Box {
                        if (layer.text.isEmpty()) {
                            Text(
                                text = stringResource(R.string.story_text_placeholder),
                                style = textStyle.copy(color = textColor.copy(alpha = 0.55f)),
                            )
                        }
                        inner()
                    }
                },
            )
        } else {
            Text(
                text = layer.text,
                color = textColor,
                textAlign = textAlign,
                modifier = textModifier,
                style = textStyle,
            )
        }
    }
}

@Composable
private fun StoryStickerLayer(
    layer: StoryLayer.Sticker,
    isSelected: Boolean,
    onSelected: () -> Unit,
    onTransformChanged: (String, Float, Float, Float, Float) -> Unit,
) {
    var offsetX by remember(layer.id) { mutableFloatStateOf(layer.offsetX) }
    var offsetY by remember(layer.id) { mutableFloatStateOf(layer.offsetY) }
    var rotation by remember(layer.id) { mutableFloatStateOf(layer.rotation) }
    var scale by remember(layer.id) { mutableFloatStateOf(layer.scale) }

    Box(
        modifier = Modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .graphicsLayer {
                rotationZ = rotation
                scaleX = scale
                scaleY = scale
            }
            .pointerInput(layer.id) {
                detectTransformGestures { _, pan, zoom, rotationChange ->
                    offsetX += pan.x
                    offsetY += pan.y
                    scale = (scale * zoom).coerceIn(0.5f, 3f)
                    rotation += rotationChange
                    onTransformChanged(layer.id, offsetX, offsetY, rotation, scale)
                    onSelected()
                }
            }
            .background(
                MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.9f),
                RoundedCornerShape(8.dp),
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Text(
            text = layer.label,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@Composable
private fun StoryDrawLayer(
    layer: StoryLayer.Draw,
    isSelected: Boolean,
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val path = Path()
        layer.points.forEachIndexed { index, point ->
            val offset = Offset(point.first, point.second)
            if (index == 0) path.moveTo(offset.x, offset.y) else path.lineTo(offset.x, offset.y)
        }
        drawPath(
            path = path,
            color = Color(layer.strokeColorArgb),
            style = Stroke(width = layer.strokeWidth),
        )
    }
}

private fun formatDurationBadge(durationMs: Long): String {
    val seconds = (durationMs / 1000L).toInt()
    return "${seconds}s"
}
