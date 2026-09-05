package com.askit.app.home.stories

import com.askit.app.home.model.Story

/**
 * One author's story set rendered as a single page of the viewer pager.
 */
data class StoryGroup(
    val authorName: String,
    val authorAvatarUrl: String?,
    val stories: List<Story>,
)

/**
 * Groups flat stories by author, preserving the first-seen order of authors.
 */
fun List<Story>.groupedByAuthor(): List<StoryGroup> {
    val byAuthor = LinkedHashMap<String, MutableList<Story>>()
    forEach { story ->
        byAuthor.getOrPut(story.authorName) { mutableListOf() }.add(story)
    }
    return byAuthor.map { (author, stories) ->
        StoryGroup(
            authorName = author,
            authorAvatarUrl = stories.first().authorAvatarUrl,
            stories = stories,
        )
    }
}
