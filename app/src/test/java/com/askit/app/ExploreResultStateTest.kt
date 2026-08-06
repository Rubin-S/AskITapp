package com.askit.app

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import com.askit.app.explore.ExploreFilterOption
import com.askit.app.explore.ExploreLocationSource
import com.askit.app.explore.ExplorePersonResult
import com.askit.app.explore.ExploreResultScope
import com.askit.app.explore.ExploreResultState
import com.askit.app.explore.ExploreScreen
import com.askit.app.explore.ExploreSearchArea
import com.askit.app.explore.ExploreTaskResult
import com.askit.app.explore.ExploreViewModel
import com.askit.app.explore.PersonMatchReason
import com.askit.app.explore.defaultExploreFilterOptions
import com.askit.app.explore.normalizeExploreResultState
import com.askit.app.explore.normalizeExploreContentStatus
import com.askit.designsystem.tasks.TaskResultStatus
import com.askit.designsystem.theme.AskITTheme
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w360dp-h800dp-normal-long-notround-any-xxxhdpi-keyshidden-nonav")
class ExploreResultStateTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun initialLoading_usesLocalizedProgressSemantics() {
        setExplore(ExploreResultState.Loading)

        composeTestRule.onNodeWithText("Loading results…").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Loading results").assertIsDisplayed()
        composeTestRule.onNodeWithTag("explore_result_professionals_loading").assertIsDisplayed()
        composeTestRule.onNodeWithTag("explore_result_tasks_loading").assertIsDisplayed()
        composeTestRule.onAllNodesWithTag("explore_result_loading_indicator").assertCountEquals(0)
        composeTestRule.onNodeWithTag("explore_result_tabs").assertIsDisplayed()
    }

    @Test
    fun freshResults_renderAllPeopleServicesAndTasksWithoutReplacingExistingRows() {
        setExplore(freshResults())

        composeTestRule.onNodeWithText("People and services").assertIsDisplayed()
        composeTestRule.onNodeWithText("Identity Person").assertIsDisplayed()
        composeTestRule.onNodeWithText("Electrician").assertIsDisplayed()
        composeTestRule.onNodeWithText("Repair switchboard").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun freshResults_respectPeopleServicesAndTasksScopes() {
        setExplore(freshResults())

        composeTestRule.onNodeWithText("People").performClick()
        composeTestRule.onNodeWithText("Identity Person").assertIsDisplayed()
        composeTestRule.onAllNodesWithText("Service Person").assertCountEquals(0)

        composeTestRule.onNodeWithText("Services").performClick()
        composeTestRule.onNodeWithText("Electrician").assertIsDisplayed()
        composeTestRule.onNodeWithText("Service Person").assertIsDisplayed()
        composeTestRule.onAllNodesWithText("Identity Person").assertCountEquals(0)

        composeTestRule.onAllNodesWithText("Tasks")[0].performClick()
        composeTestRule.onNodeWithText("Repair switchboard").assertIsDisplayed()
        composeTestRule.onAllNodesWithText("Service Person").assertCountEquals(0)
    }

    @Test
    fun emptyQuery_showsAuthoritativeCopyWithoutAnAction() {
        setExplore(ExploreResultState.Empty(ExploreResultState.EmptyReason.Query))
        composeTestRule.onNodeWithText("No results found").assertIsDisplayed()
        composeTestRule
            .onNodeWithText("Try a different search or increase the search area.")
            .assertIsDisplayed()
        composeTestRule.onAllNodesWithText("Edit filters").assertCountEquals(0)
    }

    @Test
    fun emptyFiltered_showsEditFiltersWithoutClearingControlledFilters() {
        setExplore(
            state = ExploreResultState.Empty(ExploreResultState.EmptyReason.Filters),
            appliedFilterOptions = mapOf(
                ExploreResultScope.Services to setOf(ExploreFilterOption.Remote),
            ),
        )
        composeTestRule.onNodeWithText("No results match these filters").assertIsDisplayed()
        composeTestRule.onNodeWithText("Remove one or more filters and try again.").assertIsDisplayed()
        composeTestRule.onNodeWithText("Edit filters").assertHasClickAction()
    }

    @Test
    fun editFilters_usesExistingNestedFiltersDestination() {
        val viewModel = ExploreViewModel(SavedStateHandle()).also {
            it.submitQuery("electrician")
        }
        composeTestRule.setContent {
            AskITTheme {
                AskITApp(
                    exploreViewModel = viewModel,
                    resultState = ExploreResultState.Empty(ExploreResultState.EmptyReason.Filters),
                    availableFilterOptions = defaultExploreFilterOptions(),
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Explore").performClick()
        composeTestRule.onNodeWithText("Edit filters").performScrollTo().performClick()

        composeTestRule.onNodeWithText("Filters").assertIsDisplayed()
        composeTestRule.onAllNodesWithText("Edit filters").assertCountEquals(0)
    }

    @Test
    fun generalBlockingFailure_rendersRequiredCopyWithoutDeadRetry() {
        setExplore(ExploreResultState.Failure(ExploreResultState.FailureReason.General))
        composeTestRule.onNodeWithText("Couldn’t load results").assertIsDisplayed()
        composeTestRule.onNodeWithText("Something went wrong. Try again.").assertIsDisplayed()
        composeTestRule.onNodeWithTag("explore_result_failure").assertIsDisplayed()
        composeTestRule.onAllNodesWithText("Retry").assertCountEquals(0)
    }

    @Test
    fun offlineWithoutCache_rendersBlockingOfflineCopyWithoutDeadRetry() {
        setExplore(ExploreResultState.Failure(ExploreResultState.FailureReason.Offline))
        composeTestRule.onNodeWithText("You’re offline").assertIsDisplayed()
        composeTestRule.onNodeWithText("Connect to the internet and try again.").assertIsDisplayed()
        composeTestRule.onNodeWithTag("explore_result_failure").assertIsDisplayed()
        composeTestRule.onAllNodesWithText("Retry").assertCountEquals(0)
    }

    @Test
    fun generalRetry_emitsOnceAndControlledCallerTransitionsToLoading() {
        var retryCount = 0
        val state = mutableStateOf<ExploreResultState>(
            ExploreResultState.Failure(ExploreResultState.FailureReason.General),
        )
        composeTestRule.setContent {
            AskITTheme {
                TestExploreSurface(
                    state = state.value,
                    onRetryResults = {
                        retryCount++
                        state.value = ExploreResultState.Loading
                    },
                )
            }
        }

        composeTestRule.onNodeWithText("Retry").performClick()

        assertEquals(1, retryCount)
        composeTestRule.onNodeWithContentDescription("Loading results").assertIsDisplayed()
    }

    @Test
    fun staleStatus_keepsRowsVisibleAndRefreshEmitsOnce() {
        var retryCount = 0
        setExplore(
            freshResults(status = ExploreResultState.ContentStatus.Stale),
            onRetryResults = { retryCount++ },
        )
        composeTestRule.onNodeWithText("Results may be out of date.").assertIsDisplayed()
        composeTestRule.onNodeWithText("Identity Person").assertIsDisplayed()
        composeTestRule.onNodeWithText("Refresh").performClick()
        assertEquals(1, retryCount)
    }

    @Test
    fun offlineCachedStatus_keepsVerifiedRowsVisibleWithoutDeadRetry() {
        setExplore(
            freshResults(status = ExploreResultState.ContentStatus.OfflineCached),
        )
        composeTestRule
            .onNodeWithText("You’re offline. Showing previously loaded results.")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Identity Person").assertIsDisplayed()
        composeTestRule.onAllNodesWithText("Retry").assertCountEquals(0)
    }

    @Test
    fun refreshing_retainsRowsAnnouncesProgressAndPreventsDuplicateRetry() {
        var retryCount = 0
        val state = mutableStateOf<ExploreResultState>(
            freshResults(status = ExploreResultState.ContentStatus.Stale),
        )
        composeTestRule.setContent {
            AskITTheme {
                TestExploreSurface(
                    state = state.value,
                    onRetryResults = {
                        retryCount++
                        state.value = freshResults(
                            isRefreshing = true,
                            status = ExploreResultState.ContentStatus.Stale,
                        )
                    },
                )
            }
        }

        composeTestRule.onNodeWithText("Refresh").performClick()

        assertEquals(1, retryCount)
        composeTestRule.onNodeWithContentDescription("Refreshing results").assertIsDisplayed()
        composeTestRule.onNodeWithText("Identity Person").assertIsDisplayed()
        composeTestRule.onAllNodesWithText("Refresh").assertCountEquals(0)
        composeTestRule.onAllNodesWithText("Retry").assertCountEquals(0)
    }

    @Test
    fun tasksPartialFailure_isInlineForAllOmittedForPeopleAndServicesAndBlockingForTasks() {
        setExplore(
            freshResults(
                status = ExploreResultState.ContentStatus.PartialFailure(
                    ExploreResultState.Source.Tasks,
                ),
            ),
            onRetryResults = {},
        )

        composeTestRule
            .onNodeWithText("Tasks couldn’t be loaded. Showing people and services.")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Identity Person").assertIsDisplayed()
        composeTestRule.onAllNodesWithText("Repair switchboard").assertCountEquals(0)
        composeTestRule.onAllNodesWithTag("explore_result_status").assertCountEquals(1)

        composeTestRule.onNodeWithText("People").performClick()
        composeTestRule.onAllNodesWithTag("explore_result_status").assertCountEquals(0)
        composeTestRule.onNodeWithText("Identity Person").assertIsDisplayed()

        composeTestRule.onNodeWithText("Services").performClick()
        composeTestRule.onAllNodesWithTag("explore_result_status").assertCountEquals(0)
        composeTestRule.onNodeWithText("Electrician").assertIsDisplayed()

        composeTestRule.onAllNodesWithText("Tasks")[0].performClick()
        composeTestRule.onNodeWithText("Tasks couldn’t be loaded").assertIsDisplayed()
        composeTestRule.onNodeWithText("Retry").assertHasClickAction()
    }

    @Test
    fun peopleServicesPartialFailure_isInlineForAllOmittedForTasksAndBlockingForPeopleScopes() {
        setExplore(
            freshResults(
                status = ExploreResultState.ContentStatus.PartialFailure(
                    ExploreResultState.Source.PeopleAndServices,
                ),
            ),
            onRetryResults = {},
        )

        composeTestRule
            .onNodeWithText("People and services couldn’t be loaded. Showing tasks.")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Repair switchboard").assertIsDisplayed()
        composeTestRule.onAllNodesWithText("Identity Person").assertCountEquals(0)

        composeTestRule.onAllNodesWithText("Tasks")[0].performClick()
        composeTestRule.onAllNodesWithTag("explore_result_status").assertCountEquals(0)
        composeTestRule.onNodeWithText("Repair switchboard").assertIsDisplayed()

        composeTestRule.onNodeWithText("People").performClick()
        composeTestRule.onNodeWithText("People and services couldn’t be loaded").assertIsDisplayed()

        composeTestRule.onNodeWithText("Services").performClick()
        composeTestRule.onNodeWithText("People and services couldn’t be loaded").assertIsDisplayed()
        composeTestRule.onNodeWithText("Retry").assertHasClickAction()
    }

    @Test
    fun normalization_neverTurnsRequiredSourceFailureOrOfflineWithoutRowsIntoEmpty() {
        val tasksFailure = normalizeExploreResultState(
            state = ExploreResultState.Results(
                people = emptyList(),
                tasks = emptyList(),
                status = ExploreResultState.ContentStatus.PartialFailure(
                    ExploreResultState.Source.Tasks,
                ),
            ),
            scope = ExploreResultScope.All,
            emptyReason = ExploreResultState.EmptyReason.Query,
        )
        assertEquals(
            ExploreResultState.Failure(
                ExploreResultState.FailureReason.SourceUnavailable(ExploreResultState.Source.Tasks),
            ),
            tasksFailure,
        )

        val peopleFailure = normalizeExploreResultState(
            state = ExploreResultState.Results(
                people = emptyList(),
                tasks = emptyList(),
                status = ExploreResultState.ContentStatus.PartialFailure(
                    ExploreResultState.Source.PeopleAndServices,
                ),
            ),
            scope = ExploreResultScope.All,
            emptyReason = ExploreResultState.EmptyReason.Filters,
        )
        assertEquals(
            ExploreResultState.Failure(
                ExploreResultState.FailureReason.SourceUnavailable(
                    ExploreResultState.Source.PeopleAndServices,
                ),
            ),
            peopleFailure,
        )

        val offlineWithoutRows = normalizeExploreResultState(
            state = ExploreResultState.Results(
                people = emptyList(),
                tasks = taskResults(),
                status = ExploreResultState.ContentStatus.OfflineCached,
            ),
            scope = ExploreResultScope.People,
            emptyReason = ExploreResultState.EmptyReason.Query,
        )
        assertEquals(
            ExploreResultState.Failure(ExploreResultState.FailureReason.Offline),
            offlineWithoutRows,
        )
    }

    @Test
    fun normalization_selectsOnlyTheHighestPriorityNonBlockingStatus() {
        val partialFailure = ExploreResultState.ContentStatus.PartialFailure(
            ExploreResultState.Source.Tasks,
        )

        assertEquals(
            partialFailure,
            normalizeExploreContentStatus(
                listOf(
                    ExploreResultState.ContentStatus.Stale,
                    ExploreResultState.ContentStatus.OfflineCached,
                    partialFailure,
                ),
            ),
        )
    }

    @Test
    fun tamilLoadingStringsAndProgressSemantics_areLocalized() {
        setExplore(
            state = ExploreResultState.Loading,
            locale = Locale.forLanguageTag("ta"),
        )

        composeTestRule.onNodeWithText("முடிவுகள் ஏற்றப்படுகின்றன…").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("முடிவுகள் ஏற்றப்படுகின்றன").assertIsDisplayed()
    }

    @Test
    fun tamilFailureStringsAndAction_areLocalized() {
        setExplore(
            state = ExploreResultState.Failure(ExploreResultState.FailureReason.Offline),
            locale = Locale.forLanguageTag("ta"),
            onRetryResults = {},
        )
        composeTestRule.onNodeWithText("இணைய இணைப்பு இல்லை").assertIsDisplayed()
        composeTestRule.onNodeWithText("மீண்டும் முயற்சி").assertHasClickAction()
    }

    @Test
    fun largeText_keepsFilteredActionReachableAt320Dp() {
        var editFiltersCount = 0
        setExplore(
            state = ExploreResultState.Empty(ExploreResultState.EmptyReason.Filters),
            widthDp = 320,
            fontScale = 2f,
            onEditFilters = { editFiltersCount++ },
        )

        composeTestRule
            .onNodeWithText("Edit filters")
            .performScrollTo()
            .assertIsDisplayed()
            .performClick()
        assertEquals(1, editFiltersCount)
    }

    @Test
    fun resultState_doesNotAffectBrowseMode() {
        setExplore(
            state = ExploreResultState.Failure(ExploreResultState.FailureReason.General),
            query = "",
        )
        composeTestRule.onNodeWithText("Browse services").assertIsDisplayed()
        composeTestRule.onAllNodesWithTag("explore_result_failure").assertCountEquals(0)
    }

    @Test
    fun resultState_doesNotAffectTypedSearchAssistance() {
        setExplore(
            state = ExploreResultState.Failure(ExploreResultState.FailureReason.General),
            query = "elec",
        )
        composeTestRule.onNodeWithTag("explore_search_field").performClick()
        composeTestRule.onNodeWithTag("explore_typed_suggestions").assertIsDisplayed()
        composeTestRule.onAllNodesWithTag("explore_result_failure").assertCountEquals(0)
    }

    @Test
    fun viewModel_restoresOnlyRequestInputsAndNeverStoresResultState() {
        val savedStateHandle = SavedStateHandle()
        ExploreViewModel(savedStateHandle).apply {
            submitQuery("electrician")
            onFilterScopeSelected(ExploreResultScope.Services)
            onFiltersChanged(ExploreResultScope.Services, setOf(ExploreFilterOption.Remote))
        }

        val recreated = ExploreViewModel(savedStateHandle)

        assertEquals("electrician", recreated.uiState.value.query)
        assertEquals(ExploreResultScope.Services, recreated.filterScope.value)
        assertFalse(savedStateHandle.keys().any { key -> key.contains("result_state") })
        assertTrue(savedStateHandle.keys().all { key -> !key.contains("loading") && !key.contains("failure") })
    }

    private fun setExplore(
        state: ExploreResultState,
        query: String = "electrician",
        appliedFilterOptions: Map<ExploreResultScope, Set<ExploreFilterOption>> = emptyMap(),
        widthDp: Int = 360,
        fontScale: Float = 1f,
        locale: Locale = Locale.ENGLISH,
        onRetryResults: (() -> Unit)? = null,
        onEditFilters: () -> Unit = {},
    ) {
        composeTestRule.setContent {
            val configuration = Configuration(LocalConfiguration.current).apply {
                setLocale(locale)
            }
            val localizedContext = LocalContext.current.createConfigurationContext(configuration)
            CompositionLocalProvider(
                LocalContext provides localizedContext,
                LocalDensity provides Density(1f, fontScale),
            ) {
                AskITTheme {
                    Box(
                        modifier = Modifier
                            .width(widthDp.dp)
                            .height(900.dp)
                            .testTag("explore_test_surface"),
                    ) {
                        TestExploreSurface(
                            state = state,
                            query = query,
                            appliedFilterOptions = appliedFilterOptions,
                            onRetryResults = onRetryResults,
                            onEditFilters = onEditFilters,
                        )
                    }
                }
            }
        }
    }

    @androidx.compose.runtime.Composable
    private fun TestExploreSurface(
        state: ExploreResultState,
        query: String = "electrician",
        appliedFilterOptions: Map<ExploreResultScope, Set<ExploreFilterOption>> = emptyMap(),
        onRetryResults: (() -> Unit)? = null,
        onEditFilters: () -> Unit = {},
    ) {
        ExploreScreen(
            query = query,
            searchArea = testSearchArea(),
            recentSearches = listOf("Home tutor"),
            onQueryChanged = {},
            onQueryCleared = {},
            onQuerySubmitted = {},
            onRecentSearchRemoved = {},
            onRecentSearchesCleared = {},
            onSearchFiltersClick = onEditFilters,
            resultState = state,
            onRetryResults = onRetryResults,
            availableFilterOptions = defaultExploreFilterOptions(),
            appliedFilterOptions = appliedFilterOptions,
            onPersonClick = {},
            onTaskClick = {},
        )
    }

    private fun freshResults(
        isRefreshing: Boolean = false,
        status: ExploreResultState.ContentStatus? = null,
    ) = ExploreResultState.Results(
        people = personResults(),
        tasks = taskResults(),
        isRefreshing = isRefreshing,
        status = status,
    )

    private fun personResults() = listOf(
        personResult(
            id = "identity",
            name = "Identity Person",
            primaryService = null,
            matchReasons = setOf(PersonMatchReason.Identity),
        ),
        personResult(
            id = "service",
            name = "Service Person",
            primaryService = "Electrician",
            matchReasons = setOf(PersonMatchReason.Service),
        ),
        personResult(
            id = "both",
            name = "Both Person",
            primaryService = "Plumber",
            matchReasons = setOf(PersonMatchReason.Identity, PersonMatchReason.Service),
        ),
    )

    private fun personResult(
        id: String,
        name: String,
        primaryService: String?,
        matchReasons: Set<PersonMatchReason>,
    ) = ExplorePersonResult(
        id = id,
        name = name,
        avatarUrl = null,
        primaryService = primaryService,
        additionalServices = emptyList(),
        rating = 4.8,
        reviewCount = 12,
        locationLabel = "Kallakurichi",
        priceLabel = "From ₹500",
        statusLabel = "Available today",
        matchReasons = matchReasons,
    )

    private fun taskResults() = listOf(
        ExploreTaskResult(
            id = "task",
            title = "Repair switchboard",
            category = "Electrical work",
            summary = "Replace a damaged switchboard.",
            budgetLabel = "₹800–₹1,500",
            locationLabel = "Kallakurichi",
            timingLabel = "Needed Monday",
            posterName = "Meena S.",
            postedLabel = "Posted today",
            status = TaskResultStatus.Open,
        ),
    )

    private fun testSearchArea() = ExploreSearchArea(
        placeId = null,
        displayName = "Kallakurichi",
        supportingText = "Tamil Nadu",
        latitude = 11.7401,
        longitude = 78.9597,
        radiusKm = 10,
        source = ExploreLocationSource.SAVED,
    )
}
