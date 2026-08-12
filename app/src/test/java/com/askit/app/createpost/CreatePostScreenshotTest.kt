package com.askit.app.createpost

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.lifecycle.SavedStateHandle
import com.askit.designsystem.theme.AskITTheme
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
class CreatePostScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun text_editor_light_360() {
        capture(
            viewModel = CreatePostViewModel(SavedStateHandle()),
            darkTheme = false,
            fileName = "create_post_text_editor_light_360",
        )
    }

    @Test
    fun poll_preview_dark_360() {
        val viewModel = CreatePostViewModel(SavedStateHandle()).also {
            it.selectType(PostType.POLL)
            it.updatePollQuestion("Which finish looks better?")
            it.updatePollOption(0, "Matte black")
            it.updatePollOption(1, "Natural wood")
            it.updatePollDescription("Choose the finish for the next project.")
            it.preview()
        }
        capture(
            viewModel = viewModel,
            darkTheme = true,
            fileName = "create_post_poll_preview_dark_360",
        )
    }

    @Test
    @Config(qualifiers = "ta-rIN-w360dp-h800dp-normal-long-notround-any-xxxhdpi-keyshidden-nonav")
    fun carousel_editor_tamil_360() {
        val viewModel = CreatePostViewModel(SavedStateHandle()).also {
            it.selectType(PostType.CAROUSEL)
            it.updateCarouselCaption("முடித்த பணியின் படிகள்")
        }
        capture(
            viewModel = viewModel,
            darkTheme = false,
            fileName = "create_post_carousel_editor_tamil_360",
        )
    }

    private fun capture(
        viewModel: CreatePostViewModel,
        darkTheme: Boolean,
        fileName: String,
    ) {
        composeTestRule.setContent {
            AskITTheme(darkTheme = darkTheme) {
                CreatePostRoute(
                    viewModel = viewModel,
                    onBack = {},
                    onCompleteDraft = {},
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage(
            filePath = "src/test/screenshots/$fileName.png",
        )
    }
}
