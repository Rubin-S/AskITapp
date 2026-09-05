package com.askit.app.profile.e2e

import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
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
 * Tier 4: Real-World Scenarios Test Suite
 *
 * Implements the 5 realistic application scenarios defined in TEST_INFRA.md:
 * 1. Community member (Form A) signs up, views own profile, audits with "View as Public", exits preview
 * 2. Visitor arrives at Form A community member profile, verifies no Request Service button, taps follow
 * 3. Form A member completes Form B service listing, profile dynamically updates to provider layout
 * 4. Visitor browses Form B provider, inspects rating, completed jobs, reviews tab, and taps Request Service
 * 5. Form B provider audits storefront via "View as Public" to verify trade badge and service listing appearance before exiting
 *
 * Exactly 5 end-to-end user journeys.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w360dp-h800dp-normal-long-notround-any-xxxhdpi-keyshidden-nonav")
class Tier4RealWorldScenarioTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun scenario1_communityMember_formA_auditsStorefrontWithViewAsPublic_andExits() {
        val isPreview = mutableStateOf(false)
        val editProfileTriggered = mutableStateOf(false)

        composeTestRule.setContent {
            AskITTheme {
                ProfileE2ETestScaffold(
                    state = ProfileE2EState(
                        isOwner = true,
                        isPublicPreview = isPreview.value,
                        isProvider = false,
                        displayName = "Ananya Sharma",
                        localityLine = "Adyar, Chennai · Joined 2024",
                        bio = "Community member active in local neighborhood book exchanges and carpools.",
                        activityCount = 8,
                        followerCount = 42,
                        followingCount = 19,
                    ),
                    actions = ProfileE2EActions(
                        onEditProfile = { editProfileTriggered.value = true },
                        onViewAsPublic = { isPreview.value = true },
                        onExitPreview = { isPreview.value = false },
                    ),
                )
            }
        }

        // 1. Verify owner spatial order
        val coverY = composeTestRule.onNodeWithTag("profile_cover").getUnclippedBoundsInRoot().top
        val avatarY = composeTestRule.onNodeWithTag("profile_avatar").getUnclippedBoundsInRoot().top
        val identityY = composeTestRule.onNodeWithTag("profile_identity_block").getUnclippedBoundsInRoot().top
        val metricsY = composeTestRule.onNodeWithTag("profile_metrics_bar").getUnclippedBoundsInRoot().top
        val actionsY = composeTestRule.onNodeWithTag("profile_action_row").getUnclippedBoundsInRoot().top
        val tabsY = composeTestRule.onNodeWithTag("profile_tabs").getUnclippedBoundsInRoot().top

        assertTrue("Cover <= Avatar", coverY <= avatarY)
        assertTrue("Avatar < Identity", avatarY < identityY)
        assertTrue("Identity < Metrics", identityY < metricsY)
        assertTrue("Metrics < Actions", metricsY < actionsY)
        assertTrue("Actions < Tabs", actionsY < tabsY)

