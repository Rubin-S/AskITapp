package com.askit.app.profile

import com.askit.app.home.model.Story
import com.askit.app.home.model.isAvailable
import com.askit.app.session.SessionProfile
import com.askit.app.session.SessionProfileStore
import com.askit.app.story.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test

class ProfileHighlightsTest {
    private fun story(owner: String = "owner", audience: StoryAudience = StoryAudience.Everyone) =
        Story("story", "Owner", content = StoryDraft(mediaType = StoryMediaType.SolidBackground,
            solidBackgroundArgb = 0xff112233, audience = audience), createdAtMillis = 1, authorId = owner)
    private fun highlight(story: Story = story()) = ProfileHighlight("collection", "owner", "Work", listOf(story), story.id)

    @Test fun anotherAccountsStoryCannotBeAddedAndVisitorsCannotManage() {
        val store = SessionProfileStore(SessionProfile(accountId = "owner"))
        assertThrows(IllegalArgumentException::class.java) { store.saveHighlight("visitor", highlight()) }
        assertThrows(IllegalArgumentException::class.java) { store.saveHighlight("owner", highlight(story("other"))) }
        assertTrue(store.profile.value.highlights.isEmpty())
    }
    @Test fun retentionIsExplicitAndDoesNotExtendOrdinaryStoryExpiry() {
        val expired = story()
        assertFalse(expired.isAvailable(100_000_000))
        assertEquals(listOf(expired), highlight(expired).visibleStories("visitor"))
        assertFalse(expired.isAvailable(100_000_000))
    }
    @Test fun restrictedStoriesAreVisibleOnlyToTheirOwnerWithoutAudienceAuthorization() {
        val restricted = highlight(story(audience = StoryAudience.CloseCircle))
        assertTrue(restricted.visibleStories("visitor").isEmpty())
        assertEquals(1, restricted.visibleStories("owner").size)
    }
    @Test fun deletedStoriesAndUnresolvableResharedPostsStayUnavailable() {
        assertTrue(highlight(story().copy(isDeleted = true)).visibleStories("owner").isEmpty())
        val post = com.askit.app.home.model.FeedPost("post", "Owner", content = "Public post")
        val shared = highlight(story().copy(content = story().content.copy(sharedPost = post)))
        assertTrue(shared.visibleStories("visitor").isEmpty())
        assertEquals(1, shared.visibleStories("visitor", setOf("post")).size)
        assertTrue(shared.copy(stories = listOf(shared.stories.single().copy(content = shared.stories.single().content.copy(
            sharedPost = post.copy(archived = true))))).visibleStories("visitor", setOf("post")).isEmpty())
    }
    @Test fun failedPersistenceKeepsThePreviousAccountSnapshot() {
        val initial = SessionProfile(accountId = "owner")
        val store = SessionProfileStore(initial, persist = { throw java.io.IOException("Storage unavailable") })
        assertThrows(java.io.IOException::class.java) { store.saveHighlight("owner", highlight()) }
        assertEquals(initial, store.profile.value)
    }
    @Test fun invalidCoverIsRejected() {
        val store = SessionProfileStore(SessionProfile(accountId = "owner"))
        assertThrows(IllegalArgumentException::class.java) { store.saveHighlight("owner", highlight().copy(coverStoryId = "other")) }
    }
    @Test fun renameCoverMembershipAndDeletionRoundTripWithoutDuplicatingCollections() {
        val store = SessionProfileStore(SessionProfile(accountId = "owner"))
        store.saveHighlight("owner", highlight())
        store.saveHighlight("owner", highlight().copy(name = "Repairs", stories = emptyList(), coverStoryId = null))
        assertEquals("Repairs", store.profile.value.highlights.single().name)
        assertEquals(store.profile.value, Json.decodeFromString<SessionProfile>(Json.encodeToString(store.profile.value)))
        assertThrows(IllegalArgumentException::class.java) { store.deleteHighlight("visitor", "collection") }
        store.deleteHighlight("owner", "collection")
        assertTrue(store.profile.value.highlights.isEmpty())
    }
    @Test fun cameraCacheMediaIsCopiedBeforeTheSourceCanBeEvicted() {
        val directory = java.nio.file.Files.createTempDirectory("highlight-test").toFile()
        try {
            val source = java.io.File(directory, "camera.jpg").apply { writeText("photo bytes") }
            val retained = com.askit.app.preview.retainPreviewFile(source, java.io.File(directory, "durable"))
            source.delete()
            assertEquals("photo bytes", retained.readText())
            assertEquals(retained, com.askit.app.preview.retainPreviewFile(retained, retained.parentFile!!))
        } finally { directory.deleteRecursively() }
    }
}
