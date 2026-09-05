package com.askit.app.profile.e2e

import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.askit.designsystem.theme.AskITTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Tier 3: Pairwise Combinatorial Interactions Test Suite
 *
 * Tests combinatorial interactions across features:
 * - Interaction 1: Form A Owner + View as Public -> verifies Form A visitor rules in preview
 * - Interaction 2: Form B Provider + View as Public -> verifies Form B visitor rules in preview
 * - Interaction 3: Public Preview + Tab Selection -> verifies content updates inside preview
 * - Interaction 4: Public Preview + Follow Toggle -> verifies visitor follow state simulation
 * - Interaction 5: Form A -> Form B dynamic upgrade -> verifies simultaneous metrics and tab switches
 * - Interaction 6: Visitor Mode + Form B -> verifies browsing across all 4 provider tabs
 * - Interaction 7: Full preview lifecycle -> enter preview, interact, exit preview, verify owner restoration
 *
 * Exactly 7 test cases.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w360dp-h800dp-normal-long-notround-any-xxxhdpi-keyshidden-nonav")
class Tier3CombinatorialInteractionTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun t3_formA_owner_enteringPublicPreview_verifiesFormAVisitorProtection() {
        val isPreview = mutableStateOf(false)
        composeTestRule.setContent {
            AskITTheme {
                ProfileE2ETestScaffold(
                    state = ProfileE2EState(
                        isOwner = true,
                        isPublicPreview = isPreview.value,
                        isProvider = false,
                        activityCount = 10,
                        followerCount = 85,
                        followingCount = 30,
                    ),
                    actions = ProfileE2EActions(
                        onViewAsPublic = { isPreview.value = true },
                        onExitPreview = { isPreview.value = false },
                    ),
                )
            }
        }

        // Initially in owner mode
        composeTestRule.onNodeWithTag("profile_complete_form_b_banner").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_action_view_as_public").performClick()
        composeTestRule.waitForIdle()

