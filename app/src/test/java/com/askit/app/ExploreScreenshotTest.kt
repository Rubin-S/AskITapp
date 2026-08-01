package com.askit.app

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.lifecycle.SavedStateHandle
import com.askit.app.explore.ExploreViewModel
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
class ExploreScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun explore_search_initial() {
        setApp(darkTheme = false)
        openExplore()
        capture("explore_search_initial")
    }

    @Test
    fun explore_location_row_light() {
        setApp(darkTheme = false)
        openExplore()
        capture("explore_location_row_light")
    }

    @Test
    fun explore_location_row_dark() {
        setApp(darkTheme = true)
        openExplore()
        capture("explore_location_row_dark")
    }

    @Test
    fun search_area_default_light() {
        setApp(darkTheme = false)
        openExplore()
        composeTestRule
            .onNodeWithContentDescription("Change search area. Near Kallakurichi. Within 10 km")
            .performClick()
        capture("search_area_default_light")
    }

    @Test
    fun search_area_default_dark() {
        setApp(darkTheme = true)
        openExplore()
        composeTestRule
            .onNodeWithContentDescription("Change search area. Near Kallakurichi. Within 10 km")
            .performClick()
        capture("search_area_default_dark")
    }

    private fun setApp(darkTheme: Boolean) {
        composeTestRule.setContent {
            AskITTheme(darkTheme = darkTheme) {
                AskITApp(ExploreViewModel(SavedStateHandle()))
            }
        }
    }

    private fun openExplore() {
        composeTestRule.onNodeWithContentDescription("Explore").performClick()
        composeTestRule.mainClock.advanceTimeBy(5_000)
        composeTestRule.waitForIdle()
    }

    private fun capture(name: String) {
        composeTestRule.onRoot().captureRoboImage(
            filePath = "src/test/screenshots/$name.png",
        )
    }
}
