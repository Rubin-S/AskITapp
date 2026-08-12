package com.askit.app.posttask

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.text.AnnotatedString
import com.askit.app.MainActivity
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w360dp-h800dp-normal-long-notround-any-xxxhdpi-keyshidden-nonav")
class PostTaskFlowTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun createPostTask_opensOneDestination_andHidesBottomBar() {
        openPostTask()

        composeTestRule.onNodeWithText("Post a task").assertIsDisplayed()
        composeTestRule.onAllNodesWithContentDescription("Create").assertCountEquals(0)
        composeTestRule.onNodeWithTag("post_task_category_options").assertIsDisplayed()
    }

    @Test
    fun requiredReview_keepsInput_andShowsActionableErrors() {
        openPostTask()
        composeTestRule.onNodeWithTag("post_task_title").performTextInput("Repair the tap")
        composeTestRule.onNodeWithTag("post_task_review").performScrollTo().performClick()

        composeTestRule.onNodeWithText("Choose a category.").assertIsDisplayed()
        composeTestRule.onNodeWithText("Describe the work you need.").assertIsDisplayed()
        assertEditableText("post_task_title", "Repair the tap")
    }

    @Test
    fun remoteTask_canReview_edit_andRetainsValues() {
        openPostTask()
        selectCategory("plumber")
        composeTestRule.onNodeWithTag("post_task_title").performTextInput("Repair the tap")
        composeTestRule.onNodeWithTag("post_task_details").performTextInput("The kitchen tap is leaking.")
        composeTestRule.onNodeWithTag("post_task_work_mode_remote").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("post_task_timing_asap").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("post_task_review").performScrollTo().performClick()

        composeTestRule.onNodeWithText("Review your task").assertIsDisplayed()
        composeTestRule.onAllNodesWithText("Repair the tap").assertCountEquals(2)
        composeTestRule.onAllNodesWithText("The kitchen tap is leaking.").assertCountEquals(2)

        composeTestRule.onNodeWithTag("post_task_edit").performScrollTo().performClick()
        assertEditableText("post_task_title", "Repair the tap")
        assertEditableText("post_task_details", "The kitchen tap is leaking.")
        composeTestRule.onNodeWithTag("post_task_work_mode_remote").assertIsSelected()
        composeTestRule.onNodeWithTag("post_task_timing_asap").assertIsSelected()
    }

    @Test
    fun otherCategory_canBeSelected_andTypedInReview() {
        openPostTask()
        composeTestRule.onNodeWithTag("post_task_category_dropdown").performClick()
        composeTestRule.onNodeWithTag("post_task_category_other").performClick()
        composeTestRule.onNodeWithTag("post_task_custom_category")
            .performTextInput("Furniture assembly")
        composeTestRule.onNodeWithTag("post_task_title").performTextInput("Assemble a wardrobe")
        composeTestRule.onNodeWithTag("post_task_details")
            .performTextInput("Please assemble the new wardrobe.")
        composeTestRule.onNodeWithTag("post_task_work_mode_remote").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("post_task_timing_asap").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("post_task_review").performScrollTo().performClick()

        composeTestRule.onAllNodesWithText("Furniture assembly").assertCountEquals(2)
        composeTestRule.onNodeWithTag("post_task_edit").performScrollTo().performClick()
        assertEditableText("post_task_custom_category", "Furniture assembly")
    }

    @Test
    fun backWithChanges_showsDiscardDialog_andDiscardReturnsToHome() {
        openPostTask()
        composeTestRule.onNodeWithTag("post_task_title").performTextInput("Draft task")

        composeTestRule.activity.onBackPressedDispatcher.onBackPressed()
        composeTestRule.onNodeWithText("Discard this draft?").assertIsDisplayed()
        composeTestRule.onNodeWithText("Keep editing").performClick()
        assertEditableText("post_task_title", "Draft task")

        composeTestRule.activity.onBackPressedDispatcher.onBackPressed()
        composeTestRule.onNodeWithText("Discard").performClick()
        composeTestRule.onNodeWithContentDescription("Home").assertIsSelected()
        composeTestRule.onAllNodesWithContentDescription("Create").assertCountEquals(1)
    }

    @Test
    fun activityRecreation_preservesPostTaskDraft() {
        openPostTask()
        selectCategory("plumber")
        composeTestRule.onNodeWithTag("post_task_title").performTextInput("Restored task")
        composeTestRule.activity.recreate()
        composeTestRule.waitForIdle()

        assertEditableText("post_task_title", "Restored task")
        composeTestRule.onNodeWithTag("post_task_category_dropdown")
            .assertTextContains("Plumber")
        composeTestRule.onAllNodesWithContentDescription("Create").assertCountEquals(0)
    }

    private fun openPostTask() {
        composeTestRule.onNodeWithContentDescription("Create").performClick()
        composeTestRule.onNodeWithText("Post a task").performClick()
        composeTestRule.waitForIdle()
    }

    private fun selectCategory(categoryId: String) {
        composeTestRule.onNodeWithTag("post_task_category_dropdown").performClick()
        composeTestRule.onNodeWithTag("post_task_category_$categoryId").performClick()
    }

    private fun assertEditableText(tag: String, expected: String) {
        assertEquals(
            AnnotatedString(expected),
            composeTestRule
                .onNodeWithTag(tag)
                .fetchSemanticsNode()
                .config[SemanticsProperties.EditableText],
        )
    }
}
