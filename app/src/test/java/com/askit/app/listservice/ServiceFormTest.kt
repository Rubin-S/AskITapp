package com.askit.app.listservice

import org.junit.Assert.*
import org.junit.Test

class ServiceFormTest {
    private fun ready() = ListServiceViewModel().apply {
        selectCategory("electrician")
        updateTitle("Home wiring")
        updateDescription("Electrical repairs")
        toggleDeliveryMode(ListServiceDeliveryMode.REMOTE)
        updateAvailability("Weekdays")
        updateExperience("0")
        updateContact("+919876543210", "arun@example.com")
    }

    @Test fun completeRemoteServiceAcceptsZeroExperienceAndNoRange() {
        assertNotNull(ready().buildValidatedDraft())
    }

    @Test fun experienceAndAvailabilityAndContactAreRequired() {
        for (value in listOf("", "-1", "61", "2 years")) {
            assertNull("Experience: $value", ready().apply { updateExperience(value) }.buildValidatedDraft())
        }
        assertNull(ready().apply { updateAvailability("") }.buildValidatedDraft())
        assertNull(ready().apply { updateContact("", "") }.buildValidatedDraft())
    }

    @Test fun publicListingNeverCarriesContactDetails() {
        val draft = ready().buildValidatedDraft()!!
        val listing = draft.toServiceListing("Electrical")
        assertEquals("", listing.phoneNumber)
        assertEquals("", listing.email)
        // The owner-only draft keeps the contact the provider entered.
        assertEquals("arun@example.com", draft.contactEmail)
    }

    @Test fun savingAListingNeverRewritesTheVerifiedAccountIdentity() {
        val draft = ready().apply { updateContact("+919876543210", "listing@example.com") }.buildValidatedDraft()!!
        val store = com.askit.app.session.SessionProfileStore(
            com.askit.app.session.SessionProfile(accountId = "a", phoneNumber = "+919000000001", email = "me@example.com"),
        )
        store.applyListing(draft.toServiceListing("Electrical"), draft)
        assertEquals("+919000000001", store.profile.value.phoneNumber)
        assertEquals("me@example.com", store.profile.value.email)
        assertEquals("", store.profile.value.listing?.phoneNumber)
        assertEquals("", store.profile.value.listing?.email)
        val edit = com.askit.app.profile.EditProfileFormState(
            "Arun",
            "arun",
            "",
            "",
            null,
            email = "new@example.com",
            pincode = "600001",
            phoneNumber = "+919876543211",
        )
        store.updateIdentity(edit)
        assertEquals("", store.profile.value.listing?.email)
        assertEquals(edit.email, store.profile.value.listingDraft?.contactEmail)
        assertEquals(draft.title, store.profile.value.listing?.title)
    }

    @Test fun contactSentByTheServerIsStrippedFromThePublicListing() {
        val draft = ready().buildValidatedDraft()!!
        val profile = com.askit.app.auth.AccountProfile(
            displayName = "Arun",
            username = "arun",
            city = "Chennai",
            joinedYear = "2024",
            listing = draft.toServiceListing("Electrical").copy(phoneNumber = "+919876543210", email = "arun@example.com"),
        )
            .toSessionProfile("+919876543210", "a")
        assertEquals("", profile.listing?.phoneNumber)
        assertEquals("", profile.listing?.email)
    }

    @Test fun physicalServiceLocationIsRequiredButRangeIsOptional() {
        val vm = ready().apply {
            toggleDeliveryMode(ListServiceDeliveryMode.REMOTE)
            toggleDeliveryMode(ListServiceDeliveryMode.AT_CUSTOMER_LOCATION)
            selectCustomerLocation(null, "Chennai", 13.08, 80.27)
        }
        assertNotNull(vm.buildValidatedDraft())
    }

    @Test fun requiredProfessionalDetailsHaveAReachableValidationTarget() {
        assertEquals(
            ListServiceValidationField.PROFESSIONAL_DETAILS,
            firstInvalidField(setOf(ListServiceValidationField.PROFESSIONAL_DETAILS)),
        )
    }

    @Test fun serviceContactUsesCanonicalE164PhoneNumbers() {
        val draft = ready().apply { updateContact("9876543210", "arun@example.com") }.buildValidatedDraft()!!
        assertEquals("+919876543210", draft.contactPhone)
    }
}
