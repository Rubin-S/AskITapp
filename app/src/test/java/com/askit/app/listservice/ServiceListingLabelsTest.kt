package com.askit.app.listservice

import com.askit.app.R
import com.askit.app.session.AccountRole
import com.askit.app.session.DefaultCarpentryListing
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Listing labels come from typed fields and string resources, not English baked into the model. */
class ServiceListingLabelsTest {
    // A fake resource lookup that makes the resource id and arguments visible in the output.
    private val names = mapOf(
        R.string.list_service_quote_contact to "contact",
        R.string.list_service_quote_starting_at to "from %s",
        R.string.post_task_budget_fixed_value to "fixed %s",
        R.string.list_service_quote_hourly to "hourly %s",
        R.string.list_service_quote_per_visit to "visit %s",
        R.string.post_task_budget_range_display to "range %s-%s",
        R.string.list_service_coverage_up_to_km to "radius %d",
        R.string.list_service_mode_travels_to_site to "travels",
        R.string.list_service_mode_at_provider to "at-provider",
        R.string.list_service_remote to "remote",
    )
    private val fake: (Int, Array<out Any>) -> String = { id, args -> names.getValue(id).format(*args) }

    private fun draft() = ListServiceViewModel().apply {
        selectCategory("electrician")
        updateTitle("Home wiring")
        updateDescription("Electrical repairs")
        toggleDeliveryMode(ListServiceDeliveryMode.REMOTE)
        updateAvailability("Weekdays")
        updateExperience("2")
        updateContact("+919876543210", "arun@example.com")
    }.buildValidatedDraft()!!

    @Test fun newListingsCarryTypedFieldsAndNoEnglishLabels() {
        val listing = draft().copy(pricing = ListServicePricing.Hourly("400")).toServiceListing("Electrical")
        assertEquals(ListServicePricing.Hourly("400"), listing.pricing)
        assertEquals("", listing.quoteLabel)
        assertEquals("", listing.coverageHint)
        assertEquals("", listing.responseHint)
        assertNull(listing.coverageRadiusKm)
        assertEquals(ServiceListingLabels("hourly 400", "", "remote"), listing.labels(fake))
    }

    @Test fun noPricingMeansContactForQuote() {
        val listing = draft().copy(pricing = null).toServiceListing("Electrical")
        assertEquals("contact", listing.labels(fake).quote)
    }

    @Test fun everyPricingKindHasALabel() {
        val base = draft().toServiceListing("Electrical")
        val expected = mapOf(
            ListServicePricing.ContactForQuote to "contact",
            ListServicePricing.StartingAt("300") to "from 300",
            ListServicePricing.Fixed("500") to "fixed 500",
            ListServicePricing.Hourly("200") to "hourly 200",
            ListServicePricing.PerVisit("250") to "visit 250",
            ListServicePricing.Range("100", "900") to "range 100-900",
        )
        expected.forEach { (pricing, label) -> assertEquals(label, base.copy(pricing = pricing).labels(fake).quote) }
    }

    @Test fun radiusAndDeliveryModesAreFormattedInFormOrder() {
        val listing = draft().toServiceListing("Electrical").copy(
            coverageRadiusKm = 12,
            deliveryModes = setOf(ListServiceDeliveryMode.REMOTE, ListServiceDeliveryMode.AT_CUSTOMER_LOCATION),
        )
        val labels = listing.labels(fake)
        assertEquals("radius 12", labels.coverageHint)
        assertEquals("travels · remote", labels.responseHint)
    }

    @Test fun legacyListingsKeepTheirStoredLabels() {
        val labels = DefaultCarpentryListing.labels(fake)
        assertEquals(ServiceListingLabels("Quote on visit", "Up to 10 km", "Travels to site"), labels)
    }

    @Test fun listingsSavedBeforeTheTypedFieldsStillDecode() {
        val legacyJson = """{"title":"T","category":"C","description":"D","quoteLabel":"₹500","coverage":"",""" +
            """"coverageHint":"Up to 5 km","hours":"","hoursHint":"","response":"","responseHint":"Remote",""" +
            """"tags":[],"experience":""}"""
        val listing = Json.decodeFromString(com.askit.app.session.ServiceListing.serializer(), legacyJson)
        assertNull(listing.pricing)
        assertEquals("₹500", listing.labels(fake).quote)
        assertEquals("Up to 5 km", listing.labels(fake).coverageHint)
    }

    @Test fun accountRoleStoredValuesAreTheWireValues() {
        assertEquals(setOf("Seeker", "Provider", "Both"), AccountRole.storedValues)
        assertEquals(AccountRole.Both, AccountRole.fromStoredValue("Both"))
        assertNull(AccountRole.fromStoredValue("Admin"))
    }
}
