package com.askit.app.posttask

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
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
@Config(sdk = [34], qualifiers = "ta-rIN-w360dp-h800dp-normal-long-notround-any-xxxhdpi-keyshidden-nonav")
class PostTaskTamilScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun post_task_form_tamil_360() {
        composeTestRule.setContent {
            AskITTheme(darkTheme = false) {
                PostTaskRoute(
                    viewModel = PostTaskViewModel(),
                    onBack = {},
                    onCompleteDraft = {},
                )
            }
        }
        composeTestRule.waitForIdle()

        capture("post_task_form_tamil_360")
    }

    @OptIn(ExperimentalRoborazziApi::class)
    private fun capture(name: String) {
        composeTestRule.onRoot().captureRoboImage(
            filePath = "src/test/screenshots/$name.png",
        )
    }
}
