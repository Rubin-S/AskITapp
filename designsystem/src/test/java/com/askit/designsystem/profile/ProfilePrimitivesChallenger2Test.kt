package com.askit.designsystem.profile

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.askit.designsystem.R
import com.askit.designsystem.theme.AskITTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ProfilePrimitivesChallenger2Test {

    @get:Rule
    val composeTestRule = createComposeRule()

    // ---------------------------------------------------------------------------------------------
    // VECTOR 1: Accessibility Test Tags Verification
    // ---------------------------------------------------------------------------------------------

    @Test
    fun accessibilityTags_allSpecifiedTagsPresent() {
        var editClicked = false
        var shareClicked = false
        var previewClicked = false
        var exitClicked = false
        var bannerClicked = false
        var metricClicked = false

        val metrics = listOf(
            ProfileMetricItem(id = "activity", value = "12", label = "Activity", onClick = { metricClicked = true }),
            ProfileMetricItem(id = "followers", value = "450", label = "Followers"),
            ProfileMetricItem(id = "following", value = "180", label = "Following"),
        )

        composeTestRule.setContent {
            AskITTheme {
                Column {
                    ProfileMetricsBar(metrics = metrics)
                    ProfileIdentityBlock(
                        displayName = "Empirical Tester",
                        localityLine = "Coimbatore, India",
                        tradeHeadline = "Master Electrician",
                        isVerified = true,
                        bio = "Professional tester and electrician.",
                    )
                    ProfileActionRow(
                        config = ProfileActionConfig.Owner(
                            onEditProfile = { editClicked = true },
                            onShare = { shareClicked = true },
                            onViewAsPublic = { previewClicked = true },
                        ),
                    )
                    ProfilePreviewBanner(
                        onExitPreview = { exitClicked = true },
                    )
                    CompleteFormBBanner(
                        onClick = { bannerClicked = true },
                    )
                }
            }
        }

        // 1. profile_metrics_bar
        composeTestRule.onNodeWithTag("profile_metrics_bar").assertIsDisplayed()

        // 2. profile_metric_*
        composeTestRule.onNodeWithTag("profile_metric_activity").assertIsDisplayed().performClick()
        assertTrue(metricClicked)
        composeTestRule.onNodeWithTag("profile_metric_followers").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_metric_following").assertIsDisplayed()

        // 3. profile_verified_badge
        composeTestRule.onNodeWithTag("profile_verified_badge").assertIsDisplayed()

        // 4. profile_trade_headline
        composeTestRule.onNodeWithTag("profile_trade_headline").assertIsDisplayed().assertTextEquals("Master Electrician")

        // 5. profile_action_edit
        composeTestRule.onNodeWithTag("profile_action_edit").assertIsDisplayed().performClick()
        assertTrue(editClicked)

        // 6. profile_action_share
        composeTestRule.onNodeWithTag("profile_action_share").assertIsDisplayed().performClick()
        assertTrue(shareClicked)

        // 7. profile_action_view_as_public
        composeTestRule.onNodeWithTag("profile_action_view_as_public").assertIsDisplayed().performClick()
        assertTrue(previewClicked)

        // 8. profile_preview_banner
        composeTestRule.onNodeWithTag("profile_preview_banner").assertIsDisplayed()

        // 9. profile_exit_preview
        composeTestRule.onNodeWithTag("profile_exit_preview").assertIsDisplayed().performClick()
        assertTrue(exitClicked)

        // 10. profile_complete_form_b_banner
        composeTestRule.onNodeWithTag("profile_complete_form_b_banner").assertIsDisplayed().performClick()
        assertTrue(bannerClicked)
    }

    @Test
    fun accessibilityTags_visitorTags() {
        var messageClicked = false
        var followClicked = false
        var requestServiceClicked = false

        composeTestRule.setContent {
            AskITTheme {
                ProfileActionRow(
                    config = ProfileActionConfig.Visitor(
                        onMessage = { messageClicked = true },
                        isFollowing = false,
                        onToggleFollow = { followClicked = true },
                        onRequestService = { requestServiceClicked = true },
                    ),
                )
            }
        }

        // profile_action_message
        composeTestRule.onNodeWithTag("profile_action_message").assertIsDisplayed().performClick()
        assertTrue(messageClicked)

        // profile_action_follow
        composeTestRule.onNodeWithTag("profile_action_follow").assertIsDisplayed().performClick()
        assertTrue(followClicked)

        // profile_action_request_service
        composeTestRule.onNodeWithTag("profile_action_request_service").assertIsDisplayed().performClick()
        assertTrue(requestServiceClicked)
    }

    // ---------------------------------------------------------------------------------------------
    // VECTOR 2: Backward Compatibility with Existing Callers
    // ---------------------------------------------------------------------------------------------

    @Test
    @Suppress("DEPRECATION")
    fun backwardCompatibility_legacyProfileIdentityBlock() {
        composeTestRule.setContent {
            AskITTheme {
                ProfileIdentityBlock(
                    displayName = "Arun Kumar",
                    bio = "Plumber in Coimbatore",
                    locationLine = "Gandhipuram, Coimbatore · Joined 2022",
                )
            }
        }

        composeTestRule.onNodeWithTag("profile_identity_block").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_display_name").assertIsDisplayed().assertTextEquals("Arun Kumar")
        composeTestRule.onNodeWithTag("profile_bio").assertIsDisplayed().assertTextEquals("Plumber in Coimbatore")
        composeTestRule.onNodeWithTag("profile_locality").assertIsDisplayed()
        composeTestRule.onNodeWithText("Gandhipuram, Coimbatore · Joined 2022").assertIsDisplayed()

        composeTestRule.onNodeWithTag("profile_verified_badge").assertDoesNotExist()
        composeTestRule.onNodeWithTag("profile_trade_headline").assertDoesNotExist()
    }

    @Test
    @Suppress("DEPRECATION")
    fun backwardCompatibility_legacyProfileActionRow_withAvailability() {
        var editClicked = false
        var availabilityClicked = false

        composeTestRule.setContent {
            AskITTheme {
                ProfileActionRow(
                    editLabel = "Edit profile",
                    onEdit = { editClicked = true },
                    availabilityLabel = "Set Availability",
                    onAvailability = { availabilityClicked = true },
                )
            }
        }

        // Legacy callers assert profile_edit tag
        composeTestRule.onNodeWithTag("profile_edit").assertIsDisplayed().performClick()
        assertTrue(editClicked)

        composeTestRule.onNodeWithTag("profile_availability").assertIsDisplayed().performClick()
        assertTrue(availabilityClicked)
    }

    @Test
    @Suppress("DEPRECATION")
    fun backwardCompatibility_legacyProfileActionRow_withoutAvailability() {
        composeTestRule.setContent {
            AskITTheme {
                ProfileActionRow(
                    editLabel = "Edit profile",
                    onEdit = {},
                    availabilityLabel = null,
                    onAvailability = null,
                )
            }
        }

        composeTestRule.onNodeWithTag("profile_edit").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_availability").assertDoesNotExist()
    }

    // ---------------------------------------------------------------------------------------------
    // VECTOR 3: Theme / Dark Mode Consistency
    // ---------------------------------------------------------------------------------------------

    @Test
    fun darkMode_allPrimitivesRenderCorrectlyWithoutCrash() {
        var clickedExit = false
        var clickedService = false

        val metrics = listOf(
            ProfileMetricItem(id = "rating", value = "4.95", label = "Rating", iconRes = R.drawable.ic_star_filled),
            ProfileMetricItem(id = "completed_jobs", value = "52", label = "Completed"),
            ProfileMetricItem(id = "followers", value = "200", label = "Followers"),
            ProfileMetricItem(id = "following", value = "110", label = "Following"),
        )

        composeTestRule.setContent {
            AskITTheme(darkTheme = true) {
                Column {
                    ProfileMetricsBar(metrics = metrics)
                    ProfileIdentityBlock(
                        displayName = "Dark Mode User",
                        localityLine = "RS Puram, Coimbatore",
                        tradeHeadline = "Certified Carpenter",
                        isVerified = true,
                        bio = "Dark theme bio test",
                    )
                    ProfileActionRow(
                        config = ProfileActionConfig.Visitor(
                            onMessage = {},
                            isFollowing = true,
                            onToggleFollow = {},
                            onRequestService = { clickedService = true },
                        ),
                    )
                    ProfilePreviewBanner(
                        onExitPreview = { clickedExit = true },
                    )
                    CompleteFormBBanner(
                        onClick = {},
                    )
                }
            }
        }

        // Verify elements are displayed and operable in dark mode
        composeTestRule.onNodeWithTag("profile_metrics_bar").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_metric_rating").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_identity_block").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_verified_badge").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_action_request_service").assertIsDisplayed().performClick()
        assertTrue(clickedService)
        composeTestRule.onNodeWithTag("profile_preview_banner").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_exit_preview").assertIsDisplayed().performClick()
        assertTrue(clickedExit)
        composeTestRule.onNodeWithTag("profile_complete_form_b_banner").assertIsDisplayed()
    }

    // ---------------------------------------------------------------------------------------------
    // VECTOR 4: Layout Constraint Stress at 320dp Width
    // ---------------------------------------------------------------------------------------------

    @Test
    fun layoutConstraint_320dpWidth_ownerActionRowDoesNotCrashOrClip() {
        var editCalled = false
        var shareCalled = false
        var viewCalled = false

        composeTestRule.setContent {
            AskITTheme {
                Box(modifier = Modifier.width(320.dp)) {
                    ProfileActionRow(
                        config = ProfileActionConfig.Owner(
                            onEditProfile = { editCalled = true },
                            onShare = { shareCalled = true },
                            onViewAsPublic = { viewCalled = true },
                        ),
                    )
                }
            }
        }

        composeTestRule.onNodeWithTag("profile_action_edit").assertIsDisplayed().performClick()
        assertTrue(editCalled)
        composeTestRule.onNodeWithTag("profile_action_share").assertIsDisplayed().performClick()
        assertTrue(shareCalled)
        composeTestRule.onNodeWithTag("profile_action_view_as_public").assertIsDisplayed().performClick()
        assertTrue(viewCalled)
    }

    @Test
    fun layoutConstraint_320dpWidth_visitorFormBActionRowDoesNotCrash() {
        var serviceCalled = false

        composeTestRule.setContent {
            AskITTheme {
                Box(modifier = Modifier.width(320.dp)) {
                    ProfileActionRow(
                        config = ProfileActionConfig.Visitor(
                            onMessage = {},
                            isFollowing = false,
                            onToggleFollow = {},
                            onRequestService = { serviceCalled = true },
                        ),
                    )
                }
            }
        }

        composeTestRule.onNodeWithTag("profile_action_message").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_action_follow").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_action_request_service").assertIsDisplayed().performClick()
        assertTrue(serviceCalled)
    }

    @Test
    fun layoutConstraint_320dpWidth_metricsBarFourItemsDoesNotCrash() {
        val formBMetrics = listOf(
            ProfileMetricItem(id = "rating", value = "4.9 (128)", label = "Rating", iconRes = R.drawable.ic_star_filled),
            ProfileMetricItem(id = "completed_jobs", value = "128", label = "Completed"),
            ProfileMetricItem(id = "followers", value = "1,450", label = "Followers"),
            ProfileMetricItem(id = "following", value = "320", label = "Following"),
        )

        composeTestRule.setContent {
            AskITTheme {
                Box(modifier = Modifier.width(320.dp)) {
                    ProfileMetricsBar(metrics = formBMetrics)
                }
            }
        }

        composeTestRule.onNodeWithTag("profile_metric_rating").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_metric_completed_jobs").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_metric_followers").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_metric_following").assertIsDisplayed()
    }

    @Test
    fun layoutConstraint_320dpWidth_previewBannerDoesNotCrash() {
        composeTestRule.setContent {
            AskITTheme {
                Box(modifier = Modifier.width(320.dp)) {
                    ProfilePreviewBanner(onExitPreview = {})
                }
            }
        }
        composeTestRule.onNodeWithTag("profile_preview_banner").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_exit_preview").assertIsDisplayed()
    }

    @Test
    fun layoutConstraint_320dpWidth_completeFormBBannerDoesNotCrash() {
        composeTestRule.setContent {
            AskITTheme {
                Box(modifier = Modifier.width(320.dp)) {
                    CompleteFormBBanner(onClick = {})
                }
            }
        }
        composeTestRule.onNodeWithTag("profile_complete_form_b_banner").assertIsDisplayed()
    }

    // ---------------------------------------------------------------------------------------------
    // VECTOR 5: Edge Cases & Boundary Conditions
    // ---------------------------------------------------------------------------------------------

    @Test
    fun edgeCases_emptyMetricsList_rendersWithoutError() {
        composeTestRule.setContent {
            AskITTheme {
                ProfileMetricsBar(metrics = emptyList())
            }
        }

        composeTestRule.onNodeWithTag("profile_metrics_bar").assertIsDisplayed()
    }

    @Test
    fun edgeCases_blankStringsInIdentityBlock_handledGracefully() {
        composeTestRule.setContent {
            AskITTheme {
                ProfileIdentityBlock(
                    displayName = "Blank String Test",
                    localityLine = "Coimbatore",
                    tradeHeadline = "   ",
                    isVerified = false,
                    bio = "   ",
                )
            }
        }

        composeTestRule.onNodeWithTag("profile_display_name").assertIsDisplayed().assertTextEquals("Blank String Test")
        composeTestRule.onNodeWithTag("profile_trade_headline").assertDoesNotExist()
        composeTestRule.onNodeWithTag("profile_bio").assertDoesNotExist()
        composeTestRule.onNodeWithTag("profile_verified_badge").assertDoesNotExist()
    }

    @Test
    fun edgeCases_extremelyLongStringsInIdentityBlock_doesNotCrash() {
        val veryLongName = "Dr. Bartholomew Bartholomew-Montgomery III of North Coimbatore"
        val veryLongHeadline = "Senior Master Residential and Commercial Electrical Systems Installation Engineer"
        val veryLongBio = "Extremely long biography detailing over twenty years of exceptional craftsmanship across Tamil Nadu."
        val veryLongLocality = "Flat 402, Block B, Singanallur, Coimbatore, Tamil Nadu · Joined 2019"

        composeTestRule.setContent {
            AskITTheme {
                Box(modifier = Modifier.width(320.dp)) {
                    ProfileIdentityBlock(
                        displayName = veryLongName,
                        localityLine = veryLongLocality,
                        tradeHeadline = veryLongHeadline,
                        isVerified = true,
                        bio = veryLongBio,
                    )
                }
            }
        }

        composeTestRule.onNodeWithTag("profile_display_name").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_verified_badge").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_trade_headline").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_bio").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_locality").assertIsDisplayed()
    }
}
