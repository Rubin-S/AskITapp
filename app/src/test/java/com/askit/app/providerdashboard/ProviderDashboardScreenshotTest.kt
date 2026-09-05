package com.askit.app.providerdashboard

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
class ProviderDashboardScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun provider_dashboard_light_360() {
        capture(
            darkTheme = false,
            filePath = "build/outputs/roborazzi/provider_dashboard_light_360.png",
        )
    }

    @Test
    fun provider_dashboard_dark_360() {
        capture(
            darkTheme = true,
            filePath = "build/outputs/roborazzi/provider_dashboard_dark_360.png",
        )
    }

    @Test
    @Config(qualifiers = "ta-rIN-w360dp-h800dp-normal-long-notround-any-xxxhdpi-keyshidden-nonav")
    fun provider_dashboard_tamil_360() {
        capture(
            darkTheme = false,
            filePath = "build/outputs/roborazzi/provider_dashboard_tamil_360.png",
        )
    }

    private fun capture(darkTheme: Boolean, filePath: String) {
        val profileStore = SessionProfileStore(
            initial = SessionProfile(
                displayName = "Rajesh Electrical",
                username = "rajeshelectrical",
                hasListedService = true,
                listing = ServiceListing(
                    title = "Residential Electrical Services",
                    category = "Electrician Pro",
                    experience = "8 years",
                    description = "Specializing in home wiring and emergency repairs",
                    quoteLabel = "₹350 visit",
                    coverage = "Coimbatore",
                    coverageHint = "",
                    hours = "9 AM - 6 PM",
                    hoursHint = "",
                    response = "Under 1 hr",
                    responseHint = "",
                    tags = listOf("Wiring", "AC"),
                    live = true,
                ),
                profileStrengthPercent = 85,
            ),
        )
        val viewModel = ProviderDashboardViewModel(profileStore)

        composeTestRule.setContent {
            AskITTheme(darkTheme = darkTheme) {
                ProviderDashboardRoute(
                    viewModel = viewModel,
                    onBack = {},
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
