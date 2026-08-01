package com.askit.app

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import com.askit.app.explore.ExploreScreen
import com.askit.app.explore.ExploreLocationSource
import com.askit.app.explore.ExploreSearchArea
import com.askit.app.explore.ExploreViewModel
import com.askit.designsystem.theme.AskITTheme
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
    fun searchImeAction_keepsQuery() {
        composeTestRule.setContent {
            AskITTheme(darkTheme = false) {
                ExploreScreen(
                    query = "laptop repair",
                    searchArea = testSearchArea(),
                    onQueryChanged = {},
                    onQueryCleared = {},
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
        composeTestRule.onNodeWithText("Near Kallakurichi").assertDoesNotExist()
        composeTestRule.onNodeWithText("Within 10 km").assertDoesNotExist()
        composeTestRule.onNodeWithContentDescription("Change search area").assertDoesNotExist()
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
                        onQueryChanged = {},
                        onQueryCleared = {},
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

    private fun setApp() {
        composeTestRule.setContent {
            AskITTheme(darkTheme = false) {
                AskITApp(ExploreViewModel(SavedStateHandle()))
            }
        }
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
