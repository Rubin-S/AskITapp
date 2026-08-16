package com.askit.app.listservice

import androidx.lifecycle.SavedStateHandle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ListServiceViewModelTest {

    @Test
    fun initialDraft_isClean_andReviewReportsRequiredFields() {
        val viewModel = ListServiceViewModel(SavedStateHandle())

        assertFalse(viewModel.isDirty)
        assertFalse(viewModel.review())
        assertEquals(
            setOf(
                ListServiceValidationField.CATEGORY,
                ListServiceValidationField.TITLE,
                ListServiceValidationField.DESCRIPTION,
                ListServiceValidationField.DELIVERY_MODE,
            ),
            viewModel.validationErrors(),
        )
    }

    @Test
    fun remoteOnly_service_needsNoLocation_andEmitsServiceOwnedDraft() {
        val viewModel = completeRequiredFields(ListServiceViewModel(SavedStateHandle()))
        viewModel.toggleDeliveryMode(ListServiceDeliveryMode.REMOTE)

        assertTrue(viewModel.review())
        assertEquals(
            ListServiceDraft(
                categoryId = "plumber",
                title = "Repair kitchen taps",
                description = "I repair leaking taps and replace worn washers.",
                deliveryModes = setOf(ListServiceDeliveryMode.REMOTE),
                details = ListServiceDetails(
                    availability = null,
                    experience = null,
                    typicalDuration = null,
                    materials = null,
                    credentials = null,
                    warranty = null,
                    advanceNotice = null,
                ),
            ),
            viewModel.buildValidatedDraft(),
        )
    }

    @Test
    fun otherCategory_requiresTypedValue_andUsesCustomTextInDraft() {
        val viewModel = completeRequiredFields(ListServiceViewModel(SavedStateHandle()))
        viewModel.selectCategory(LIST_SERVICE_OTHER_CATEGORY_ID)
        viewModel.toggleDeliveryMode(ListServiceDeliveryMode.REMOTE)

        assertFalse(viewModel.review())
        assertTrue(ListServiceValidationField.CATEGORY in viewModel.validationErrors())

        viewModel.updateCustomCategory("Furniture assembly")
        assertTrue(viewModel.review())
        assertEquals(LIST_SERVICE_OTHER_CATEGORY_ID, viewModel.buildValidatedDraft()?.categoryId)
        assertEquals("Furniture assembly", viewModel.buildValidatedDraft()?.customCategory)
    }

    @Test
    fun customerLocation_requiresAreaAndRadius_andStoresOnlyStructuredPublicLocation() {
        val viewModel = completeRequiredFields(ListServiceViewModel(SavedStateHandle()))
        viewModel.toggleDeliveryMode(ListServiceDeliveryMode.AT_CUSTOMER_LOCATION)

        assertFalse(viewModel.review())
        assertTrue(ListServiceValidationField.CUSTOMER_LOCATION in viewModel.validationErrors())
        assertTrue(ListServiceValidationField.COVERAGE_RADIUS in viewModel.validationErrors())

        viewModel.selectCustomerLocation(
            placeId = "place-anna-nagar",
            publicAreaLabel = "Anna Nagar",
            latitude = 13.0850,
            longitude = 80.2101,
        )
        viewModel.selectCoverageRadius(10)

        assertTrue(viewModel.review())
        assertEquals(
            ListServiceCoverage(
                publicAreaLabel = "Anna Nagar",
                placeId = "place-anna-nagar",
                latitude = 13.0850,
                longitude = 80.2101,
                radiusKm = 10,
            ),
            viewModel.buildValidatedDraft()?.customerCoverage,
        )
    }

    @Test
    fun providerLocation_requiresARealPublicArea_andCanBeCombinedWithRemote() {
        val viewModel = completeRequiredFields(ListServiceViewModel(SavedStateHandle()))
        viewModel.toggleDeliveryMode(ListServiceDeliveryMode.AT_PROVIDER_LOCATION)
        viewModel.toggleDeliveryMode(ListServiceDeliveryMode.REMOTE)

        assertFalse(viewModel.review())
        assertTrue(ListServiceValidationField.PROVIDER_LOCATION in viewModel.validationErrors())

        viewModel.selectProviderLocation(
            placeId = "place-adyar",
            publicAreaLabel = "Adyar",
            latitude = 13.0067,
            longitude = 80.2572,
        )
        assertTrue(viewModel.review())
        assertEquals("Adyar", viewModel.buildValidatedDraft()?.providerLocation?.publicAreaLabel)
        assertEquals(
            setOf(
                ListServiceDeliveryMode.AT_PROVIDER_LOCATION,
                ListServiceDeliveryMode.REMOTE,
            ),
            viewModel.buildValidatedDraft()?.deliveryModes,
        )
    }

    @Test
    fun pricing_modes_validatePositiveAmount_andRangeOrder() {
        val viewModel = completeRequiredFields(ListServiceViewModel(SavedStateHandle()))
        viewModel.toggleDeliveryMode(ListServiceDeliveryMode.REMOTE)
        viewModel.toggleOptionalSection(ListServiceOptionalSection.PRICING)
        viewModel.selectPricingMode(ListServicePricingMode.RANGE)
        viewModel.updateMinimumPrice("3000")
        viewModel.updateMaximumPrice("2000")

        assertFalse(viewModel.review())
        assertTrue(ListServiceValidationField.PRICE_RANGE in viewModel.validationErrors())

        viewModel.updateMaximumPrice("5000")
        assertTrue(viewModel.review())
        assertEquals(
            ListServicePricing.Range("3000", "5000"),
            viewModel.buildValidatedDraft()?.pricing,
        )

        viewModel.edit()
        viewModel.selectPricingMode(ListServicePricingMode.CONTACT_FOR_QUOTE)
        assertTrue(viewModel.review())
        assertEquals(
            ListServicePricing.ContactForQuote,
            viewModel.buildValidatedDraft()?.pricing,
        )
    }

    @Test
    fun richState_survivesSavedStateRestoration_afterOptionalSectionsCollapse() {
        val savedStateHandle = SavedStateHandle()
        val viewModel = completeRequiredFields(ListServiceViewModel(savedStateHandle))
        viewModel.selectCategory(LIST_SERVICE_OTHER_CATEGORY_ID)
        viewModel.updateCustomCategory("Furniture assembly")
        viewModel.toggleDeliveryMode(ListServiceDeliveryMode.AT_CUSTOMER_LOCATION)
        viewModel.toggleDeliveryMode(ListServiceDeliveryMode.REMOTE)
        viewModel.selectCustomerLocation("place-1", "Anna Nagar", 13.0850, 80.2101)
        viewModel.selectCoverageRadius(25)
        viewModel.toggleOptionalSection(ListServiceOptionalSection.PRICING)
        viewModel.selectPricingMode(ListServicePricingMode.STARTING_AT)
        viewModel.updatePriceAmount("1500")
        viewModel.toggleOptionalSection(ListServiceOptionalSection.PRICING)
        viewModel.toggleOptionalSection(ListServiceOptionalSection.MORE_DETAILS)
        viewModel.updateAvailability("Weekday evenings")
        viewModel.updateExperience("8 years")
        viewModel.updateTypicalDuration("2–3 hours")
        viewModel.updateMaterials("I bring hand tools")
        viewModel.updateCredentials("ITI certified")
        viewModel.updateWarranty("30-day workmanship support")
        viewModel.updateAdvanceNotice("Two days")
        viewModel.setPortfolioUris(listOf("content://photo/1", "content://photo/2"))
        viewModel.removePortfolioPhoto("content://photo/1")

        val restored = ListServiceViewModel(savedStateHandle)
        val restoredState = restored.formState.value
        assertEquals(LIST_SERVICE_OTHER_CATEGORY_ID, restoredState.categoryId)
        assertEquals("Furniture assembly", restoredState.customCategory)
        assertEquals(
            setOf(
                ListServiceDeliveryMode.AT_CUSTOMER_LOCATION,
                ListServiceDeliveryMode.REMOTE,
            ),
            restoredState.deliveryModes,
        )
        assertEquals("Anna Nagar", restoredState.customerAreaLabel)
        assertEquals(25, restoredState.coverageRadiusKm)
        assertEquals(ListServicePricingMode.STARTING_AT, restoredState.pricingMode)
        assertEquals("1500", restoredState.priceAmount)
        assertEquals(listOf("content://photo/2"), restoredState.portfolioUris)
        assertEquals("ITI certified", restoredState.credentials)
        assertTrue(restored.isDirty)
    }

    @Test
    fun publishedDraft_mapsOntoListing_andLoadDraftRestoresTitle() {
        val viewModel = completeRequiredFields(ListServiceViewModel(SavedStateHandle()))
        viewModel.toggleDeliveryMode(ListServiceDeliveryMode.REMOTE)
        viewModel.updateExperience("5 yrs exp")
        assertTrue(viewModel.review())
        val draft = requireNotNull(viewModel.buildValidatedDraft())
        val listing = draft.toServiceListing("Plumber")
        assertEquals("Repair kitchen taps", listing.title)
        assertEquals("Plumber", listing.category)
        assertEquals("plumber", listing.categoryId)
        assertEquals("5 yrs exp", listing.experience)

        val editor = ListServiceViewModel(SavedStateHandle())
        editor.loadDraft(draft)
        assertEquals("Repair kitchen taps", editor.formState.value.title)
        assertEquals("plumber", editor.formState.value.categoryId)
        assertEquals("5 yrs exp", editor.formState.value.experience)
        assertEquals(setOf(ListServiceDeliveryMode.REMOTE), editor.formState.value.deliveryModes)
    }

    @Test
    fun serviceDraft_doesNotOwnPersonIdentityOrContactFields() {
        val fieldNames = ListServiceDraft::class.java.declaredFields.map { it.name }.toSet()

        listOf(
            "personName",
            "avatarUrl",
            "phone",
            "email",
            "bio",
            "rating",
            "reviewCount",
            "verification",
        ).forEach { forbiddenField ->
            assertFalse("Unexpected Person field: $forbiddenField", forbiddenField in fieldNames)
        }
    }

    private fun completeRequiredFields(viewModel: ListServiceViewModel): ListServiceViewModel {
        viewModel.selectCategory("plumber")
        viewModel.updateTitle("Repair kitchen taps")
        viewModel.updateDescription("I repair leaking taps and replace worn washers.")
        return viewModel
    }
}
