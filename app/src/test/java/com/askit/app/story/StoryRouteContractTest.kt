package com.askit.app.story

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.SavedStateHandle
import com.askit.designsystem.theme.AskITTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w360dp-h800dp-normal-long-notround-any-xxxhdpi-keyshidden-nonav")
class StoryRouteContractTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun capture_overflow_opensStickerTrayFromEditor() {
        val viewModel = StoryViewModel(SavedStateHandle())
        viewModel.onMediaCaptured("content://photo", StoryMediaType.Photo)

        composeTestRule.setContent {
            AskITTheme(darkTheme = false) {
                StoryRoute(
                    viewModel = viewModel,
                    onBack = {},
                    useFakeCapturePreview = true,
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Back").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Stickers").performClick()
        composeTestRule.onNodeWithText("Search stickers").assertIsDisplayed()
    }

    @Test
    fun editor_next_opensShareSheet() {
        val viewModel = StoryViewModel(SavedStateHandle())
        viewModel.onMediaCaptured("content://photo", StoryMediaType.Photo)

        composeTestRule.setContent {
            AskITTheme(darkTheme = false) {
                StoryRoute(
                    viewModel = viewModel,
                    onBack = {},
                    useFakeCapturePreview = true,
                )
            }
        }

        composeTestRule.onNodeWithText("Next").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("story_share_sheet").assertIsDisplayed()
    }

    @Test
    fun editor_textTool_opensDoneControl() {
        val viewModel = StoryViewModel(SavedStateHandle())
        viewModel.onMediaCaptured("content://photo", StoryMediaType.Photo)

        composeTestRule.setContent {
            AskITTheme(darkTheme = false) {
                StoryRoute(
                    viewModel = viewModel,
                    onBack = {},
                    useFakeCapturePreview = true,
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Text").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription("Done").assertIsDisplayed()
    }
}
