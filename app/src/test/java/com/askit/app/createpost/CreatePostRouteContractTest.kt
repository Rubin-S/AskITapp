package com.askit.app.createpost

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.lifecycle.SavedStateHandle
import com.askit.designsystem.theme.AskITTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w360dp-h800dp-normal-long-notround-any-xxxhdpi-keyshidden-nonav")
class CreatePostRouteContractTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun textFlow_validatesPreviewsEditsAndCompletesOneDraft() {
        val viewModel = CreatePostViewModel(SavedStateHandle())
        var completed: PostDraft? = null
        var completionCount = 0
        composeTestRule.setContent {
            AskITTheme(darkTheme = false) {
                CreatePostRoute(
                    viewModel = viewModel,
                    onBack = {},
                    onCompleteDraft = {
                        completed = it
                        completionCount += 1
                    },
                )
            }
        }

        composeTestRule.onNodeWithText("Create post").assertIsDisplayed()
        composeTestRule.onNodeWithTag("create_post_preview_top").performClick()
        composeTestRule.onNodeWithText("Write something for your post.").assertIsDisplayed()
        composeTestRule.onNodeWithTag("create_post_text_body")
            .performTextInput("A multiline\ncommunity update")
        composeTestRule.onNodeWithTag("create_post_preview_top").performClick()
        composeTestRule.onNodeWithText("Post preview").assertIsDisplayed()
        composeTestRule.onAllNodesWithTag("create_post_edit_top").assertCountEquals(0)
        composeTestRule.onNodeWithTag("create_post_content").performScrollToNode(
            hasTestTag("post_feed_text_body"),
        )
        composeTestRule.onNodeWithTag("post_feed_text_body").assertIsDisplayed()
        composeTestRule.onNodeWithTag("create_post_edit").performClick()
        composeTestRule.onNodeWithTag("create_post_text_body").assertIsDisplayed()
        composeTestRule.onNodeWithTag("create_post_text_body").performTextInput(" edited")
        composeTestRule.onNodeWithTag("create_post_preview_top").performClick()
        composeTestRule.onNodeWithTag("create_post_complete").performClick()

        assertEquals(1, completionCount)
        assertTrue(completed?.content is PostContentDraft.Text)
        assertEquals(
            "editedA multiline\ncommunity update",
            (completed?.content as PostContentDraft.Text).body,
        )
    }

    @Test
    fun typeSelector_startsWithText_andSwitchesEmptyDraftImmediately() {
        val viewModel = CreatePostViewModel(SavedStateHandle())
        composeTestRule.setContent {
            AskITTheme(darkTheme = false) {
                CreatePostRoute(viewModel = viewModel, onBack = {})
            }
        }

        composeTestRule.onNodeWithTag("create_post_type_text").assertIsDisplayed()
        composeTestRule.onNodeWithTag("create_post_type_photo").performClick()
        composeTestRule.onNodeWithText("Add a photo to your post").assertIsDisplayed()
    }
}
