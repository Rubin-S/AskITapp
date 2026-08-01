package com.askit.app

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
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
    fun activityRecreation_preservesExploreAndQuery() {
        openExplore()
        composeTestRule.onNodeWithTag("explore_search_field").performTextInput("electrician")

        composeTestRule.activity.recreate()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription("Explore").assertIsSelected()
        composeTestRule.onNodeWithText("electrician").assertIsDisplayed()
    }

    private fun openExplore() {
        composeTestRule.onNodeWithContentDescription("Explore").performClick()
        composeTestRule.onNodeWithContentDescription("Explore").assertIsSelected()
        composeTestRule.onNodeWithTag("explore_search_field").assertExists()
    }
}
