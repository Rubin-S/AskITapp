package com.askit.app

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.text.AnnotatedString
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w360dp-h800dp-normal-long-notround-any-xxxhdpi-keyshidden-nonav")
class CreationNavigationLifecycleTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun cleanDraft_toolbarBack_exitsEachCreationRoute() {
        openTask()
        toolbarBack()
        assertHome()

        openService()
        toolbarBack()
        assertHome()

        openPost()
        toolbarBack()
        assertHome()
    }

    @Test
    fun cleanDraft_systemBack_exitsEachCreationRoute() {
        openTask()
        systemBack()
        assertHome()

        openService()
        systemBack()
        assertHome()

        openPost()
        systemBack()
        assertHome()
    }

    @Test
    fun dirtyDraft_toolbarBack_showsCanonicalDiscard_andDiscardExitsEachRoute() {
        openTask()
        composeTestRule.onNodeWithTag("post_task_title").performTextInput("Repair leaking tap")
        toolbarBack()
        assertDiscardDialog()
        composeTestRule.onNodeWithText("Keep editing").performClick()
        assertEditableText("post_task_title", "Repair leaking tap")
        toolbarBack()
        discard()
        assertHome()

        openService()
        composeTestRule.onNodeWithTag("list_service_title_field")
            .performTextInput("Furniture assembly")
        toolbarBack()
        assertDiscardDialog()
        composeTestRule.onNodeWithText("Keep editing").performClick()
        assertEditableText("list_service_title_field", "Furniture assembly")
        toolbarBack()
        discard()
        assertHome()

        openPost()
        composeTestRule.onNodeWithTag("create_post_text_body")
            .performTextInput("Community update")
        toolbarBack()
        assertDiscardDialog()
        composeTestRule.onNodeWithText("Keep editing").performClick()
        assertEditableText("create_post_text_body", "Community update")
        toolbarBack()
        discard()
        assertHome()
    }

    @Test
    fun dirtyDraft_systemBack_showsDiscard_andKeepEditingPreservesEachRoute() {
        openTask()
        composeTestRule.onNodeWithTag("post_task_title").performTextInput("Repair leaking tap")
        systemBack()
        assertDiscardDialog()
        composeTestRule.onNodeWithText("Keep editing").performClick()
        assertEditableText("post_task_title", "Repair leaking tap")
        systemBack()
        discard()
        assertHome()

        openService()
        composeTestRule.onNodeWithTag("list_service_title_field")
            .performTextInput("Furniture assembly")
        systemBack()
        assertDiscardDialog()
        composeTestRule.onNodeWithText("Keep editing").performClick()
        assertEditableText("list_service_title_field", "Furniture assembly")
        systemBack()
        discard()
        assertHome()

        openPost()
        composeTestRule.onNodeWithTag("create_post_text_body")
            .performTextInput("Community update")
        systemBack()
        assertDiscardDialog()
        composeTestRule.onNodeWithText("Keep editing").performClick()
        assertEditableText("create_post_text_body", "Community update")
        systemBack()
        discard()
        assertHome()
    }

    @Test
    fun dialogSystemBack_returnsToEditing_withoutDiscarding() {
        openTask()
        composeTestRule.onNodeWithTag("post_task_title").performTextInput("Repair leaking tap")

        systemBack()
        assertDiscardDialog()
        systemBack()

        composeTestRule.onAllNodesWithText("Discard this draft?").assertCountEquals(0)
        assertEditableText("post_task_title", "Repair leaking tap")
        composeTestRule.onAllNodesWithContentDescription("Create").assertCountEquals(0)
    }

    @Test
    fun discardedDrafts_reopenClean_andDoNotLeakAcrossFlows() {
        openTask()
        composeTestRule.onNodeWithTag("post_task_title").performTextInput("Repair leaking tap")
        systemBack()
        discard()
        assertHome()

        openService()
        composeTestRule.onAllNodesWithText("Repair leaking tap").assertCountEquals(0)
        composeTestRule.onNodeWithTag("list_service_title_field")
            .performTextInput("Furniture assembly")
        systemBack()
        discard()
        assertHome()

        openTask()
        assertEditableText("post_task_title", "")
        composeTestRule.onAllNodesWithText("Furniture assembly").assertCountEquals(0)
        toolbarBack()
        assertHome()

        openPost()
        composeTestRule.onAllNodesWithText("Repair leaking tap").assertCountEquals(0)
        composeTestRule.onAllNodesWithText("Furniture assembly").assertCountEquals(0)
        composeTestRule.onNodeWithTag("create_post_text_body")
            .performTextInput("Community update")
        systemBack()
        discard()
        assertHome()

        openTask()
        composeTestRule.onAllNodesWithText("Community update").assertCountEquals(0)
        toolbarBack()
        assertHome()

        openService()
        composeTestRule.onAllNodesWithText("Community update").assertCountEquals(0)
        assertEditableText("list_service_title_field", "")
        toolbarBack()
        assertHome()
    }

    @Test
    fun activityRecreation_restoresCurrentCreationRoute_withoutDuplicateEntry() {
        openTask()
        composeTestRule.onNodeWithTag("post_task_title").performTextInput("Restored task")
        recreateActivity()
        assertEditableText("post_task_title", "Restored task")
        composeTestRule.onAllNodesWithContentDescription("Create").assertCountEquals(0)
        toolbarBack()
        discard()
        assertHome()

        openService()
        composeTestRule.onNodeWithTag("list_service_title_field")
            .performTextInput("Restored service")
        recreateActivity()
        assertEditableText("list_service_title_field", "Restored service")
        composeTestRule.onAllNodesWithContentDescription("Create").assertCountEquals(0)
        toolbarBack()
        discard()
        assertHome()

        openPost()
        composeTestRule.onNodeWithTag("create_post_text_body")
            .performTextInput("Restored post")
        recreateActivity()
        assertEditableText("create_post_text_body", "Restored post")
        composeTestRule.onAllNodesWithContentDescription("Create").assertCountEquals(0)
        toolbarBack()
        discard()
        assertHome()
    }

    @Test
    fun createSheet_dismissThenReopen_andChooseTask_pushesOneCreationEntry() {
        openCreateSheet()
        composeTestRule.onNodeWithContentDescription("Close sheet").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onAllNodesWithText("Post a task").assertCountEquals(0)

        openTask()
        composeTestRule.onNodeWithText("Post a task").assertIsDisplayed()
        composeTestRule.onAllNodesWithContentDescription("Create").assertCountEquals(0)
        systemBack()
        assertHome()
    }

    private fun openCreateSheet() {
        composeTestRule.onNodeWithContentDescription("Create").performClick()
        composeTestRule.onNodeWithText("Create").assertIsDisplayed()
    }

    private fun openTask() {
        openCreateSheet()
        composeTestRule.onNodeWithText("Post a task").performClick()
        composeTestRule.onNodeWithTag("post_task_category_options").assertIsDisplayed()
        composeTestRule.onAllNodesWithContentDescription("Create").assertCountEquals(0)
    }

    private fun openService() {
        openCreateSheet()
        composeTestRule.onNodeWithText("List a service").performClick()
        composeTestRule.onNodeWithTag("list_service_category_options").assertIsDisplayed()
        composeTestRule.onAllNodesWithContentDescription("Create").assertCountEquals(0)
    }

    private fun openPost() {
        openCreateSheet()
        composeTestRule.onNodeWithText("Create a post").performClick()
        composeTestRule.onNodeWithText("Create post").assertIsDisplayed()
        composeTestRule.onAllNodesWithContentDescription("Create").assertCountEquals(0)
    }

    private fun toolbarBack() {
        composeTestRule.onNodeWithContentDescription("Back").performClick()
        composeTestRule.waitForIdle()
    }

    private fun systemBack() {
        composeTestRule.activity.onBackPressedDispatcher.onBackPressed()
        composeTestRule.waitForIdle()
    }

    private fun discard() {
        composeTestRule.onNodeWithText("Discard").performClick()
        composeTestRule.waitForIdle()
    }

    private fun assertDiscardDialog() {
        composeTestRule.onNodeWithText("Discard this draft?").assertIsDisplayed()
        composeTestRule.onNodeWithText("Your changes will be lost.").assertIsDisplayed()
        composeTestRule.onNodeWithText("Keep editing").assertIsDisplayed()
        composeTestRule.onNodeWithText("Discard").assertIsDisplayed()
    }

    private fun assertHome() {
        composeTestRule.onNodeWithContentDescription("Home").assertIsSelected()
        composeTestRule.onAllNodesWithContentDescription("Create").assertCountEquals(1)
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

    private fun recreateActivity() {
        composeTestRule.activity.recreate()
        composeTestRule.waitForIdle()
    }
}
