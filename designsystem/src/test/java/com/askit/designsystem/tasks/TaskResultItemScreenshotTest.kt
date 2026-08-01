package com.askit.designsystem.tasks

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
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
class TaskResultItemScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun task_result_items_light() {
        captureItems(darkTheme = false, fileName = "task_result_items_light")
    }

    @Test
    fun task_result_items_dark() {
        captureItems(darkTheme = true, fileName = "task_result_items_dark")
    }

    private fun captureItems(darkTheme: Boolean, fileName: String) {
        composeTestRule.setContent {
            AskITTheme(darkTheme = darkTheme) {
                Surface {
                    Column(modifier = Modifier.width(360.dp).fillMaxWidth()) {
                        OpenLocalTask()
                        HorizontalDivider()
                        AppliedRemoteTask()
                        HorizontalDivider()
                        FilledTask()
                        HorizontalDivider()
                        Box(modifier = Modifier.width(320.dp)) {
                            LongContentTask()
                        }
                    }
                }
            }
        }

        composeTestRule.onRoot().captureRoboImage(
            filePath = "src/test/screenshots/$fileName.png",
        )
    }

    @Composable
    private fun OpenLocalTask() {
        TaskResultItem(
            title = "Repair laptop charging port",
            category = "Computer repair",
            summary = "Laptop only charges when the cable is held at an angle.",
            budgetLabel = "₹800–₹1,500",
            locationLabel = "Kallakurichi",
            timingLabel = "Needed Monday",
            posterName = "Arun P.",
            postedLabel = "Posted 2h ago",
            status = TaskResultStatus.Open,
            onClick = {},
        )
    }

    @Composable
    private fun AppliedRemoteTask() {
        TaskResultItem(
            title = "Translate a two-page document",
            category = "Translation",
            summary = "Translate the supplied English document into Tamil.",
            budgetLabel = "Quote required",
            locationLabel = "Remote",
            timingLabel = "Flexible",
            posterName = "Suresh K.",
            postedLabel = "Posted 40m ago",
            status = TaskResultStatus.Applied,
            onClick = {},
        )
    }

    @Composable
    private fun FilledTask() {
        TaskResultItem(
            title = "Repair washing machine",
            category = "Appliance repair",
            summary = "Machine stops during the spin cycle.",
            budgetLabel = "₹700–₹1,000",
            locationLabel = "Kallakurichi",
            timingLabel = "Needed yesterday",
            posterName = "Lakshmi R.",
            postedLabel = "Posted yesterday",
            status = TaskResultStatus.Filled,
            onClick = {},
        )
    }

    @Composable
    private fun LongContentTask() {
        TaskResultItem(
            title = "A very long task title that should remain readable at narrow widths",
            category = "A long category label for a task with detailed scope",
            summary = "A long task summary that can grow to two lines without clipping important context.",
            budgetLabel = "Quote required",
            locationLabel = "A long locality label for a narrow screen",
            timingLabel = "Needed next Monday afternoon",
            posterName = "A privacy-safe poster display name",
            postedLabel = "Posted yesterday",
            status = TaskResultStatus.Closed,
            onClick = {},
        )
    }
}
