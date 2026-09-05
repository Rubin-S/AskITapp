package com.askit.app.creatordashboard

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import com.askit.app.session.ServiceListing
import com.askit.app.session.SessionProfile
import com.askit.app.session.SessionProfileStore
import com.askit.designsystem.theme.AskITTheme
import com.github.takahirom.roborazzi.RoborazziOptions
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
class CreatorDashboardScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun creator_dashboard_light_360() {
        capture(
            darkTheme = false,
            filePath = "build/outputs/roborazzi/creator_dashboard_light_360.png",
        )
    }

    @Test
    fun creator_dashboard_dark_360() {
        capture(
            darkTheme = true,
            filePath = "build/outputs/roborazzi/creator_dashboard_dark_360.png",
        )
    }

    @Test
    @Config(qualifiers = "ta-rIN-w360dp-h800dp-normal-long-notround-any-xxxhdpi-keyshidden-nonav")
    fun creator_dashboard_tamil_360() {
        capture(
            darkTheme = false,
            filePath = "build/outputs/roborazzi/creator_dashboard_tamil_360.png",
        )
    }

    private fun capture(darkTheme: Boolean, filePath: String) {
        val profileStore = SessionProfileStore(
            initial = SessionProfile(
                displayName = "Rajesh Electrical",
                username = "rajeshelectrical",
                hasListedService = true,
                listing = ServiceListing(
                    title = "Electrician Pro",
                    category = "Electrical",
                    description = "Home electrical services",
                    quoteLabel = "₹500",
                    coverage = "Coimbatore",
                    coverageHint = "",
                    hours = "9 AM - 6 PM",
                    hoursHint = "",
                    response = "Under 1 hr",
                    responseHint = "",
                    tags = listOf("Wiring", "AC"),
                    experience = "5 yrs",
                ),
            ),
        )
        val viewModel = CreatorDashboardViewModel(profileStore)

        composeTestRule.setContent {
            AskITTheme(darkTheme = darkTheme) {
                CreatorDashboardRoute(
                    viewModel = viewModel,
                    onBack = {},
                    onCreatePost = {},
                    onOpenChat = {},
                    onOpenPostDetail = {},
                    onOpenProfile = {},
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().captureRoboImage(
            filePath = filePath,
            roborazziOptions = RoborazziOptions(
                recordOptions = RoborazziOptions.RecordOptions(
                    resizeScale = 1.0,
                ),
            ),
        )
    }
}
