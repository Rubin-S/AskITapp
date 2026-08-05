package com.askit.app

import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.lifecycle.SavedStateHandle
import com.askit.app.explore.ExploreFilterOption
import com.askit.app.explore.ExplorePersonResult
import com.askit.app.explore.ExploreResultState
import com.askit.app.explore.ExploreResultScope
import com.askit.app.explore.ExploreSortOption
import com.askit.app.explore.ExploreViewModel
import com.askit.app.explore.ExploreTaskResult
import com.askit.app.explore.PersonMatchReason
import com.askit.app.explore.defaultExploreFilterOptions
import com.askit.designsystem.tasks.TaskResultStatus
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
    fun explore_filters_services_light() {
        setApp(darkTheme = false, submittedResults = true)
        openExplore()
        composeTestRule.onNodeWithText("Services").performClick()
        composeTestRule.onNodeWithTag("explore_filter_button").performClick()
        capture("explore_filters_services_light")
    }

    @Test
    fun explore_filters_services_dark() {
        setApp(darkTheme = true, submittedResults = true)
        openExplore()
        composeTestRule.onNodeWithText("Services").performClick()
        composeTestRule.onNodeWithTag("explore_filter_button").performClick()
        capture("explore_filters_services_dark")
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

    @Test
    fun explore_sort_services_menu_light() {
        setApp(darkTheme = false, submittedResults = true, sortMenu = true)
        openExplore()
        composeTestRule.onNodeWithText("Services").performClick()
        composeTestRule.onNodeWithTag("explore_sort_control").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Nearest").assertIsDisplayed()
        capture("explore_sort_services_menu_light", includePopup = true)
    }

    @Test
    fun explore_sort_services_menu_dark() {
        setApp(darkTheme = true, submittedResults = true, sortMenu = true)
        openExplore()
        composeTestRule.onNodeWithText("Services").performClick()
        composeTestRule.onNodeWithTag("explore_sort_control").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Nearest").assertIsDisplayed()
        capture("explore_sort_services_menu_dark", includePopup = true)
    }

    @Test
    @Config(qualifiers = "w320dp-h800dp-normal-long-notround-any-xxxhdpi-keyshidden-nonav")
    fun explore_result_failure_320_light() {
        setApp(
            darkTheme = false,
            resultState = ExploreResultState.Failure(ExploreResultState.FailureReason.General),
        )
        openExplore()
        capture("explore_result_failure_320_light")
    }

    @Test
    fun explore_result_empty_360_dark() {
        setApp(
            darkTheme = true,
            resultState = ExploreResultState.Empty(ExploreResultState.EmptyReason.Filters),
            appliedFilters = mapOf(
                ExploreResultScope.Services to setOf(ExploreFilterOption.Remote),
            ),
        )
        openExplore()
        composeTestRule.onNodeWithText("Services").performClick()
        capture("explore_result_empty_360_dark")
    }

    @Test
    @Config(qualifiers = "w412dp-h915dp-normal-long-notround-any-xxxhdpi-keyshidden-nonav")
    fun explore_result_stale_412_light() {
        setApp(
            darkTheme = false,
            resultState = ExploreResultState.Results(
                people = submittedPeople(),
                tasks = submittedTasks(),
                status = ExploreResultState.ContentStatus.Stale,
            ),
        )
        openExplore()
        capture("explore_result_stale_412_light")
    }

    @Test
    @Config(qualifiers = "ta-rIN-w360dp-h800dp-normal-long-notround-any-xxxhdpi-keyshidden-nonav")
    fun explore_result_partial_tamil_360_dark() {
        setApp(
            darkTheme = true,
            resultState = ExploreResultState.Results(
                people = submittedPeople(),
                tasks = submittedTasks(),
                status = ExploreResultState.ContentStatus.PartialFailure(
                    ExploreResultState.Source.Tasks,
                ),
            ),
        )
        openExplore("ஆராயுங்கள்")
        capture("explore_result_partial_tamil_360_dark")
    }

    @Test
    @Config(qualifiers = "w320dp-h800dp-normal-long-notround-any-xxxhdpi-keyshidden-nonav")
    fun explore_result_refreshing_large_text_320_light() {
        setApp(
            darkTheme = false,
            fontScale = 1.6f,
            resultState = ExploreResultState.Results(
                people = submittedPeople(),
                tasks = submittedTasks(),
                isRefreshing = true,
                status = ExploreResultState.ContentStatus.OfflineCached,
            ),
        )
        openExplore()
        capture("explore_result_refreshing_large_text_320_light")
    }

    @Test
    @Config(qualifiers = "w800dp-h412dp-normal-long-notround-any-xxxhdpi-keyshidden-nonav")
    fun explore_submitted_results_landscape_800_light() {
        setApp(darkTheme = false, submittedResults = true)
        openExplore()
        capture("explore_submitted_results_landscape_800_light")
    }

    @Test
    @Config(qualifiers = "w320dp-h412dp-normal-long-notround-any-xxxhdpi-keyshidden-nonav")
    fun explore_submitted_results_constrained_320_light() {
        setApp(darkTheme = false, submittedResults = true)
        openExplore()
        capture("explore_submitted_results_constrained_320_light")
    }

    @Test
    @Config(qualifiers = "w320dp-h800dp-normal-long-notround-any-xxxhdpi-keyshidden-nonav")
    fun explore_result_font_scale_130_320_light() {
        setApp(darkTheme = false, submittedResults = true, fontScale = 1.3f)
        openExplore()
        capture("explore_result_font_scale_130_320_light")
    }

    @Test
    @Config(qualifiers = "w320dp-h800dp-normal-long-notround-any-xxxhdpi-keyshidden-nonav")
    fun explore_result_font_scale_150_320_light() {
        setApp(darkTheme = false, submittedResults = true, fontScale = 1.5f)
        openExplore()
        capture("explore_result_font_scale_150_320_light")
    }

    @Test
    @Config(qualifiers = "w320dp-h800dp-normal-long-notround-any-xxxhdpi-keyshidden-nonav")
    fun explore_result_font_scale_200_320_light() {
        setApp(darkTheme = false, submittedResults = true, fontScale = 2f)
        openExplore()
        capture("explore_result_font_scale_200_320_light")
    }

    private fun setApp(
        darkTheme: Boolean,
        withHistory: Boolean = false,
        typedQuery: Boolean = false,
        submittedResults: Boolean = false,
        sortMenu: Boolean = false,
        resultState: ExploreResultState? = null,
        appliedFilters: Map<ExploreResultScope, Set<ExploreFilterOption>>? = null,
        fontScale: Float = 1f,
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
            if (submittedResults || resultState != null) {
                it.submitQuery("electrician")
            }
        }
        val sortOptions = if (sortMenu) {
            mapOf(
                ExploreResultScope.Services to listOf(
                    ExploreSortOption.BestMatch,
                    ExploreSortOption.Nearest,
                    ExploreSortOption.RatingHighToLow,
                ),
            )
        } else {
            emptyMap()
        }
        val selectedSortOptions = if (sortMenu) {
            mapOf(ExploreResultScope.Services to ExploreSortOption.BestMatch)
        } else {
            emptyMap()
        }
        val onSortChanged: ((ExploreResultScope, ExploreSortOption) -> Unit)? =
            if (sortMenu) ({ _, _ -> }) else null
        val availableFilterOptions = if (submittedResults || resultState != null) {
            defaultExploreFilterOptions()
        } else {
            emptyMap()
        }
        val appliedFilterOptions = appliedFilters ?: if (sortMenu) {
            mapOf(
                ExploreResultScope.Services to setOf(
                    ExploreFilterOption.RatingFourPlus,
                    ExploreFilterOption.Remote,
                ),
            )
        } else {
            emptyMap()
        }
        composeTestRule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(density.density, fontScale),
            ) {
                AskITTheme(darkTheme = darkTheme) {
                    AskITApp(
                        exploreViewModel = viewModel,
                        resultState = resultState ?: if (submittedResults) {
                            ExploreResultState.Results(
                                people = submittedPeople(),
                                tasks = submittedTasks(),
                            )
                        } else {
                            ExploreResultState.Loading
                        },
                        availableSortOptions = sortOptions,
                        selectedSortOptions = selectedSortOptions,
                        onSortChanged = onSortChanged,
                        availableFilterOptions = availableFilterOptions,
                        appliedFilterOptions = appliedFilterOptions,
                        onPersonClick = {},
                        onTaskClick = {},
                    )
                }
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
            priceLabel = "From ₹500",
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
            priceLabel = "From ₹500",
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

    private fun openExplore(contentDescription: String = "Explore") {
        composeTestRule.onNodeWithContentDescription(contentDescription).performClick()
        composeTestRule.mainClock.advanceTimeBy(5_000)
        composeTestRule.waitForIdle()
    }

    @OptIn(ExperimentalRoborazziApi::class)
    private fun capture(name: String, includePopup: Boolean = false) {
        if (includePopup) {
            composeTestRule.onNodeWithTag("explore_sort_menu").captureRoboImage(
                filePath = "src/test/screenshots/$name.png",
            )
        } else {
            composeTestRule.onRoot().captureRoboImage(
                filePath = "src/test/screenshots/$name.png",
            )
        }
    }
}
