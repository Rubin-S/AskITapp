package com.askit.app

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import com.askit.app.explore.ExploreFilterOption
import com.askit.app.explore.ExplorePersonResult
import com.askit.app.explore.ExploreResultState
import com.askit.app.explore.ExploreResultScope
import com.askit.app.explore.ExploreScreen
import com.askit.app.explore.ExploreSearchArea
import com.askit.app.explore.ExploreSortOption
import com.askit.app.explore.ExploreViewModel
import com.askit.app.explore.ExploreLocationSource
import com.askit.app.explore.PersonMatchReason
import com.askit.app.explore.SearchAreaScreen
import com.askit.designsystem.theme.AskITTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.util.Locale

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w360dp-h800dp-normal-long-notround-any-xxxhdpi-keyshidden-nonav")
class ExploreFiltersTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun filters_screenUsesScopeGroupsCallerOrderAndIgnoresUnsupportedOptions() {
        setFilterScreen(
            filterScope = ExploreResultScope.Services,
            available = listOf(
                ExploreFilterOption.AvailableThisWeek,
                ExploreFilterOption.RatingFourPlus,
                ExploreFilterOption.RatingFourPlus,
                ExploreFilterOption.TaskOpen,
                ExploreFilterOption.Remote,
            ),
            applied = setOf(
                ExploreFilterOption.RatingFourPlus,
                ExploreFilterOption.TaskOpen,
                ExploreFilterOption.NeededToday,
            ),
        )

        composeTestRule.onNodeWithText("Filters").assertIsDisplayed()
        composeTestRule.onNodeWithText("Filters for Services").assertIsDisplayed()
        composeTestRule.onNodeWithText("Rating").assertIsDisplayed()
        composeTestRule.onNodeWithText("Work location").assertIsDisplayed()
        composeTestRule.onNodeWithText("Availability").assertIsDisplayed()
        composeTestRule.onNodeWithText("4+ rating").assertIsSelected()
        composeTestRule.onNodeWithText("Remote").assertIsDisplayed()
        composeTestRule.onNodeWithText("Available this week").performScrollTo().assertIsDisplayed()
        composeTestRule.onAllNodesWithText("Open").assertCountEquals(0)
        composeTestRule.onAllNodesWithText("Needed today").assertCountEquals(0)

