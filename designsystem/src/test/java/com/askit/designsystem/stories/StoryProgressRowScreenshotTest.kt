package com.askit.designsystem.stories

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
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
class StoryProgressRowScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun firstSegmentStarting_light() = capture(
        darkTheme = false,
        fileName = "story_progress_first_light_360",
        activeSegmentIndex = 0,
        activeSegmentFraction = 0.4f,
    )

    @Test
    fun middleSegment_light() = capture(
        darkTheme = false,
        fileName = "story_progress_middle_light_360",
        activeSegmentIndex = 1,
        activeSegmentFraction = 0.75f,
    )

    @Test
    fun middleSegment_dark() = capture(
        darkTheme = true,
        fileName = "story_progress_middle_dark_360",
        activeSegmentIndex = 1,
        activeSegmentFraction = 0.75f,
    )

    private fun capture(
        darkTheme: Boolean,
        fileName: String,
        activeSegmentIndex: Int,
        activeSegmentFraction: Float,
    ) {
        composeTestRule.setContent {
            AskITTheme(darkTheme = darkTheme) {
                Surface(color = Color(0xFF101010)) {
                    StoryProgressRow(
                        segmentCount = 4,
                        activeSegmentIndex = activeSegmentIndex,
                        activeSegmentFraction = activeSegmentFraction,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 16.dp),
                    )
                }
            }
        }
        composeTestRule.onRoot().captureRoboImage(
            filePath = "src/test/screenshots/$fileName.png",
        )
    }
}
