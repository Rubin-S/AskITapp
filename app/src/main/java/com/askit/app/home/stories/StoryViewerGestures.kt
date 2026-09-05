package com.askit.app.home.stories

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput

/**
 * Instagram-style story tap surface: tapping the left half goes back, the right half advances,
 * and holding anywhere pauses until release.
 */
fun Modifier.storyTapGestures(
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onHoldChanged: (Boolean) -> Unit,
): Modifier = pointerInput(Unit) {
    detectTapGestures(
        onPress = {
            onHoldChanged(true)
            try {
                tryAwaitRelease()
            } finally {
                onHoldChanged(false)
            }
        },
        onTap = { offset ->
            if (offset.x < size.width / 2f) onPrevious() else onNext()
        },
    )
}

/**
 * Vertical drag-to-dismiss: reports drag deltas so the caller can translate content, and asks the
 * caller to settle (dismiss or spring back) when the gesture ends.
 */
fun Modifier.storyVerticalDrag(
    onDragDeltaY: (Float) -> Unit,
    onSettle: () -> Unit,
): Modifier = pointerInput(Unit) {
    detectVerticalDragGestures(
        onVerticalDrag = { change, dragAmount ->
            change.consume()
            onDragDeltaY(dragAmount)
        },
        onDragEnd = onSettle,
        onDragCancel = onSettle,
    )
}
