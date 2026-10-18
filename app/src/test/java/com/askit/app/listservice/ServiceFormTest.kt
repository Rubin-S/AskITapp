package com.askit.app.listservice

import org.junit.Assert.*
import org.junit.Test

class ServiceFormTest {
    private fun ready() = ListServiceViewModel().apply {
        selectCategory("electrician"); updateTitle("Home wiring"); updateDescription("Electrical repairs")
        toggleDeliveryMode(ListServiceDeliveryMode.REMOTE)
        updateAvailability("Weekdays"); updateExperience("0")
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
    @Test fun listingAndAccountContactsStayCanonicalThroughBothForms() {
        val draft = ready().buildValidatedDraft()!!
        val store = com.askit.app.session.SessionProfileStore()
        store.applyListing(draft.toServiceListing("Electrical"), draft)
        assertEquals(draft.contactEmail, store.profile.value.email)
        val edit = com.askit.app.profile.EditProfileFormState("Arun", "arun", "", "", null,
            email = "new@example.com", pincode = "600001", phoneNumber = "+919876543211")
        store.updateIdentity(edit)
        assertEquals(edit.email, store.profile.value.listing?.email)
        assertEquals(edit.email, store.profile.value.listingDraft?.contactEmail)
        assertEquals(draft.title, store.profile.value.listing?.title)
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
        assertEquals(ListServiceValidationField.PROFESSIONAL_DETAILS,
            firstInvalidField(setOf(ListServiceValidationField.PROFESSIONAL_DETAILS)))
    }
    @Test fun serviceContactUsesCanonicalE164PhoneNumbers() {
        val draft = ready().apply { updateContact("9876543210", "arun@example.com") }.buildValidatedDraft()!!
        assertEquals("+919876543210", draft.contactPhone)
    }
}
