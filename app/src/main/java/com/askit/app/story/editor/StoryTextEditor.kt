package com.askit.app.story.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatAlignLeft
import androidx.compose.material.icons.automirrored.filled.FormatAlignRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.askit.app.R
import com.askit.app.story.StoryLayer
import com.askit.app.story.StoryTextAlignment
import com.askit.app.story.StoryViewModel

private val STORY_TEXT_COLORS = listOf(0xFFFFFFFF, 0xFF000000, 0xFF7CE605, 0xFF3D7100)

@Composable
fun StoryTextEditor(
    viewModel: StoryViewModel,
    layer: StoryLayer.Text,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val chipColors = FilterChipDefaults.filterChipColors(
        containerColor = Color.White.copy(alpha = 0.12f),
        labelColor = Color.White,
        selectedContainerColor = Color.White,
        selectedLabelColor = Color.Black,
    )

    Box(modifier = modifier.fillMaxSize()) {
        StoryTextSizeSlider(
            value = layer.textSize,
            onValueChange = { viewModel.updateTextLayer(layer.id, textSize = it) },
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 8.dp),
        )

        IconButton(
            onClick = onDone,
            modifier = Modifier
                .statusBarsPadding()
                .align(Alignment.TopEnd)
                .padding(4.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = stringResource(R.string.story_text_done),
                tint = Color.White,
            )
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.35f))
                .horizontalScroll(rememberScrollState())
                .padding(start = 8.dp, end = 56.dp, top = 8.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            STORY_FONT_OPTIONS.forEach { font ->
                val fontDescription = fontContentDescription(font)
                FilterChip(
                    selected = layer.fontFamily == font,
                    onClick = { viewModel.updateTextLayer(layer.id, fontFamily = font) },
                    label = {
                        Text(
                            text = "Aa",
                            fontFamily = storyFontFamily(font),
                        )
                    },
                    colors = chipColors,
                    modifier = Modifier.semantics {
                        contentDescription = fontDescription
                    },
                )
            }
            STORY_TEXT_COLORS.forEach { color ->
                val selected = layer.colorArgb == color
                val wellDescription = colorContentDescription(color)
                IconButton(
                    onClick = { viewModel.updateTextLayer(layer.id, colorArgb = color) },
                    modifier = Modifier.semantics {
                        contentDescription = wellDescription
                    },
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .border(
                                width = 2.dp,
                                color = when {
                                    selected -> Color.White
                                    color == 0xFF000000 -> Color.White.copy(alpha = 0.7f)
                                    else -> Color.Transparent
                                },
                                shape = CircleShape,
                            )
                            .padding(3.dp)
                            .background(Color(color), CircleShape),
                    )
                }
            }
            AlignmentIcon(
                icon = Icons.AutoMirrored.Filled.FormatAlignLeft,
                description = stringResource(R.string.story_text_align_start),
                selected = layer.alignment == StoryTextAlignment.Start,
                onClick = {
                    viewModel.updateTextLayer(layer.id, alignment = StoryTextAlignment.Start)
                },
            )
            AlignmentIcon(
                icon = Icons.Filled.FormatAlignCenter,
                description = stringResource(R.string.story_text_align_center),
                selected = layer.alignment == StoryTextAlignment.Center,
                onClick = {
                    viewModel.updateTextLayer(layer.id, alignment = StoryTextAlignment.Center)
                },
            )
            AlignmentIcon(
                icon = Icons.AutoMirrored.Filled.FormatAlignRight,
                description = stringResource(R.string.story_text_align_end),
                selected = layer.alignment == StoryTextAlignment.End,
                onClick = {
                    viewModel.updateTextLayer(layer.id, alignment = StoryTextAlignment.End)
                },
            )
            IconButton(
                onClick = {
                    viewModel.updateTextLayer(
                        layer.id,
                        hasBackground = !layer.hasBackground,
                        backgroundArgb = 0xFF000000,
                    )
                },
            ) {
                Icon(
                    imageVector = Icons.Default.FormatColorFill,
                    contentDescription = stringResource(R.string.story_text_background),
                    tint = if (layer.hasBackground) Color.Black else Color.White,
                    modifier = Modifier
                        .background(
                            color = if (layer.hasBackground) Color.White else Color.Transparent,
                            shape = CircleShape,
                        )
                        .padding(6.dp),
                )
            }
        }
    }
}

@Composable
private fun AlignmentIcon(
    icon: ImageVector,
    description: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = if (selected) Color.Black else Color.White,
            modifier = Modifier
                .background(
                    color = if (selected) Color.White else Color.Transparent,
                    shape = CircleShape,
                )
                .padding(6.dp),
        )
    }
}

@Composable
private fun StoryTextSizeSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val sizeDescription = stringResource(R.string.story_text_size)
    Box(
        modifier = modifier
            .width(48.dp)
            .height(180.dp)
            .semantics { contentDescription = sizeDescription },
        contentAlignment = Alignment.Center,
    ) {
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 16f..64f,
            modifier = Modifier.requiredWidth(180.dp).graphicsLayer { rotationZ = -90f },
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color.White,
                inactiveTrackColor = Color.White.copy(alpha = 0.4f),
            ),
        )
    }
}

@Composable
private fun fontContentDescription(font: String): String = stringResource(
    when (font) {
        "Serif" -> R.string.story_text_font_serif
        "Monospace" -> R.string.story_text_font_monospace
        "Sans" -> R.string.story_text_font_sans
        else -> R.string.story_text_font_default
    },
)

@Composable
private fun colorContentDescription(color: Long): String = stringResource(
    when (color) {
        0xFF000000 -> R.string.story_text_color_black
        0xFF7CE605 -> R.string.story_text_color_lime
        0xFF3D7100 -> R.string.story_text_color_forest
        else -> R.string.story_text_color_white
    },
)
