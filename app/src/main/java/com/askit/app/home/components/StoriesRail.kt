package com.askit.app.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.askit.app.home.model.Story

@Composable
fun StoriesRail(
    stories: List<Story>,
    modifier: Modifier = Modifier,
    onStoryClick: (Story) -> Unit = {},
    onAddStoryClick: () -> Unit = {},
) {
    val myStory = stories.firstOrNull { it.isOwn }
    val otherStories = stories.filter { !it.isOwn }

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "your_story") {
            AddStoryAvatar(
                myStory = myStory,
                onStoryClick = onStoryClick,
                onAddStoryClick = onAddStoryClick,
            )
        }
        items(
            items = otherStories,
            key = { it.id },
        ) { story ->
            StoryAvatar(
                story = story,
                onClick = { onStoryClick(story) },
            )
        }
    }
}
