package com.askit.app.posttask

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
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
class PostTaskRouteContractTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun completeDraft_emitsValidatedOtherCategory_andKeepsReviewVisible() {
        val viewModel = PostTaskViewModel(SavedStateHandle()).also {
            it.selectCategory(POST_TASK_OTHER_CATEGORY_ID)
            it.updateCustomCategory("Furniture assembly")
            it.updateTitle("Assemble a wardrobe")
            it.updateDetails("Please assemble the new wardrobe.")
            it.selectWorkMode(PostTaskWorkMode.REMOTE)
            it.selectTiming(PostTaskTimingMode.ASAP)
            check(it.review())
        }
        var emittedDraft: PostTaskDraft? = null
        var completionCount = 0

        composeTestRule.setContent {
            AskITTheme {
                PostTaskRoute(
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

        composeTestRule.onNodeWithTag("post_task_edit").performScrollTo().performClick()
        composeTestRule
            .onNodeWithTag("post_task_title")
            .performScrollTo()
            .performTextInput("edited ")
        composeTestRule.onNodeWithTag("post_task_review").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("post_task_complete").performScrollTo().performClick()

        assertEquals(1, completionCount)
        assertEquals(POST_TASK_OTHER_CATEGORY_ID, emittedDraft?.categoryId)
        assertEquals("Furniture assembly", emittedDraft?.customCategory)
        assertEquals("edited Assemble a wardrobe", emittedDraft?.title)
        composeTestRule
            .onNodeWithText("Review your task")
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun providerPreview_exposesPublicTaskData_butNotPrivateCreationData() {
        val viewModel = PostTaskViewModel(SavedStateHandle()).also {
            it.selectCategory(POST_TASK_OTHER_CATEGORY_ID)
            it.updateCustomCategory("Furniture assembly")
            it.updateTitle("Assemble a wardrobe")
            it.updateDetails("Please assemble the new wardrobe in the bedroom.")
            it.selectWorkMode(PostTaskWorkMode.AT_MY_LOCATION)
            it.selectLocation(
                placeId = "place-anna-nagar",
                publicAreaLabel = "Anna Nagar",
                latitude = 13.0850,
                longitude = 80.2101,
            )
            it.updatePrivateAddressOrLandmark("Flat 7B, 12 Example Street")
            it.selectTiming(PostTaskTimingMode.ASAP)
            it.selectBudgetMode(PostTaskBudgetMode.FIXED)
            it.updateFixedBudget("1200")
            it.setPhotoUris(
                listOf(
                    "android.resource://com.askit.app/${R.drawable.explore_results_empty}",
                    "android.resource://com.askit.app/${R.drawable.explore_results_server}",
                ),
            )
            it.updateQuantityOrMeasurements("2 wardrobes")
            it.updateBrandOrModel("IKEA PAX")
            it.selectMaterialsPolicy(PostTaskMaterialsPolicy.PROVIDER_BRINGS_MATERIALS)
            it.updateAccessInstructions("Call when you arrive at the gate.")
            check(it.review())
        }

        composeTestRule.setContent {
            AskITTheme {
                PostTaskRoute(
                    viewModel = viewModel,
                    onBack = {},
                )
            }
        }
        composeTestRule.waitForIdle()

        val preview = composeTestRule
            .onNodeWithTag("post_task_provider_preview_card", useUnmergedTree = true)
            .performScrollTo()
        preview.assertIsDisplayed()
        composeTestRule
            .onNodeWithContentDescription("2 photos", useUnmergedTree = true)
            .assertIsDisplayed()
        val previewText = semanticsText(preview.fetchSemanticsNode())

        assertTrue(previewText.contains("Furniture assembly"))
        assertTrue(previewText.contains("Anna Nagar"))
        assertTrue(previewText.contains("₹1200"))
        assertFalse(previewText.contains("Flat 7B, 12 Example Street"))
        assertFalse(previewText.contains("Call when you arrive at the gate."))
        assertFalse(previewText.contains("13.085"))
        assertFalse(previewText.contains("80.2101"))
        composeTestRule
            .onNodeWithText("Flat 7B, 12 Example Street")
            .performScrollTo()
            .assertIsDisplayed()
    }

    private fun semanticsText(node: SemanticsNode): String = buildString {
        if (node.config.contains(SemanticsProperties.Text)) {
            node.config[SemanticsProperties.Text].forEach { append(it.text).append('\n') }
        }
        node.children.forEach { append(semanticsText(it)) }
    }
}
