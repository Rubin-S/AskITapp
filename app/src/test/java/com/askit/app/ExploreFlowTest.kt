package com.askit.app

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextInput
import androidx.lifecycle.SavedStateHandle
import com.askit.app.explore.ExploreScreen
import com.askit.app.explore.ExploreViewModel
import com.askit.designsystem.theme.AskITTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

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
                    onQueryChanged = {},
                    onQueryCleared = {},
                )
            }
        }

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
    fun viewModel_rehydratesQueryFromSavedStateHandle() {
        val savedStateHandle = SavedStateHandle()
        ExploreViewModel(savedStateHandle).onQueryChanged("electrician")

        val recreatedViewModel = ExploreViewModel(savedStateHandle)

        assertEquals("electrician", recreatedViewModel.uiState.value.query)
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
}
