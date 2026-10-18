package com.askit.app.profile

import com.askit.app.home.model.isVisibleTo
import com.askit.app.home.model.Story
import com.askit.app.story.StoryAudience
import kotlinx.serialization.Serializable

/** A retained collection. Ordinary story availability remains a separate 24-hour policy. */
@Serializable
data class ProfileHighlight(
    val id: String,
    val ownerId: String,
    val name: String,
    val stories: List<Story> = emptyList(),
    val coverStoryId: String? = null,
) {
    fun validate(actorId: String) {
        require(actorId.isNotBlank() && actorId == ownerId)
        require(id.isNotBlank() && name.isNotBlank() && name.length <= 60)
        require(stories.all { it.authorId == ownerId && it.id.isNotBlank() })
        require(stories.map { it.id }.distinct().size == stories.size)
        require(coverStoryId == null || stories.any { it.id == coverStoryId })
    }

    /** Without server audience authorization, restricted stories are available only to their owner. */
    fun visibleStories(viewerId: String?, authorizedPostIds: Set<String> = emptySet()): List<Story> = stories.filter {
        !it.isDeleted && it.authorId == ownerId &&
            (it.content.sharedPost == null || (it.content.sharedPost.id in authorizedPostIds && !it.content.sharedPost.archived)) &&
            (viewerId == ownerId || it.isVisibleTo(viewerId))
    }
}
