package com.askit.app.listservice

import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.lifecycle.SavedStateHandle
import com.askit.app.R
import com.askit.designsystem.theme.AskITTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w360dp-h800dp-normal-long-notround-any-xxxhdpi-keyshidden-nonav")
class ListServiceRouteContractTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun customerPreview_exposesPublicServiceData_butNotMatchingCoordinatesOrPersonData() {
        val viewModel = richCustomerViewModel()
        var emittedDraft: ListServiceDraft? = null
        var completionCount = 0

        composeTestRule.setContent {
            AskITTheme {
                ListServiceRoute(
                    viewModel = viewModel,
                    onBack = {},
                    onCompleteDraft = {
                        emittedDraft = it
                        completionCount += 1
                    },
                )
            }
        }
        composeTestRule.waitForIdle()

        val preview = composeTestRule
            .onNodeWithTag("list_service_customer_preview_card", useUnmergedTree = true)
            .performScrollTo()
        preview.assertIsDisplayed()
        val previewText = semanticsText(preview.fetchSemanticsNode())

        assertTrue(previewText.contains("Furniture assembly"))
        assertTrue(previewText.contains("Anna Nagar"))
        assertTrue(previewText.contains("₹1500"))
        assertTrue(previewText.contains("Remote"))
        assertFalse(previewText.contains("13.085"))
        assertFalse(previewText.contains("80.2101"))
        assertFalse(previewText.contains("Flat 7B"))
        assertFalse(previewText.contains("+91"))

        composeTestRule.onNodeWithTag("list_service_edit").performScrollTo().performClick()
        composeTestRule
            .onNodeWithTag("list_service_title_field")
            .performScrollTo()
            .performTextInput("edited ")
        composeTestRule.onNodeWithTag("list_service_review").performScrollTo().performClick()
        composeTestRule
            .onNodeWithTag("list_service_complete", useUnmergedTree = true)
            .performScrollTo()
            .performClick()
        assertEquals(1, completionCount)
        assertEquals(LIST_SERVICE_OTHER_CATEGORY_ID, emittedDraft?.categoryId)
        assertEquals("Furniture assembly", emittedDraft?.customCategory)
        assertEquals("edited Assemble a wardrobe", emittedDraft?.title)
        assertEquals("Anna Nagar", emittedDraft?.customerCoverage?.publicAreaLabel)
    }

    @Test
    fun sparseReview_omitsEmptyOptionalRows_andRemoteOnlyHasNoLocationFollowUp() {
        val viewModel = ListServiceViewModel(SavedStateHandle()).also {
            it.selectCategory("plumber")
            it.updateTitle("Remote tap advice")
            it.updateDescription("I help customers diagnose tap problems over a video call.")
            it.toggleDeliveryMode(ListServiceDeliveryMode.REMOTE)
            check(it.review())
        }

        composeTestRule.setContent {
            AskITTheme {
                ListServiceRoute(viewModel = viewModel, onBack = {})
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithTag("list_service_customer_preview_card", useUnmergedTree = true)
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Remote-only services do not need a geographic follow-up.")
            .assertDoesNotExist()
        composeTestRule.onNodeWithText("Availability").assertDoesNotExist()
        composeTestRule.onNodeWithText("Contact for quote").assertDoesNotExist()
        val preview = composeTestRule
            .onNodeWithTag("list_service_customer_preview_card", useUnmergedTree = true)
            .performScrollTo()
        val previewText = semanticsText(preview.fetchSemanticsNode())
        assertTrue(previewText.contains("Remote tap advice"))
        assertTrue(previewText.contains("I help customers diagnose tap problems over a video call."))
        assertTrue(previewText.contains("Remote"))
    }

    @Test
    fun form_otherCategory_isVisibleImmediatelyBelowCategory() {
        val viewModel = ListServiceViewModel(SavedStateHandle()).also {
            it.selectCategory(LIST_SERVICE_OTHER_CATEGORY_ID)
        }

        composeTestRule.setContent {
            AskITTheme {
                ListServiceRoute(viewModel = viewModel, onBack = {})
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithTag("list_service_custom_category")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("What service do you offer?").assertIsDisplayed()
    }

    private fun richCustomerViewModel(): ListServiceViewModel =
        ListServiceViewModel(SavedStateHandle()).also { viewModel ->
            viewModel.selectCategory(LIST_SERVICE_OTHER_CATEGORY_ID)
            viewModel.updateCustomCategory("Furniture assembly")
            viewModel.updateTitle("Assemble a wardrobe")
            viewModel.updateDescription("I assemble flat-pack wardrobes and make sure doors align correctly.")
            viewModel.toggleDeliveryMode(ListServiceDeliveryMode.AT_CUSTOMER_LOCATION)
            viewModel.toggleDeliveryMode(ListServiceDeliveryMode.REMOTE)
            viewModel.selectCustomerLocation(
                placeId = "place-anna-nagar",
                publicAreaLabel = "Anna Nagar",
                latitude = 13.0850,
                longitude = 80.2101,
            )
            viewModel.selectCoverageRadius(10)
            viewModel.toggleOptionalSection(ListServiceOptionalSection.PRICING)
            viewModel.selectPricingMode(ListServicePricingMode.STARTING_AT)
            viewModel.updatePriceAmount("1500")
            viewModel.setPortfolioUris(
                listOf(
                    "android.resource://com.askit.app/${R.drawable.explore_results_empty}",
                    "android.resource://com.askit.app/${R.drawable.explore_results_server}",
                ),
            )
            viewModel.toggleOptionalSection(ListServiceOptionalSection.MORE_DETAILS)
            viewModel.updateExperience("8 years")
            check(viewModel.review())
        }

    private fun semanticsText(node: SemanticsNode): String = buildString {
        if (node.config.contains(SemanticsProperties.Text)) {
            node.config[SemanticsProperties.Text].forEach { append(it.text).append('\n') }
        }
        node.children.forEach { append(semanticsText(it)) }
    }
}
