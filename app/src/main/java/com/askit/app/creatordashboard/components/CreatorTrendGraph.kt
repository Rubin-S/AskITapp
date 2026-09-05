package com.askit.app.creatordashboard.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun CreatorTrendGraph(
    points: List<Float>,
    lowerBounds: List<Float>,
    upperBounds: List<Float>,
    modifier: Modifier = Modifier,
    height: Dp = 80.dp,
    lineColor: Color = MaterialTheme.colorScheme.secondary,
    envelopeColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
) {
    if (points.size < 2) return

    Spacer(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .drawWithCache {
                val w = size.width
                val h = size.height
                val count = points.size
                val stepX = w / (count - 1)

                val maxVal = maxOf(
                    upperBounds.maxOrNull() ?: 1f,
                    points.maxOrNull() ?: 1f
                ).coerceAtLeast(1f) * 1.15f
                val minVal = 0f

                fun valueToY(value: Float): Float {
                    val normalized = ((value - minVal) / (maxVal - minVal)).coerceIn(0f, 1f)
                    return h - (normalized * (h - 8.dp.toPx())) - 4.dp.toPx()
                }

                // 1. Envelope Path
                val envelopePath = Path()
                if (upperBounds.size == count && lowerBounds.size == count) {
                    for (i in 0 until count) {
                        val x = i * stepX
                        val y = valueToY(upperBounds[i])
                        if (i == 0) envelopePath.moveTo(x, y) else envelopePath.lineTo(x, y)
                    }
                    for (i in (count - 1) downTo 0) {
                        val x = i * stepX
                        val y = valueToY(lowerBounds[i])
                        envelopePath.lineTo(x, y)
                    }
                    envelopePath.close()
                }

                // 2. Trend Line and Fill Gradient
                val trendPath = Path()
                val fillPath = Path()

                for (i in 0 until count) {
                    val x = i * stepX
                    val y = valueToY(points[i])
                    if (i == 0) {
                        trendPath.moveTo(x, y)
                        fillPath.moveTo(x, y)
                    } else {
                        val prevX = (i - 1) * stepX
                        val prevY = valueToY(points[i - 1])
                        val cx = (prevX + x) / 2f
                        trendPath.cubicTo(cx, prevY, cx, y, x, y)
                        fillPath.cubicTo(cx, prevY, cx, y, x, y)
                    }
                }

                fillPath.lineTo(w, h)
                fillPath.lineTo(0f, h)
                fillPath.close()

                val gradientBrush = Brush.verticalGradient(
                    colors = listOf(
                        lineColor.copy(alpha = 0.22f),
                        lineColor.copy(alpha = 0.02f),
                    ),
                    startY = 0f,
                    endY = h,
                )

                onDrawBehind {
                    if (!envelopePath.isEmpty) {
                        drawPath(envelopePath, color = envelopeColor)
                    }
                    drawPath(fillPath, brush = gradientBrush)
                    drawPath(
                        path = trendPath,
                        color = lineColor,
                        style = Stroke(
                            width = 2.5.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round,
                        ),
                    )
                    // Draw active dot at the last point
                    val lastX = (count - 1) * stepX
                    val lastY = valueToY(points.last())
                    drawCircle(
                        color = lineColor,
                        radius = 4.dp.toPx(),
                        center = Offset(lastX, lastY),
                    )
                }
            }
    )
}