        val groupTops = listOf("Rating", "Work location", "Availability").map {
            composeTestRule.onNodeWithText(it).fetchSemanticsNode().boundsInRoot.top
        }
        assertTrue(groupTops.zipWithNext().all { (first, second) -> first < second })
    }

    @Test
    fun filters_draftClearAndApplyAreControlledAndBackDoesNotApply() {
        var applyCount = 0
        var appliedOptions = emptySet<ExploreFilterOption>()
        var backCount = 0
        setFilterScreen(
            filterScope = ExploreResultScope.Services,
            available = listOf(
                ExploreFilterOption.RatingFourPlus,
                ExploreFilterOption.Remote,
                ExploreFilterOption.AvailableToday,
            ),
            onBack = { backCount++ },
            onApply = { _, options ->
                applyCount++
                appliedOptions = options
            },
        )

        composeTestRule.onNodeWithTag("explore_clear_filters").assertIsNotEnabled()
        composeTestRule
            .onNodeWithTag("explore_filter_option_remote")
            .performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("explore_filter_option_remote").assertIsSelected()
        composeTestRule.onNodeWithTag("explore_clear_filters").assertIsEnabled()
        composeTestRule.onNodeWithTag("explore_clear_filters").performClick()
        composeTestRule.onNodeWithTag("explore_clear_filters").assertIsNotEnabled()

        composeTestRule.onNodeWithTag("explore_filter_option_ratingfourplus").performClick()
        composeTestRule.onNodeWithText("Apply").performClick()

        assertEquals(1, applyCount)
        assertEquals(setOf(ExploreFilterOption.RatingFourPlus), appliedOptions)
        assertEquals(0, backCount)
    }

    @Test
    fun filters_backDiscardsDraftWithoutApplying() {
        var applyCount = 0
        var backCount = 0
        setFilterScreen(
            filterScope = ExploreResultScope.Tasks,
            available = listOf(ExploreFilterOption.TaskOpen, ExploreFilterOption.Remote),
            onBack = { backCount++ },
            onApply = { _, _ -> applyCount++ },
        )

        composeTestRule.onNodeWithTag("explore_filter_option_taskopen").performScrollTo().performClick()
        composeTestRule.onNodeWithContentDescription("Back").performClick()

        assertEquals(1, backCount)
        assertEquals(0, applyCount)
    }

    @Test
    fun submittedResults_showScopeAwareAppliedChipsAndUseOneControlledRemovalCallback() {
        val applied = mutableStateOf(
            mapOf(
                ExploreResultScope.Services to setOf(
                    ExploreFilterOption.RatingFourPlus,
                    ExploreFilterOption.Remote,
                    ExploreFilterOption.TaskOpen,
                ),
            ),
        )
        var callbackCount = 0
        var callbackScope: ExploreResultScope? = null
        composeTestRule.setContent {
            AskITTheme {
                ExploreScreen(
                    query = "electrician",
                    searchArea = testSearchArea(),
                    recentSearches = emptyList(),
                    onQueryChanged = {},
                    onQueryCleared = {},
                    onQuerySubmitted = {},
                    onRecentSearchRemoved = {},
                    onRecentSearchesCleared = {},
                    onSearchFiltersClick = {},
                    resultState = ExploreResultState.Results(
                        people = listOf(
                            ExplorePersonResult(
                                id = "person",
                                name = "Ravi Kumar",
                                avatarUrl = null,
                                primaryService = "Electrician",
                                additionalServices = emptyList(),
                                rating = 4.8,
                                reviewCount = 12,
                                locationLabel = "Kallakurichi",
                                priceLabel = null,
                                statusLabel = null,
                                matchReasons = setOf(PersonMatchReason.Service),
                            ),
                        ),
                        tasks = emptyList(),
                    ),
                    availableSortOptions = mapOf(
                        ExploreResultScope.Services to listOf(
                            ExploreSortOption.BestMatch,
                            ExploreSortOption.Nearest,
                        ),
                    ),
                    selectedSortOptions = mapOf(
                        ExploreResultScope.Services to ExploreSortOption.BestMatch,
                    ),
                    onSortChanged = { _, _ -> },
                    availableFilterOptions = mapOf(
                        ExploreResultScope.Services to listOf(
                            ExploreFilterOption.RatingFourPlus,
                            ExploreFilterOption.Remote,
                            ExploreFilterOption.TaskOpen,
                        ),
                    ),
                    appliedFilterOptions = applied.value,
                    onFiltersChanged = { scope, options ->
                        callbackCount++
                        callbackScope = scope
                        applied.value = mapOf(scope to options)
                    },
                    onPersonClick = {},
                )
            }
        }

        composeTestRule.onAllNodesWithTag("explore_applied_filters").assertCountEquals(0)
        composeTestRule.onAllNodesWithTag("explore_sort_control").assertCountEquals(0)
        composeTestRule.onNodeWithText("Services").performClick()
        composeTestRule
            .onAllNodesWithTag("explore_search_filter_action")
            .assertCountEquals(1)
        composeTestRule
            .onAllNodesWithContentDescription("Search filters, 2 filters applied")
            .assertCountEquals(1)
        composeTestRule.onAllNodesWithText("2", useUnmergedTree = true).assertCountEquals(0)
        composeTestRule.onNodeWithText("4+ rating").assertIsDisplayed()
        composeTestRule.onNodeWithText("Remote").performClick()
        composeTestRule.waitForIdle()

        assertEquals(1, callbackCount)
        assertEquals(ExploreResultScope.Services, callbackScope)
        assertEquals(
            setOf(ExploreFilterOption.RatingFourPlus),
            applied.value[ExploreResultScope.Services],
        )
        composeTestRule.onNodeWithText("4+ rating").assertIsDisplayed()
        composeTestRule.onAllNodesWithText("Open").assertCountEquals(0)
        composeTestRule.onNodeWithTag("explore_sort_control").assertIsDisplayed()
    }

    @Test
    fun filters_draftRestoresAcrossSavedInstanceState() {
        val restorationTester = StateRestorationTester(composeTestRule)
        restorationTester.setContent {
            AskITTheme {
                SearchAreaScreen(
                    confirmedArea = testSearchArea(),
                    filterScope = ExploreResultScope.Tasks,
                    availableFilterOptions = mapOf(
                        ExploreResultScope.Tasks to listOf(ExploreFilterOption.TaskOpen),
                    ),
                    onBack = {},
                    onApply = { _, _ -> },
                )
            }
        }

        composeTestRule.onNodeWithText("Open").performClick()
        restorationTester.emulateSavedInstanceStateRestore()

        composeTestRule.onNodeWithTag("explore_filter_option_taskopen").assertIsSelected()
    }

    @Test
    fun exploreFilterButton_localizesToTamilAtANarrowWidth() {
        composeTestRule.setContent {
            val configuration = Configuration(LocalConfiguration.current).apply {
                setLocale(Locale.forLanguageTag("ta"))
            }
            val tamilContext = LocalContext.current.createConfigurationContext(configuration)
            CompositionLocalProvider(
                LocalContext provides tamilContext,
                androidx.compose.ui.platform.LocalDensity provides Density(1f, 1.3f),
            ) {
                AskITTheme {
                    Box(
                        modifier = androidx.compose.ui.Modifier
                            .width(320.dp)
                            .height(900.dp),
                    ) {
                        ExploreScreen(
                            query = "",
                            searchArea = testSearchArea(),
                            recentSearches = emptyList(),
                            onQueryChanged = {},
                            onQueryCleared = {},
                            onQuerySubmitted = {},
                            onRecentSearchRemoved = {},
                            onRecentSearchesCleared = {},
                            onSearchFiltersClick = {},
                        )
                    }
                }
            }
        }

        composeTestRule
            .onNodeWithContentDescription("தேடல் வடிகட்டிகள்")
            .assertIsDisplayed()
    }

    @Test
    fun filters_routeCommitsLocationAndOptionalFiltersThroughExistingNavigationEntry() {
        val viewModel = ExploreViewModel(SavedStateHandle()).also {
            it.submitQuery("electrician")
        }
        composeTestRule.setContent {
            AskITTheme {
                AskITApp(
                    exploreViewModel = viewModel,
                    availableFilterOptions = mapOf(
                        ExploreResultScope.Services to listOf(ExploreFilterOption.Remote),
                    ),
                    resultState = ExploreResultState.Results(
                        people = listOf(
                            ExplorePersonResult(
                                id = "service-person",
                                name = "Ravi Kumar",
                                avatarUrl = null,
                                primaryService = "Electrician",
                                additionalServices = emptyList(),
                                rating = 4.8,
                                reviewCount = 8,
                                locationLabel = "Kallakurichi",
                                priceLabel = null,
                                statusLabel = null,
                                matchReasons = setOf(PersonMatchReason.Service),
                            ),
                        ),
                        tasks = emptyList(),
                    ),
                    onPersonClick = {},
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Explore").performClick()
        composeTestRule.onNodeWithText("Services").performClick()
        composeTestRule.onNodeWithTag("explore_search_filter_action").performClick()
        composeTestRule.onNodeWithTag("explore_filter_option_remote").performClick()
        composeTestRule.onNodeWithText("Apply").performClick()
        composeTestRule.waitForIdle()

        assertEquals(
            setOf(ExploreFilterOption.Remote),
            viewModel.appliedFilterOptions.value[ExploreResultScope.Services],
        )
        composeTestRule.onNodeWithText("Remote").assertIsDisplayed()
    }

    @Test
    fun viewModel_rehydratesAppliedFiltersFromSavedStateHandle() {
        val savedStateHandle = SavedStateHandle()
        ExploreViewModel(savedStateHandle).onFiltersChanged(
            ExploreResultScope.Tasks,
            setOf(ExploreFilterOption.TaskOpen, ExploreFilterOption.NeededToday),
        )

        val recreatedViewModel = ExploreViewModel(savedStateHandle)

        assertEquals(
            setOf(ExploreFilterOption.TaskOpen, ExploreFilterOption.NeededToday),
            recreatedViewModel.appliedFilterOptions.value[ExploreResultScope.Tasks],
        )
    }

    private fun setFilterScreen(
        filterScope: ExploreResultScope,
        available: List<ExploreFilterOption>,
        applied: Set<ExploreFilterOption> = emptySet(),
        onBack: () -> Unit = {},
        onApply: (ExploreSearchArea, Set<ExploreFilterOption>) -> Unit = { _, _ -> },
    ) {
        composeTestRule.setContent {
            AskITTheme {
                SearchAreaScreen(
                    confirmedArea = testSearchArea(),
                    filterScope = filterScope,
                    availableFilterOptions = mapOf(filterScope to available),
                    appliedFilterOptions = mapOf(filterScope to applied),
                    onBack = onBack,
                    onApply = onApply,
                )
            }
        }
    }

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
