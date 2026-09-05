package com.askit.designsystem.profile

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.askit.designsystem.theme.AskITTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ProfileActionRowTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun ownerConfig_rendersEditShareViewAsPublic_andInvokesCallbacks() {
        var editClicked = false
        var shareClicked = false
        var previewClicked = false

        val config = ProfileActionConfig.Owner(
            onEditProfile = { editClicked = true },
            onShare = { shareClicked = true },
            onViewAsPublic = { previewClicked = true },
        )

        composeTestRule.setContent {
            AskITTheme {
                ProfileActionRow(config = config)
            }
        }

        composeTestRule.onNodeWithTag("profile_action_edit").assertIsDisplayed().performClick()
        assertEquals(true, editClicked)

        composeTestRule.onNodeWithTag("profile_action_share").assertIsDisplayed().performClick()
        assertEquals(true, shareClicked)

        composeTestRule.onNodeWithTag("profile_action_view_as_public").assertIsDisplayed().performClick()
        assertEquals(true, previewClicked)

        // Visitor buttons must NOT exist
        composeTestRule.onNodeWithTag("profile_action_message").assertDoesNotExist()
        composeTestRule.onNodeWithTag("profile_action_follow").assertDoesNotExist()
        composeTestRule.onNodeWithTag("profile_action_request_service").assertDoesNotExist()
    }

    @Test
    fun visitorFormA_rendersMessageAndFollow_omitsRequestService() {
        var messageClicked = false
        var followClicked = false

        val config = ProfileActionConfig.Visitor(
            onMessage = { messageClicked = true },
            isFollowing = false,
            onToggleFollow = { followClicked = true },
            onRequestService = null, // Form A member
        )

        composeTestRule.setContent {
            AskITTheme {
                ProfileActionRow(config = config)
            }
        }

        composeTestRule.onNodeWithTag("profile_action_message").assertIsDisplayed().performClick()
        assertEquals(true, messageClicked)

        composeTestRule.onNodeWithTag("profile_action_follow")
            .assertIsDisplayed()
            .assertTextEquals("Follow")
            .performClick()
        assertEquals(true, followClicked)

        // Request Service MUST NOT exist for Form A member
        composeTestRule.onNodeWithTag("profile_action_request_service").assertDoesNotExist()
    }

    @Test
    fun visitor_whenFollowing_displaysFollowingText() {
        val config = ProfileActionConfig.Visitor(
            onMessage = {},
            isFollowing = true,
            onToggleFollow = {},
            onRequestService = null,
        )

        composeTestRule.setContent {
            AskITTheme {
                ProfileActionRow(config = config)
            }
        }

        composeTestRule.onNodeWithTag("profile_action_follow")
            .assertIsDisplayed()
            .assertTextEquals("Following")
    }

    @Test
    fun visitorFormB_rendersRequestService_andInvokesCallback() {
        var serviceClicked = false

        val config = ProfileActionConfig.Visitor(
            onMessage = {},
            isFollowing = false,
            onToggleFollow = {},
            onRequestService = { serviceClicked = true }, // Form B provider
        )

        composeTestRule.setContent {
            AskITTheme {
                ProfileActionRow(config = config)
            }
        }

        composeTestRule.onNodeWithTag("profile_action_message").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_action_follow").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_action_request_service")
            .assertIsDisplayed()
            .performClick()
        assertEquals(true, serviceClicked)
    }

    @Test
    @Suppress("DEPRECATION")
    fun legacyOverload_rendersEditAndAvailability() {
        var editClicked = false
        var availabilityClicked = false

        composeTestRule.setContent {
            AskITTheme {
                ProfileActionRow(
                    editLabel = "Edit profile",
                    onEdit = { editClicked = true },
                    availabilityLabel = "Availability",
                    onAvailability = { availabilityClicked = true },
                )
            }
        }

        composeTestRule.onNodeWithTag("profile_edit").assertIsDisplayed().performClick()
        assertEquals(true, editClicked)

        composeTestRule.onNodeWithTag("profile_availability").assertIsDisplayed().performClick()
        assertEquals(true, availabilityClicked)
    }
}
