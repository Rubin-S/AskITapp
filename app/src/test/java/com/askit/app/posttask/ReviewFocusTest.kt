package com.askit.app.posttask

import com.askit.app.listservice.ListServiceOptionalSection
import com.askit.app.listservice.ListServicePricingMode
import com.askit.app.listservice.ListServiceValidationField
import com.askit.app.listservice.ListServiceViewModel
import com.askit.app.listservice.owningSection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ReviewFocusTest {
    @Test fun reviewExpandsTheCollapsedBudgetSectionThatOwnsTheError() {
        val viewModel = PostTaskViewModel().apply {
            selectBudgetMode(PostTaskBudgetMode.FIXED)
            updateFixedBudget("")
        }
        assertFalse(PostTaskOptionalSection.BUDGET in viewModel.formState.value.expandedOptionalSections)
        assertFalse(viewModel.review())
        assertTrue(PostTaskOptionalSection.BUDGET in viewModel.formState.value.expandedOptionalSections)
    }

    @Test fun reviewExpandsTheCollapsedPricingSectionThatOwnsTheError() {
        val viewModel = ListServiceViewModel().apply {
            selectPricingMode(ListServicePricingMode.RANGE)
            updateMinimumPrice("900")
            updateMaximumPrice("100")
        }
        assertFalse(ListServiceOptionalSection.PRICING in viewModel.formState.value.expandedOptionalSections)
        assertFalse(viewModel.review())
        assertTrue(ListServiceOptionalSection.PRICING in viewModel.formState.value.expandedOptionalSections)
    }

    @Test fun onlyFieldsInsideCollapsibleSectionsHaveAnOwningSection() {
        assertEquals(PostTaskOptionalSection.BUDGET, PostTaskValidationField.BUDGET_RANGE.owningSection())
        assertEquals(PostTaskOptionalSection.PREFERENCES, PostTaskValidationField.AGE_PREFERENCE.owningSection())
        assertNull(PostTaskValidationField.TITLE.owningSection())
        assertEquals(ListServiceOptionalSection.PRICING, ListServiceValidationField.PRICE.owningSection())
        assertNull(ListServiceValidationField.TITLE.owningSection())
    }
}
