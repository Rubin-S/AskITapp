package com.askit.app.profile

import androidx.activity.compose.BackHandler
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.askit.app.R
import com.askit.app.home.stories.*

/** Reuses the story renderer with an explicit retained context and no ordinary-story mutations. */
@Composable
internal fun HighlightPlayback(collection: ProfileHighlight, viewerId: String?, authorizedPostIds: Set<String>, onDismiss: () -> Unit, onOpenPost: (String) -> Unit) {
    val stories = collection.visibleStories(viewerId, authorizedPostIds)
    var index by rememberSaveable(collection.id, viewerId) { mutableIntStateOf(0) }
    var paused by remember { mutableStateOf(false) }
    if (stories.isEmpty()) {
        AlertDialog(onDismissRequest = onDismiss, title = { Text(collection.name) },
            text = { Text(stringResource(R.string.profile_highlight_unavailable)) },
            confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.explore_back)) } })
        return
    }
    val state = StoryViewerUiState(isLoading = false,
        groups = listOf(StoryGroup(collection.name, stories.first().authorAvatarUrl, stories, collection.ownerId)),
        itemIndex = index.coerceAtMost(stories.lastIndex), paused = paused,
        playbackContext = StoryPlaybackContext.Highlight(collection.ownerId, collection.id))
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        BackHandler(onBack = onDismiss)
        StoryViewerScreen(state = state,
            onNext = { if (index < stories.lastIndex) index++ else onDismiss() },
            onPrevious = { index = (index - 1).coerceAtLeast(0) }, onPauseChanged = { paused = it },
            onLikeClick = {}, onShareClick = {}, socialLikes = emptySet(), onSelectGroup = {},
            onDismiss = onDismiss, onViewPost = onOpenPost)
    }
}
