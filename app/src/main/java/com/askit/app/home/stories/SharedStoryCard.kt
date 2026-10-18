package com.askit.app.home.stories

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.askit.app.R
import com.askit.app.home.model.Story
import com.askit.app.home.model.isAvailable
import com.askit.app.inbox.StoryReference
import com.askit.app.story.StoryMediaType
import com.askit.app.story.editor.StoryCanvas
import com.askit.app.story.editor.StoryFrame
import com.askit.app.story.stickers.PostReshareSticker
import kotlinx.coroutines.delay

/** A reference to the live story, rather than a downloadable copy of its media. */
@Composable
fun SharedStoryCard(reference: StoryReference, resolveStory: (String) -> Story?, onOpen: (String) -> Unit) {
    var story by remember(reference.storyId) { mutableStateOf(resolveStory(reference.storyId)) }
    LaunchedEffect(reference.storyId) {
        while (true) {
            story = resolveStory(reference.storyId)?.takeIf { it.isAvailable() }
            delay(1_000)
        }
    }
    Surface(
        onClick = { onOpen(reference.storyId) },
        enabled = story != null,
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 2.dp,
        modifier = Modifier.width(168.dp).testTag("chat_story_card"),
    ) {
        Column {
            Box(Modifier.fillMaxWidth().aspectRatio(9f / 16f).clip(RoundedCornerShape(20.dp)), contentAlignment = Alignment.Center) {
                val current = story
                if (current == null) {
                    Text(stringResource(R.string.story_shared_unavailable), Modifier.padding(16.dp))
                } else {
                    val draft = current.content
                    StoryFrame(draft.viewport) {
                        StoryCanvas(
                            mediaUri = draft.mediaUri, mediaType = draft.mediaType,
                            solidBackgroundArgb = draft.solidBackgroundArgb, durationMs = draft.durationMs,
                            transform = draft.transform, layers = draft.layers, readOnly = true,
                            videoPlaying = false, trimStartMs = draft.trimStartMs, trimEndMs = draft.trimEndMs,
                        )
                        draft.sharedPost?.let { post ->
                            Box(Modifier.fillMaxSize().padding(horizontal = 24.dp), contentAlignment = Alignment.Center) {
                                PostReshareSticker(post = post, cardStyle = draft.reshareCardStyle)
                            }
                        }
                    }
                    if (draft.mediaType == StoryMediaType.Video) {
                        Text("▶", color = Color.White, style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.background(Color.Black.copy(alpha = .5f), RoundedCornerShape(50)).padding(8.dp))
                    }
                }
            }
            Text(stringResource(R.string.story_shared_author, reference.authorName),
                style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(12.dp))
        }
    }
}
