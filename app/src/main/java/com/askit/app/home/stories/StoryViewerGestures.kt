package com.askit.app.home.stories

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput

/**
 * Instagram-style story tap surface: tapping the left half goes back, the right half advances,
 * and holding anywhere pauses until release.
 */
@Composable
fun Modifier.storyTapGestures(
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onHoldChanged: (Boolean) -> Unit,
): Modifier {
    val latestPrevious by rememberUpdatedState(onPrevious)
    val latestNext by rememberUpdatedState(onNext)
    val latestHoldChanged by rememberUpdatedState(onHoldChanged)
    return pointerInput(Unit) {
        detectTapGestures(
            onPress = {
                latestHoldChanged(true)
                try {
                    tryAwaitRelease()
                } finally {
                    latestHoldChanged(false)
                }
            },
            onTap = { offset ->
                if (offset.x < size.width / 2f) latestPrevious() else latestNext()
            },
        )
    }
}

/**
 * Vertical drag-to-dismiss: reports drag deltas so the caller can translate content, and asks the
 * caller to settle (dismiss or spring back) when the gesture ends.
 */
@Composable
fun Modifier.storyVerticalDrag(
    onDragDeltaY: (Float) -> Unit,
    onSettle: () -> Unit,
): Modifier {
    val latestDrag by rememberUpdatedState(onDragDeltaY)
    val latestSettle by rememberUpdatedState(onSettle)
    return pointerInput(Unit) {
        detectVerticalDragGestures(
            onVerticalDrag = { change, dragAmount ->
                change.consume()
                latestDrag(dragAmount)
            },
            onDragEnd = { latestSettle() },
            onDragCancel = { latestSettle() },
        )
    }
}