        // 2. Owner controls and motivational banner are visible
        composeTestRule.onNodeWithTag("profile_action_edit").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_action_view_as_public").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_complete_form_b_banner").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_camera").assertIsDisplayed()

        // 3. Owner audits profile via "View as Public"
        composeTestRule.onNodeWithTag("profile_action_view_as_public").performClick()
        composeTestRule.waitForIdle()

        // 4. In Preview Mode: Floating exit banner is visible, camera affordance hidden
        composeTestRule.onNodeWithTag("profile_preview_banner").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_camera").assertDoesNotExist()

        // 5. Visitor actions shown, Form A visitor protection verified
        composeTestRule.onNodeWithTag("profile_action_message").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_action_follow").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_action_request_service").assertDoesNotExist()
        composeTestRule.onNodeWithTag("profile_tab_services").assertDoesNotExist()

        // 6. Tapping Exit Preview restores owner mode
        composeTestRule.onNodeWithTag("profile_exit_preview").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("profile_preview_banner").assertDoesNotExist()
        composeTestRule.onNodeWithTag("profile_action_edit").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_camera").assertIsDisplayed()
        assertFalse(isPreview.value)
    }

    @Test
    fun scenario2_visitorArrivesAtFormAMemberProfile_verifiesProtectionAndFollows() {
        val isFollowing = mutableStateOf(false)
        val followerCount = mutableIntStateOf(120)

        composeTestRule.setContent {
            AskITTheme {
                ProfileE2ETestScaffold(
                    state = ProfileE2EState(
                        isOwner = false,
                        isProvider = false,
                        displayName = "Vikram Raman",
                        localityLine = "Mylapore, Chennai · Joined 2023",
                        bio = "Avid gardener and volunteer coordinator.",
                        activityCount = 22,
                        followerCount = followerCount.intValue,
                        followingCount = 65,
                        isFollowing = isFollowing.value,
                    ),
                    actions = ProfileE2EActions(
                        onToggleFollow = {
                            isFollowing.value = !isFollowing.value
                            if (isFollowing.value) followerCount.intValue += 1 else followerCount.intValue -= 1
                        },
                    ),
                )
            }
        }

        // 1. Verify Visitor view without owner controls
        composeTestRule.onNodeWithTag("profile_action_edit").assertDoesNotExist()
        composeTestRule.onNodeWithTag("profile_action_view_as_public").assertDoesNotExist()
        composeTestRule.onNodeWithTag("profile_complete_form_b_banner").assertDoesNotExist()
        composeTestRule.onNodeWithTag("profile_camera").assertDoesNotExist()

        // 2. Verify Form A Visitor Protection
        composeTestRule.onNodeWithTag("profile_action_request_service").assertDoesNotExist()
        composeTestRule.onNodeWithTag("profile_tab_services").assertDoesNotExist()
        composeTestRule.onNodeWithTag("profile_tab_gallery").assertDoesNotExist()

        // 3. Verify Community Metrics (Activity, Followers, Following)
        composeTestRule.onNodeWithTag("profile_metric_activity").assertIsDisplayed()
        composeTestRule.onNodeWithText("22").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_metric_followers").assertIsDisplayed()
        composeTestRule.onNodeWithText("120").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_metric_following").assertIsDisplayed()
        composeTestRule.onNodeWithText("65").assertIsDisplayed()

        // 4. Tap "Follow"
        composeTestRule.onNodeWithTag("profile_action_follow").assertTextContains("Follow")
        composeTestRule.onNodeWithTag("profile_action_follow").performClick()
        composeTestRule.waitForIdle()

        // 5. Follow state updated
        assertTrue(isFollowing.value)
        composeTestRule.onNodeWithTag("profile_action_follow").assertTextContains("Following")
        composeTestRule.onNodeWithText("121").assertIsDisplayed()
    }

    @Test
    fun scenario3_formAMemberCompletesFormB_dynamicProfileUpgrade() {
        val isProvider = mutableStateOf(false)
        val selectedTab = mutableIntStateOf(0)

        composeTestRule.setContent {
            AskITTheme {
                ProfileE2ETestScaffold(
                    state = ProfileE2EState(
                        isOwner = true,
                        isProvider = isProvider.value,
                        displayName = "Suresh Kumar",
                        localityLine = "T. Nagar, Chennai · Joined 2022",
                        tradeHeadline = if (isProvider.value) "Master Plumber" else null,
                        isVerified = isProvider.value,
                        bio = "Plumbing specialist for pipe leak detection and drainage unclogging.",
                        activityCount = 30,
                        rating = if (isProvider.value) 4.8 else null,
                        completedJobsCount = if (isProvider.value) 62 else null,
                        followerCount = 140,
                        followingCount = 50,
                        selectedTabIndex = selectedTab.intValue,
                    ),
                    actions = ProfileE2EActions(
                        onCompleteFormB = { isProvider.value = true },
                        onTabSelected = { selectedTab.intValue = it },
                    ),
                )
            }
        }

        // 1. Initial State: Form A Member
        composeTestRule.onNodeWithTag("profile_metric_activity").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_complete_form_b_banner").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_tab_activity").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_verified_badge").assertDoesNotExist()

        // 2. User completes Form B service listing
        composeTestRule.onNodeWithTag("profile_form_b_cta").performClick()
        composeTestRule.waitForIdle()

        // 3. Verify upgraded Form B Provider layout
        assertTrue(isProvider.value)
        composeTestRule.onNodeWithTag("profile_verified_badge").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_trade_headline").assertTextContains("Master Plumber")

        // 4. Metrics transitioned to Provider stats
        composeTestRule.onNodeWithTag("profile_metric_activity").assertDoesNotExist()
        composeTestRule.onNodeWithTag("profile_metric_rating").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_metric_jobs").assertIsDisplayed()
        composeTestRule.onNodeWithText("62").assertIsDisplayed()

        // 5. Tabs transitioned to Provider tabs
        composeTestRule.onNodeWithTag("profile_tab_services").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_tab_gallery").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_complete_form_b_banner").assertDoesNotExist()
    }

    @Test
    fun scenario4_visitorBrowsesFormBProvider_inspectsTrustSignals_andRequestsService() {
        var serviceRequested = false
        val selectedTab = mutableIntStateOf(0)

        composeTestRule.setContent {
            AskITTheme {
                ProfileE2ETestScaffold(
                    state = ProfileE2EState(
                        isOwner = false,
                        isProvider = true,
                        displayName = "Priya Sharma",
                        tradeHeadline = "Certified Electrician",
                        isVerified = true,
                        rating = 4.9,
                        completedJobsCount = 48,
                        localityLine = "1.2 km away · Member since 2022",
                        bio = "Certified electrician specializing in inverter repairs and wiring safety audits.",
                        selectedTabIndex = selectedTab.intValue,
                    ),
                    actions = ProfileE2EActions(
                        onRequestService = { serviceRequested = true },
                        onTabSelected = { selectedTab.intValue = it },
                    ),
                )
            }
        }

        // 1. Trust signals prominently displayed
        composeTestRule.onNodeWithTag("profile_verified_badge").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_trade_headline").assertTextContains("Certified Electrician")
        composeTestRule.onNodeWithTag("profile_metric_rating").assertIsDisplayed()
        composeTestRule.onNodeWithText("★ 4.9").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_metric_jobs").assertIsDisplayed()
        composeTestRule.onNodeWithText("48").assertIsDisplayed()

        // 2. Browse to Reviews tab to inspect feedback
        composeTestRule.onNodeWithTag("profile_tab_reviews").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("profile_content_reviews").assertExists()

        // 3. Tap "Request Service"
        composeTestRule.onNodeWithTag("profile_action_request_service").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_action_request_service").performClick()
        composeTestRule.waitForIdle()

        assertTrue("Service request callback must be dispatched", serviceRequested)
    }

    @Test
    fun scenario5_formBProviderAuditsPublicStorefront_verifiesBadgesAndServices_beforeExiting() {
        val isPreview = mutableStateOf(false)
        val selectedTab = mutableIntStateOf(0)

        composeTestRule.setContent {
            AskITTheme {
                ProfileE2ETestScaffold(
                    state = ProfileE2EState(
                        isOwner = true,
                        isPublicPreview = isPreview.value,
                        isProvider = true,
                        displayName = "Karthik Raja",
                        tradeHeadline = "AC Technician",
                        isVerified = true,
                        rating = 4.7,
                        completedJobsCount = 35,
                        localityLine = "Anna Nagar, Chennai · Joined 2023",
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

        // 1. Provider starts in owner mode
        composeTestRule.onNodeWithTag("profile_action_view_as_public").assertIsDisplayed()

        // 2. Tap "View as Public"
        composeTestRule.onNodeWithTag("profile_action_view_as_public").performClick()
        composeTestRule.waitForIdle()

        // 3. Verify public visitor preview is rendered
        assertTrue(isPreview.value)
        composeTestRule.onNodeWithTag("profile_preview_banner").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_action_request_service").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_verified_badge").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_trade_headline").assertTextContains("AC Technician")

        // 4. Audit Showcase tab
        composeTestRule.onNodeWithTag("profile_tab_gallery").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("profile_content_gallery").assertExists()

        // 5. Audit completed, exit preview
        composeTestRule.onNodeWithTag("profile_exit_preview").performClick()
        composeTestRule.waitForIdle()

        // 6. Owner mode restored
        assertFalse(isPreview.value)
        composeTestRule.onNodeWithTag("profile_preview_banner").assertDoesNotExist()
        composeTestRule.onNodeWithTag("profile_action_edit").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_action_share").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_action_view_as_public").assertIsDisplayed()
    }
}
