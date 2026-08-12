package com.askit.app

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
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
import com.askit.app.explore.ExploreBrowseState
import com.askit.app.explore.ExploreBrowseStatus
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
    fun explore_browse_empty_states_light() {
        setApp(
            darkTheme = false,
            browseState = ExploreBrowseState(
                services = ExploreBrowseStatus.Empty,
                professionals = ExploreBrowseStatus.Empty,
                tasks = ExploreBrowseStatus.Empty,
            ),
        )
        openExplore()
        capture("explore_browse_empty_states_light")
    }

    @Test
    fun explore_browse_offline_states_dark() {
        setApp(
            darkTheme = true,
            browseState = ExploreBrowseState(
                services = ExploreBrowseStatus.Offline,
                professionals = ExploreBrowseStatus.Offline,
                tasks = ExploreBrowseStatus.Offline,
            ),
        )
        openExplore()
        capture("explore_browse_offline_states_dark")
    }

    @Test
    fun explore_browse_server_states_light() {
        setApp(
            darkTheme = false,
            browseState = ExploreBrowseState(
                services = ExploreBrowseStatus.ServerUnavailable,
                professionals = ExploreBrowseStatus.ServerUnavailable,
                tasks = ExploreBrowseStatus.ServerUnavailable,
            ),
        )
        openExplore()
        capture("explore_browse_server_states_light")
    }

    @Test
    fun explore_browse_loading_states_dark() {
        setApp(
            darkTheme = true,
            browseState = ExploreBrowseState(
                services = ExploreBrowseStatus.Loading,
                professionals = ExploreBrowseStatus.Loading,
                tasks = ExploreBrowseStatus.Loading,
            ),
        )
        openExplore()
        capture("explore_browse_loading_states_dark")
    }

    @Test
    fun explore_search_active_light() {
        setApp(darkTheme = false, withHistory = true)
        openExplore()
        openSearch()
        capture("explore_search_active_light")
    }

    @Test
    fun explore_search_active_dark() {
        setApp(darkTheme = true, withHistory = true)
        openExplore()
        openSearch()
        capture("explore_search_active_dark")
    }

    @Test
    fun explore_typed_suggestions_light() {
        setApp(darkTheme = false, typedQuery = true)
        openExplore()
        openSearch()
        composeTestRule.onNodeWithTag("explore_location_summary").performTouchInput { click() }
        capture("explore_typed_suggestions_light")
    }

    @Test
    fun explore_typed_suggestions_dark() {
        setApp(darkTheme = true, typedQuery = true)
        openExplore()
        openSearch()
        composeTestRule.onNodeWithTag("explore_location_summary").performTouchInput { click() }
        capture("explore_typed_suggestions_dark")
    }

    @Test
    fun explore_filters_services_light() {
        setApp(darkTheme = false, submittedResults = true)
        openExplore()
        composeTestRule.onNodeWithText("Services").performClick()
        composeTestRule.onNodeWithTag("explore_search_filter_action").performClick()
        capture("explore_filters_services_light")
    }

    @Test
    fun explore_filters_services_dark() {
        setApp(darkTheme = true, submittedResults = true)
        openExplore()
        composeTestRule.onNodeWithText("Services").performClick()
        composeTestRule.onNodeWithTag("explore_search_filter_action").performClick()
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
    fun explore_result_loading_360_light() {
        setApp(
            darkTheme = false,
            resultState = ExploreResultState.Loading,
        )
        openExplore()
        capture("explore_result_loading_360_light")
    }

    @Test
    fun explore_result_offline_360_dark() {
        setApp(
            darkTheme = true,
            resultState = ExploreResultState.Failure(ExploreResultState.FailureReason.Offline),
        )
        openExplore()
        capture("explore_result_offline_360_dark")
    }

    @Test
    fun explore_result_server_360_light() {
        setApp(
            darkTheme = false,
            resultState = ExploreResultState.Failure(
                ExploreResultState.FailureReason.SourceUnavailable(
                    ExploreResultState.Source.Tasks,
                ),
            ),
        )
        openExplore()
        capture("explore_result_server_360_light")
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

    @Test
    fun explore_task_card_variants_light_360() {
        setApp(
            darkTheme = false,
            resultState = ExploreResultState.Results(
                people = emptyList(),
                tasks = taskCardVariants(),
            ),
        )
        openExplore()
        capture("explore_task_card_variants_light_360")
    }

    @Test
    fun explore_task_card_variants_dark_360() {
        setApp(
            darkTheme = true,
            resultState = ExploreResultState.Results(
                people = emptyList(),
                tasks = taskCardVariants(),
            ),
        )
        openExplore()
        capture("explore_task_card_variants_dark_360")
    }

    @Test
    @Config(qualifiers = "ta-rIN-w360dp-h800dp-normal-long-notround-any-xxxhdpi-keyshidden-nonav")
    fun explore_task_card_variants_tamil_360() {
        setApp(
            darkTheme = false,
            resultState = ExploreResultState.Results(
                people = emptyList(),
                tasks = tamilTaskCardVariants(),
            ),
        )
        openExplore("ஆராயுங்கள்")
        capture("explore_task_card_variants_tamil_360")
    }

    @Test
    @Config(qualifiers = "w320dp-h800dp-normal-long-notround-any-xxxhdpi-keyshidden-nonav")
    fun explore_task_card_variants_large_text_320() {
        setApp(
            darkTheme = false,
            resultState = ExploreResultState.Results(
                people = emptyList(),
                tasks = taskCardVariants(),
            ),
            fontScale = 1.5f,
        )
        openExplore()
        capture("explore_task_card_variants_large_text_320")
    }

    @Test
    @Config(qualifiers = "w800dp-h412dp-normal-long-notround-any-xxxhdpi-keyshidden-nonav")
    fun explore_task_card_variants_landscape_800() {
        setApp(
            darkTheme = false,
            resultState = ExploreResultState.Results(
                people = emptyList(),
                tasks = taskCardVariants(),
            ),
        )
        openExplore()
        capture("explore_task_card_variants_landscape_800")
    }

    private fun setApp(
        darkTheme: Boolean,
        withHistory: Boolean = false,
        typedQuery: Boolean = false,
        submittedResults: Boolean = false,
        sortMenu: Boolean = false,
        resultState: ExploreResultState? = null,
        browseState: ExploreBrowseState = ExploreBrowseState(),
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
                        browseState = browseState,
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

    private fun taskCardVariants() = listOf(
        ExploreTaskResult(
            id = "photo-task",
            title = "Repair leaking kitchen tap",
            category = "Plumber",
            summary = "Water is leaking around the base of the tap.",
            budgetLabel = "₹800–₹1,500",
            locationLabel = "Anna Nagar",
            timingLabel = "Needed Monday",
            posterName = "Arun P.",
            postedLabel = "Posted 12m ago",
            status = TaskResultStatus.Open,
            photoModels = listOf(samplePhoto()),
            distanceLabel = "2.4 km",
            scopeHighlights = listOf("Kitchen sink", "Provider brings materials"),
        ),
        ExploreTaskResult(
            id = "multi-photo-task",
            title = "Furniture assembly",
            category = "Furniture assembly",
            summary = "Assemble a new flat-pack wardrobe in the bedroom.",
            budgetLabel = "",
            locationLabel = "Anna Nagar",
            timingLabel = "Flexible",
            posterName = "Meena S.",
            postedLabel = "Posted 2h ago",
            status = TaskResultStatus.Open,
            photoModels = listOf(
                samplePhoto(),
                samplePhoto(),
            ),
            distanceLabel = "4.1 km",
            scopeHighlights = listOf("120 × 60 cm · IKEA PAX"),
        ),
        ExploreTaskResult(
            id = "text-only-task",
            title = "Translate a two-page document",
            category = "Translation",
            summary = "Translate the supplied English document into Tamil.",
            budgetLabel = "Request quotes",
            locationLabel = "Remote",
            timingLabel = "Flexible",
            posterName = "Suresh K.",
            postedLabel = "Posted yesterday",
            status = TaskResultStatus.Applied,
        ),
    )

    private fun tamilTaskCardVariants() = listOf(
        ExploreTaskResult(
            id = "tamil-photo-task",
            title = "சமையலறை குழாயில் கசிவை சரிசெய்யவும்",
            category = "பிளம்பர்",
            summary = "குழாயின் அடிப்பகுதியில் தண்ணீர் கசிகிறது.",
            budgetLabel = "₹800–₹1,500",
            locationLabel = "அண்ணா நகர்",
            timingLabel = "திங்கட்கிழமை தேவை",
            posterName = "அருண் பி.",
            postedLabel = "12 நிமிடங்களுக்கு முன்",
            status = TaskResultStatus.Open,
            photoModels = listOf(samplePhoto()),
            distanceLabel = "2.4 கி.மீ.",
            scopeHighlights = listOf("சமையலறை தொட்டி"),
        ),
        ExploreTaskResult(
            id = "tamil-text-only-task",
            title = "இரண்டு பக்க ஆவணத்தை மொழிபெயர்க்கவும்",
            category = "மொழிபெயர்ப்பு",
            summary = "வழங்கப்பட்ட ஆங்கில ஆவணத்தைத் தமிழில் மொழிபெயர்க்கவும்.",
            budgetLabel = "மேற்கோள்களைக் கோரு",
            locationLabel = "தொலைநிலை",
            timingLabel = "நெகிழ்வானது",
            posterName = "சுரேஷ் கே.",
            postedLabel = "நேற்று பதிவிடப்பட்டது",
            status = TaskResultStatus.Open,
        ),
    )

    private fun openExplore(contentDescription: String = "Explore") {
        composeTestRule.onNodeWithContentDescription(contentDescription).performClick()
        composeTestRule.mainClock.advanceTimeBy(5_000)
        composeTestRule.waitForIdle()
    }

    private fun samplePhoto(): Bitmap = Bitmap.createBitmap(240, 160, Bitmap.Config.ARGB_8888).also {
        Canvas(it).drawColor(Color.rgb(51, 112, 148))
        Canvas(it).drawRect(
            30f,
            24f,
            210f,
            136f,
            Paint().apply { color = Color.rgb(237, 176, 82) },
        )
    }

    private fun openSearch() {
        composeTestRule.onNodeWithTag("explore_search_field").performClick()
        composeTestRule.mainClock.advanceTimeBy(1_000)
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
