package com.askit.designsystem.posts

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.progressSemantics
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.askit.designsystem.R
import kotlin.math.roundToInt

@Composable
fun BeforeAfterSlider(
    before: PostFeedMedia,
    after: PostFeedMedia,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val beforePainter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(context).data(before.model).crossfade(false).build(),
    )
    val afterPainter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(context).data(after.model).crossfade(false).build(),
    )
    val beforeState by beforePainter.state.collectAsState()
    val afterState by afterPainter.state.collectAsState()
    val ratio = imageRatio(afterState) ?: imageRatio(beforeState) ?: 1f
    var fraction by remember { mutableFloatStateOf(0.5f) }
    val beforeLabel = stringResource(R.string.post_feed_before)
    val afterLabel = stringResource(R.string.post_feed_after)
    val sliderDescription = stringResource(R.string.post_feed_before_after_slider)
    val density = LocalDensity.current
    var widthPx by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatioFor(ratio)
            .clipToBounds()
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .onSizeChanged { widthPx = it.width.toFloat() }
            .progressSemantics(fraction, 0f..1f)
            .semantics { contentDescription = sliderDescription }
            .testTag("post_feed_before_after_slider")
            .pointerInput(Unit) {
                detectHorizontalDragGestures { change, dragAmount ->
                    change.consume()
                    val width = size.width.toFloat().takeIf { it > 0f } ?: return@detectHorizontalDragGestures
                    fraction = (fraction + dragAmount / width).coerceIn(0.05f, 0.95f)
                }
            },
    ) {
        SliderImage(painter = afterPainter, state = afterState)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawWithContent {
                    clipRect(right = size.width * fraction) {
                        this@drawWithContent.drawContent()
                    }
                },
        ) {
            SliderImage(painter = beforePainter, state = beforeState)
        }
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset {
                    IntOffset(
                        x = (fraction * widthPx).roundToInt() - with(density) { 1.dp.roundToPx() },
                        y = 0,
                    )
                }
                .fillMaxHeight()
                .width(2.dp)
                .background(MaterialTheme.colorScheme.inverseSurface),
        )
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset {
                    IntOffset(
                        x = (fraction * widthPx).roundToInt() - with(density) { 24.dp.roundToPx() },
                        y = 0,
                    )
                }
                .size(48.dp)
                .border(2.dp, MaterialTheme.colorScheme.inverseSurface, CircleShape)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.92f), CircleShape),
        )
        ComparisonLabel(
            text = beforeLabel,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp),
        )
        ComparisonLabel(
            text = afterLabel,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp),
        )
    }
}

@Composable
private fun SliderImage(
    painter: AsyncImagePainter,
    state: AsyncImagePainter.State,
) {
    if (state is AsyncImagePainter.State.Error) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = stringResource(R.string.post_feed_photo_unavailable),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    } else {
        Image(
            painter = painter,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun ComparisonLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.inverseOnSurface,
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.78f),
                shape = MaterialTheme.shapes.small,
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
    )
}

private fun Modifier.aspectRatioFor(ratio: Float): Modifier =
    this.then(Modifier.aspectRatio(ratio.coerceIn(0.65f, 1.85f)))

private fun imageRatio(state: AsyncImagePainter.State): Float? {
    val success = state as? AsyncImagePainter.State.Success ?: return null
    val width = success.result.image.width
    val height = success.result.image.height
    if (width <= 0 || height <= 0) return null
    return width.toFloat() / height.toFloat()
}
