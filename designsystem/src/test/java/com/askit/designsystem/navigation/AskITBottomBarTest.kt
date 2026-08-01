package com.askit.designsystem.navigation

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import com.askit.designsystem.theme.AskITTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AskITBottomBarTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun destinationCallbacks_emitCorrectDestinations() {
        val clicks = mutableListOf<AskITDestination>()
        setBottomBar(onDestinationClick = { clicks += it })

        composeTestRule.onNodeWithContentDescription("Home").performClick()
        composeTestRule.onNodeWithContentDescription("Explore").performClick()
        composeTestRule.onNodeWithContentDescription("Inbox").performClick()
        composeTestRule.onNodeWithContentDescription("Profile").performClick()

        assertEquals(
            listOf(
                AskITDestination.Home,
                AskITDestination.Explore,
                AskITDestination.Inbox,
                AskITDestination.Profile,
            ),
            clicks,
        )
    }

    @Test
    fun create_invokesOnlyOnCreateClick() {
        var createClicks = 0
        var destinationClicks = 0
        setBottomBar(
            onDestinationClick = { destinationClicks++ },
            onCreateClick = { createClicks++ },
        )

        composeTestRule.onNodeWithContentDescription("Create").performClick()
        assertEquals(1, createClicks)
        assertEquals(0, destinationClicks)
    }

    @Test
    fun selectedDestination_exposesSelectedSemantics() {
        setBottomBar(selected = AskITDestination.Explore)
        composeTestRule.onNodeWithContentDescription("Explore").assertIsSelected()
    }

    @Test
    fun noVisibleNavigationLabels() {
        setBottomBar(selected = AskITDestination.Home)
        // Labels are not composed as visible text; only tooltips show after long-press.
        composeTestRule.onAllNodesWithText("Home").assertCountEquals(0)
        composeTestRule.onAllNodesWithText("Explore").assertCountEquals(0)
        composeTestRule.onAllNodesWithText("Inbox").assertCountEquals(0)
        composeTestRule.onAllNodesWithText("Profile").assertCountEquals(0)
        composeTestRule.onAllNodesWithText("Create").assertCountEquals(0)
    }

    @Test
    fun longPress_showsEachTooltip() {
        setBottomBar()
        listOf("Home", "Explore", "Create", "Inbox", "Profile").forEach { name ->
            composeTestRule
                .onNodeWithContentDescription(name)
                .performTouchInput { longClick() }
            composeTestRule.onNodeWithText(name).assertIsDisplayed()
        }
    }

    @Test
    fun badge_absentForZero() {
        setBottomBar(unreadCount = 0)
        composeTestRule.onAllNodesWithText("0").assertCountEquals(0)
        composeTestRule.onAllNodesWithText("99+").assertCountEquals(0)
    }

    @Test
    fun badge_absentForNegative() {
        setBottomBar(unreadCount = -3)
        composeTestRule.onAllNodesWithText("0").assertCountEquals(0)
        composeTestRule
            .onAllNodesWithContentDescription("Inbox, -3 unread messages")
            .assertCountEquals(0)
    }

    @Test
    fun badge_showsOneToNinetyNine() {
        setBottomBar(unreadCount = 2)
        composeTestRule.onNodeWithText("2").assertIsDisplayed()
        composeTestRule
            .onNodeWithContentDescription("Inbox, 2 unread messages")
            .assertIsDisplayed()
    }

    @Test
    fun badge_shows99PlusAboveNinetyNine() {
        setBottomBar(unreadCount = 150)
        composeTestRule.onNodeWithText("99+").assertIsDisplayed()
        composeTestRule
            .onNodeWithContentDescription("Inbox, 150 unread messages")
            .assertIsDisplayed()
    }

    @Test
    fun profileFallback_whenAvatarUnavailable() {
        setBottomBar(selected = AskITDestination.Profile, avatarUrl = null)
        composeTestRule.onNodeWithContentDescription("Profile").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Profile").assertIsSelected()
    }

    @Test
    fun profileSelected_withAvatar_isSelectedAndClickable() {
        setBottomBar(
            selected = AskITDestination.Profile,
            avatarUrl = "file:///android_asset/missing.png",
        )
        composeTestRule.onNodeWithContentDescription("Profile").assertIsSelected()
        composeTestRule.onNodeWithContentDescription("Profile").assertHasClickAction()
    }

    @Test
    fun everyControl_remainsClickable() {
        setBottomBar()
        listOf("Home", "Explore", "Create", "Inbox", "Profile").forEach { name ->
            composeTestRule.onNodeWithContentDescription(name).assertHasClickAction()
        }
    }

    @Test
    fun createSheet_actionsAndDismiss() {
        var dismissed = false
        val actions = mutableListOf<AskITCreateAction>()
        composeTestRule.setContent {
            AskITTheme(darkTheme = false) {
                AskITCreateSheet(
                    onDismiss = { dismissed = true },
                    onActionClick = { actions += it },
                )
            }
        }

        composeTestRule.onNodeWithText("Post a task").assertIsDisplayed()
        composeTestRule.onNodeWithText("Add a service").assertIsDisplayed()
        composeTestRule.onNodeWithText("Share your work").assertIsDisplayed()

        composeTestRule.onNodeWithText("Post a task").performClick()
        assertTrue(dismissed)
        assertEquals(listOf(AskITCreateAction.PostTask), actions)
    }

    private fun setBottomBar(
        selected: AskITDestination = AskITDestination.Home,
        avatarUrl: String? = null,
        unreadCount: Int = 0,
        onDestinationClick: (AskITDestination) -> Unit = {},
        onCreateClick: () -> Unit = {},
    ) {
        composeTestRule.setContent {
            AskITTheme(darkTheme = false) {
                AskITBottomBar(
                    selectedDestination = selected,
                    avatarUrl = avatarUrl,
                    unreadCount = unreadCount,
                    onDestinationClick = onDestinationClick,
                    onCreateClick = onCreateClick,
                )
            }
        }
    }
}
