package com.askit.app

import android.content.res.Configuration
import android.content.Context
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithTag
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
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.Role
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
import com.askit.app.explore.PersonMatchReason
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
import androidx.test.core.app.ApplicationProvider

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
    fun whitespaceOnlyTypedQuery_usesActiveEmptyAssistance() {
        setApp()
        openExplore()
        searchField().performClick()
        searchField().performTextInput("   ")

        composeTestRule.onNodeWithText("Suggested categories").assertIsDisplayed()
        composeTestRule.onAllNodesWithTag("explore_typed_suggestions").assertCountEquals(0)
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
        composeTestRule.onNodeWithTag("explore_typed_suggestions").assertIsDisplayed()
        composeTestRule.onAllNodesWithText("Plumber").assertCountEquals(2)
    }

    @Test
    fun typedQuery_showsMatchingHistoryServicesAndDirectSearch() {
        val viewModel = ExploreViewModel(SavedStateHandle()).also {
            it.submitQuery("Other recent")
            it.submitQuery("Electrician repair")
            it.onQueryChanged("elec")
        }
        setApp(viewModel)
        openExplore()
        searchField().performClick()

        composeTestRule.onNodeWithText("Recent searches").assertIsDisplayed()
        composeTestRule.onNodeWithText("Electrician repair").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Services").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Electrician").performScrollTo().assertIsDisplayed()
        composeTestRule
            .onNodeWithText("Search for \u201celec\u201d")
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule.onAllNodesWithTag("explore_typed_suggestion_row").assertCountEquals(3)
    }

    @Test
    fun typedQuery_withoutLocalMatches_showsOnlyDirectSearch() {
        val viewModel = ExploreViewModel(SavedStateHandle()).also {
            it.onQueryChanged("laptop charging problem")
        }
        setApp(viewModel)
        openExplore()
        searchField().performClick()

        composeTestRule.onAllNodesWithText("Recent searches").assertCountEquals(0)
        composeTestRule.onAllNodesWithText("Services").assertCountEquals(0)
        composeTestRule
            .onNodeWithText("Search for \u201claptop charging problem\u201d")
            .performScrollTo()
            .assertHasClickAction()
        composeTestRule.onAllNodesWithTag("explore_typed_suggestion_row").assertCountEquals(1)
    }

    @Test
    fun typedQuery_exactMatch_hidesDirectSearch() {
        showTypedQuery("ELECTRICIAN")
        composeTestRule.onAllNodesWithText("Electrician").assertCountEquals(1)
        composeTestRule.onAllNodesWithText("Search for \u201cELECTRICIAN\u201d").assertCountEquals(0)
    }

    @Test
    fun typedQuery_wholeLabelPrefix_matchesCaseInsensitively_afterWhitespaceNormalization() {
        showTypedQuery("  elec  ")
        composeTestRule.onNodeWithText("Electrician").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Search for \u201celec\u201d").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun typedQuery_wordPrefix_matchesServiceCategory() {
        showTypedQuery("tut")
        composeTestRule.onNodeWithText("Home tutor").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun typedQuery_containedMatch_keepsRelatedServicesWithoutFuzzyValues() {
        showTypedQuery("air")
        composeTestRule.onNodeWithText("AC repair").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Appliance repair").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun typedQuery_unrelatedValue_showsDirectSearchOnly() {
        showTypedQuery("laptpo")
        composeTestRule.onAllNodesWithText("Electrician").assertCountEquals(0)
        composeTestRule.onNodeWithText("Search for \u201claptpo\u201d").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun typedQuery_capsSourcesAndTotalActions() {
        val viewModel = ExploreViewModel(SavedStateHandle()).also {
            listOf(
                "first electrician",
                "second electrician",
                "third electrician",
                "fourth electrician",
            ).forEach(it::submitQuery)
            it.onQueryChanged("e")
        }
        setApp(viewModel)
        openExplore()
        searchField().performClick()

        composeTestRule.onAllNodesWithTag("explore_typed_suggestion_row").assertCountEquals(6)
        composeTestRule.onNodeWithText("fourth electrician").assertIsDisplayed()
        composeTestRule.onNodeWithText("third electrician").assertIsDisplayed()
        composeTestRule.onAllNodesWithText("second electrician").assertCountEquals(0)
        composeTestRule.onAllNodesWithText("Services").assertCountEquals(1)
    }

    @Test
    fun typedQuery_deduplicatesHistoryBeforeService_andHidesDirectForExactMatch() {
        showTypedQuery("elec", recentSearches = listOf("Electrician"))
        composeTestRule.onAllNodesWithText("Electrician").assertCountEquals(1)
        composeTestRule.onAllNodesWithTag("explore_typed_suggestion_row").assertCountEquals(2)
        composeTestRule.onAllNodesWithText("Services").assertCountEquals(0)
    }

    @Test
    fun typedSuggestions_exposeHeadingsAndLocalizedActionLabels() {
        showTypedQuery("elec", recentSearches = listOf("Electrician repair"))

        assertEquals(
            true,
            composeTestRule.onNodeWithText("Recent searches")
                .fetchSemanticsNode()
                .config
                .contains(SemanticsProperties.Heading),
        )
        assertEquals(
            true,
            composeTestRule.onNodeWithText("Services")
                .fetchSemanticsNode()
                .config
                .contains(SemanticsProperties.Heading),
        )
        assertEquals(
            "Search for Electrician",
            composeTestRule.onNodeWithText("Electrician")
                .fetchSemanticsNode()
                .config[SemanticsActions.OnClick]
                .label,
        )
        assertEquals(
            "Search for elec",
            composeTestRule.onNodeWithText("Search for \u201celec\u201d")
                .fetchSemanticsNode()
                .config[SemanticsActions.OnClick]
                .label,
        )
    }

    @Test
    fun clearingTypedSearch_restoresActiveEmptyVisibility() {
        val viewModel = ExploreViewModel(SavedStateHandle()).also {
            it.submitQuery("Electrician repair")
            it.onQueryCleared()
        }
        setApp(viewModel)
        openExplore()
        searchField().performClick()
        searchField().performTextInput("elec")

        composeTestRule.onNodeWithTag("explore_typed_suggestions").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Clear search").performClick()
        composeTestRule.onNodeWithText("Recent searches").assertIsDisplayed()
        composeTestRule.onAllNodesWithText("Services").assertCountEquals(0)
    }

    @Test
    fun typedRecentSuggestion_selectionUsesExistingSubmissionPath() {
        val viewModel = ExploreViewModel(SavedStateHandle()).also {
            it.submitQuery("Electrician repair")
            it.onQueryChanged("elec")
        }
        setApp(viewModel)
        openExplore()
        searchField().performClick()

        composeTestRule.onNodeWithText("Electrician repair").performScrollTo().performClick()

        assertEquals("Electrician repair", viewModel.uiState.value.query)
        assertEquals("Electrician repair", viewModel.uiState.value.recentSearches.first())
        composeTestRule.onAllNodesWithTag("explore_typed_suggestions").assertCountEquals(0)
        composeTestRule.onAllNodesWithText("Search area").assertCountEquals(0)
    }

    @Test
    fun typedServiceSuggestion_selectionUsesExistingSubmissionPath() {
        val viewModel = ExploreViewModel(SavedStateHandle()).also {
            it.onQueryChanged("elec")
        }
        setApp(viewModel)
        openExplore()
        searchField().performClick()

        composeTestRule.onNodeWithText("Electrician").performScrollTo().performClick()

        assertEquals("Electrician", viewModel.uiState.value.query)
        assertEquals(listOf("Electrician"), viewModel.uiState.value.recentSearches)
        composeTestRule.onAllNodesWithTag("explore_typed_suggestions").assertCountEquals(0)
    }

    @Test
    fun typedDirectSearch_selectionNormalizesAndStoresQuery() {
        val viewModel = ExploreViewModel(SavedStateHandle()).also {
            it.onQueryChanged("  laptop   charging issue  ")
        }
        setApp(viewModel)
        openExplore()
        searchField().performClick()

        composeTestRule
            .onNodeWithText("Search for \u201claptop charging issue\u201d")
            .performScrollTo()
            .performClick()

        assertEquals("laptop charging issue", viewModel.uiState.value.query)
        assertEquals(listOf("laptop charging issue"), viewModel.uiState.value.recentSearches)
        composeTestRule.onAllNodesWithTag("explore_typed_suggestions").assertCountEquals(0)
    }

    @Test
    fun typingDoesNotMutateHistory_andImeSubmitsTypedQuery_notFirstSuggestion() {
        val viewModel = ExploreViewModel(SavedStateHandle()).also {
            it.submitQuery("Electrician repair")
            it.onQueryCleared()
        }
        setApp(viewModel)
        openExplore()
        searchField().performClick()
        searchField().performTextInput("elec")

        assertEquals(listOf("Electrician repair"), viewModel.uiState.value.recentSearches)
        assertEquals("elec", viewModel.uiState.value.query)
        searchField().performImeAction()

        assertEquals(listOf("elec", "Electrician repair"), viewModel.uiState.value.recentSearches)
        composeTestRule.onAllNodesWithTag("explore_typed_suggestions").assertCountEquals(0)
    }

    @Test
    fun typedSuggestions_useTamilLabels_andAccessibleRows() {
        val baseContext = ApplicationProvider.getApplicationContext<Context>()
        val tamilContext = baseContext.createConfigurationContext(
            Configuration(baseContext.resources.configuration).apply {
                setLocale(Locale.forLanguageTag("ta"))
            },
        )
        val tamilElectrician = tamilContext.getString(R.string.explore_category_electrician)
        composeTestRule.setContent {
            CompositionLocalProvider(LocalContext provides tamilContext) {
                AskITTheme(darkTheme = false) {
                    ExploreScreen(
                        query = tamilElectrician.take(3),
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

        composeTestRule.onNodeWithTag("explore_search_field").performClick()
        composeTestRule.onNodeWithText(tamilContext.getString(R.string.explore_services)).performScrollTo()
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText(tamilElectrician)
            .performScrollTo()
            .assertHasClickAction()
    }

    @Test
    fun typedSuggestions_remainReachableAtNarrowWidthAndLargeText() {
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
                            query = "elec",
                            searchArea = testSearchArea(),
                            recentSearches = listOf("Electrician repair"),
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

        composeTestRule.onNodeWithTag("explore_search_field").performClick()
        listOf(320, 360, 412).forEach { width ->
            contentWidth.value = width.dp
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithText("Electrician").performScrollTo().assertIsDisplayed()
            composeTestRule
                .onNodeWithText("Search for \u201celec\u201d")
                .performScrollTo()
                .assertIsDisplayed()
        }
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
    fun submittedResults_allShowsUniquePeopleBeforeTasks_andDefaultsToAll() {
        setSubmittedContent()

        composeTestRule.onNodeWithTag("explore_result_tabs").assertIsDisplayed()
        composeTestRule.onNodeWithText("All").assertIsSelected()
        composeTestRule.onNodeWithText("People and services").assertIsDisplayed()
        composeTestRule.onNodeWithTag("explore_submitted_tasks").assertIsDisplayed()
        composeTestRule.onNodeWithText("Arun Kumar").assertIsDisplayed()
        composeTestRule.onAllNodesWithText("Both-reason person").assertCountEquals(1)
        composeTestRule.onNodeWithText("Task 1").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun submittedResultScopes_filterByMatchReason_andKeepScopedHeadingsHidden() {
        setSubmittedContent()

        composeTestRule.onNodeWithText("People").performClick()
        composeTestRule.onNodeWithText("People").assertIsSelected()
        composeTestRule.onNodeWithText("Arun Kumar").assertIsDisplayed()
        composeTestRule.onNodeWithText("Identity professional").assertIsDisplayed()
        composeTestRule.onNodeWithText("Both-reason person").assertIsDisplayed()
        composeTestRule.onAllNodesWithText("Service professional").assertCountEquals(0)
        composeTestRule.onAllNodesWithTag("explore_submitted_tasks").assertCountEquals(0)
        composeTestRule.onAllNodesWithText("People and services").assertCountEquals(0)

        composeTestRule.onNodeWithText("Services").performClick()
        composeTestRule.onNodeWithText("Services").assertIsSelected()
        composeTestRule.onNodeWithText("Service professional").assertIsDisplayed()
        composeTestRule.onNodeWithText("Both-reason person").assertIsDisplayed()
        composeTestRule.onAllNodesWithText("Arun Kumar").assertCountEquals(0)
        composeTestRule.onAllNodesWithText("Identity professional").assertCountEquals(0)
        composeTestRule.onAllNodesWithText("Identity-only unrelated service").assertCountEquals(0)
        composeTestRule.onAllNodesWithTag("explore_submitted_tasks").assertCountEquals(0)

        composeTestRule.onNodeWithText("Tasks").performClick()
        composeTestRule.onNodeWithText("Tasks").assertIsSelected()
        composeTestRule.onNodeWithText("Task 1").performScrollTo().assertIsDisplayed()
        composeTestRule.onAllNodesWithText("Arun Kumar").assertCountEquals(0)
        composeTestRule.onAllNodesWithText("Service professional").assertCountEquals(0)
    }

    @Test
    fun submittedResults_preserveSuppliedOrder_andRenderMoreThanFour() {
        val people = submittedPeople() + personResult(
            id = "person-6",
            name = "Sixth submitted person",
            reasons = setOf(PersonMatchReason.Identity),
        )
        val tasks = submittedTasks() + taskResult("task-6", "Task 6")
        setSubmittedContent(people = people, tasks = tasks)

        composeTestRule.onNodeWithText("Sixth submitted person").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Task 6").performScrollTo().assertIsDisplayed()
        composeTestRule.onAllNodesWithText("See all", substring = true).assertCountEquals(0)
    }

    @Test
    fun submittedResultClicks_passStableIdsOnce_andTabSelectionDoesNotSubmit() {
        var selectedPersonId: String? = null
        var selectedTaskId: String? = null
        var submissions = 0
        setSubmittedContent(
            onQuerySubmitted = { submissions++ },
            onPersonClick = { selectedPersonId = it },
            onTaskClick = { selectedTaskId = it },
        )

        composeTestRule.onNodeWithText("Services").performClick()
        composeTestRule.onNodeWithText("Service professional").performScrollTo().performClick()
        assertEquals("service-person", selectedPersonId)
        assertEquals(0, submissions)

        composeTestRule.onNodeWithText("Tasks").performClick()
        composeTestRule.onNodeWithText("Task 1").performScrollTo().performClick()
        assertEquals("task-1", selectedTaskId)
        assertEquals(0, submissions)
    }

    @Test
    fun submittedResultTabs_hideWithoutRequiredCallbacks() {
        setSubmittedContent(onPersonClick = null, onTaskClick = null)

        composeTestRule.onNodeWithTag("explore_result_tabs").assertIsDisplayed()
        composeTestRule.onAllNodesWithText("People and services").assertCountEquals(0)
        composeTestRule.onAllNodesWithText("Task 1").assertCountEquals(0)
    }

    @Test
    fun submittedResultTabs_remainReachableAtSupportedWidthsAndLargeText() {
        val contentWidth = mutableStateOf(320.dp)
        composeTestRule.setContent {
            AskITTheme(darkTheme = false) {
                CompositionLocalProvider(LocalDensity provides Density(1f, 2f)) {
                    Box(
                        modifier = androidx.compose.ui.Modifier
                            .width(contentWidth.value)
                            .height(900.dp),
                    ) {
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
                            submittedPeople = submittedPeople(),
                            submittedTasks = submittedTasks(),
                            onPersonClick = {},
                            onTaskClick = {},
                        )
                    }
                }
            }
        }

        listOf(320, 360, 412).forEach { width ->
            contentWidth.value = width.dp
            composeTestRule.waitForIdle()
            composeTestRule
                .onNodeWithTag("explore_result_tabs")
                .assertIsDisplayed()
                .performTouchInput { swipeLeft() }
            composeTestRule.onNodeWithText("All").assertIsSelected()
            composeTestRule
                .onAllNodesWithText("Tasks")[0]
                .assertIsDisplayed()
                .assertHasClickAction()
        }
    }

    @Test
    fun changingSubmittedQuery_resetsScope_andEditingShowsSuggestionsInsteadOfStaleResults() {
        val query = mutableStateOf("electrician")
        composeTestRule.setContent {
            AskITTheme(darkTheme = false) {
                ExploreScreen(
                    query = query.value,
                    searchArea = testSearchArea(),
                    recentSearches = emptyList(),
                    onQueryChanged = { query.value = it },
                    onQueryCleared = { query.value = "" },
                    onQuerySubmitted = {},
                    onRecentSearchRemoved = {},
                    onRecentSearchesCleared = {},
                    onSearchFiltersClick = {},
                    submittedPeople = submittedPeople(),
                    submittedTasks = submittedTasks(),
                    onPersonClick = {},
                    onTaskClick = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Services").performClick()
        composeTestRule.onNodeWithText("Services").assertIsSelected()
        query.value = "plumber"
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("All").assertIsSelected()

        searchField().performClick()
        composeTestRule.onNodeWithTag("explore_typed_suggestions").assertIsDisplayed()
        composeTestRule.onAllNodesWithTag("explore_result_tabs").assertCountEquals(0)
    }

    @Test
    fun selectedScope_survivesStateRestoration_forUnchangedQuery() {
        val restorationTester = StateRestorationTester(composeTestRule)
        restorationTester.setContent {
            AskITTheme(darkTheme = false) {
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
                    submittedPeople = submittedPeople(),
                    submittedTasks = submittedTasks(),
                    onPersonClick = {},
                    onTaskClick = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Services").performClick()
        restorationTester.emulateSavedInstanceStateRestore()

        composeTestRule.onNodeWithText("Services").assertIsSelected()
    }

    @Test
    fun resultTabs_useMaterialTabSemantics_andTamilLabelsRemainComplete() {
        composeTestRule.setContent {
            val configuration = Configuration(LocalConfiguration.current).apply {
                setLocale(Locale.forLanguageTag("ta"))
            }
            val tamilContext = LocalContext.current.createConfigurationContext(configuration)
            CompositionLocalProvider(LocalContext provides tamilContext) {
                AskITTheme(darkTheme = false) {
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
                        submittedPeople = submittedPeople(),
                        submittedTasks = submittedTasks(),
                        onPersonClick = {},
                        onTaskClick = {},
                    )
                }
            }
        }

        val allTab = composeTestRule.onNodeWithText("அனைத்தும்")
        allTab.assertIsSelected()
        assertEquals(Role.Tab, allTab.fetchSemanticsNode().config[SemanticsProperties.Role])
        composeTestRule.onNodeWithText("மக்கள்").assertIsDisplayed()
        composeTestRule.onNodeWithText("சேவைகள்").assertIsDisplayed()
        composeTestRule.onAllNodesWithText("பணிகள்").assertCountEquals(2)
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

    private fun showTypedQuery(
        query: String,
        recentSearches: List<String> = emptyList(),
    ) {
        composeTestRule.setContent {
            AskITTheme(darkTheme = false) {
                ExploreScreen(
                    query = query,
                    searchArea = testSearchArea(),
                    recentSearches = recentSearches,
                    onQueryChanged = {},
                    onQueryCleared = {},
                    onQuerySubmitted = {},
                    onRecentSearchRemoved = {},
                    onRecentSearchesCleared = {},
                    onSearchFiltersClick = {},
                )
            }
        }
        composeTestRule.onNodeWithTag("explore_search_field").performClick()
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

    private fun setSubmittedContent(
        query: String = "electrician",
        people: List<ExplorePersonResult> = submittedPeople(),
        tasks: List<ExploreTaskResult> = submittedTasks(),
        onQuerySubmitted: (String) -> Unit = {},
        onPersonClick: ((String) -> Unit)? = {},
        onTaskClick: ((String) -> Unit)? = {},
    ) {
        composeTestRule.setContent {
            AskITTheme(darkTheme = false) {
                ExploreScreen(
                    query = query,
                    searchArea = testSearchArea(),
                    recentSearches = emptyList(),
                    onQueryChanged = {},
                    onQueryCleared = {},
                    onQuerySubmitted = onQuerySubmitted,
                    onRecentSearchRemoved = {},
                    onRecentSearchesCleared = {},
                    onSearchFiltersClick = {},
                    submittedPeople = people,
                    submittedTasks = tasks,
                    onPersonClick = onPersonClick,
                    onTaskClick = onTaskClick,
                )
            }
        }
    }

    private fun submittedPeople(): List<ExplorePersonResult> = listOf(
        personResult(
            id = "identity-person",
            name = "Arun Kumar",
            primaryService = null,
            rating = 4.8,
            reviewCount = 36,
            priceLabel = "From â‚¹500",
            statusLabel = "Available today",
            reasons = setOf(PersonMatchReason.Identity),
        ),
        personResult(
            id = "identity-professional",
            name = "Identity professional",
            primaryService = "Home tutor",
            reasons = setOf(PersonMatchReason.Identity),
        ),
        personResult(
            id = "service-person",
            name = "Service professional",
            primaryService = "Electrician",
            rating = 4.8,
            reviewCount = 36,
            reasons = setOf(PersonMatchReason.Service),
        ),
        personResult(
            id = "both-person",
            name = "Both-reason person",
            primaryService = "Plumber",
            reasons = setOf(PersonMatchReason.Identity, PersonMatchReason.Service),
        ),
        personResult(
            id = "identity-unrelated-service",
            name = "Identity-only unrelated service",
            primaryService = "Cleaning",
            reasons = setOf(PersonMatchReason.Identity),
        ),
    )

    private fun submittedTasks(): List<ExploreTaskResult> = (1..5).map { index ->
        taskResult("task-$index", "Task $index")
    }

    private fun personResult(
        id: String,
        name: String,
        reasons: Set<PersonMatchReason>,
        primaryService: String? = "Electrician",
        rating: Double? = null,
        reviewCount: Int = 0,
        priceLabel: String? = null,
        statusLabel: String? = null,
    ) = ExplorePersonResult(
        id = id,
        name = name,
        avatarUrl = null,
        primaryService = primaryService,
        additionalServices = emptyList(),
        rating = rating,
        reviewCount = reviewCount,
        locationLabel = "Kallakurichi",
        priceLabel = priceLabel,
        statusLabel = statusLabel,
        matchReasons = reasons,
    )

    private fun taskResult(id: String, title: String) = ExploreTaskResult(
        id = id,
        title = title,
        category = "Electrical work",
        summary = "Repair work near Kallakurichi",
        budgetLabel = "₹800–₹1,500",
        locationLabel = "Kallakurichi",
        timingLabel = "Needed Monday",
        posterName = "Poster $id",
        postedLabel = "Posted today",
        status = TaskResultStatus.Open,
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
