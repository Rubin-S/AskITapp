package com.askit.app.home

import com.askit.app.home.data.LocalHomeRepository
import com.askit.app.home.model.Story
import com.askit.app.home.stories.StorySocialStore
import com.askit.app.inbox.ChatContact
import com.askit.app.inbox.InboxSeed
import com.askit.app.inbox.InboxStore
import com.askit.app.inbox.MessageAccess
import com.askit.app.profile.ProfileHighlight
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

/**
 * Until the server authorises audiences, a Close circle or Message story is visible only to its
 * author: on the Home rail, in the viewer, in highlights and when forwarding.
 */
class StoryAudienceTest {
    private val public = story("public", StoryAudience.Everyone)
    private val circle = story("circle", StoryAudience.CloseCircle)
    private val message = story("message", StoryAudience.Message)

    @Test fun homeRailAndViewerEnforceTheAudienceForTheActiveAccount() = runBlocking {
        val social = StorySocialStore()
        val home = LocalHomeRepository(InMemoryTaskRepository(), listOf(public, circle, message), storySocial = social)

        social.activateAccount("viewer")
        assertEquals(listOf("public"), home.getStories().first().map { it.id })
        assertNull(home.getStory("circle"))

        social.activateAccount("owner")
        assertEquals(listOf("public", "circle", "message"), home.getStories().first().map { it.id })
        assertNotNull(home.getStory("circle"))
    }

    @Test fun highlightsShowRestrictedAndForeignStoriesToNoOneElse() {
        val foreign = story("foreign", StoryAudience.Everyone, author = "someone-else")
        val highlight = ProfileHighlight("h1", ownerId = "owner", name = "Work", stories = listOf(public, circle, foreign))
        assertEquals(listOf("public"), highlight.visibleStories("viewer").map { it.id })
        assertEquals(listOf("public"), highlight.visibleStories(null).map { it.id })
        assertEquals(listOf("public", "circle"), highlight.visibleStories("owner").map { it.id })
    }

    @Test fun restrictedStoriesCannotBeForwardedToChats() {
        val friend = ChatContact("friend", "Friend")
        val inbox = InboxStore(InboxSeed(contacts = listOf(friend)), messageAccess = { MessageAccess.Allowed })
        assertFalse(inbox.sendStory(friend, circle))
        assertFalse(inbox.sendStory(friend, message))
        assertTrue(inbox.sendStory(friend, public))
    }

    private fun story(id: String, audience: StoryAudience, author: String = "owner") = Story(
        id = id,
        authorName = "Author",
        authorId = author,
        content = StoryDraft(audience = audience),
        createdAtMillis = System.currentTimeMillis(),
    )
}
