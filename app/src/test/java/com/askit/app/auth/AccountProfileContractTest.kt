package com.askit.app.auth

import com.askit.app.listservice.ListServiceDeliveryMode
import com.askit.app.listservice.ListServiceDetails
import com.askit.app.listservice.ListServiceDraft
import com.askit.app.listservice.ListServicePricing
import com.askit.app.listservice.toServiceListing
import com.askit.app.profile.ProfileHighlight
import org.junit.Assert.*
import org.junit.Test

class AccountProfileContractTest {
    private fun account() = AccountProfile(displayName = "Arun", username = "arun", city = "Chennai", joinedYear = "2023",
        email = "arun@example.com", pincode = "641001")

    private val draft = ListServiceDraft(
        categoryId = "electrician", title = "Electrical repairs and maintenance",
        description = "Home wiring, lighting installation and electrical troubleshooting.",
        deliveryModes = setOf(ListServiceDeliveryMode.REMOTE),
        pricing = ListServicePricing.Hourly("350"), portfolioUris = emptyList(),
        contactPhone = "+919876543298", contactEmail = "arun@example.com",
        details = ListServiceDetails("Weekdays, 9 AM - 6 PM", "5", "1-2 hours", "Standard tools", "Electrical technician", "30 days", "One day"),
    )

    @Test fun ownerContractRetainsTheEditableServiceAndHighlights() {
        val highlight = ProfileHighlight("work", "a", "Work")
        val profile = account().copy(listing = draft.toServiceListing("Electrician"), listingDraft = draft,
            highlights = listOf(highlight)).toSessionProfile("+919876543298", "a")
        assertEquals(draft, profile.listingDraft)
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
