package com.askit.app.auth

import com.askit.app.preview.previewProfile
import com.askit.app.profile.ProfileHighlight
import org.junit.Assert.*
import org.junit.Test

class AccountProfileContractTest {
    private fun account() = AccountProfile(displayName = "Arun", username = "arun", joinedYear = "2023",
        email = "arun@example.com", pincode = "641001")

    @Test fun ownerContractRetainsTheEditableServiceAndHighlights() {
        val sample = previewProfile("a")
        val highlight = ProfileHighlight("work", "a", "Work")
        val profile = account().copy(email = sample.email!!, listing = sample.listing, listingDraft = sample.listingDraft,
            highlights = listOf(highlight)).toSessionProfile("+919876543298", "a")
        assertEquals(sample.listingDraft, profile.listingDraft)
        assertEquals(listOf(highlight), profile.highlights)
        assertEquals("a", profile.accountId)
    }
    @Test fun foreignHighlightCannotEnterAnAccountSnapshot() {
        assertThrows(AuthApiException::class.java) {
            account().copy(highlights = listOf(ProfileHighlight("work", "other", "Work")))
                .toSessionProfile("+919876543298", "a")
        }
    }
}
