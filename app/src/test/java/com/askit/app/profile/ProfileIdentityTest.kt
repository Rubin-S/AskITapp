package com.askit.app.profile

import com.askit.app.home.details.UserProfileData
import com.askit.app.session.DefaultCarpentryListing
import com.askit.app.session.SessionProfile
import org.junit.Assert.*
import org.junit.Test

class ProfileIdentityTest {
    @Test fun providerIdentityUsesTheSharedServiceTitleAndDescription() {
        val profile = SessionProfile(
            displayName = "Arun Kumar",
            bio = "Personal account text",
            listing = DefaultCarpentryListing.copy(title = "Master plumber", category = "Plumbing", description = "Licensed plumber"),
        )
        val state = profile.toUiState()
        assertTrue(state.isProvider)
        assertEquals("Arun Kumar", state.displayName)
        assertEquals("Master plumber", state.tradeHeadline)
        assertEquals("Licensed plumber", state.bio)
    }

    @Test fun seekerTitleIsOptionalAndPublicPreviewKeepsTheSameIdentity() {
        val profile = SessionProfile(username = "arun", title = "Homeowner", bio = "Looking for local help")
        val owner = profile.toUiState()
        val preview = profile.toUiState(isPublicPreview = true)
        assertFalse(owner.isProvider)
        assertEquals("Homeowner", owner.tradeHeadline)
        assertEquals(owner.displayName, preview.displayName)
        assertEquals(owner.bio, preview.bio)
        assertEquals(owner.tradeHeadline, preview.tradeHeadline)
    }

    @Test fun usernameFormattingNeverChangesTheStoredUsername() {
        val profile = SessionProfile(username = "@arun")
        assertEquals("arun", profile.toUiState().displayUsername)
        assertEquals("@arun", profile.username)
    }

    @Test fun visitorIdentityUsesTheSameRulesForBothRoles() {
        for (provider in listOf(false, true)) {
            val visitor = UserProfileData(
                id = "account-1",
                username = "arun",
                name = "Arun Kumar",
                trade = "Master plumber",
                bio = "Public description",
                isProvider = provider,
            ).toUiState()
            assertEquals("account-1", visitor.userId)
            assertEquals("arun", visitor.displayUsername)
            assertEquals("Arun Kumar", visitor.displayName)
            assertEquals("Public description", visitor.bio)
            assertEquals(provider, visitor.isProvider)
            assertFalse(visitor.isOwner)
        }
    }

    @org.junit.Test fun publicAdapterPreservesOptionalSeekerIdentity() {
        val seeker = com.askit.app.session.SessionProfile(accountId = "owner", title = "Homeowner", bio = "Planning repairs")
        val visitor = seeker.toPublicProfile().toUiState()
        org.junit.Assert.assertEquals("Homeowner", visitor.tradeHeadline)
        org.junit.Assert.assertEquals("Planning repairs", visitor.bio)
        org.junit.Assert.assertFalse(visitor.isOwner)
    }
}
