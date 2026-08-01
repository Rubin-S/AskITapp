package com.askit.app

import android.content.res.Configuration
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import com.askit.app.explore.ExplorePersonResult
import com.askit.app.explore.ExploreScreen
import com.askit.app.explore.ExploreLocationSource
import com.askit.app.explore.ExploreSearchArea
import com.askit.app.explore.ExploreTaskResult
import com.askit.app.explore.ExploreViewModel
import com.askit.designsystem.tasks.TaskResultStatus
import com.askit.designsystem.theme.AskITTheme
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import androidx.compose.ui.unit.dp

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w360dp-h800dp-normal-long-notround-any-xxxhdpi-keyshidden-nonav")
class ExploreFlowTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun tappingExplore_opensSearch_selectsExplore_andKeepsOneBottomBar() {
        setApp()

        composeTestRule.onNodeWithContentDescription("Explore").performClick()
        settleNavigation()

        searchField().assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Explore").assertIsSelected()
        composeTestRule.onAllNodesWithContentDescription("Create").assertCountEquals(1)
    }

    @Test
    fun queryEntry_andClear_updateTheDisplayedState() {
        setApp()
        openExplore()

        searchField().performTextInput("electrician")
        composeTestRule.onNodeWithText("electrician").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Clear search").assertIsDisplayed()

        composeTestRule.onNodeWithContentDescription("Clear search").performClick()

        composeTestRule.onAllNodesWithContentDescription("Clear search").assertCountEquals(0)
        composeTestRule.onNodeWithTag("explore_search_field").assertExists()
    }

    @Test
    fun idleExplore_hidesActiveSearchContent() {
        setApp()
        openExplore()

        composeTestRule.onAllNodesWithText("Recent searches").assertCountEquals(0)
        composeTestRule.onAllNodesWithText("Suggested categories").assertCountEquals(0)
        composeTestRule.onNodeWithText("Browse services").assertIsDisplayed()
        composeTestRule.onNodeWithText("Electrician").assertIsDisplayed()
    }

    @Test
    fun browseServices_rendersAllSixLocalizedCategoryActions() {
        setApp()
        openExplore()

        listOf(
            "Electrician",
            "Plumber",
            "Cleaning",
            "AC repair",
            "Home tutor",
            "Appliance repair",
        ).forEachIndexed { index, category ->
            composeTestRule.onNodeWithTag("explore_browse_category_row").performScrollToIndex(index)
            composeTestRule.onNodeWithText(category).assertHasClickAction()
        }

        composeTestRule.onNodeWithTag("explore_browse_category_row")
            .performTouchInput { swipeLeft() }
        composeTestRule.onNodeWithText("Appliance repair").assertIsDisplayed()
    }

    @Test
    fun browseCategory_clickSubmitsQuery_updatesHistory_andHidesBrowse() {
        val viewModel = ExploreViewModel(SavedStateHandle())
        setApp(viewModel)
        openExplore()

        composeTestRule.onNodeWithText("Electrician").performClick()

        assertEquals("Electrician", viewModel.uiState.value.query)
        assertEquals(listOf("Electrician"), viewModel.uiState.value.recentSearches)
        composeTestRule.onAllNodesWithText("Browse services").assertCountEquals(0)
        composeTestRule.onAllNodesWithText("Suggested categories").assertCountEquals(0)
    }

    @Test
    fun browseServices_usesTamilLabels_andKeepsTilesAccessible() {
        composeTestRule.setContent {
            val configuration = Configuration(LocalConfiguration.current).apply {
                setLocale(Locale.forLanguageTag("ta"))
            }
            val tamilContext = LocalContext.current.createConfigurationContext(configuration)
            CompositionLocalProvider(LocalContext provides tamilContext) {
                AskITTheme(darkTheme = false) {
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

        composeTestRule.onNodeWithText("சேவைகளை உலாவுங்கள்").assertIsDisplayed()
        composeTestRule.onNodeWithText("எலக்ட்ரீஷியன்").assertHasClickAction()
    }

    @Test
    fun browseServices_remainsReachableAtNarrowWidthsAndLargeText() {
        val contentWidth = mutableStateOf(320.dp)
        composeTestRule.setContent {
            AskITTheme(darkTheme = false) {
                CompositionLocalProvider(LocalDensity provides Density(1f, 2f)) {
                    Box(
                        modifier = androidx.compose.ui.Modifier
                            .width(contentWidth.value)
                            .height(700.dp),
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

        listOf(320, 360, 412).forEach { width ->
            contentWidth.value = width.dp
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithText("Browse services").assertIsDisplayed()
            composeTestRule.onNodeWithTag("explore_browse_category_row").assertIsDisplayed()
        }
    }

    @Test
    fun focusingEmptySearch_showsCategories_withoutEmptyHistory() {
        setApp()
        openExplore()
        searchField().performClick()

        composeTestRule.onNodeWithText("Suggested categories").assertIsDisplayed()
        composeTestRule.onNodeWithText("Electrician").assertIsDisplayed()
        composeTestRule.onAllNodesWithText("Recent searches").assertCountEquals(0)
        composeTestRule.onAllNodesWithText("Browse services").assertCountEquals(0)
    }

    @Test
    fun typedQuery_hidesRecentSearchesAndCategories() {
        setAppWithHistory()
        openExplore()
        searchField().performClick()
        searchField().performTextInput("Plumber")

        composeTestRule.onAllNodesWithText("Recent searches").assertCountEquals(0)
        composeTestRule.onAllNodesWithText("Suggested categories").assertCountEquals(0)
        composeTestRule.onAllNodesWithText("Browse services").assertCountEquals(0)
    }

    @Test
    fun clearingActiveQuery_restoresHistoryAndCategories() {
        setAppWithHistory()
        openExplore()
        searchField().performClick()
        searchField().performTextInput("Plumber")
        composeTestRule.onNodeWithContentDescription("Clear search").performClick()

        composeTestRule.onNodeWithText("Recent searches").assertIsDisplayed()
        composeTestRule.onNodeWithText("Suggested categories").assertIsDisplayed()
    }

    @Test
    fun clearingInactiveQuery_keepsActiveSearchContentHidden() {
        val viewModel = ExploreViewModel(SavedStateHandle()).also {
            it.onQueryChanged("Electrician")
        }
        setApp(viewModel)
        openExplore()
        composeTestRule.onNodeWithContentDescription("Clear search").performClick()

        composeTestRule.onAllNodesWithText("Recent searches").assertCountEquals(0)
        composeTestRule.onAllNodesWithText("Suggested categories").assertCountEquals(0)
        composeTestRule.onNodeWithText("Browse services").assertIsDisplayed()
    }

    @Test
    fun imeSearch_normalizesAndStoresQuery_thenClosesActiveSearch() {
        val viewModel = ExploreViewModel(SavedStateHandle())
        setApp(viewModel)
        openExplore()
        searchField().performClick()
        searchField().performTextInput("  Laptop   repair  ")
        searchField().performImeAction()

        composeTestRule.onNodeWithText("Laptop repair").assertIsDisplayed()
        composeTestRule.onAllNodesWithText("Suggested categories").assertCountEquals(0)
        assertEquals(listOf("Laptop repair"), viewModel.uiState.value.recentSearches)
    }

    @Test
    fun recentSearch_selection_submitsAndClosesSearch() {
        setAppWithHistory()
        openExplore()
        searchField().performClick()

        composeTestRule
            .onNodeWithContentDescription("Search for Electrician")
            .assertHasClickAction()
            .performClick()

        composeTestRule.onNodeWithText("Electrician").assertIsDisplayed()
        composeTestRule.onAllNodesWithText("Suggested categories").assertCountEquals(0)
    }

    @Test
    fun recentSearch_remove_doesNotSubmit_orChangeCurrentQuery() {
        val viewModel = ExploreViewModel(SavedStateHandle()).also {
            it.submitQuery("Laptop repair")
            it.submitQuery("Electrician")
            it.onQueryCleared()
        }
        setApp(viewModel)
        openExplore()
        searchField().performClick()

        composeTestRule
            .onNodeWithContentDescription("Remove Laptop repair from recent searches")
            .performClick()

        composeTestRule.onAllNodesWithText("Laptop repair").assertCountEquals(0)
        composeTestRule.onNodeWithTag("explore_search_field").assertIsDisplayed()
        composeTestRule.waitForIdle()
        assertEquals("", viewModel.uiState.value.query)
        assertEquals(listOf("Electrician"), viewModel.uiState.value.recentSearches)
    }

    @Test
    fun clearAll_removesHistory_butLeavesCategoriesVisible() {
        setAppWithHistory()
        openExplore()
        searchField().performClick()

        composeTestRule.onNodeWithText("Clear all").performClick()

        composeTestRule.onAllNodesWithText("Recent searches").assertCountEquals(0)
        composeTestRule.onNodeWithText("Suggested categories").assertIsDisplayed()
    }

    @Test
    fun clearAll_isHiddenWhenOnlyOneRecentSearchExists() {
        val viewModel = ExploreViewModel(SavedStateHandle()).also {
            it.submitQuery("Electrician")
            it.onQueryCleared()
        }
        setApp(viewModel)
        openExplore()
        searchField().performClick()

        composeTestRule.onNodeWithText("Recent searches").assertIsDisplayed()
        composeTestRule.onAllNodesWithText("Clear all").assertCountEquals(0)
    }

    @Test
    fun optionalResultSections_showOnlyWithActions_limitToFour_andPreserveIds() {
        var selectedPersonId: String? = null
        var selectedTaskId: String? = null
        val people = (1..5).map { index ->
            ExplorePersonResult(
                id = "person-$index",
                name = "Professional $index",
                avatarUrl = null,
                primaryService = "Electrician",
                additionalServices = emptyList(),
                rating = null,
                reviewCount = 0,
                locationLabel = "Kallakurichi",
                priceLabel = null,
                statusLabel = null,
            )
        }
        val tasks = (1..5).map { index ->
            ExploreTaskResult(
                id = "task-$index",
                title = "Task $index",
                category = "Repair",
                summary = null,
                budgetLabel = "Quote required",
                locationLabel = "Kallakurichi",
                timingLabel = "Flexible",
                posterName = "Poster $index",
                postedLabel = "Posted today",
                status = TaskResultStatus.Open,
            )
        }

        composeTestRule.setContent {
            AskITTheme(darkTheme = false) {
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
                    people = people,
                    tasks = tasks,
                    onPersonClick = { selectedPersonId = it },
                    onTaskClick = { selectedTaskId = it },
                )
            }
        }

        val peopleHeading = composeTestRule.onNodeWithText("Nearby professionals")
        peopleHeading.assertIsDisplayed()
        assertEquals(true, peopleHeading.fetchSemanticsNode().config.contains(SemanticsProperties.Heading))
        composeTestRule.onAllNodesWithText("Professional 5").assertCountEquals(0)
        composeTestRule.onNodeWithText("Professional 1").performScrollTo().performClick()
        assertEquals("person-1", selectedPersonId)

        val tasksHeading = composeTestRule.onNodeWithText("Open tasks nearby")
        tasksHeading.performScrollTo().assertIsDisplayed()
        assertEquals(true, tasksHeading.fetchSemanticsNode().config.contains(SemanticsProperties.Heading))
        composeTestRule.onAllNodesWithText("Task 5").assertCountEquals(0)
        composeTestRule.onNodeWithText("Task 1").performScrollTo().performClick()
        assertEquals("task-1", selectedTaskId)
    }

    @Test
    fun optionalResultSections_withoutActions_areHidden() {
        composeTestRule.setContent {
            AskITTheme(darkTheme = false) {
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
                    people = listOf(
                        ExplorePersonResult(
                            id = "person-1",
                            name = "Actionless person",
                            avatarUrl = null,
                            primaryService = "Electrician",
                            additionalServices = emptyList(),
                            rating = null,
                            reviewCount = 0,
                            locationLabel = "Kallakurichi",
                            priceLabel = null,
                            statusLabel = null,
                        ),
                    ),
                )
            }
        }

        composeTestRule.onAllNodesWithText("Nearby professionals").assertCountEquals(0)
    }

    @Test
    fun suggestedCategory_selection_submitsWithoutNavigation() {
        val viewModel = ExploreViewModel(SavedStateHandle())
        setApp(viewModel)
        openExplore()
        searchField().performClick()

        composeTestRule.onNodeWithText("Plumber").performClick()

        composeTestRule.onNodeWithText("Plumber").assertIsDisplayed()
        composeTestRule.onAllNodesWithText("Search area").assertCountEquals(0)
        assertEquals(listOf("Plumber"), viewModel.uiState.value.recentSearches)
    }

    @Test
    fun viewModel_normalizes_deduplicates_andLimitsRecentSearches() {
        val viewModel = ExploreViewModel(SavedStateHandle())

        viewModel.submitQuery("  Electrician  ")
        viewModel.submitQuery("Laptop   repair")
        viewModel.submitQuery("Home tutor")
        viewModel.submitQuery("Plumber")
        viewModel.submitQuery("Cleaning")
        viewModel.submitQuery("electrician")
        composeTestRule.waitForIdle()

        assertEquals(
            listOf("electrician", "Cleaning", "Plumber", "Home tutor"),
            viewModel.uiState.value.recentSearches,
        )
        assertEquals("electrician", viewModel.uiState.value.query)
    }

    @Test
    fun viewModel_rejectsWhitespace_andDoesNotStorePartialTyping() {
        val viewModel = ExploreViewModel(SavedStateHandle())

        viewModel.submitQuery("   ")
        viewModel.onQueryChanged("Partial typing")
        composeTestRule.waitForIdle()

        assertEquals("Partial typing", viewModel.uiState.value.query)
        assertEquals(emptyList<String>(), viewModel.uiState.value.recentSearches)
        viewModel.submitQuery("   ")
        composeTestRule.waitForIdle()
        assertEquals("", viewModel.uiState.value.query)
    }

    @Test
    fun viewModel_history_survivesSavedStateRecreation() {
        val savedStateHandle = SavedStateHandle()
        ExploreViewModel(savedStateHandle).apply {
            submitQuery("Home tutor")
            submitQuery("Electrician")
        }
        composeTestRule.waitForIdle()

        val recreatedViewModel = ExploreViewModel(savedStateHandle)

        assertEquals(
            listOf("Electrician", "Home tutor"),
            recreatedViewModel.uiState.value.recentSearches,
        )
    }

    @Test
    fun searchImeAction_keepsQuery() {
        composeTestRule.setContent {
            AskITTheme(darkTheme = false) {
                ExploreScreen(
                    query = "laptop repair",
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

        searchField().performClick()
        searchField().performImeAction()

        composeTestRule.onNodeWithText("laptop repair").assertIsDisplayed()
    }

    @Test
    fun switchingRootDestinations_preservesExploreQuery() {
        setApp()
        openExplore()
        searchField().performTextInput("laptop repair")

        composeTestRule.onNodeWithContentDescription("Home").performClick()
        composeTestRule.onNodeWithContentDescription("Inbox").performClick()
        composeTestRule.onNodeWithContentDescription("Explore").performClick()

        composeTestRule.onNodeWithText("laptop repair").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Explore").assertIsSelected()
    }

    @Test
    fun filterButton_opensSearchArea_andBackDiscardsDraft() {
        setApp()
        openExplore()
        searchField().performTextInput("electrician")

        filterButton().performClick()
        composeTestRule.onNodeWithText("Search area").assertIsDisplayed()

        composeTestRule.onNodeWithContentDescription("Within 25 km").performClick()
        composeTestRule.onNodeWithContentDescription("Back").performClick()

        summary().assertIsDisplayed()
        composeTestRule.onNodeWithText("Kallakurichi · 10 km").assertIsDisplayed()
        composeTestRule.onNodeWithText("electrician").assertIsDisplayed()
    }

    @Test
    fun applyingSearchArea_updatesConfirmedRadius() {
        setApp()
        openExplore()
        filterButton().performClick()

        composeTestRule.onNodeWithContentDescription("Within 25 km").performClick()
        composeTestRule.onNodeWithText("Apply").performClick()

        composeTestRule.onNodeWithText("Kallakurichi · 25 km").assertIsDisplayed()
    }

    @Test
    fun exploreHeader_showsCompactSummary_andFilterButton() {
        setApp()
        openExplore()

        filterButton().assertIsDisplayed()
        filterButton().assertWidthIsEqualTo(48.dp)
        composeTestRule
            .onNodeWithContentDescription(
                "Search filters. Current area: Kallakurichi, within 10 kilometres.",
            )
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Kallakurichi · 10 km").assertIsDisplayed()
        assertFalse(summary().fetchSemanticsNode().config.contains(SemanticsActions.OnClick))
        composeTestRule.onAllNodesWithText("Near Kallakurichi").assertCountEquals(0)
        composeTestRule.onAllNodesWithText("Within 10 km").assertCountEquals(0)
        composeTestRule.onAllNodesWithContentDescription("Change search area").assertCountEquals(0)
    }

    @Test
    fun filterButton_longPress_showsFiltersTooltip() {
        setApp()
        openExplore()

        filterButton().performTouchInput { longClick() }

        composeTestRule.onNodeWithText("Filters").assertIsDisplayed()
    }

    @Test
    fun filterButton_reopen_doesNotDuplicateSearchAreaDestination() {
        setApp()
        openExplore()

        filterButton().performClick()
        composeTestRule.onNodeWithText("Search area").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Back").performClick()

        filterButton().performClick()
        composeTestRule.onNodeWithText("Search area").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Back").performClick()
        composeTestRule.onNodeWithText("Kallakurichi · 10 km").assertIsDisplayed()
    }

    @Test
    fun exploreHeader_remainsUsableAtSupportedNarrowWidths() {
        val contentWidth = mutableStateOf(320.dp)
        composeTestRule.setContent {
            AskITTheme(darkTheme = false) {
                Box(
                    modifier = androidx.compose.ui.Modifier
                        .width(contentWidth.value)
                        .height(240.dp),
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

        listOf(320, 360, 412).forEach { width ->
            contentWidth.value = width.dp
            composeTestRule.waitForIdle()
            searchField().assertIsDisplayed()
            filterButton().assertIsDisplayed().assertWidthIsEqualTo(48.dp)
            summary().assertIsDisplayed()
        }
    }

    @Test
    fun viewModel_rehydratesQueryFromSavedStateHandle() {
        val savedStateHandle = SavedStateHandle()
        ExploreViewModel(savedStateHandle).onQueryChanged("electrician")

        val recreatedViewModel = ExploreViewModel(savedStateHandle)

        assertEquals("electrician", recreatedViewModel.uiState.value.query)
    }

    @Test
    fun viewModel_rehydratesAppliedSearchAreaFromSavedStateHandle() {
        val savedStateHandle = SavedStateHandle()
        ExploreViewModel(savedStateHandle).onSearchAreaApplied(
            testSearchArea().copy(radiusKm = 25),
        )

        val recreatedViewModel = ExploreViewModel(savedStateHandle)

        assertEquals(25, recreatedViewModel.uiState.value.searchArea.radiusKm)
        assertEquals("Kallakurichi", recreatedViewModel.uiState.value.searchArea.displayName)
    }

    @Test
    fun searchField_hasLocalizedLabel_andClearActionHasLocalizedSemantics() {
        setApp()
        openExplore()

        composeTestRule.onNodeWithText("Search people, services, or tasks").assertIsDisplayed()
        searchField().performTextInput("task")
        composeTestRule.onNodeWithContentDescription("Clear search").assertIsDisplayed()
    }

    private fun setApp(viewModel: ExploreViewModel = ExploreViewModel(SavedStateHandle())) {
        composeTestRule.setContent {
            AskITTheme(darkTheme = false) {
                AskITApp(viewModel)
            }
        }
    }

    private fun setAppWithHistory() {
        val viewModel = ExploreViewModel(SavedStateHandle()).also {
            it.submitQuery("Home tutor")
            it.submitQuery("Laptop repair")
            it.submitQuery("Electrician")
            it.onQueryCleared()
        }
        setApp(viewModel)
    }

    private fun openExplore() {
        composeTestRule.onNodeWithContentDescription("Explore").performClick()
        settleNavigation()
        searchField().assertIsDisplayed()
    }

    private fun settleNavigation() {
        composeTestRule.mainClock.advanceTimeBy(5_000)
        composeTestRule.waitForIdle()
    }

    private fun searchField() = composeTestRule.onNodeWithTag("explore_search_field")

    private fun filterButton() = composeTestRule.onNodeWithTag("explore_filter_button")

    private fun summary() = composeTestRule.onNodeWithTag("explore_location_summary")

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
