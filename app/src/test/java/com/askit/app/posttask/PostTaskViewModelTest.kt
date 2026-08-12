package com.askit.app.posttask

import androidx.lifecycle.SavedStateHandle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PostTaskViewModelTest {

    @Test
    fun initialDraft_isClean_andReviewReportsRequiredFields() {
        val viewModel = PostTaskViewModel(SavedStateHandle())

        assertFalse(viewModel.isDirty)
        assertFalse(viewModel.review())
        assertEquals(
            setOf(
                PostTaskValidationField.CATEGORY,
                PostTaskValidationField.TITLE,
                PostTaskValidationField.DETAILS,
                PostTaskValidationField.WORK_MODE,
                PostTaskValidationField.TIMING,
            ),
            viewModel.validationErrors(),
        )
    }

    @Test
    fun remoteTask_canBeReviewed_withoutLocation_andEmitsExactDraft() {
        val viewModel = completeRequiredFields(PostTaskViewModel(SavedStateHandle()))
        viewModel.selectWorkMode(PostTaskWorkMode.REMOTE)

        assertTrue(viewModel.review())
        assertEquals(
            PostTaskDraft(
                categoryId = "plumber",
                title = "Repair the tap",
                details = "The kitchen tap is leaking.",
                workMode = PostTaskWorkMode.REMOTE,
                location = null,
                timingMode = PostTaskTimingMode.ASAP,
                selectedDateMillis = null,
                budget = null,
                photos = emptyList(),
                itemDetails = null,
                accessInstructions = null,
            ),
            viewModel.buildValidatedDraft(),
        )
    }

    @Test
    fun otherCategory_requiresTypedValue_andEmitsItSeparately() {
        val viewModel = completeRequiredFields(PostTaskViewModel(SavedStateHandle()))
        viewModel.selectCategory(POST_TASK_OTHER_CATEGORY_ID)
        viewModel.selectWorkMode(PostTaskWorkMode.REMOTE)

        assertFalse(viewModel.review())
        assertTrue(PostTaskValidationField.CATEGORY in viewModel.validationErrors())

        viewModel.updateCustomCategory("Furniture assembly")
        assertTrue(viewModel.review())
        assertEquals(POST_TASK_OTHER_CATEGORY_ID, viewModel.buildValidatedDraft()?.categoryId)
        assertEquals("Furniture assembly", viewModel.buildValidatedDraft()?.customCategory)
    }

    @Test
    fun atMyLocation_requiresStructuredLocation_butProviderLocationDoesNot() {
        val viewModel = completeRequiredFields(PostTaskViewModel(SavedStateHandle()))
        viewModel.selectWorkMode(PostTaskWorkMode.AT_MY_LOCATION)

        assertFalse(viewModel.review())
        assertTrue(PostTaskValidationField.LOCATION in viewModel.validationErrors())

        viewModel.selectLocation(
            placeId = "place-123",
            publicAreaLabel = "Kallakurichi",
            latitude = 11.7401,
            longitude = 78.9597,
        )
        viewModel.updatePrivateAddressOrLandmark("Near the east gate")
        assertTrue(viewModel.review())
        assertEquals("Kallakurichi", viewModel.buildValidatedDraft()?.location?.publicAreaLabel)
        assertEquals(11.7401, viewModel.buildValidatedDraft()?.location?.latitude)
        assertEquals("Near the east gate", viewModel.buildValidatedDraft()?.location?.privateAddressOrLandmark)

        viewModel.edit()
        viewModel.selectWorkMode(PostTaskWorkMode.AT_PROVIDER_LOCATION)
        assertTrue(viewModel.review())
        assertEquals(null, viewModel.buildValidatedDraft()?.location)
    }

    @Test
    fun timingModes_andDate_areValidatedOnlyWhenRelevant() {
        val viewModel = completeRequiredFields(PostTaskViewModel(SavedStateHandle()))
        viewModel.selectWorkMode(PostTaskWorkMode.REMOTE)
        viewModel.selectTiming(PostTaskTimingMode.DATE)

        assertFalse(viewModel.review())
        assertTrue(PostTaskValidationField.DATE in viewModel.validationErrors())

        viewModel.selectDate(1_756_000_000_000)
        assertTrue(viewModel.review())
        assertEquals(1_756_000_000_000, viewModel.buildValidatedDraft()?.selectedDateMillis)

        viewModel.edit()
        viewModel.selectTiming(PostTaskTimingMode.FLEXIBLE)
        assertTrue(viewModel.review())
        assertEquals(null, viewModel.buildValidatedDraft()?.selectedDateMillis)
    }

    @Test
    fun budgetModes_validateAmounts_andRequestQuotesNeedsNoAmount() {
        val viewModel = completeRequiredFields(PostTaskViewModel(SavedStateHandle()))
        viewModel.selectWorkMode(PostTaskWorkMode.REMOTE)
        viewModel.toggleOptionalSection(PostTaskOptionalSection.BUDGET)
        viewModel.selectBudgetMode(PostTaskBudgetMode.FIXED)

        assertFalse(viewModel.review())
        assertTrue(PostTaskValidationField.FIXED_BUDGET in viewModel.validationErrors())

        viewModel.updateFixedBudget("2500")
        assertTrue(viewModel.review())
        assertEquals(PostTaskBudget.Fixed("2500"), viewModel.buildValidatedDraft()?.budget)

        viewModel.edit()
        viewModel.selectBudgetMode(PostTaskBudgetMode.RANGE)
        viewModel.updateMinimumBudget("3000")
        viewModel.updateMaximumBudget("2000")
        assertFalse(viewModel.review())
        assertTrue(PostTaskValidationField.BUDGET_RANGE in viewModel.validationErrors())

        viewModel.updateMaximumBudget("5000")
        assertTrue(viewModel.review())
        assertEquals(PostTaskBudget.Range("3000", "5000"), viewModel.buildValidatedDraft()?.budget)

        viewModel.edit()
        viewModel.selectBudgetMode(PostTaskBudgetMode.REQUEST_QUOTES)
        assertTrue(viewModel.review())
        assertEquals(PostTaskBudget.RequestQuotes, viewModel.buildValidatedDraft()?.budget)
    }

    @Test
    fun optionalValues_surviveCollapse_andSavedStateRestoration() {
        val savedStateHandle = SavedStateHandle()
        val viewModel = completeRequiredFields(PostTaskViewModel(savedStateHandle))
        viewModel.selectWorkMode(PostTaskWorkMode.REMOTE)
        viewModel.toggleOptionalSection(PostTaskOptionalSection.ITEM_DETAILS)
        viewModel.updateQuantityOrMeasurements("2 taps")
        viewModel.updateBrandOrModel("Aqua")
        viewModel.selectMaterialsPolicy(PostTaskMaterialsPolicy.REQUESTER_HAS_MATERIALS)
        viewModel.toggleOptionalSection(PostTaskOptionalSection.ITEM_DETAILS)
        viewModel.setPhotoUris(listOf("content://photo/1", "content://photo/2"))
        viewModel.removePhoto("content://photo/1")

        val restored = PostTaskViewModel(savedStateHandle)
        assertEquals("2 taps", restored.formState.value.quantityOrMeasurements)
        assertEquals("Aqua", restored.formState.value.brandOrModel)
        assertEquals(PostTaskMaterialsPolicy.REQUESTER_HAS_MATERIALS, restored.formState.value.materialsPolicy)
        assertEquals(listOf("content://photo/2"), restored.formState.value.photoUris)
        assertTrue(restored.isDirty)
    }

    private fun completeRequiredFields(viewModel: PostTaskViewModel): PostTaskViewModel {
        viewModel.selectCategory("plumber")
        viewModel.updateTitle("Repair the tap")
        viewModel.updateDetails("The kitchen tap is leaking.")
        viewModel.selectTiming(PostTaskTimingMode.ASAP)
        return viewModel
    }
}