        // In preview mode: Form A visitor protection applies
        composeTestRule.onNodeWithTag("profile_preview_banner").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_action_message").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_action_follow").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_action_request_service").assertDoesNotExist()
        composeTestRule.onNodeWithTag("profile_complete_form_b_banner").assertDoesNotExist()
        composeTestRule.onNodeWithTag("profile_tab_services").assertDoesNotExist()
        composeTestRule.onNodeWithTag("profile_tab_gallery").assertDoesNotExist()
    }

    @Test
    fun t3_formB_provider_enteringPublicPreview_verifiesFormBVisitorRules() {
        val isPreview = mutableStateOf(false)
        composeTestRule.setContent {
            AskITTheme {
                ProfileE2ETestScaffold(
                    state = ProfileE2EState(
                        isOwner = true,
                        isPublicPreview = isPreview.value,
                        isProvider = true,
                        tradeHeadline = "Certified Electrician",
                        isVerified = true,
                        rating = 4.9,
                        completedJobsCount = 48,
                    ),
                    actions = ProfileE2EActions(
                        onViewAsPublic = { isPreview.value = true },
                        onExitPreview = { isPreview.value = false },
                    ),
                )
            }
        }

        composeTestRule.onNodeWithTag("profile_action_view_as_public").performClick()
        composeTestRule.waitForIdle()

        // In preview mode: Form B provider storefront rendered
        composeTestRule.onNodeWithTag("profile_preview_banner").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_verified_badge").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_trade_headline").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_action_message").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_action_follow").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_action_request_service").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_tab_services").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_tab_gallery").assertIsDisplayed()
    }

    @Test
    fun t3_tabSwitching_insidePublicPreview_updatesContentSeamlessly() {
        val activeTabIndex = mutableIntStateOf(0)
        composeTestRule.setContent {
            AskITTheme {
                ProfileE2ETestScaffold(
                    state = ProfileE2EState(
                        isOwner = true,
                        isPublicPreview = true,
                        isProvider = true,
                        selectedTabIndex = activeTabIndex.intValue,
                    ),
                    actions = ProfileE2EActions(
                        onTabSelected = { activeTabIndex.intValue = it },
                    ),
                )
            }
        }

        // Initially Services tab
        composeTestRule.onNodeWithTag("profile_content_services").assertExists()

        // Tap Showcase (Gallery) tab
        composeTestRule.onNodeWithTag("profile_tab_gallery").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("profile_content_gallery").assertExists()

        // Floating banner remains visible throughout tab navigation
        composeTestRule.onNodeWithTag("profile_preview_banner").assertIsDisplayed()
    }

    @Test
    fun t3_followToggle_insidePublicPreview_updatesFollowButtonText() {
        val isFollowing = mutableStateOf(false)
        composeTestRule.setContent {
            AskITTheme {
                ProfileE2ETestScaffold(
                    state = ProfileE2EState(
                        isOwner = true,
                        isPublicPreview = true,
                        isFollowing = isFollowing.value,
                    ),
                    actions = ProfileE2EActions(
                        onToggleFollow = { isFollowing.value = !isFollowing.value },
                    ),
                )
            }
        }

        composeTestRule.onNodeWithTag("profile_action_follow").assertTextContains("Follow")
        composeTestRule.onNodeWithTag("profile_action_follow").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("profile_action_follow").assertTextContains("Following")
    }

    @Test
    fun t3_formA_to_formB_dynamicUpgrade_switchesMetricsAndTabs() {
        val isProvider = mutableStateOf(false)
        composeTestRule.setContent {
            AskITTheme {
                ProfileE2ETestScaffold(
                    state = ProfileE2EState(
                        isOwner = true,
                        isProvider = isProvider.value,
                        activityCount = 12,
                        rating = if (isProvider.value) 5.0 else null,
                        completedJobsCount = if (isProvider.value) 1 else null,
                        tradeHeadline = if (isProvider.value) "Licensed Plumber" else null,
                        isVerified = isProvider.value,
                    ),
                    actions = ProfileE2EActions(
                        onCompleteFormB = { isProvider.value = true },
                    ),
                )
            }
        }

        // Form A initial checks
        composeTestRule.onNodeWithTag("profile_metric_activity").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_complete_form_b_banner").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_tab_activity").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_tab_services").assertDoesNotExist()

        // Complete Form B upgrade
        composeTestRule.onNodeWithTag("profile_form_b_cta").performClick()
        composeTestRule.waitForIdle()

        // Form B updated checks
        composeTestRule.onNodeWithTag("profile_metric_activity").assertDoesNotExist()
        composeTestRule.onNodeWithTag("profile_metric_rating").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_metric_jobs").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_complete_form_b_banner").assertDoesNotExist()
        composeTestRule.onNodeWithTag("profile_tab_services").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_tab_gallery").assertIsDisplayed()
    }

    @Test
    fun t3_formB_visitor_switchingTabs_inspectsServicesAndReviews() {
        val selectedTab = mutableIntStateOf(0)
        composeTestRule.setContent {
            AskITTheme {
                ProfileE2ETestScaffold(
                    state = ProfileE2EState(
                        isOwner = false,
                        isProvider = true,
                        selectedTabIndex = selectedTab.intValue,
                    ),
                    actions = ProfileE2EActions(
                        onTabSelected = { selectedTab.intValue = it },
                    ),
                )
            }
        }

        // Tab 0: Services
        composeTestRule.onNodeWithTag("profile_content_services").assertExists()

        // Tab 1: Gallery
        composeTestRule.onNodeWithTag("profile_tab_gallery").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("profile_content_gallery").assertExists()

        // Tab 2: Reviews
        composeTestRule.onNodeWithTag("profile_tab_reviews").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("profile_content_reviews").assertExists()

        // Tab 3: About
        composeTestRule.onNodeWithTag("profile_tab_about").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("profile_content_about").assertExists()
    }

    @Test
    fun t3_previewExit_afterInteractions_restoresOriginalOwnerState() {
        val isPreview = mutableStateOf(false)
        val selectedTab = mutableIntStateOf(1)
        composeTestRule.setContent {
            AskITTheme {
                ProfileE2ETestScaffold(
                    state = ProfileE2EState(
                        isOwner = true,
                        isPublicPreview = isPreview.value,
                        selectedTabIndex = selectedTab.intValue,
                    ),
                    actions = ProfileE2EActions(
                        onViewAsPublic = { isPreview.value = true },
                        onExitPreview = { isPreview.value = false },
                        onTabSelected = { selectedTab.intValue = it },
                    ),
                )
            }
        }

        // 1. Enter preview
        composeTestRule.onNodeWithTag("profile_action_view_as_public").performClick()
        composeTestRule.waitForIdle()
        assertTrue(isPreview.value)

        // 2. Interact inside preview (switch tab)
        composeTestRule.onNodeWithTag("profile_tab_reviews").performClick()
        composeTestRule.waitForIdle()
        assertEquals(2, selectedTab.intValue)

        // 3. Exit preview
        composeTestRule.onNodeWithTag("profile_exit_preview").performClick()
        composeTestRule.waitForIdle()
        assertFalse(isPreview.value)

        // 4. Verify owner state restored
        composeTestRule.onNodeWithTag("profile_action_edit").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_action_share").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_action_view_as_public").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_preview_banner").assertDoesNotExist()
    }
}
