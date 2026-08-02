package com.askit.app

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w360dp-h800dp-normal-long-notround-any-xxxhdpi-keyshidden-nonav")
class NavigationPersistenceTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun appStartsAtHome_andCreateOpensSheet_once() {
        composeTestRule.onNodeWithContentDescription("Home").assertIsSelected()
        composeTestRule.onNodeWithContentDescription("Create").performClick()
        composeTestRule.onNodeWithText("Post a task").assertIsDisplayed()
    }

    @Test
    fun switchingTabs_doesNotReplayHistoricalTabs_andBackExitsThroughHome() {
        openExplore()
        composeTestRule.onNodeWithContentDescription("Inbox").performClick()
        composeTestRule.onNodeWithContentDescription("Inbox").assertIsSelected()

        composeTestRule.activity.onBackPressedDispatcher.onBackPressed()
        composeTestRule.onNodeWithContentDescription("Home").assertIsSelected()
        composeTestRule.onNodeWithContentDescription("Explore").assertIsDisplayed()

        composeTestRule.activity.onBackPressedDispatcher.onBackPressed()
        composeTestRule.onNodeWithContentDescription("Home").assertIsSelected()
    }

    @Test
    fun backFromExploreRoot_returnsDirectlyToHome() {
        openExplore()

        composeTestRule.activity.onBackPressedDispatcher.onBackPressed()

        composeTestRule.onNodeWithContentDescription("Home").assertIsSelected()
        composeTestRule.onNodeWithContentDescription("Explore").assertIsDisplayed()
    }

    @Test
    fun reselectingExplore_doesNotAddDuplicateRoot() {
        openExplore()
        repeat(3) {
            composeTestRule.onNodeWithContentDescription("Explore").performClick()
        }

        composeTestRule.activity.onBackPressedDispatcher.onBackPressed()
        composeTestRule.onNodeWithContentDescription("Home").assertIsSelected()
    }

    @Test
    fun switchingAwayAndReturning_preservesExploreQuery() {
        openExplore()
        composeTestRule.onNodeWithTag("explore_search_field").performTextInput("electrician")

        composeTestRule.onNodeWithContentDescription("Inbox").performClick()
        composeTestRule.onNodeWithContentDescription("Explore").performClick()

        composeTestRule.onNodeWithText("electrician").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Explore").assertIsSelected()
        composeTestRule.onAllNodesWithContentDescription("Create").assertCountEquals(1)
    }

    @Test
    fun filterButton_opensNestedFilters_andBackReturnsToExplore() {
        openExplore()

        composeTestRule.onNodeWithTag("explore_filter_button").performClick()
        composeTestRule.onNodeWithText("Filters").assertIsDisplayed()
        composeTestRule.onAllNodesWithContentDescription("Create").assertCountEquals(0)

        composeTestRule.onNodeWithContentDescription("Back").performClick()
        composeTestRule.onNodeWithTag("explore_search_field").assertIsDisplayed()
        composeTestRule.onAllNodesWithContentDescription("Create").assertCountEquals(1)
    }

    @Test
    fun activityRecreation_preservesConfirmedSearchAreaFromFilters() {
        openExplore()
        composeTestRule.onNodeWithTag("explore_filter_button").performClick()
        composeTestRule.onNodeWithContentDescription("Within 25 km").performClick()
        composeTestRule.onNodeWithText("Apply").performClick()

        composeTestRule.activity.recreate()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Kallakurichi · 25 km").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Explore").assertIsSelected()
    }

    @Test
    fun activityRecreation_preservesExploreAndQuery() {
        openExplore()
        composeTestRule.onNodeWithTag("explore_search_field").performTextInput("electrician")

        composeTestRule.activity.recreate()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription("Explore").assertIsSelected()
        composeTestRule.onNodeWithText("electrician").assertIsDisplayed()
    }

    @Test
    fun backWhileSearchIsActive_closesSearchBeforeNavigation() {
        openExplore()
        composeTestRule.onNodeWithTag("explore_search_field").performClick()
        composeTestRule.onNodeWithText("Suggested categories").assertIsDisplayed()

        composeTestRule.activity.onBackPressedDispatcher.onBackPressed()
        composeTestRule.onAllNodesWithText("Suggested categories").assertCountEquals(0)
        composeTestRule.onNodeWithText("Browse services").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Explore").assertIsSelected()

        composeTestRule.activity.onBackPressedDispatcher.onBackPressed()
        composeTestRule.onNodeWithContentDescription("Home").assertIsSelected()
    }

    @Test
    fun filterWhileSearchIsActive_closesSearchBeforeOpeningFilters() {
        openExplore()
        composeTestRule.onNodeWithTag("explore_search_field").performClick()
        composeTestRule.onNodeWithTag("explore_search_field").performTextInput("elec")
        composeTestRule.onNodeWithTag("explore_typed_suggestions").assertIsDisplayed()

        composeTestRule.onNodeWithTag("explore_filter_button").performClick()

        composeTestRule.onNodeWithText("Filters").assertIsDisplayed()
        composeTestRule.onAllNodesWithTag("explore_typed_suggestions").assertCountEquals(0)
    }

    @Test
    fun backWhileTypedSearchIsActive_closesSuggestions_withoutSubmitting() {
        openExplore()
        composeTestRule.onNodeWithTag("explore_search_field").performClick()
        composeTestRule.onNodeWithTag("explore_search_field").performTextInput("elec")

        composeTestRule.onNodeWithTag("explore_typed_suggestions").assertIsDisplayed()
        composeTestRule.activity.onBackPressedDispatcher.onBackPressed()

        composeTestRule.onAllNodesWithTag("explore_typed_suggestions").assertCountEquals(0)
        composeTestRule.onNodeWithText("elec").assertIsDisplayed()
        composeTestRule.onAllNodesWithText("Browse services").assertCountEquals(0)
    }

    private fun openExplore() {
        composeTestRule.onNodeWithContentDescription("Explore").performClick()
        composeTestRule.onNodeWithContentDescription("Explore").assertIsSelected()
        composeTestRule.onNodeWithTag("explore_search_field").assertExists()
    }
}
