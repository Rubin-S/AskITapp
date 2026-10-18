package com.askit.app.home.stories

import com.askit.app.home.model.Story

/**
 * One author's story set rendered as a single page of the viewer pager.
 */
data class StoryGroup(
    val authorName: String,
    val authorAvatarUrl: String?,
    val stories: List<Story>,
    val authorId: String? = null,
)

/**
 * Groups flat stories by author, preserving the first-seen order of authors.
 */
fun List<Story>.groupedByAuthor(): List<StoryGroup> {
    val byAuthor = LinkedHashMap<String, MutableList<Story>>()
    forEach { story ->
        val author = story.authorId?.takeIf(String::isNotBlank) ?: "unknown:${story.id}"
        byAuthor.getOrPut(author) { mutableListOf() }.add(story)
    }
    return byAuthor.values.map { stories ->
        StoryGroup(
            authorName = stories.first().authorName,
            authorAvatarUrl = stories.first().authorAvatarUrl,
            stories = stories,
            authorId = stories.first().authorId,
        )
    }
}
