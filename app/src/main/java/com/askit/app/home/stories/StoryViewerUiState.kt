package com.askit.app.home.stories

import com.askit.app.home.model.Story

sealed interface StoryPlaybackContext {
    data object Ephemeral : StoryPlaybackContext
    data class Highlight(val ownerId: String, val collectionId: String) : StoryPlaybackContext
}

data class StoryViewerUiState(
    val isLoading: Boolean = true,
    val playbackContext: StoryPlaybackContext = StoryPlaybackContext.Ephemeral,
    val groups: List<StoryGroup> = emptyList(),
    val groupIndex: Int = 0,
    val itemIndex: Int = 0,
    val paused: Boolean = false,
    val menuStoryId: String? = null,
    val reporting: Boolean = false,
    val reportReason: StoryReportReason? = null,
    val actionError: StoryActionFailure? = null,
    val shouldClose: Boolean = false,
    val entryStoryId: String? = null,
) {
    val currentGroup: StoryGroup? get() = groups.getOrNull(groupIndex)
    val currentStory: Story? get() = currentGroup?.stories?.getOrNull(itemIndex)

    companion object {
        /**
         * Builds the initial state positioned at [startStoryId]; unknown ids open the first story.
         */
        fun forStart(groups: List<StoryGroup>, startStoryId: String?): StoryViewerUiState {
            var groupIndex = 0
            var itemIndex = 0
            groups.forEachIndexed { g, group ->
                val i = group.stories.indexOfFirst { it.id == startStoryId }
                if (i >= 0) {
                    groupIndex = g
                    itemIndex = i
                }
            }
            return StoryViewerUiState(
                isLoading = false,
                groups = groups,
                groupIndex = groupIndex,
                itemIndex = itemIndex,
            )
        }
    }
}
