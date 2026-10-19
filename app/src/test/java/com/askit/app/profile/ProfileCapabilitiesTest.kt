package com.askit.app.profile

import com.askit.app.session.ServiceListing
import com.askit.app.session.SessionProfile
import com.askit.app.session.SessionProfileStore
import com.askit.app.testing.MainDispatcherRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Release has no profile backend yet, so it must not offer (or perform) anything that can't
 * succeed: no edit, dashboards, post-task or social actions, and no profile writes.
 */
class ProfileCapabilitiesTest {
    @get:Rule val main = MainDispatcherRule()

    private val release = ProfileCapabilities.forBuild(clientPreview = false)

    @Test fun releaseOwnersOnlySeeActionsTheirBuildCanComplete() {
        // ProfileRoute registers callbacks only for what the build supports; in release that is View as and Contact.
        val releaseCallbacks = setOf(ProfileActionId.VIEW_AS, ProfileActionId.CONTACT)
        val provider = SessionProfile(accountId = "me", listing = listing()).toUiState()
        val seeker = SessionProfile(accountId = "me").toUiState()
        assertEquals(listOf(ProfileActionId.VIEW_AS), visibleProfileActionIds(provider, releaseCallbacks))
        assertEquals(listOf(ProfileActionId.VIEW_AS), visibleProfileActionIds(seeker, releaseCallbacks))
    }

    @Test fun contactIsOfferedOnlyForAValidPublishedContactAndVisitorsCannotActOnThemselves() {
        val visitorView = SessionProfile(accountId = "pro", listing = listing()).toUiState().copy(isOwner = false)
        val everything = ProfileActionId.entries.toSet()
        assertFalse(ProfileActionId.CONTACT in visibleProfileActionIds(visitorView, everything))
        val withPhone = visitorView.copy(listing = listing().copy(phoneNumber = "+919876543210"))
        assertTrue(ProfileActionId.CONTACT in visibleProfileActionIds(withPhone, everything))

        assertFalse(canMutateProfileVisitor(viewerId = "pro", ownerId = "pro", publicPreview = false))
        assertFalse(canMutateProfileVisitor(viewerId = null, ownerId = "pro", publicPreview = false))
        assertFalse(canMutateProfileVisitor(viewerId = "me", ownerId = "pro", publicPreview = true))
        assertTrue(canMutateProfileVisitor(viewerId = "me", ownerId = "pro", publicPreview = false))
    }

    @Test fun releaseRefusesProfileWritesAndChangesNothing() = runBlocking {
        val before = SessionProfile(accountId = "me", displayName = "Asha", city = "Karaikal", phoneNumber = "+919876543210")
        val store = SessionProfileStore(before)
        val vm = ProfileViewModel(store, capabilities = release, io = Dispatchers.Unconfined)
        val edit = EditProfileFormState("Someone Else", "", "Chennai", "", null, phoneNumber = "+919876543210")

        assertTrue(edit.isValid)
        assertFalse(vm.saveIdentity(edit))
        assertFalse(vm.setBanner("https://cdn.example/banner.jpg"))
        vm.setAvatar("https://cdn.example/avatar.jpg")
        assertEquals(before, store.profile.value)
    }

    private fun listing() = ServiceListing(
        title = "Carpentry",
        category = "carpentry",
        description = "Doors and windows",
        coverage = "Karaikal",
        hours = "9-5",
        hoursHint = "",
        response = "1h",
        tags = emptyList(),
        experience = "5",
    )
}
