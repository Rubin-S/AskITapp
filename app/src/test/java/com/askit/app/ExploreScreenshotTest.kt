package com.askit.app

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
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
    fun explore_compact_header_light() {
        setApp(darkTheme = false)
        openExplore()
        capture("explore_compact_header_light")
    }

    @Test
    fun explore_compact_header_dark() {
        setApp(darkTheme = true)
        openExplore()
        capture("explore_compact_header_dark")
    }

    @Test
    fun explore_search_active_light() {
        setApp(darkTheme = false, withHistory = true)
        openExplore()
        composeTestRule.onNodeWithTag("explore_search_field").performClick()
        capture("explore_search_active_light")
    }

    @Test
    fun explore_search_active_dark() {
        setApp(darkTheme = true, withHistory = true)
        openExplore()
        composeTestRule.onNodeWithTag("explore_search_field").performClick()
        capture("explore_search_active_dark")
    }

    @Test
    fun search_area_default_light() {
        setApp(darkTheme = false)
        openExplore()
        composeTestRule.onNodeWithTag("explore_filter_button").performClick()
        capture("search_area_default_light")
    }

    @Test
    fun search_area_default_dark() {
        setApp(darkTheme = true)
        openExplore()
        composeTestRule.onNodeWithTag("explore_filter_button").performClick()
        capture("search_area_default_dark")
    }

    private fun setApp(darkTheme: Boolean, withHistory: Boolean = false) {
        val viewModel = ExploreViewModel(SavedStateHandle()).also {
            if (withHistory) {
                it.submitQuery("Home tutor")
                it.submitQuery("Laptop repair")
                it.submitQuery("Electrician")
                it.onQueryCleared()
            }
        }
        composeTestRule.setContent {
            AskITTheme(darkTheme = darkTheme) {
                AskITApp(viewModel)
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
