package com.askit.app.home

import com.askit.app.home.data.LocalHomeRepository
import com.askit.app.home.model.Story
import com.askit.app.home.model.isShareable
import com.askit.app.home.model.isVisibleTo
import com.askit.app.home.stories.StorySocialStore
import com.askit.app.inbox.ChatContact
import com.askit.app.inbox.InboxSeed
import com.askit.app.inbox.InboxStore
import com.askit.app.inbox.MessageAccess
import com.askit.app.story.StoryAudience
import com.askit.app.story.StoryDraft
import com.askit.app.task.InMemoryTaskRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StoryAudienceTest {
    private fun story(id: String, audience: StoryAudience, author: String = "owner") = Story(
        id = id,
        authorName = "Owner",
        authorId = author,
        content = StoryDraft(audience = audience),
        createdAtMillis = System.currentTimeMillis(),
    )

    private val public = story("public", StoryAudience.Everyone)
    private val circle = story("circle", StoryAudience.CloseCircle)
    private val message = story("message", StoryAudience.Message)

    @Test fun restrictedStoriesAreVisibleOnlyToTheirAuthor() {
        assertTrue(public.isVisibleTo(null) && public.isVisibleTo("viewer"))
        assertFalse(circle.isVisibleTo("viewer") || circle.isVisibleTo(null) || circle.isVisibleTo(""))
        assertFalse(message.isVisibleTo("viewer"))
        assertTrue(circle.isVisibleTo("owner") && message.isVisibleTo("owner"))
    }

    @Test fun homeRailAndViewerEnforceTheAudience() = runBlocking {
        val social = StorySocialStore()
        val repository = LocalHomeRepository(InMemoryTaskRepository(), listOf(public, circle, message), storySocial = social)

        social.activateAccount("viewer")
        assertEquals(listOf("public"), repository.getStories().first().map { it.id })
        assertNull(repository.getStory("circle"))
        assertNull(repository.getStory("message"))

        social.activateAccount(null)
        assertEquals(listOf("public"), repository.getStories().first().map { it.id })

        social.activateAccount("owner")
        assertEquals(listOf("public", "circle", "message"), repository.getStories().first().map { it.id })
        assertNotNull(repository.getStory("circle"))
    }

    @Test fun restrictedStoriesCannotBeForwardedToChats() {
        val contact = ChatContact("friend", "Friend")
        val inbox = InboxStore(InboxSeed(contacts = listOf(contact)), messageAccess = { MessageAccess.Allowed })
        assertFalse(circle.isShareable)
        assertFalse(inbox.sendStory(contact, circle))
        assertFalse(inbox.sendStory(contact, message))
        assertTrue(inbox.sendStory(contact, public))
    }
}
