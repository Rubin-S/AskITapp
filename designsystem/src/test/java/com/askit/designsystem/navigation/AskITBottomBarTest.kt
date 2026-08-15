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
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Density
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
        composeTestRule.onNodeWithContentDescription("Messages").performClick()
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
    fun destinations_hideVisibleLabels() {
        setBottomBar(selected = AskITDestination.Home)
        listOf("Home", "Explore", "Messages", "Profile").forEach { name ->
            composeTestRule.onAllNodesWithText(name).assertCountEquals(0)
            composeTestRule.onNodeWithContentDescription(name).assertIsDisplayed()
        }
    }

    @Test
    fun longPress_showsEachTooltip() {
        setBottomBar()
        listOf("Home", "Explore", "Create", "Messages", "Profile").forEach { name ->
            composeTestRule
                .onNodeWithContentDescription(name)
                .performTouchInput { longClick() }
            composeTestRule.onAllNodesWithText(name)[0].assertIsDisplayed()
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
            .onAllNodesWithContentDescription("Messages, -3 unread messages")
            .assertCountEquals(0)
    }

    @Test
    fun badge_showsOneToNinetyNine() {
        setBottomBar(unreadCount = 2)
        composeTestRule.onAllNodesWithText("2", useUnmergedTree = true).assertCountEquals(1)
        composeTestRule
            .onNodeWithContentDescription("Messages, 2 unread messages")
            .assertIsDisplayed()
    }

    @Test
    fun badge_shows99PlusAboveNinetyNine() {
        setBottomBar(unreadCount = 150)
        composeTestRule.onAllNodesWithText("99+", useUnmergedTree = true).assertCountEquals(1)
        composeTestRule
            .onNodeWithContentDescription("Messages, 150 unread messages")
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
        listOf("Home", "Explore", "Create", "Messages", "Profile").forEach { name ->
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

        composeTestRule.onNodeWithText("Create").assertIsDisplayed()
        composeTestRule.onNodeWithText("Post a task").assertIsDisplayed()
        composeTestRule.onNodeWithText("List a service").assertIsDisplayed()
        composeTestRule.onNodeWithText("Create a post").assertIsDisplayed()
        listOf(
            "Find someone to help with a job or problem",
            "Offer a service to customers",
            "Share an update, photo, carousel, before/after, or poll",
        ).forEach { supportingCopy ->
            composeTestRule.onNodeWithText(supportingCopy).assertIsDisplayed()
        }

        val actionTitles = listOf("Post a task", "List a service", "Create a post")
        actionTitles.forEach { title ->
            composeTestRule.onNodeWithText(title).assertHasClickAction()
        }
        composeTestRule.onAllNodes(
            SemanticsMatcher("Create action button") { node ->
                node.config.contains(SemanticsProperties.Role) &&
                    node.config[SemanticsProperties.Role] == Role.Button
            },
            useUnmergedTree = true,
        ).assertCountEquals(3)
        val actionPositions = actionTitles.map { title ->
            composeTestRule.onNodeWithText(title).fetchSemanticsNode().boundsInRoot.top
        }
        assertTrue(actionPositions[0] < actionPositions[1])
        assertTrue(actionPositions[1] < actionPositions[2])
        assertTrue(
            composeTestRule
                .onNodeWithText("Create")
                .fetchSemanticsNode()
                .config
                .contains(SemanticsProperties.Heading),
        )

        composeTestRule.onNodeWithText("Post a task").performClick()
        assertTrue(dismissed)
        assertEquals(listOf(AskITCreateAction.PostTask), actions)
    }

    @Test
    @Config(qualifiers = "ta-rIN-w360dp-h800dp-normal-long-notround-any-xxxhdpi-keyshidden-nonav")
    fun createSheet_tamilUsesLocalizedHeadingActionsAndCopy() {
        composeTestRule.setContent {
            AskITTheme(darkTheme = false) {
                AskITCreateSheet(onDismiss = {}, onActionClick = {})
            }
        }

        listOf(
            "உருவாக்கு",
            "பணியைப் பதிவிடு",
            "சேவையைப் பட்டியலிடு",
            "பதிவை உருவாக்கு",
            "ஒரு வேலை அல்லது சிக்கலுக்கு உதவ ஒருவரைக் கண்டுபிடிக்கவும்",
            "வாடிக்கையாளர்களுக்கு உங்கள் சேவையை வழங்குங்கள்",
            "புதுப்பிப்பு, புகைப்படம், கருசல், முன்/பின் ஒப்பீடு அல்லது வாக்கெடுப்பைப் பகிரவும்",
        ).forEach { text ->
            composeTestRule.onNodeWithText(text).assertIsDisplayed()
        }
    }

    @Test
    @Config(qualifiers = "w320dp-h360dp-normal-long-notround-any-xxxhdpi-keyshidden-nonav")
    fun createSheet_largeTextKeepsAllActionsReachableInCompactHeight() {
        val fontScale = mutableStateOf(1f)
        composeTestRule.setContent {
            AskITTheme(darkTheme = false) {
                val currentDensity = LocalDensity.current
                CompositionLocalProvider(
                    LocalDensity provides Density(currentDensity.density, fontScale.value),
                ) {
                    AskITCreateSheet(onDismiss = {}, onActionClick = {})
                }
            }
        }

        listOf(1f, 1.3f, 1.5f, 2f).forEach { scale ->
            composeTestRule.runOnUiThread { fontScale.value = scale }
            composeTestRule.waitForIdle()
            listOf("Post a task", "List a service", "Create a post").forEach { title ->
                composeTestRule
                    .onNodeWithText(title)
                    .performScrollTo()
                    .assertIsDisplayed()
                    .assertHasClickAction()
            }
        }
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
