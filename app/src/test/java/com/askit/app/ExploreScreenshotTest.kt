package com.askit.app

import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.lifecycle.SavedStateHandle
import com.askit.app.explore.ExplorePersonResult
import com.askit.app.explore.ExploreViewModel
import com.askit.app.explore.ExploreTaskResult
import com.askit.app.explore.PersonMatchReason
import com.askit.designsystem.tasks.TaskResultStatus
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
    fun explore_typed_suggestions_light() {
        setApp(darkTheme = false, typedQuery = true)
        openExplore()
        composeTestRule.onNodeWithTag("explore_search_field").performClick()
        composeTestRule.onNodeWithTag("explore_location_summary").performTouchInput { click() }
        capture("explore_typed_suggestions_light")
    }

    @Test
    fun explore_typed_suggestions_dark() {
        setApp(darkTheme = true, typedQuery = true)
        openExplore()
        composeTestRule.onNodeWithTag("explore_search_field").performClick()
        composeTestRule.onNodeWithTag("explore_location_summary").performTouchInput { click() }
        capture("explore_typed_suggestions_dark")
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

    @Test
    fun explore_submitted_results_all_light() {
        setApp(darkTheme = false, submittedResults = true)
        openExplore()
        capture("explore_submitted_results_all_light")
    }

    @Test
    fun explore_submitted_results_all_dark() {
        setApp(darkTheme = true, submittedResults = true)
        openExplore()
        capture("explore_submitted_results_all_dark")
    }

    private fun setApp(
        darkTheme: Boolean,
        withHistory: Boolean = false,
        typedQuery: Boolean = false,
        submittedResults: Boolean = false,
    ) {
        val viewModel = ExploreViewModel(SavedStateHandle()).also {
            if (withHistory) {
                it.submitQuery("Home tutor")
                it.submitQuery("Laptop repair")
                it.submitQuery("Electrician")
                it.onQueryCleared()
            }
            if (typedQuery) {
                it.submitQuery("Electrician repair")
                it.submitQuery("Other recent")
                it.onQueryChanged("elec")
            }
            if (submittedResults) {
                it.submitQuery("electrician")
            }
        }
        composeTestRule.setContent {
            AskITTheme(darkTheme = darkTheme) {
                AskITApp(
                    exploreViewModel = viewModel,
                    submittedPeople = if (submittedResults) submittedPeople() else emptyList(),
                    submittedTasks = if (submittedResults) submittedTasks() else emptyList(),
                    onPersonClick = {},
                    onTaskClick = {},
                )
            }
        }
    }

    private fun submittedPeople() = listOf(
        ExplorePersonResult(
            id = "identity-person",
            name = "Arun Kumar",
            avatarUrl = null,
            primaryService = null,
            additionalServices = emptyList(),
            rating = 4.8,
            reviewCount = 36,
            locationLabel = "Kallakurichi",
            priceLabel = "From â‚¹500",
            statusLabel = "Available today",
            matchReasons = setOf(PersonMatchReason.Identity),
        ),
        ExplorePersonResult(
            id = "service-person",
            name = "Ravi Kumar",
            avatarUrl = null,
            primaryService = "Electrician",
            additionalServices = listOf("Fan installation", "Wiring"),
            rating = 4.8,
            reviewCount = 36,
            locationLabel = "2.4 km",
            priceLabel = "From â‚¹500",
            statusLabel = "Available today",
            matchReasons = setOf(PersonMatchReason.Service),
        ),
        ExplorePersonResult(
            id = "both-person",
            name = "Both Match",
            avatarUrl = null,
            primaryService = "Plumber",
            additionalServices = emptyList(),
            rating = null,
            reviewCount = 0,
            locationLabel = "Kallakurichi",
            priceLabel = null,
            statusLabel = null,
            matchReasons = setOf(PersonMatchReason.Identity, PersonMatchReason.Service),
        ),
    )

    private fun submittedTasks() = listOf(
        ExploreTaskResult(
            id = "task-1",
            title = "Repair damaged switchboard",
            category = "Electrical work",
            summary = "Replace a damaged switchboard safely.",
            budgetLabel = "₹800–₹1,500",
            locationLabel = "Kallakurichi",
            timingLabel = "Needed Monday",
            posterName = "Meena S.",
            postedLabel = "Posted today",
            status = TaskResultStatus.Open,
        ),
    )

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
