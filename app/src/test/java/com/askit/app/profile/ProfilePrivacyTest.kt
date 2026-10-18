package com.askit.app.profile

import com.askit.app.session.SessionProfile
import org.junit.Assert.*
import org.junit.Test

class ProfilePrivacyTest {
    @Test fun distanceRequiresExplicitConsentViewerPostcodeAndFiniteDistance() {
        val profile = SessionProfile()
        assertNull(profile.approximateDistance("641001", 3.5))
        val consented = profile.copy(discloseApproximateLocation = true)
        assertNull(consented.approximateDistance("", 3.5))
        assertNull(consented.approximateDistance("641001", null))
        assertNull(consented.approximateDistance("641001", Double.NaN))
        assertNull(consented.approximateDistance("641001", -1.0))
        assertEquals(4, consented.approximateDistance("641001", 3.5))
    }
    @Test fun bannerMapsToOwnerAndReadOnlyPublicPreview() {
        val profile = SessionProfile(bannerUrl = "content://photo/banner")
        assertTrue(profile.toUiState().canEditMedia)
        assertFalse(profile.toUiState(isPublicPreview = true).canEditMedia)
        assertFalse(ProfileUiState(isOwner = false).canEditMedia)
        assertEquals(profile.bannerUrl, profile.toUiState().bannerUrl)
        assertEquals(profile.bannerUrl, profile.toUiState(isPublicPreview = true).bannerUrl)
    }
    @Test fun removingBannerRestoresNeutralCover() {
        val store = com.askit.app.session.SessionProfileStore(SessionProfile(accountId = "owner", bannerUrl = "content://banner"))
        store.setBanner("owner", null)
        assertNull(store.profile.value.toUiState().bannerUrl)
    }
    @Test fun publicDistanceMappingIsPrivateUnlessAllInputsExist() {
        val user = com.askit.app.home.details.UserProfileData("other", "other", "Other",
            discloseApproximateLocation = true, postcodeDistanceKm = 2.7)
        assertNull(user.toUiState().approximateDistanceKm)
        assertEquals(3, user.toUiState(viewerPostcode = "641001").approximateDistanceKm)
        assertNull(user.copy(discloseApproximateLocation = false).toUiState(viewerPostcode = "641001").approximateDistanceKm)
    }
}
