package com.askit.app.profile

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.askit.app.home.details.UserProfileScreen
import com.askit.designsystem.theme.AskITTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Contract test suite for [UserProfileScreen] (visitor view).
 * Verifies Form A vs Form B dynamic metrics, tabs, actions, and visitor protections.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w360dp-h800dp-normal-long-notround-any-xxxhdpi-keyshidden-nonav")
class UserProfileScreenContractTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun visitorFormA_showsThreeMetricsAndThreeTabs_noRequestServiceButton() {
        composeTestRule.setContent {
            AskITTheme {
                UserProfileScreen(
                    userId = "member-1",
                    onBack = {},
                )
            }
        }

        // Metrics: exactly 3 (Activity, Followers, Following)
        composeTestRule.onNodeWithTag("profile_metric_activity").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_metric_followers").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_metric_following").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_metric_rating").assertDoesNotExist()
        composeTestRule.onNodeWithTag("profile_metric_completed_jobs").assertDoesNotExist()

        // Tabs: exactly 3 (Activity, About, Reviews)
        composeTestRule.onNodeWithTag("profile_tab_activity").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_tab_about").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_tab_reviews").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_tab_services").assertDoesNotExist()
        composeTestRule.onNodeWithTag("profile_tab_gallery").assertDoesNotExist()

        // Visitor actions
        composeTestRule.onNodeWithTag("profile_action_message").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_action_follow").assertIsDisplayed()
        // Strict visitor protection: no Request Service CTA for Form A
        composeTestRule.onNodeWithTag("profile_action_request_service").assertDoesNotExist()
        composeTestRule.onNodeWithTag("profile_action_edit").assertDoesNotExist()
        composeTestRule.onNodeWithTag("profile_action_view_as_public").assertDoesNotExist()

        // Owner affordances hidden
        composeTestRule.onNodeWithTag("profile_camera").assertDoesNotExist()
        composeTestRule.onNodeWithTag("profile_complete_form_b_banner").assertDoesNotExist()
        composeTestRule.onNodeWithTag("profile_preview_banner").assertDoesNotExist()
    }

    @Test
    fun visitorFormB_showsFourMetricsAndFourTabs_andRequestServiceButton() {
        composeTestRule.setContent {
            AskITTheme {
                UserProfileScreen(
                    userId = "pro-1",
                    onBack = {},
                )
            }
        }

        // Metrics: exactly 4 (Rating, Completed, Followers, Following)
        composeTestRule.onNodeWithTag("profile_metric_rating").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_metric_completed_jobs").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_metric_followers").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_metric_following").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_metric_activity").assertDoesNotExist()

        // Tabs: exactly 4 (Services, Showcase/Gallery, Reviews, About)
        composeTestRule.onNodeWithTag("profile_tab_services").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_tab_gallery").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_tab_reviews").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_tab_about").assertIsDisplayed()

        // Visitor actions include contextual Request Service for Form B
        composeTestRule.onNodeWithTag("profile_action_message").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_action_follow").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_action_request_service").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_action_edit").assertDoesNotExist()
        composeTestRule.onNodeWithTag("profile_action_view_as_public").assertDoesNotExist()

        // Owner affordances hidden
        composeTestRule.onNodeWithTag("profile_camera").assertDoesNotExist()
        composeTestRule.onNodeWithTag("profile_complete_form_b_banner").assertDoesNotExist()
        composeTestRule.onNodeWithTag("profile_preview_banner").assertDoesNotExist()
    }

    @Test
    fun visitorSpatialOrdering_verifiesUniversalOrderOnUserProfileScreen() {
        composeTestRule.setContent {
            AskITTheme {
                UserProfileScreen(
                    userId = "pro-1",
                    onBack = {},
                )
            }
        }

        val coverY = composeTestRule.onNodeWithTag("profile_cover").getUnclippedBoundsInRoot().top
        val avatarY = composeTestRule.onNodeWithTag("profile_avatar").getUnclippedBoundsInRoot().top
        val identityY = composeTestRule.onNodeWithTag("profile_identity_block").getUnclippedBoundsInRoot().top
        val metricsY = composeTestRule.onNodeWithTag("profile_metrics_bar").getUnclippedBoundsInRoot().top
        val actionsY = composeTestRule.onNodeWithTag("profile_action_row").getUnclippedBoundsInRoot().top
        val tabsY = composeTestRule.onNodeWithTag("profile_tabs").getUnclippedBoundsInRoot().top
        val contentY = composeTestRule.onNodeWithTag("profile_content_section").getUnclippedBoundsInRoot().top

        assertTrue("Cover <= Avatar", coverY <= avatarY)
        assertTrue("Avatar < Identity", avatarY < identityY)
        assertTrue("Identity < Metrics", identityY < metricsY)
        assertTrue("Metrics < Actions", metricsY < actionsY)
        assertTrue("Actions < Tabs", actionsY < tabsY)
        assertTrue("Tabs < Content", tabsY < contentY)

        composeTestRule.onNodeWithTag("profile_complete_form_b_banner").assertDoesNotExist()
    }

    @Test
    fun visitorInteractions_messageAndRequestService_invokeCallbacks() {
        var messageClicked = false
        var requestServiceClicked = false

        composeTestRule.setContent {
            AskITTheme {
                UserProfileScreen(
                    userId = "pro-1",
                    onBack = {},
                    onMessage = { messageClicked = true },
                    onRequestService = { requestServiceClicked = true },
                )
            }
        }

        composeTestRule.onNodeWithTag("profile_action_message").performClick()
        assertTrue("Message callback must be invoked", messageClicked)

        composeTestRule.onNodeWithTag("profile_action_request_service").performClick()
        assertTrue("Request Service callback must be invoked", requestServiceClicked)
    }

    @Test
    fun visitorInteractions_toggleFollow_updatesFollowState() {
        composeTestRule.setContent {
            AskITTheme {
                UserProfileScreen(
                    userId = "member-1",
                    onBack = {},
                )
            }
        }

        composeTestRule.onNode(hasTestTag("profile_action_follow") and hasText("Follow")).assertIsDisplayed()

        composeTestRule.onNodeWithTag("profile_action_follow").performClick()
        composeTestRule.onNode(hasTestTag("profile_action_follow") and hasText("Following")).assertIsDisplayed()

        composeTestRule.onNodeWithTag("profile_action_follow").performClick()
        composeTestRule.onNode(hasTestTag("profile_action_follow") and hasText("Follow")).assertIsDisplayed()
    }

    @Test
    fun visitorTopBar_backButton_invokesOnBack() {
        var backClicked = false

        composeTestRule.setContent {
            AskITTheme {
                UserProfileScreen(
                    userId = "member-1",
                    onBack = { backClicked = true },
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Back").performClick()
        assertTrue("Back callback must be invoked", backClicked)
    }
}
