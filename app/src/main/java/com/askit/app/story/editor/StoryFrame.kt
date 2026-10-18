package com.askit.app.story.editor

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import com.askit.app.story.StoryViewport
import kotlin.math.min
import kotlin.math.roundToInt

/** Keeps the authored pixel coordinates and typography together when fitting another screen. */
@Composable
internal fun StoryFrame(viewport: StoryViewport?, modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    val density = viewport?.let { Density(it.density, it.fontScale) } ?: LocalDensity.current
    CompositionLocalProvider(LocalDensity provides density) {
        Layout(
            modifier = modifier.fillMaxSize().clipToBounds(),
            content = {
                Box(Modifier.fillMaxSize().clipToBounds(), contentAlignment = Alignment.Center, content = content)
            },
        ) { measurables, constraints ->
            val width = constraints.maxWidth
            val height = constraints.maxHeight
            val sourceWidth = viewport?.widthPx ?: width.coerceAtLeast(1)
            val sourceHeight = viewport?.heightPx ?: height.coerceAtLeast(1)
            val canvas = measurables.single().measure(Constraints.fixed(sourceWidth, sourceHeight))
            val scale = min(width.toFloat() / sourceWidth, height.toFloat() / sourceHeight)
            layout(width, height) {
                canvas.placeWithLayer(
                    ((width - sourceWidth * scale) / 2).roundToInt(),
                    ((height - sourceHeight * scale) / 2).roundToInt(),
                ) {
                    transformOrigin = TransformOrigin(0f, 0f)
                    scaleX = scale
                    scaleY = scale
                }
            }
        }
    }
}

// One portrait template, fitted within available safe bounds rather than stretched to the screen.
@Composable
internal fun StoryStage(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    BoxWithConstraints(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        val width = minOf(maxWidth, maxHeight * (9f / 16f))
        Box(
            Modifier.size(width, width * (16f / 9f)).clipToBounds(),
            content = content,
        )
    }
}
