package com.askit.app.home.stories

import android.view.accessibility.AccessibilityManager
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics

/**
 * Instagram-style story tap surface: tapping the left half goes back, the right half advances,
 * and holding anywhere pauses until release.
 */
@Composable
fun Modifier.storyTapGestures(onPrevious: () -> Unit, onNext: () -> Unit, onHoldChanged: (Boolean) -> Unit): Modifier {
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
fun Modifier.storyVerticalDrag(onDragDeltaY: (Float) -> Unit, onSettle: () -> Unit): Modifier {
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

/**
 * Screen-reader and switch-access equivalents of the tap and hold gestures (WCAG 2.1.1 and
 * 2.2.1): TalkBack exposes these as custom actions on the story surface. [pauseLabel] is null
 * when pausing is not offered (for example while touch exploration already keeps the story paused).
 */
fun Modifier.storyAccessibilityActions(
    description: String,
    labels: StoryA11yLabels,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onTogglePause: () -> Unit,
): Modifier = semantics {
    contentDescription = description
    customActions = buildList {
        add(CustomAccessibilityAction(labels.next) { onNext().let { true } })
        add(CustomAccessibilityAction(labels.previous) { onPrevious().let { true } })
        labels.pause?.let { add(CustomAccessibilityAction(it) { onTogglePause().let { true } }) }
    }
}

/** Localized labels for [storyAccessibilityActions]. */
data class StoryA11yLabels(val next: String, val previous: String, val pause: String?)

/**
 * True while TalkBack (or another touch-exploration service) is on. The story viewer stays
 * paused then, so a screen-reader user is never raced by the 5 s auto-advance.
 */
@Composable
fun rememberTouchExplorationEnabled(): Boolean {
    val context = LocalContext.current
    val manager = remember(context) { context.getSystemService(AccessibilityManager::class.java) }
    var enabled by remember(manager) { mutableStateOf(manager?.isTouchExplorationEnabled == true) }
    DisposableEffect(manager) {
        val listener = AccessibilityManager.TouchExplorationStateChangeListener { enabled = it }
        manager?.addTouchExplorationStateChangeListener(listener)
        onDispose { manager?.removeTouchExplorationStateChangeListener(listener) }
    }
    return enabled
}
