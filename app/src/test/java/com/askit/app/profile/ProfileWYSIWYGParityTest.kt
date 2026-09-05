package com.askit.app.profile

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.askit.app.home.details.UserProfileScreen
import com.askit.app.home.details.getUserProfileById
import com.askit.app.session.ServiceListing
import com.askit.app.session.SessionProfile
import com.askit.designsystem.theme.AskITTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * 1:1 WYSIWYG Visual and Structural Parity Test between:
 * - Owner ProfileRoute in in-place "View as Public" preview mode
 * - Visitor UserProfileScreen
 *
 * Verifies that header, cover, avatar, identity, metrics, actions, and tabs occupy
 * identical vertical positions and render identical component structures across both screens.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w360dp-h800dp-normal-long-notround-any-xxxhdpi-keyshidden-nonav")
class ProfileWYSIWYGParityTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private enum class ParityMode {
        OwnerPreview,
        Visitor,
    }

    // =========================================================================
    // 1. Form B Provider 1:1 WYSIWYG Bounds & Spatial Parity
    // =========================================================================

    @Test
    fun formB_wysiwygSpatialParity_betweenOwnerPreviewAndVisitorScreen() {
        val proData = getUserProfileById("pro-1")

        val formBSession = SessionProfile(
            displayName = proData.name,
            username = proData.username,
            bio = proData.bio,
            city = proData.location,
            joinedYear = "2022",
            hasListedService = true,
            listing = ServiceListing(
                title = proData.trade,
                category = proData.trade,
                description = proData.bio,
                quoteLabel = "Visit / Quote",
                coverage = proData.location,
                coverageHint = "Service Area",
                hours = "Mon - Sat 9AM - 6PM",
                hoursHint = "Working Hours",
                response = "< 1 hr",
                responseHint = "Response Time",
                tags = proData.skills,
                experience = proData.memberSince,
                live = true,
            ),
            followerCount = proData.followerCount,
            followingCount = proData.followingCount,
        )

        val mode = mutableStateOf(ParityMode.OwnerPreview)

        composeTestRule.setContent {
            AskITTheme {
                when (mode.value) {
                    ParityMode.OwnerPreview -> ProfileRoute(
                        profile = formBSession,
                        jobs = emptyList(),
                        onEditProfile = {},
                        onEditListing = {},
                        onUploadWork = {},
                        onOpenJob = {},
                        onViewAllJobs = {},
                        onSaveAbout = {},
                        onSaveLookingFor = {},
                        onSaveSkills = {},
                        onSaveAvailability = {},
                        onSetAvatar = {},
                        onAddLicense = {},
                        onUsernameCopied = {},
                    )
                    ParityMode.Visitor -> UserProfileScreen(
                        userId = "pro-1",
                        onBack = {},
                    )
                }
            }
        }

        // Switch owner profile to in-place preview
        composeTestRule.onNodeWithTag("profile_action_view_as_public").performClick()
        composeTestRule.waitForIdle()

        // Record bounds in ProfileRoute preview mode
        val previewCoverBounds = composeTestRule.onNodeWithTag("profile_cover").getUnclippedBoundsInRoot()
        val previewAvatarBounds = composeTestRule.onNodeWithTag("profile_avatar").getUnclippedBoundsInRoot()
        val previewIdentityBounds = composeTestRule.onNodeWithTag("profile_identity_block").getUnclippedBoundsInRoot()
        val previewMetricsBounds = composeTestRule.onNodeWithTag("profile_metrics_bar").getUnclippedBoundsInRoot()
        val previewActionsBounds = composeTestRule.onNodeWithTag("profile_action_row").getUnclippedBoundsInRoot()
        val previewTabsBounds = composeTestRule.onNodeWithTag("profile_tabs").getUnclippedBoundsInRoot()

        // Switch to visitor screen
        mode.value = ParityMode.Visitor
        composeTestRule.waitForIdle()

        // Record bounds in UserProfileScreen visitor mode
        val visitorCoverBounds = composeTestRule.onNodeWithTag("profile_cover").getUnclippedBoundsInRoot()
        val visitorAvatarBounds = composeTestRule.onNodeWithTag("profile_avatar").getUnclippedBoundsInRoot()
        val visitorIdentityBounds = composeTestRule.onNodeWithTag("profile_identity_block").getUnclippedBoundsInRoot()
        val visitorMetricsBounds = composeTestRule.onNodeWithTag("profile_metrics_bar").getUnclippedBoundsInRoot()
        val visitorActionsBounds = composeTestRule.onNodeWithTag("profile_action_row").getUnclippedBoundsInRoot()
        val visitorTabsBounds = composeTestRule.onNodeWithTag("profile_tabs").getUnclippedBoundsInRoot()

        // Assert 1:1 bounds parity (within 1.0dp tolerance)
        assertEquals(
            "Cover top position must match within 1.0dp",
            previewCoverBounds.top.value,
            visitorCoverBounds.top.value,
            1.0f,
        )
        assertEquals(
            "Cover height must match within 1.0dp",
            previewCoverBounds.bottom.value - previewCoverBounds.top.value,
            visitorCoverBounds.bottom.value - visitorCoverBounds.top.value,
            1.0f,
        )
        assertEquals(
            "Avatar top position must match within 1.0dp",
            previewAvatarBounds.top.value,
            visitorAvatarBounds.top.value,
            1.0f,
        )
        assertEquals(
            "Avatar height must match within 1.0dp",
            previewAvatarBounds.bottom.value - previewAvatarBounds.top.value,
            visitorAvatarBounds.bottom.value - visitorAvatarBounds.top.value,
            1.0f,
        )
        assertEquals(
            "Identity block top position must match within 1.0dp",
            previewIdentityBounds.top.value,
            visitorIdentityBounds.top.value,
            1.0f,
        )
        assertEquals(
            "Metrics bar top position must match within 1.0dp",
            previewMetricsBounds.top.value,
            visitorMetricsBounds.top.value,
            1.0f,
        )
        assertEquals(
            "Action row top position must match within 1.0dp",
            previewActionsBounds.top.value,
            visitorActionsBounds.top.value,
            1.0f,
        )
        assertEquals(
            "Tabs top position must match within 1.0dp",
            previewTabsBounds.top.value,
            visitorTabsBounds.top.value,
            1.0f,
        )
    }

    // =========================================================================
    // 2. Form A Member 1:1 WYSIWYG Bounds & Spatial Parity
    // =========================================================================

    @Test
    fun formA_wysiwygSpatialParity_betweenOwnerPreviewAndVisitorScreen() {
        val memberData = getUserProfileById("member-1")

        val formASession = SessionProfile(
            displayName = memberData.name,
            username = memberData.username,
            bio = memberData.bio,
            city = memberData.location,
            joinedYear = "2023",
            hasListedService = false,
            followerCount = memberData.followerCount,
            followingCount = memberData.followingCount,
        )

        val mode = mutableStateOf(ParityMode.OwnerPreview)

        composeTestRule.setContent {
            AskITTheme {
                when (mode.value) {
                    ParityMode.OwnerPreview -> ProfileRoute(
                        profile = formASession,
                        jobs = emptyList(),
                        onEditProfile = {},
                        onEditListing = {},
                        onUploadWork = {},
                        onOpenJob = {},
                        onViewAllJobs = {},
                        onSaveAbout = {},
                        onSaveLookingFor = {},
                        onSaveSkills = {},
                        onSaveAvailability = {},
                        onSetAvatar = {},
                        onAddLicense = {},
                        onUsernameCopied = {},
                    )
                    ParityMode.Visitor -> UserProfileScreen(
                        userId = "member-1",
                        onBack = {},
                    )
                }
            }
        }

        // Switch to preview mode
        composeTestRule.onNodeWithTag("profile_action_view_as_public").performClick()
        composeTestRule.waitForIdle()

        val previewCoverBounds = composeTestRule.onNodeWithTag("profile_cover").getUnclippedBoundsInRoot()
        val previewAvatarBounds = composeTestRule.onNodeWithTag("profile_avatar").getUnclippedBoundsInRoot()
        val previewIdentityBounds = composeTestRule.onNodeWithTag("profile_identity_block").getUnclippedBoundsInRoot()
        val previewMetricsBounds = composeTestRule.onNodeWithTag("profile_metrics_bar").getUnclippedBoundsInRoot()
        val previewActionsBounds = composeTestRule.onNodeWithTag("profile_action_row").getUnclippedBoundsInRoot()
        val previewTabsBounds = composeTestRule.onNodeWithTag("profile_tabs").getUnclippedBoundsInRoot()

        // Switch to visitor screen
        mode.value = ParityMode.Visitor
        composeTestRule.waitForIdle()

        val visitorCoverBounds = composeTestRule.onNodeWithTag("profile_cover").getUnclippedBoundsInRoot()
        val visitorAvatarBounds = composeTestRule.onNodeWithTag("profile_avatar").getUnclippedBoundsInRoot()
        val visitorIdentityBounds = composeTestRule.onNodeWithTag("profile_identity_block").getUnclippedBoundsInRoot()
        val visitorMetricsBounds = composeTestRule.onNodeWithTag("profile_metrics_bar").getUnclippedBoundsInRoot()
        val visitorActionsBounds = composeTestRule.onNodeWithTag("profile_action_row").getUnclippedBoundsInRoot()
        val visitorTabsBounds = composeTestRule.onNodeWithTag("profile_tabs").getUnclippedBoundsInRoot()

        // Assert Form A 1:1 bounds parity
        assertEquals(
            "Cover top position must match within 1.0dp",
            previewCoverBounds.top.value,
            visitorCoverBounds.top.value,
            1.0f,
        )
        assertEquals(
            "Avatar top position must match within 1.0dp",
            previewAvatarBounds.top.value,
            visitorAvatarBounds.top.value,
            1.0f,
        )
        assertEquals(
            "Identity block top position must match within 1.0dp",
            previewIdentityBounds.top.value,
            visitorIdentityBounds.top.value,
            1.0f,
        )
        assertEquals(
            "Metrics bar top position must match within 1.0dp",
            previewMetricsBounds.top.value,
            visitorMetricsBounds.top.value,
            1.0f,
        )
        assertEquals(
            "Action row top position must match within 1.0dp",
            previewActionsBounds.top.value,
            visitorActionsBounds.top.value,
            1.0f,
        )
        assertEquals(
            "Tabs top position must match within 1.0dp",
            previewTabsBounds.top.value,
            visitorTabsBounds.top.value,
            1.0f,
        )
    }

    // =========================================================================
    // 3. Form B Semantic & Structural Parity
    // =========================================================================

    @Test
    fun formB_semanticParity_betweenOwnerPreviewAndVisitorScreen() {
        val proData = getUserProfileById("pro-1")

        val formBSession = SessionProfile(
            displayName = proData.name,
            username = proData.username,
            bio = proData.bio,
            city = proData.location,
            joinedYear = "2022",
            hasListedService = true,
            listing = ServiceListing(
                title = proData.trade,
                category = proData.trade,
                description = proData.bio,
                quoteLabel = "Visit / Quote",
                coverage = proData.location,
                coverageHint = "Service Area",
                hours = "Mon - Sat 9AM - 6PM",
                hoursHint = "Working Hours",
                response = "< 1 hr",
                responseHint = "Response Time",
                tags = proData.skills,
                experience = proData.memberSince,
                live = true,
            ),
            followerCount = proData.followerCount,
            followingCount = proData.followingCount,
        )

        val mode = mutableStateOf(ParityMode.OwnerPreview)

        composeTestRule.setContent {
            AskITTheme {
                when (mode.value) {
                    ParityMode.OwnerPreview -> ProfileRoute(
                        profile = formBSession,
                        jobs = emptyList(),
                        onEditProfile = {},
                        onEditListing = {},
                        onUploadWork = {},
                        onOpenJob = {},
                        onViewAllJobs = {},
                        onSaveAbout = {},
                        onSaveLookingFor = {},
                        onSaveSkills = {},
                        onSaveAvailability = {},
                        onSetAvatar = {},
                        onAddLicense = {},
                        onUsernameCopied = {},
                    )
                    ParityMode.Visitor -> UserProfileScreen(
                        userId = "pro-1",
                        onBack = {},
                    )
                }
            }
        }

        // Verify Owner Preview
        composeTestRule.onNodeWithTag("profile_action_view_as_public").performClick()

        // In preview mode:
        composeTestRule.onNodeWithTag("profile_preview_banner").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_action_message").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_action_follow").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_action_request_service").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_metric_rating").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_metric_completed_jobs").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_tab_services").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_tab_gallery").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_camera").assertDoesNotExist()
        composeTestRule.onNodeWithTag("profile_complete_form_b_banner").assertDoesNotExist()

        // Verify Visitor Screen
        mode.value = ParityMode.Visitor
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("profile_action_message").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_action_follow").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_action_request_service").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_metric_rating").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_metric_completed_jobs").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_tab_services").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_tab_gallery").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_camera").assertDoesNotExist()
        composeTestRule.onNodeWithTag("profile_complete_form_b_banner").assertDoesNotExist()
        composeTestRule.onNodeWithTag("profile_preview_banner").assertDoesNotExist()
    }

    // =========================================================================
    // 4. Form A Semantic & Structural Parity (Visitor Protection)
    // =========================================================================

    @Test
    fun formA_semanticParity_strictVisitorProtectionAcrossBothScreens() {
        val memberData = getUserProfileById("member-1")

        val formASession = SessionProfile(
            displayName = memberData.name,
            username = memberData.username,
            bio = memberData.bio,
            city = memberData.location,
            joinedYear = "2023",
            hasListedService = false,
            followerCount = memberData.followerCount,
            followingCount = memberData.followingCount,
        )

        val mode = mutableStateOf(ParityMode.OwnerPreview)

        composeTestRule.setContent {
            AskITTheme {
                when (mode.value) {
                    ParityMode.OwnerPreview -> ProfileRoute(
                        profile = formASession,
                        jobs = emptyList(),
                        onEditProfile = {},
                        onEditListing = {},
                        onUploadWork = {},
                        onOpenJob = {},
                        onViewAllJobs = {},
                        onSaveAbout = {},
                        onSaveLookingFor = {},
                        onSaveSkills = {},
                        onSaveAvailability = {},
                        onSetAvatar = {},
                        onAddLicense = {},
                        onUsernameCopied = {},
                    )
                    ParityMode.Visitor -> UserProfileScreen(
                        userId = "member-1",
                        onBack = {},
                    )
                }
            }
        }

        // Verify Owner Preview
        composeTestRule.onNodeWithTag("profile_action_view_as_public").performClick()

        // Strict Form A checks in preview:
        composeTestRule.onNodeWithTag("profile_action_request_service").assertDoesNotExist()
        composeTestRule.onNodeWithTag("profile_complete_form_b_banner").assertDoesNotExist()
        composeTestRule.onNodeWithTag("profile_camera").assertDoesNotExist()
        composeTestRule.onNodeWithTag("profile_tab_services").assertDoesNotExist()
        composeTestRule.onNodeWithTag("profile_tab_gallery").assertDoesNotExist()
        composeTestRule.onNodeWithTag("profile_metric_activity").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_tab_activity").assertIsDisplayed()

        // Verify Visitor Screen
        mode.value = ParityMode.Visitor
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("profile_action_request_service").assertDoesNotExist()
        composeTestRule.onNodeWithTag("profile_complete_form_b_banner").assertDoesNotExist()
        composeTestRule.onNodeWithTag("profile_camera").assertDoesNotExist()
        composeTestRule.onNodeWithTag("profile_tab_services").assertDoesNotExist()
        composeTestRule.onNodeWithTag("profile_tab_gallery").assertDoesNotExist()
        composeTestRule.onNodeWithTag("profile_metric_activity").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_tab_activity").assertIsDisplayed()
    }
}
