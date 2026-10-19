package com.askit.app.profile

import com.askit.app.config.AppLinks
import com.askit.app.config.profileUrl
import org.junit.Assert.*
import org.junit.Test

class ProfileActionsTest {
    @Test fun actionSetsMatchAllFourContexts() {
        assertEquals(
            listOf(ProfileActionId.EDIT, ProfileActionId.VIEW_AS, ProfileActionId.PROVIDER_DASHBOARD),
            profileActionIds(true, true),
        )
        assertEquals(listOf(ProfileActionId.FOLLOW, ProfileActionId.CONTACT, ProfileActionId.MESSAGE), profileActionIds(true, false))
        assertEquals(
            // Seekers can preview their public profile too ("View as").
            listOf(ProfileActionId.EDIT, ProfileActionId.VIEW_AS, ProfileActionId.CREATOR_DASHBOARD, ProfileActionId.POST_TASK),
            profileActionIds(false, true),
        )
        assertEquals(listOf(ProfileActionId.FOLLOW, ProfileActionId.MESSAGE), profileActionIds(false, false))
    }

    @Test fun publicPreviewSelfAndSignedOutViewsCannotMutate() {
        assertFalse(canMutateProfileVisitor("owner", "owner", false))
        assertFalse(canMutateProfileVisitor("visitor", "owner", true))
        assertFalse(canMutateProfileVisitor(null, "owner", false))
        assertTrue(canMutateProfileVisitor("visitor", "owner", false))
    }

    @Test fun contactIsOfferedOnlyWhenTheListingPublishesValidContact() {
        val listing = com.askit.app.session.DefaultCarpentryListing
        assertFalse(listing.copy(phoneNumber = "", email = "").hasPublishedContact)
        assertFalse(listing.copy(phoneNumber = "12", email = "not-an-email").hasPublishedContact)
        assertFalse((null as com.askit.app.session.ServiceListing?).hasPublishedContact)
        assertTrue(listing.copy(phoneNumber = "+919876543210").hasPublishedContact)
        assertTrue(listing.copy(email = "work@example.com").hasPublishedContact)
    }

    @Test fun ownerAndPublicAdaptersNeverPublishListingContact() {
        val owner = com.askit.app.session.SessionProfile(
            accountId = "owner",
            listing = com.askit.app.session.DefaultCarpentryListing.copy(phoneNumber = "+919876543210", email = "work@example.com"),
        )
        assertFalse(owner.toPublicProfile().listing.hasPublishedContact)
    }

    @Test fun cameraDenialExplainsAndPermanentDenialOffersSettings() {
        assertEquals(CameraPermissionOutcome.Capture, cameraPermissionOutcome(granted = true, canAskAgain = false))
        assertEquals(CameraPermissionOutcome.Explain, cameraPermissionOutcome(granted = false, canAskAgain = true))
        assertEquals(CameraPermissionOutcome.OpenSettings, cameraPermissionOutcome(granted = false, canAskAgain = false))
    }

    @Test fun releaseOwnersOnlySeeActionsTheirBuildCanComplete() {
        val provider = com.askit.app.session.SessionProfile(
            accountId = "o",
            listing = com.askit.app.session.DefaultCarpentryListing,
        ).toUiState()
        val seeker = com.askit.app.session.SessionProfile(accountId = "o").toUiState()
        // Release passes callbacks only for View as (always works) and Contact (dropped without published contact).
        val release = setOf(ProfileActionId.VIEW_AS, ProfileActionId.CONTACT)
        assertEquals(listOf(ProfileActionId.VIEW_AS), visibleProfileActionIds(provider, release))
        assertEquals(listOf(ProfileActionId.VIEW_AS), visibleProfileActionIds(seeker, release))
        assertEquals(profileActionIds(true, true), visibleProfileActionIds(provider, ProfileActionId.entries.toSet()))
        // "View as" previews the visitor buttons (disabled) whatever this build supports.
        val preview = com.askit.app.session.SessionProfile(accountId = "o", listing = com.askit.app.session.DefaultCarpentryListing)
            .toUiState(isPublicPreview = true)
        assertEquals(listOf(ProfileActionId.FOLLOW, ProfileActionId.MESSAGE), visibleProfileActionIds(preview, emptySet()))
    }

    @Test fun publicLinksRequireRealHttpsConfiguration() {
        assertNull(AppLinks().profileUrl("owner"))
        assertNull(AppLinks(publicProfileBaseUrl = "http://example.com/u").profileUrl("owner"))
        assertNull(AppLinks(publicProfileBaseUrl = "https://user:secret@example.com/u").profileUrl("owner"))
        assertNull(AppLinks(publicProfileBaseUrl = "https://example.com/u").profileUrl(""))
        assertEquals(
            "https://example.com/u/account%2Fone",
            AppLinks(publicProfileBaseUrl = "https://example.com/u").profileUrl("account/one"),
        )
    }
}
