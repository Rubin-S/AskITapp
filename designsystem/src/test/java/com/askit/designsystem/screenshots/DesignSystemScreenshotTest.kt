package com.askit.designsystem.screenshots

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.askit.designsystem.R
import com.askit.designsystem.components.AskITPrimaryButton
import com.askit.designsystem.empty.AskITEmptyState
import com.askit.designsystem.inputs.AskITTextField
import com.askit.designsystem.jobs.AskITSecondaryButton
import com.askit.designsystem.jobs.JobBannerTone
import com.askit.designsystem.jobs.JobStatusBanner
import com.askit.designsystem.jobs.JobStepState
import com.askit.designsystem.jobs.JobStepper
import com.askit.designsystem.jobs.JobStepperItem
import com.askit.designsystem.jobs.OtpBoxes
import com.askit.designsystem.navigation.AskITSectionTabs
import com.askit.designsystem.navigation.SectionTab
import com.askit.designsystem.profile.ChipRow
import com.askit.designsystem.profile.MetricSpec
import com.askit.designsystem.profile.MetricTrio
import com.askit.designsystem.stories.StoryProgressRow
import com.askit.designsystem.theme.AskITTheme
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Design-system components in light and dark. Their copy comes from the caller, so locale
 * coverage (en/ta) lives in the app's screenshot tests.
 *
 * Record: ./gradlew :designsystem:recordRoborazziDebug   Verify: ./gradlew :designsystem:verifyRoborazziDebug
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w360dp-h640dp-mdpi")
class DesignSystemScreenshotTest {
    @Test fun buttons() = snapshot("buttons") {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            AskITPrimaryButton(onClick = {}, modifier = Modifier.fillMaxWidth()) { Text("Post a task") }
            AskITPrimaryButton(onClick = {}, modifier = Modifier.fillMaxWidth(), enabled = false) { Text("Post a task") }
            AskITSecondaryButton(onClick = {}, modifier = Modifier.fillMaxWidth()) { Text("Save draft") }
        }
    }

    @Test fun textFields() = snapshot("text_fields") {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            AskITTextField(value = "Fix kitchen tap", onValueChange = {}, label = { Text("Title") })
            AskITTextField(
                value = "",
                onValueChange = {},
                label = { Text("Budget") },
                isError = true,
                supportingText = { Text("Enter an amount") },
            )
        }
    }

    @Test fun emptyState() = snapshot("empty_state") {
        Box(Modifier.height(320.dp)) {
            AskITEmptyState(
                iconRes = R.drawable.ic_chat,
                title = "No messages yet",
                supporting = "Conversations with providers show up here.",
                actionLabel = "Explore services",
                onAction = {},
            )
        }
    }

    @Test fun jobStatus() = snapshot("job_status") {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            JobStatusBanner("Request sent", "Waiting for Ravi to accept", JobBannerTone.Neutral)
            JobStatusBanner("On the way", "Ravi is heading to you", JobBannerTone.Progress)
            JobStatusBanner("Start code needed", "Share the code when Ravi arrives", JobBannerTone.Warning)
            JobStepper(
                steps = listOf(
                    JobStepperItem("Requested", JobStepState.Done),
                    JobStepperItem("Accepted", JobStepState.Current),
                    JobStepperItem("Started", JobStepState.Upcoming),
                ),
                stepOfDescription = "Step 2 of 3",
            )
            OtpBoxes(value = "12", onValueChange = {}, autoFocus = false)
        }
    }

    @Test fun profileBits() = snapshot("profile_bits") {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricTrio(MetricSpec("4.8", "Rating"), MetricSpec("126", "Jobs"), MetricSpec("2y", "On AskIT"))
            ChipRow(chips = listOf("Plumbing", "Electrical", "Tamil", "English"))
            AskITSectionTabs(
                tabs = listOf(SectionTab("about", "About"), SectionTab("services", "Services"), SectionTab("reviews", "Reviews")),
                selectedId = "services",
                onSelect = {},
            )
        }
    }

    @Test fun storyProgress() = snapshot("story_progress") {
        Box(
            Modifier
                .fillMaxWidth()
                .background(Color.Black)
                .padding(12.dp),
        ) {
            StoryProgressRow(segmentCount = 4, activeSegmentIndex = 1, activeSegmentFraction = { 0.5f })
        }
    }

    private fun snapshot(name: String, content: @Composable () -> Unit) {
        for ((qualifiers, suffix) in listOf("+notnight" to "light", "+night" to "dark")) {
            RuntimeEnvironment.setQualifiers(qualifiers)
            captureRoboImage(filePath = "src/test/screenshots/${name}_$suffix.png") {
                AskITTheme {
                    Surface(color = MaterialTheme.colorScheme.background) {
                        Box(Modifier.padding(16.dp)) { content() }
                    }
                }
            }
        }
    }
}
