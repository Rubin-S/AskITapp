package com.askit.app.home.stories

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember

/**
 * Drives one story item's playback fraction (0f..1f) over [STORY_ITEM_DURATION_MS].
 *
 * Restarts when [restartKey] changes, freezes at the current fraction while [paused], and resumes
 * from exactly where it stopped. Invokes [onFinished] once the fraction reaches 1f.
 */
private const val STORY_ITEM_DURATION_MS = 5_000

@Composable
fun rememberStoryItemProgress(
    restartKey: String,
    paused: Boolean,
    onFinished: () -> Unit,
): Float {
    val progress = remember(restartKey) { Animatable(0f) }
    LaunchedEffect(restartKey, paused) {
        if (paused) {
            progress.stop()
            return@LaunchedEffect
        }
        val remaining = ((1f - progress.value) * STORY_ITEM_DURATION_MS).toLong()
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = remaining.coerceAtLeast(1).toInt(),
                easing = LinearEasing,
            ),
        )
        onFinished()
    }
    return progress.value
}
