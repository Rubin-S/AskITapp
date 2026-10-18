package com.askit.app.profile

import com.askit.app.session.DefaultCarpentryListing
import com.askit.app.session.SessionProfile
import com.askit.app.task.Task
import org.junit.Assert.*
import org.junit.Test

class ProfileSectionsTest {
    @Test fun sectionsRemainVisibleWithoutContentForBothRoles() {
        assertEquals(listOf("about", "showcase", "services", "tasks"),
            SessionProfile(listing = DefaultCarpentryListing).toUiState().tabs.map { it.id })
        assertEquals(listOf("posts", "tasks"), SessionProfile().toUiState().tabs.map { it.id })
    }
    @Test fun selectionUsesStableIdsAndFallsBackWhenTheRoleChanges() {
        val provider = SessionProfile(listing = DefaultCarpentryListing)
        assertEquals("tasks", provider.toUiState(selectedSectionId = "tasks").selectedSectionId)
        assertEquals("about", provider.toUiState(selectedSectionId = "reviews").selectedSectionId)
        assertEquals("posts", SessionProfile().toUiState(selectedSectionId = "about").selectedSectionId)
    }
    @Test fun tasksMatchStableOwnerIdsNeverDisplayNamesOrMissingIds() {
        fun task(id: String, owner: String?) = Task(id = id, title = "Fix tap", category = "Plumbing", budget = "Quote", location = "City",
            posterName = "Same name", urgency = "Flexible", postedTime = "Today", description = "Repair", posterId = owner)
        val tasks = listOf(task("own", "a"), task("other", "b"), task("unknown", null))
        assertEquals(listOf("own"), tasks.forProfile("a").map { it.id })
        assertTrue(tasks.forProfile("").isEmpty())
    }
}
