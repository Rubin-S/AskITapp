package com.askit.app.posttask

import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.focus.FocusRequester

/**
 * Focuses a form field after the next frame, so a section that was just expanded has attached
 * its [FocusRequester]. Never throws: an unattached requester is ignored instead of crashing.
 */
internal suspend fun focusAfterFrame(requester: FocusRequester) {
    withFrameNanos { }
    runCatching { requester.requestFocus() }
}
