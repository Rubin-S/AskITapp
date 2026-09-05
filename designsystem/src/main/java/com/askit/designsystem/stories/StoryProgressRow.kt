package com.askit.designsystem.stories

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

/**
 * Segmented story progress indicator: completed segments are full, the active segment fills by
 * [activeSegmentFraction] (0f..1f), upcoming segments are empty.
 */
@Composable
fun StoryProgressRow(
    segmentCount: Int,
    activeSegmentIndex: Int,
    activeSegmentFraction: Float,
    modifier: Modifier = Modifier,
    trackColor: Color = Color.White.copy(alpha = 0.35f),
    fillColor: Color = Color.White,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .testTag("story_progress_row"),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(segmentCount) { index ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(2.5.dp)
                    .clip(SegmentShape)
                    .background(trackColor),
            ) {
                val fillFraction = when {
                    index < activeSegmentIndex -> 1f
                    index == activeSegmentIndex -> activeSegmentFraction.coerceIn(0f, 1f)
                    else -> 0f
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fillFraction)
                        .fillMaxHeight()
                        .clip(SegmentShape)
                        .background(fillColor),
                )
            }
        }
    }
}

private val SegmentShape = RoundedCornerShape(percent = 50)
