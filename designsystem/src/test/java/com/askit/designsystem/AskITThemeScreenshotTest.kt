package com.askit.designsystem

import androidx.compose.material3.Text
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
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
@Config(sdk = [34])
class AskITThemeScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun askITTheme_rendersBaseline() {
        composeTestRule.setContent {
            AskITTheme(darkTheme = false) {
                Text(text = "AskIT")
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }
}
