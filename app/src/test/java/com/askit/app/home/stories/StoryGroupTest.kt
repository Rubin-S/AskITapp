package com.askit.app.home.stories

import com.askit.app.home.model.Story
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StoryGroupTest {

    @Test
    fun groupedByAuthor_keepsFirstSeenAuthorOrder() {
        val stories = listOf(
            story(id = "s1", author = "amy"),
            story(id = "s2", author = "bob"),
            story(id = "s3", author = "amy"),
        )

        val groups = stories.groupedByAuthor()

        assertEquals(listOf("amy", "bob"), groups.map { it.authorName })
    }

    @Test
    fun groupedByAuthor_collectsAllStoriesPerAuthor() {
        val stories = listOf(
            story(id = "s1", author = "amy"),
            story(id = "s2", author = "bob"),
            story(id = "s3", author = "amy"),
        )

        val groups = stories.groupedByAuthor()

        assertEquals(listOf("s1", "s3"), groups.first().stories.map { it.id })
        assertTrue(groups.first().stories.contains(stories[2]))
    }

    @Test
    fun groupedByAuthor_emptyInputYieldsEmptyGroups() {
        assertTrue(emptyList<Story>().groupedByAuthor().isEmpty())
    }

    private fun story(id: String, author: String) = Story(
        id = id,
        authorName = author,
        authorAvatarUrl = null,
        mediaUrl = "https://example.com/$id.jpg",
        caption = null,
        createdAtMillis = 0L,
    )
}
