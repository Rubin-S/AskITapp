package com.askit.app.story

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.lifecycle.SavedStateHandle
import com.askit.designsystem.theme.AskITTheme
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w360dp-h800dp-normal-long-notround-any-xxxhdpi-keyshidden-nonav")
class StoryScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun story_capture_light_360() {
        setCapture(darkTheme = false)
        capture("story_capture_light_360")
    }

    @Test
    fun story_capture_dark_360() {
        setCapture(darkTheme = true)
        capture("story_capture_dark_360")
    }

    @Test
    fun story_editor_light_360() {
        setEditor(darkTheme = false)
        capture("story_editor_light_360")
    }

    @Test
    fun story_editor_dark_360() {
        setEditor(darkTheme = true)
        capture("story_editor_dark_360")
    }

    @Test
    fun story_text_tool_light_360() {
        val viewModel = StoryViewModel(SavedStateHandle()).apply {
            onMediaCaptured("content://photo", StoryMediaType.Photo)
            addTextLayer("Hello")
        }
        composeTestRule.setContent {
            AskITTheme(darkTheme = false) {
                StoryRoute(viewModel = viewModel, onBack = {}, useFakeCapturePreview = true)
            }
        }
        composeTestRule.onNodeWithContentDescription("Text").performClick()
        composeTestRule.waitForIdle()
        capture("story_text_tool_light_360")
    }

    @Test
    fun story_sticker_sheet_light_360() {
        val viewModel = StoryViewModel(SavedStateHandle()).apply {
            onMediaCaptured("content://photo", StoryMediaType.Photo)
            openStickerTray()
        }
        composeTestRule.setContent {
            AskITTheme(darkTheme = false) {
                StoryRoute(viewModel = viewModel, onBack = {}, useFakeCapturePreview = true)
            }
        }
        composeTestRule.waitForIdle()
        capture("story_sticker_sheet_light_360")
    }

    @Test
    fun story_share_sheet_dark_360() {
        val viewModel = StoryViewModel(SavedStateHandle()).apply {
            onMediaCaptured("content://photo", StoryMediaType.Photo)
            openShareSheet()
        }
        composeTestRule.setContent {
            AskITTheme(darkTheme = true) {
                StoryRoute(viewModel = viewModel, onBack = {}, useFakeCapturePreview = true)
            }
        }
        composeTestRule.waitForIdle()
        capture("story_share_sheet_dark_360")
    }

    @Test
    @Config(qualifiers = "ta-rIN-w360dp-h800dp-normal-long-notround-any-xxxhdpi-keyshidden-nonav")
    fun story_capture_tamil_360() {
        setCapture(darkTheme = false)
        capture("story_capture_tamil_360")
    }

    @Test
    @Config(qualifiers = "ta-rIN-w360dp-h800dp-normal-long-notround-any-xxxhdpi-keyshidden-nonav")
    fun story_editor_tamil_360() {
        setEditor(darkTheme = false)
        capture("story_editor_tamil_360")
    }

    private fun setCapture(darkTheme: Boolean) {
        composeTestRule.setContent {
            AskITTheme(darkTheme = darkTheme) {
                StoryRoute(
                    viewModel = StoryViewModel(SavedStateHandle()),
                    onBack = {},
                    useFakeCapturePreview = true,
                )
            }
        }
        composeTestRule.waitForIdle()
    }

    private fun setEditor(darkTheme: Boolean) {
        val viewModel = StoryViewModel(SavedStateHandle()).apply {
            onMediaCaptured("content://photo", StoryMediaType.Photo)
            updateCaption("Neighborhood update")
        }
        composeTestRule.setContent {
            AskITTheme(darkTheme = darkTheme) {
                StoryRoute(viewModel = viewModel, onBack = {}, useFakeCapturePreview = true)
            }
        }
        composeTestRule.waitForIdle()
    }

    @OptIn(ExperimentalRoborazziApi::class)
    private fun capture(name: String) {
        composeTestRule.onRoot().captureRoboImage(
            filePath = "src/test/screenshots/$name.png",
        )
    }
}
