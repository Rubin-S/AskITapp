package com.askit.app.profile

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.askit.app.session.ServiceListing
import com.askit.app.session.SessionProfile
import com.askit.designsystem.theme.AskITTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Unit & Contract test suite for [UniversalProfileScaffold].
 * Verifies spatial ordering, action row switching, banner visibility, and 1:1 WYSIWYG parity.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w360dp-h800dp-normal-long-notround-any-xxxhdpi-keyshidden-nonav")
class UniversalProfileScaffoldTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val formAListingSession = SessionProfile(
        displayName = "Rahul Verma",
        username = "rahul_v",
        bio = "Community member",
        city = "Bengaluru",
        joinedYear = "2023",
        hasListedService = false,
        followerCount = 84,
        followingCount = 31,
    )

    private val formBListingSession = SessionProfile(
        displayName = "Priya Sharma",
        username = "priya_e",
        bio = "Certified electrician",
        city = "Chennai",
        joinedYear = "2022",
        hasListedService = true,
        listing = ServiceListing(
            title = "Electrical Safety & Repair",
            category = "Certified Electrician",
            description = "Wiring repairs and installations.",
            quoteLabel = "Free Estimate",
            coverage = "10 km",
            coverageHint = "Service area",
            hours = "9 AM - 6 PM",
            hoursHint = "Working hours",
            response = "< 1 hr",
            responseHint = "Reply time",
            tags = listOf("Wiring", "Inverter"),
            experience = "8+ years",
            live = true,
        ),
        followerCount = 120,
        followingCount = 45,
    )

    // =========================================================================
    // 1. Universal Spatial Ordering
    // =========================================================================

    @Test
    fun spatialOrdering_ownerFormA_verifiesVerticalHierarchy() {
        val uiState = formAListingSession.toUiState()

        composeTestRule.setContent {
            AskITTheme {
                UniversalProfileScaffold(
                    uiState = uiState,
                    isVisitor = false,
                    showTopBar = true,
                )
            }
        }

        val topBarY = composeTestRule.onNodeWithTag("profile_top_bar").getUnclippedBoundsInRoot().top
        val coverY = composeTestRule.onNodeWithTag("profile_cover").getUnclippedBoundsInRoot().top
        val avatarY = composeTestRule.onNodeWithTag("profile_avatar").getUnclippedBoundsInRoot().top
        val identityY = composeTestRule.onNodeWithTag("profile_identity_block").getUnclippedBoundsInRoot().top
        val metricsY = composeTestRule.onNodeWithTag("profile_metrics_bar").getUnclippedBoundsInRoot().top
        val actionsY = composeTestRule.onNodeWithTag("profile_action_row").getUnclippedBoundsInRoot().top
        val bannerY = composeTestRule.onNodeWithTag("profile_complete_form_b_banner").getUnclippedBoundsInRoot().top
        val tabsY = composeTestRule.onNodeWithTag("profile_tabs").getUnclippedBoundsInRoot().top
        val contentY = composeTestRule.onNodeWithTag("profile_content_section").getUnclippedBoundsInRoot().top

        assertTrue("TopBar <= Cover", topBarY <= coverY)
        assertTrue("Cover <= Avatar", coverY <= avatarY)
        assertTrue("Avatar < Identity", avatarY < identityY)
        assertTrue("Identity < Metrics", identityY < metricsY)
        assertTrue("Metrics < Actions", metricsY < actionsY)
        assertTrue("Actions < Banner", actionsY < bannerY)
        assertTrue("Banner < Tabs", bannerY < tabsY)
        assertTrue("Tabs < Content", tabsY < contentY)
    }

    @Test
    fun spatialOrdering_providerFormB_verifiesVerticalHierarchy() {
        val uiState = formBListingSession.toUiState()

        composeTestRule.setContent {
            AskITTheme {
                UniversalProfileScaffold(
                    uiState = uiState,
                    isVisitor = false,
                    showTopBar = true,
                )
            }
        }

        val topBarY = composeTestRule.onNodeWithTag("profile_top_bar").getUnclippedBoundsInRoot().top
        val coverY = composeTestRule.onNodeWithTag("profile_cover").getUnclippedBoundsInRoot().top
        val avatarY = composeTestRule.onNodeWithTag("profile_avatar").getUnclippedBoundsInRoot().top
        val identityY = composeTestRule.onNodeWithTag("profile_identity_block").getUnclippedBoundsInRoot().top
        val metricsY = composeTestRule.onNodeWithTag("profile_metrics_bar").getUnclippedBoundsInRoot().top
        val actionsY = composeTestRule.onNodeWithTag("profile_action_row").getUnclippedBoundsInRoot().top
        val tabsY = composeTestRule.onNodeWithTag("profile_tabs").getUnclippedBoundsInRoot().top
        val contentY = composeTestRule.onNodeWithTag("profile_content_section").getUnclippedBoundsInRoot().top

        assertTrue("TopBar <= Cover", topBarY <= coverY)
        assertTrue("Cover <= Avatar", coverY <= avatarY)
        assertTrue("Avatar < Identity", avatarY < identityY)
        assertTrue("Identity < Metrics", identityY < metricsY)
        assertTrue("Metrics < Actions", metricsY < actionsY)
        assertTrue("Actions < Tabs", actionsY < tabsY)
        assertTrue("Tabs < Content", tabsY < contentY)

        // Form B must NOT render CompleteFormBBanner
        composeTestRule.onNodeWithTag("profile_complete_form_b_banner").assertDoesNotExist()
    }

    // =========================================================================
    // 2. Contextual Action Switching
    // =========================================================================

    @Test
    fun contextualActions_ownerMode_showsOwnerActionsOnly() {
        val uiState = formAListingSession.toUiState(isPublicPreview = false)

        composeTestRule.setContent {
            AskITTheme {
                UniversalProfileScaffold(uiState = uiState, isVisitor = false)
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
    fun contextualActions_visitorFormA_showsMessageAndFollow_noRequestService() {
        val uiState = formAListingSession.toUiState()

        composeTestRule.setContent {
            AskITTheme {
                UniversalProfileScaffold(
                    uiState = uiState,
                    isVisitor = true,
                    onRequestService = { /* should not be invoked */ },
                )
            }
        }

        composeTestRule.onNodeWithTag("profile_action_message").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_action_follow").assertIsDisplayed()

        // Form A visitor protection: Request Service & Owner actions must NOT exist
        composeTestRule.onNodeWithTag("profile_action_request_service").assertDoesNotExist()
        composeTestRule.onNodeWithTag("profile_action_edit").assertDoesNotExist()
        composeTestRule.onNodeWithTag("profile_action_share").assertDoesNotExist()
        composeTestRule.onNodeWithTag("profile_action_view_as_public").assertDoesNotExist()
    }

    @Test
    fun contextualActions_visitorFormB_showsMessageFollowAndRequestService() {
        val uiState = formBListingSession.toUiState()

        composeTestRule.setContent {
            AskITTheme {
                UniversalProfileScaffold(
                    uiState = uiState,
                    isVisitor = true,
                    onRequestService = {},
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

    // =========================================================================
    // 3. Camera Affordance: Owner Edit Mode Only
    // =========================================================================

    @Test
    fun cameraAffordance_ownerMode_cameraButtonDisplayed() {
        composeTestRule.setContent {
            AskITTheme {
                UniversalProfileScaffold(
                    uiState = formAListingSession.toUiState(isPublicPreview = false),
                    isVisitor = false,
                )
            }
        }
        composeTestRule.onNodeWithTag("profile_camera").assertIsDisplayed()
    }

    @Test
    fun cameraAffordance_visitorMode_cameraButtonDoesNotExist() {
        composeTestRule.setContent {
            AskITTheme {
                UniversalProfileScaffold(
                    uiState = formAListingSession.toUiState(isPublicPreview = false),
                    isVisitor = true,
                )
            }
        }
        composeTestRule.onNodeWithTag("profile_camera").assertDoesNotExist()
    }

    @Test
    fun cameraAffordance_publicPreviewMode_cameraButtonDoesNotExist() {
        composeTestRule.setContent {
            AskITTheme {
                UniversalProfileScaffold(
                    uiState = formAListingSession.toUiState(isPublicPreview = true),
                    isVisitor = false,
                )
            }
        }
        composeTestRule.onNodeWithTag("profile_camera").assertDoesNotExist()
    }

    // =========================================================================
    // 4. CompleteFormBBanner: Form A Owner Only
    // =========================================================================

    @Test
    fun completeFormBBanner_formAOwner_bannerDisplayed() {
        composeTestRule.setContent {
            AskITTheme {
                UniversalProfileScaffold(
                    uiState = formAListingSession.toUiState(isPublicPreview = false),
                    isVisitor = false,
                )
            }
        }
        composeTestRule.onNodeWithTag("profile_complete_form_b_banner").assertIsDisplayed()
    }

    @Test
    fun completeFormBBanner_formAVisitor_bannerDoesNotExist() {
        composeTestRule.setContent {
            AskITTheme {
                UniversalProfileScaffold(
                    uiState = formAListingSession.toUiState(isPublicPreview = false),
                    isVisitor = true,
                )
            }
        }
        composeTestRule.onNodeWithTag("profile_complete_form_b_banner").assertDoesNotExist()
    }

    @Test
    fun completeFormBBanner_formAPublicPreview_bannerDoesNotExist() {
        composeTestRule.setContent {
            AskITTheme {
                UniversalProfileScaffold(
                    uiState = formAListingSession.toUiState(isPublicPreview = true),
                    isVisitor = false,
                )
            }
        }
        composeTestRule.onNodeWithTag("profile_complete_form_b_banner").assertDoesNotExist()
    }

    @Test
    fun completeFormBBanner_formBOwner_bannerDoesNotExist() {
        composeTestRule.setContent {
            AskITTheme {
                UniversalProfileScaffold(
                    uiState = formBListingSession.toUiState(isPublicPreview = false),
                    isVisitor = false,
                )
            }
        }
        composeTestRule.onNodeWithTag("profile_complete_form_b_banner").assertDoesNotExist()
    }

    @Test
    fun providerDashboardBanner_formBOwner_bannerDisplayed_andClickTriggersCallback() {
        var dashboardClicked = false
        composeTestRule.setContent {
            AskITTheme {
                UniversalProfileScaffold(
                    uiState = formBListingSession.toUiState(isPublicPreview = false),
                    isVisitor = false,
                    onOpenProviderDashboard = { dashboardClicked = true },
                )
            }
        }
        composeTestRule.onNodeWithTag("profile_provider_dashboard_banner").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_provider_dashboard_banner").performClick()
        assertTrue(dashboardClicked)
    }

    @Test
    fun providerDashboardBanner_formAOwner_bannerDoesNotExist() {
        composeTestRule.setContent {
            AskITTheme {
                UniversalProfileScaffold(
                    uiState = formAListingSession.toUiState(isPublicPreview = false),
                    isVisitor = false,
                )
            }
        }
        composeTestRule.onNodeWithTag("profile_provider_dashboard_banner").assertDoesNotExist()
    }

    @Test
    fun providerDashboardBanner_formBVisitor_bannerDoesNotExist() {
        composeTestRule.setContent {
            AskITTheme {
                UniversalProfileScaffold(
                    uiState = formBListingSession.toUiState(isPublicPreview = false),
                    isVisitor = true,
                )
            }
        }
        composeTestRule.onNodeWithTag("profile_provider_dashboard_banner").assertDoesNotExist()
    }

    // =========================================================================
    // 5. Floating ProfilePreviewBanner
    // =========================================================================

    @Test
    fun previewBanner_displayedInPublicPreview_andExitClickTriggersCallback() {
        var exitPreviewClicked = false
        val uiState = formAListingSession.toUiState(isPublicPreview = true)

        composeTestRule.setContent {
            AskITTheme {
                UniversalProfileScaffold(
                    uiState = uiState,
                    isVisitor = false,
                    onExitPreview = { exitPreviewClicked = true },
                )
            }
        }

        composeTestRule.onNodeWithTag("profile_preview_banner").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_exit_preview").performClick()
        assertTrue("onExitPreview callback must be invoked", exitPreviewClicked)
    }

    @Test
    fun previewBanner_hiddenInNormalOwnerAndVisitorModes() {
        composeTestRule.setContent {
            AskITTheme {
                UniversalProfileScaffold(
                    uiState = formAListingSession.toUiState(isPublicPreview = false),
                    isVisitor = false,
                )
            }
        }
        composeTestRule.onNodeWithTag("profile_preview_banner").assertDoesNotExist()
    }

    // =========================================================================
    // 6. Dynamic Tab Switching
    // =========================================================================

    @Test
    fun tabSwitching_formA_rendersCorrespondingContentPane() {
        val uiState = formAListingSession.toUiState()

        composeTestRule.setContent {
            AskITTheme {
                UniversalProfileScaffold(uiState = uiState)
            }
        }

        // Initially on Activity tab
        composeTestRule.onNodeWithTag("profile_content_activity").assertIsDisplayed()

        // Switch to About tab
        composeTestRule.onNodeWithTag("profile_tab_about").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("profile_content_about").assertIsDisplayed()

        // Switch to Reviews tab
        composeTestRule.onNodeWithTag("profile_tab_reviews").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("profile_content_reviews").assertIsDisplayed()
    }

    // =========================================================================
    // 7. 1:1 WYSIWYG Parity Between Owner and Visitor
    // =========================================================================

    @Test
    fun wysiwygParity_avatarAndIdentityDimensions_identicalBetweenOwnerAndVisitor() {
        val uiState = formBListingSession.toUiState()
        val isVisitorState = androidx.compose.runtime.mutableStateOf(false)

        composeTestRule.setContent {
            AskITTheme {
                UniversalProfileScaffold(
                    uiState = uiState,
                    isVisitor = isVisitorState.value,
                    showTopBar = false,
                )
            }
        }
        val ownerAvatarBounds = composeTestRule.onNodeWithTag("profile_avatar").getUnclippedBoundsInRoot()
        val ownerIdentityBounds = composeTestRule.onNodeWithTag("profile_identity_block").getUnclippedBoundsInRoot()

        isVisitorState.value = true
        composeTestRule.waitForIdle()

        val visitorAvatarBounds = composeTestRule.onNodeWithTag("profile_avatar").getUnclippedBoundsInRoot()
        val visitorIdentityBounds = composeTestRule.onNodeWithTag("profile_identity_block").getUnclippedBoundsInRoot()

        assertEquals(
            "Avatar top position must be identical across owner and visitor",
            ownerAvatarBounds.top.value,
            visitorAvatarBounds.top.value,
            1f,
        )
        assertEquals(
            "Avatar height must be identical across owner and visitor",
            ownerAvatarBounds.bottom.value - ownerAvatarBounds.top.value,
            visitorAvatarBounds.bottom.value - visitorAvatarBounds.top.value,
            1f,
        )
        assertEquals(
            "Identity block top position must be identical across owner and visitor",
            ownerIdentityBounds.top.value,
            visitorIdentityBounds.top.value,
            1f,
        )
    }
}
