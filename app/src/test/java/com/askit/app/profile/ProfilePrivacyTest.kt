package com.askit.app.profile

import com.askit.app.session.SessionProfile
import org.junit.Assert.*
import org.junit.Test

class ProfilePrivacyTest {
    @Test fun distanceRequiresExplicitConsentViewerPostcodeAndFiniteDistance() {
        assertNull(approximateProfileDistance(consent = false, viewerPostcode = "641001", distanceKm = 3.5))
        assertNull(approximateProfileDistance(consent = true, viewerPostcode = "", distanceKm = 3.5))
        assertNull(approximateProfileDistance(consent = true, viewerPostcode = null, distanceKm = 3.5))
        assertNull(approximateProfileDistance(consent = true, viewerPostcode = "641001", distanceKm = null))
        assertNull(approximateProfileDistance(consent = true, viewerPostcode = "641001", distanceKm = Double.NaN))
        assertNull(approximateProfileDistance(consent = true, viewerPostcode = "641001", distanceKm = -1.0))
        assertEquals(4, approximateProfileDistance(consent = true, viewerPostcode = "641001", distanceKm = 3.5))
    }

    @Test fun bannerMapsToOwnerAndReadOnlyPublicPreview() {
        val profile = SessionProfile(bannerUrl = "content://photo/banner")
        assertTrue(profile.toUiState().isOwnerView)
        assertFalse(profile.toUiState(isPublicPreview = true).isOwnerView)
        assertFalse(ProfileUiState(isOwner = false).isOwnerView)
        assertEquals(profile.bannerUrl, profile.toUiState().bannerUrl)
        assertEquals(profile.bannerUrl, profile.toUiState(isPublicPreview = true).bannerUrl)
    }

    @Test fun removingBannerRestoresNeutralCover() {
        val store = com.askit.app.session.SessionProfileStore(SessionProfile(accountId = "owner", bannerUrl = "content://banner"))
        store.setBanner("owner", null)
        assertNull(store.profile.value.toUiState().bannerUrl)
    }

    @Test fun publicDistanceMappingIsPrivateUnlessAllInputsExist() {
        val user = com.askit.app.home.details.UserProfileData(
            "other",
            "other",
            "Other",
            discloseApproximateLocation = true,
            postcodeDistanceKm = 2.7,
        )
        assertNull(user.toUiState().approximateDistanceKm)
        assertEquals(3, user.toUiState(viewerPostcode = "641001").approximateDistanceKm)
        assertNull(user.copy(discloseApproximateLocation = false).toUiState(viewerPostcode = "641001").approximateDistanceKm)
    }
}
