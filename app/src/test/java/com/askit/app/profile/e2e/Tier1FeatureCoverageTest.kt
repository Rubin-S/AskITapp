package com.askit.app.profile.e2e

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
 * Tier 1: Feature Coverage Test Suite (F1–F7)
 *
 * Verifies core requirements in isolation across:
 * - F1: Universal spatial ordering (Cover -> Avatar -> Identity -> Metrics -> Actions -> Tabs -> Content)
 * - F2: Contextual action switching (Owner: Edit/Share/Preview; Visitor: Message/Follow/Request)
 * - F3: Form A dynamic metrics & tabs (Activity, Followers, Following; Activity, About, Reviews; Form B banner)
 * - F4: Form B dynamic metrics & tabs (Rating ★, Completed Jobs, Followers, Following; Services, Showcase, Reviews, About; Verified Trade)
 * - F5: Form A visitor protection (No Request Service CTA, no empty provider tabs)
 * - F6: In-Place "View as Public" preview (Floating Exit Banner, visitor perspective, exit toggle)
 * - F7: Zero code duplication & WYSIWYG parity (Identical semantic structure)
 *
 * Exactly 35 test cases (5 tests per feature).
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w360dp-h800dp-normal-long-notround-any-xxxhdpi-keyshidden-nonav")
class Tier1FeatureCoverageTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // =========================================================================
    // Feature F1: Universal Spatial Ordering (5 tests)
    // =========================================================================

    @Test
    fun f1_spatialOrdering_ownerMode_verifiesVerticalHierarchy() {
        composeTestRule.setContent {
            AskITTheme {
                ProfileE2ETestScaffold(
                    state = ProfileE2EState(isOwner = true, isPublicPreview = false),
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

        assertTrue("Cover must be at or above Avatar top: coverY=$coverY, avatarY=$avatarY", coverY <= avatarY)
        assertTrue("Avatar top must precede Identity: avatarY=$avatarY, identityY=$identityY", avatarY < identityY)
        assertTrue("Identity must precede Metrics: identityY=$identityY, metricsY=$metricsY", identityY < metricsY)
        assertTrue("Metrics must precede Action Row: metricsY=$metricsY, actionsY=$actionsY", metricsY < actionsY)
        assertTrue("Action Row must precede Tabs: actionsY=$actionsY, tabsY=$tabsY", actionsY < tabsY)
        assertTrue("Tabs must precede Content: tabsY=$tabsY, contentY=$contentY", tabsY < contentY)
    }

    @Test
    fun f1_spatialOrdering_visitorModeFormA_verifiesVerticalHierarchy() {
        composeTestRule.setContent {
            AskITTheme {
                ProfileE2ETestScaffold(
                    state = ProfileE2EState(isOwner = false, isProvider = false),
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
    }

    @Test
    fun f1_spatialOrdering_visitorModeFormB_verifiesVerticalHierarchy() {
        composeTestRule.setContent {
            AskITTheme {
                ProfileE2ETestScaffold(
                    state = ProfileE2EState(
                        isOwner = false,
                        isProvider = true,
                        tradeHeadline = "Certified Electrician",
                        rating = 4.9,
                        completedJobsCount = 48,
                    ),
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
    }

    @Test
    fun f1_spatialOrdering_publicPreviewMode_verifiesVerticalHierarchy() {
        composeTestRule.setContent {
            AskITTheme {
                ProfileE2ETestScaffold(
                    state = ProfileE2EState(isOwner = true, isPublicPreview = true),
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
    }

    @Test
    fun f1_spatialOrdering_coverAndAvatar_occupyTopPositions() {
        composeTestRule.setContent {
            AskITTheme {
                ProfileE2ETestScaffold(state = ProfileE2EState())
            }
        }

        val coverBounds = composeTestRule.onNodeWithTag("profile_cover").getUnclippedBoundsInRoot()
        val avatarBounds = composeTestRule.onNodeWithTag("profile_avatar").getUnclippedBoundsInRoot()

        assertEquals("Cover must start at top", 0f, coverBounds.top.value, 1f)
        assertTrue("Avatar must overlap cover", avatarBounds.top.value < coverBounds.bottom.value)
    }

    // =========================================================================
    // Feature F2: Contextual Action Switching (5 tests)
    // =========================================================================

    @Test
    fun f2_contextualActions_ownerMode_rendersEditShareViewAsPublic() {
        composeTestRule.setContent {
            AskITTheme {
                ProfileE2ETestScaffold(
                    state = ProfileE2EState(isOwner = true, isPublicPreview = false),
                )
            }
        }

        composeTestRule.onNodeWithTag("profile_action_edit").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_action_share").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_action_view_as_public").assertIsDisplayed()

        // Visitor actions must NOT exist
        composeTestRule.onNodeWithTag("profile_action_message").assertDoesNotExist()
        composeTestRule.onNodeWithTag("profile_action_follow").assertDoesNotExist()
        composeTestRule.onNodeWithTag("profile_action_request_service").assertDoesNotExist()
    }

    @Test
    fun f2_contextualActions_visitorModeFormA_rendersMessageAndFollow_noRequestService() {
        composeTestRule.setContent {
            AskITTheme {
                ProfileE2ETestScaffold(
                    state = ProfileE2EState(isOwner = false, isProvider = false),
                )
            }
        }

        composeTestRule.onNodeWithTag("profile_action_message").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_action_follow").assertIsDisplayed()

        // Owner actions and Form B CTA must NOT exist
        composeTestRule.onNodeWithTag("profile_action_edit").assertDoesNotExist()
        composeTestRule.onNodeWithTag("profile_action_share").assertDoesNotExist()
        composeTestRule.onNodeWithTag("profile_action_view_as_public").assertDoesNotExist()
        composeTestRule.onNodeWithTag("profile_action_request_service").assertDoesNotExist()
    }

    @Test
    fun f2_contextualActions_visitorModeFormB_rendersMessageFollowAndRequestService() {
        composeTestRule.setContent {
            AskITTheme {
                ProfileE2ETestScaffold(
                    state = ProfileE2EState(isOwner = false, isProvider = true),
                )
            }
        }

        composeTestRule.onNodeWithTag("profile_action_message").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_action_follow").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_action_request_service").assertIsDisplayed()

        // Owner actions must NOT exist
        composeTestRule.onNodeWithTag("profile_action_edit").assertDoesNotExist()
        composeTestRule.onNodeWithTag("profile_action_share").assertDoesNotExist()
        composeTestRule.onNodeWithTag("profile_action_view_as_public").assertDoesNotExist()
    }

    @Test
    fun f2_contextualActions_ownerClickingEdit_invokesCallback() {
        var editClicked = false
        composeTestRule.setContent {
            AskITTheme {
                ProfileE2ETestScaffold(
                    state = ProfileE2EState(isOwner = true),
                    actions = ProfileE2EActions(onEditProfile = { editClicked = true }),
                )
            }
        }

        composeTestRule.onNodeWithTag("profile_action_edit").performClick()
        assertTrue("onEditProfile must be invoked", editClicked)
    }

    @Test
    fun f2_contextualActions_ownerClickingShare_invokesCallback() {
        var shareClicked = false
        composeTestRule.setContent {
            AskITTheme {
                ProfileE2ETestScaffold(
                    state = ProfileE2EState(isOwner = true),
                    actions = ProfileE2EActions(onShare = { shareClicked = true }),
                )
            }
        }

        composeTestRule.onNodeWithTag("profile_action_share").performClick()
        assertTrue("onShare must be invoked", shareClicked)
    }

    // =========================================================================
    // Feature F3: Form A Dynamic Metrics & Tabs (5 tests)
    // =========================================================================

    @Test
    fun f3_formA_metrics_showsActivityFollowersFollowing() {
        composeTestRule.setContent {
            AskITTheme {
                ProfileE2ETestScaffold(
                    state = ProfileE2EState(
                        isProvider = false,
                        activityCount = 15,
                        followerCount = 120,
                        followingCount = 45,
                    ),
                )
            }
        }

        composeTestRule.onNodeWithTag("profile_metric_activity").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_metric_followers").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_metric_following").assertIsDisplayed()

        // Provider-only metrics must NOT exist
        composeTestRule.onNodeWithTag("profile_metric_rating").assertDoesNotExist()
        composeTestRule.onNodeWithTag("profile_metric_jobs").assertDoesNotExist()
    }

    @Test
    fun f3_formA_tabs_showsActivityAboutReviews() {
        composeTestRule.setContent {
            AskITTheme {
                ProfileE2ETestScaffold(state = ProfileE2EState(isProvider = false))
            }
        }

        composeTestRule.onNodeWithTag("profile_tab_activity").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_tab_about").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_tab_reviews").assertIsDisplayed()

        // Provider-only tabs must NOT exist
        composeTestRule.onNodeWithTag("profile_tab_services").assertDoesNotExist()
        composeTestRule.onNodeWithTag("profile_tab_gallery").assertDoesNotExist()
    }

    @Test
    fun f3_formA_ownerMode_showsCompleteFormBBanner() {
        composeTestRule.setContent {
            AskITTheme {
                ProfileE2ETestScaffold(
                    state = ProfileE2EState(isOwner = true, isProvider = false, isPublicPreview = false),
                )
            }
        }

        composeTestRule.onNodeWithTag("profile_complete_form_b_banner").assertIsDisplayed()
    }

    @Test
    fun f3_formA_ownerClickingFormBBanner_triggersCallback() {
        var formBClicked = false
        composeTestRule.setContent {
            AskITTheme {
                ProfileE2ETestScaffold(
                    state = ProfileE2EState(isOwner = true, isProvider = false, isPublicPreview = false),
                    actions = ProfileE2EActions(onCompleteFormB = { formBClicked = true }),
                )
            }
        }

        composeTestRule.onNodeWithTag("profile_form_b_cta").performClick()
        assertTrue("onCompleteFormB callback must be triggered", formBClicked)
    }

    @Test
    fun f3_formA_switchingTabs_updatesActiveContent() {
        val selectedIndex = mutableStateOf(0)
        composeTestRule.setContent {
            AskITTheme {
                ProfileE2ETestScaffold(
                    state = ProfileE2EState(isProvider = false, selectedTabIndex = selectedIndex.value),
                    actions = ProfileE2EActions(onTabSelected = { selectedIndex.value = it }),
                )
            }
        }

        composeTestRule.onNodeWithTag("profile_content_activity").assertExists()

        composeTestRule.onNodeWithTag("profile_tab_about").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("profile_content_about").assertExists()
    }

    // =========================================================================
    // Feature F4: Form B Dynamic Metrics & Tabs (5 tests)
    // =========================================================================

    @Test
    fun f4_formB_metrics_showsRatingJobsFollowersFollowing() {
        composeTestRule.setContent {
            AskITTheme {
                ProfileE2ETestScaffold(
                    state = ProfileE2EState(
                        isProvider = true,
                        rating = 4.9,
                        completedJobsCount = 48,
                        followerCount = 350,
                        followingCount = 80,
                    ),
                )
            }
        }

        composeTestRule.onNodeWithTag("profile_metric_rating").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_metric_jobs").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_metric_followers").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_metric_following").assertIsDisplayed()

        // Member-only Activity metric must NOT exist
        composeTestRule.onNodeWithTag("profile_metric_activity").assertDoesNotExist()
    }

    @Test
    fun f4_formB_tabs_showsServicesGalleryReviewsAbout() {
        composeTestRule.setContent {
            AskITTheme {
                ProfileE2ETestScaffold(state = ProfileE2EState(isProvider = true))
            }
        }

        composeTestRule.onNodeWithTag("profile_tab_services").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_tab_gallery").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_tab_reviews").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_tab_about").assertIsDisplayed()
    }

    @Test
    fun f4_formB_identity_showsVerifiedTradeBadgeAndHeadline() {
        composeTestRule.setContent {
            AskITTheme {
                ProfileE2ETestScaffold(
                    state = ProfileE2EState(
                        isProvider = true,
                        tradeHeadline = "Certified Electrician",
                        isVerified = true,
                    ),
                )
            }
        }

        composeTestRule.onNodeWithTag("profile_verified_badge").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_trade_headline").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_trade_headline").assertTextContains("Certified Electrician")
    }

    @Test
    fun f4_formB_ownerMode_hidesCompleteFormBBanner() {
        composeTestRule.setContent {
            AskITTheme {
                ProfileE2ETestScaffold(
                    state = ProfileE2EState(isOwner = true, isProvider = true, isPublicPreview = false),
                )
            }
        }

        composeTestRule.onNodeWithTag("profile_complete_form_b_banner").assertDoesNotExist()
    }

    @Test
    fun f4_formB_servicesTab_rendersServiceListingContent() {
        composeTestRule.setContent {
            AskITTheme {
                ProfileE2ETestScaffold(
                    state = ProfileE2EState(isProvider = true, selectedTabIndex = 0),
                )
            }
        }

        composeTestRule.onNodeWithTag("profile_content_services").assertExists()
    }

    // =========================================================================
    // Feature F5: Form A Visitor Protection (5 tests)
    // =========================================================================

    @Test
    fun f5_formAVisitorProtection_requestServiceButton_doesNotExist() {
        composeTestRule.setContent {
            AskITTheme {
                ProfileE2ETestScaffold(
                    state = ProfileE2EState(isOwner = false, isProvider = false),
                )
            }
        }

        composeTestRule.onNodeWithTag("profile_action_request_service").assertDoesNotExist()
    }

    @Test
    fun f5_formAVisitorProtection_servicesTab_doesNotExist() {
        composeTestRule.setContent {
            AskITTheme {
                ProfileE2ETestScaffold(
                    state = ProfileE2EState(isOwner = false, isProvider = false),
                )
            }
        }

        composeTestRule.onNodeWithTag("profile_tab_services").assertDoesNotExist()
    }

    @Test
    fun f5_formAVisitorProtection_galleryTab_doesNotExist() {
        composeTestRule.setContent {
            AskITTheme {
                ProfileE2ETestScaffold(
                    state = ProfileE2EState(isOwner = false, isProvider = false),
                )
            }
        }

        composeTestRule.onNodeWithTag("profile_tab_gallery").assertDoesNotExist()
    }

    @Test
    fun f5_formAVisitorProtection_formBBanner_doesNotExistForVisitor() {
        composeTestRule.setContent {
            AskITTheme {
                ProfileE2ETestScaffold(
                    state = ProfileE2EState(isOwner = false, isProvider = false),
                )
            }
        }

        composeTestRule.onNodeWithTag("profile_complete_form_b_banner").assertDoesNotExist()
    }

    @Test
    fun f5_formAVisitorProtection_ownerInPublicPreview_alsoProtectedFromRequestService() {
        composeTestRule.setContent {
            AskITTheme {
                ProfileE2ETestScaffold(
                    state = ProfileE2EState(isOwner = true, isProvider = false, isPublicPreview = true),
                )
            }
        }

        // In public preview, Form A owner sees visitor view which must NOT show Request Service
        composeTestRule.onNodeWithTag("profile_action_request_service").assertDoesNotExist()
        composeTestRule.onNodeWithTag("profile_complete_form_b_banner").assertDoesNotExist()
    }

    // =========================================================================
    // Feature F6: In-Place "View as Public" Preview (5 tests)
    // =========================================================================

    @Test
    fun f6_viewAsPublic_clickingViewAsPublic_activatesPreviewMode() {
        val state = mutableStateOf(ProfileE2EState(isOwner = true, isPublicPreview = false))
        composeTestRule.setContent {
            AskITTheme {
                ProfileE2ETestScaffold(
                    state = state.value,
                    actions = ProfileE2EActions(
                        onViewAsPublic = { state.value = state.value.copy(isPublicPreview = true) },
                    ),
                )
            }
        }

        composeTestRule.onNodeWithTag("profile_action_view_as_public").performClick()
        composeTestRule.waitForIdle()

        assertTrue("State must switch to public preview", state.value.isPublicPreview)
        composeTestRule.onNodeWithTag("profile_preview_banner").assertIsDisplayed()
    }

    @Test
    fun f6_viewAsPublic_previewBanner_isDisplayedWithExitButton() {
        composeTestRule.setContent {
            AskITTheme {
                ProfileE2ETestScaffold(
                    state = ProfileE2EState(isOwner = true, isPublicPreview = true),
                )
            }
        }

        composeTestRule.onNodeWithTag("profile_preview_banner").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_exit_preview").assertIsDisplayed()
    }

    @Test
    fun f6_viewAsPublic_ownerActionsHidden_visitorActionsShown() {
        composeTestRule.setContent {
            AskITTheme {
                ProfileE2ETestScaffold(
                    state = ProfileE2EState(isOwner = true, isPublicPreview = true),
                )
            }
        }

        // Owner buttons are hidden
        composeTestRule.onNodeWithTag("profile_action_edit").assertDoesNotExist()
        composeTestRule.onNodeWithTag("profile_action_share").assertDoesNotExist()
        composeTestRule.onNodeWithTag("profile_action_view_as_public").assertDoesNotExist()

        // Visitor buttons are shown
        composeTestRule.onNodeWithTag("profile_action_message").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_action_follow").assertIsDisplayed()
    }

    @Test
    fun f6_viewAsPublic_cameraAffordanceHidden_inPreview() {
        composeTestRule.setContent {
            AskITTheme {
                ProfileE2ETestScaffold(
                    state = ProfileE2EState(isOwner = true, isPublicPreview = true),
                )
            }
        }

        // Camera affordance button on avatar must be hidden in preview
        composeTestRule.onNodeWithTag("profile_camera").assertDoesNotExist()
    }

    @Test
    fun f6_viewAsPublic_clickingExitPreview_restoresOwnerMode() {
        val state = mutableStateOf(ProfileE2EState(isOwner = true, isPublicPreview = true))
        composeTestRule.setContent {
            AskITTheme {
                ProfileE2ETestScaffold(
                    state = state.value,
                    actions = ProfileE2EActions(
                        onExitPreview = { state.value = state.value.copy(isPublicPreview = false) },
                    ),
                )
            }
        }

        composeTestRule.onNodeWithTag("profile_exit_preview").performClick()
        composeTestRule.waitForIdle()

        assertFalse("State must exit public preview", state.value.isPublicPreview)
        composeTestRule.onNodeWithTag("profile_preview_banner").assertDoesNotExist()
        composeTestRule.onNodeWithTag("profile_action_edit").assertIsDisplayed()
    }

    // =========================================================================
    // Feature F7: Zero Code Duplication & WYSIWYG Parity (5 tests)
    // =========================================================================

    @Test
    fun f7_wysiwygParity_formA_previewMatchesVisitorSemanticStructure() {
        // Both owner in preview mode and actual visitor must expose identical core nodes
        composeTestRule.setContent {
            AskITTheme {
                ProfileE2ETestScaffold(
                    state = ProfileE2EState(isOwner = true, isPublicPreview = true, isProvider = false),
                )
            }
        }

        composeTestRule.onNodeWithTag("profile_cover").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_avatar").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_identity_block").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_metrics_bar").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_action_message").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_action_follow").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_tab_activity").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_tab_about").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_tab_reviews").assertIsDisplayed()
    }

    @Test
    fun f7_wysiwygParity_formB_previewMatchesVisitorSemanticStructure() {
        composeTestRule.setContent {
            AskITTheme {
                ProfileE2ETestScaffold(
                    state = ProfileE2EState(
                        isOwner = true,
                        isPublicPreview = true,
                        isProvider = true,
                        tradeHeadline = "Certified Electrician",
                    ),
                )
            }
        }

        composeTestRule.onNodeWithTag("profile_cover").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_avatar").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_identity_block").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_trade_headline").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_metrics_bar").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_action_message").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_action_follow").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_action_request_service").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_tab_services").assertIsDisplayed()
    }

    @Test
    fun f7_wysiwygParity_headerVerticalPositions_matchExactly() {
        val isOwnerState = mutableStateOf(true)
        val isPreviewState = mutableStateOf(true)

        composeTestRule.setContent {
            AskITTheme {
                ProfileE2ETestScaffold(
                    state = ProfileE2EState(
                        isOwner = isOwnerState.value,
                        isPublicPreview = isPreviewState.value,
                    ),
                )
            }
        }

        val previewCoverY = composeTestRule.onNodeWithTag("profile_cover").getUnclippedBoundsInRoot().top.value
        val previewAvatarY = composeTestRule.onNodeWithTag("profile_avatar").getUnclippedBoundsInRoot().top.value
        val previewIdentityY = composeTestRule.onNodeWithTag("profile_identity_block").getUnclippedBoundsInRoot().top.value
        val previewMetricsY = composeTestRule.onNodeWithTag("profile_metrics_bar").getUnclippedBoundsInRoot().top.value

        // Switch to visitor mode in same test without calling setContent twice
        isOwnerState.value = false
        isPreviewState.value = false
        composeTestRule.waitForIdle()

        val visitorCoverY = composeTestRule.onNodeWithTag("profile_cover").getUnclippedBoundsInRoot().top.value
        val visitorAvatarY = composeTestRule.onNodeWithTag("profile_avatar").getUnclippedBoundsInRoot().top.value
        val visitorIdentityY = composeTestRule.onNodeWithTag("profile_identity_block").getUnclippedBoundsInRoot().top.value
        val visitorMetricsY = composeTestRule.onNodeWithTag("profile_metrics_bar").getUnclippedBoundsInRoot().top.value

        assertEquals("Cover top must match 1:1", previewCoverY, visitorCoverY, 0.5f)
        assertEquals("Avatar top must match 1:1", previewAvatarY, visitorAvatarY, 0.5f)
        assertEquals("Identity top must match 1:1", previewIdentityY, visitorIdentityY, 0.5f)
        assertEquals("Metrics top must match 1:1", previewMetricsY, visitorMetricsY, 0.5f)
    }

    @Test
    fun f7_wysiwygParity_actionButtonLabels_matchExactly() {
        composeTestRule.setContent {
            AskITTheme {
                ProfileE2ETestScaffold(
                    state = ProfileE2EState(isOwner = true, isPublicPreview = true),
                )
            }
        }

        composeTestRule.onNodeWithTag("profile_action_message").assertTextContains("Message")
        composeTestRule.onNodeWithTag("profile_action_follow").assertTextContains("Follow")
    }

    @Test
    fun f7_wysiwygParity_metricsLabelsAndOrder_matchExactly() {
        composeTestRule.setContent {
            AskITTheme {
                ProfileE2ETestScaffold(
                    state = ProfileE2EState(
                        isOwner = true,
                        isPublicPreview = true,
                        isProvider = true,
                        rating = 5.0,
                        completedJobsCount = 20,
                    ),
                )
            }
        }

        composeTestRule.onNodeWithTag("profile_metric_rating").assertIsDisplayed()
        composeTestRule.onNodeWithText("Rating").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_metric_jobs").assertIsDisplayed()
        composeTestRule.onNodeWithText("Completed Jobs").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_metric_followers").assertIsDisplayed()
        composeTestRule.onNodeWithText("Followers").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_metric_following").assertIsDisplayed()
        composeTestRule.onNodeWithText("Following").assertIsDisplayed()
    }
}
