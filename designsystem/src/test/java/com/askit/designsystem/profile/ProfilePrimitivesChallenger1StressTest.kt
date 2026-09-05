package com.askit.designsystem.profile

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.askit.designsystem.R
import com.askit.designsystem.theme.AskITTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Adversarial stress test suite authored by Challenger 1.
 * Evaluates empty inputs, boundary values, null actions, rapid interactions,
 * and layout constraints across all Milestone 1 design system profile primitives.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ProfilePrimitivesChallenger1StressTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // =========================================================================
    // 1. ProfileMetricsBar Stress Tests
    // =========================================================================

    @Test
    fun metricsBar_emptyList_rendersGracefullyWithoutCrash() {
        composeTestRule.setContent {
            AskITTheme {
                ProfileMetricsBar(metrics = emptyList())
            }
        }

        composeTestRule.onNodeWithTag("profile_metrics_bar").assertIsDisplayed()
        composeTestRule.onAllNodesWithTag("profile_metric_").assertCountEquals(0)
    }

    @Test
    fun metricsBar_singleItem_rendersFullWidthWithoutDividers() {
        var clicked = false
        val singleMetric = listOf(
            ProfileMetricItem(
                id = "reputation",
                value = "100%",
                label = "Reputation",
                onClick = { clicked = true },
            ),
        )

        composeTestRule.setContent {
            AskITTheme {
                ProfileMetricsBar(metrics = singleMetric)
            }
        }

        composeTestRule.onNodeWithTag("profile_metrics_bar").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_metric_reputation")
            .assertIsDisplayed()
            .performClick()
        assertEquals(true, clicked)
        composeTestRule.onNodeWithText("100%").assertIsDisplayed()
        composeTestRule.onNodeWithText("Reputation").assertIsDisplayed()
    }

    @Test
    fun metricsBar_extremeNumbersAndSymbols_rendersAccurately() {
        val extremeMetrics = listOf(
            ProfileMetricItem(id = "huge", value = "999999+", label = "Views"),
            ProfileMetricItem(id = "decimal", value = "4.999 ★", label = "Rating"),
            ProfileMetricItem(id = "negative", value = "-50", label = "Credits"),
            ProfileMetricItem(id = "zero", value = "0", label = "Pending"),
        )

        composeTestRule.setContent {
            AskITTheme {
                ProfileMetricsBar(metrics = extremeMetrics)
            }
        }

        composeTestRule.onNodeWithTag("profile_metric_huge").assertIsDisplayed()
        composeTestRule.onNodeWithText("999999+").assertIsDisplayed()
        composeTestRule.onNodeWithText("Views").assertIsDisplayed()

        composeTestRule.onNodeWithTag("profile_metric_decimal").assertIsDisplayed()
        composeTestRule.onNodeWithText("4.999 ★").assertIsDisplayed()

        composeTestRule.onNodeWithTag("profile_metric_negative").assertIsDisplayed()
        composeTestRule.onNodeWithText("-50").assertIsDisplayed()

        composeTestRule.onNodeWithTag("profile_metric_zero").assertIsDisplayed()
        composeTestRule.onNodeWithText("0").assertIsDisplayed()
    }

    @Test
    fun metricsBar_longLabelsAndCustomTints_rendersWithoutCrash() {
        val longLabelMetrics = listOf(
            ProfileMetricItem(
                id = "metric_1",
                value = "128",
                label = "Community Assistance Points",
                iconRes = R.drawable.ic_verified,
                iconTint = Color.Magenta,
            ),
            ProfileMetricItem(
                id = "metric_2",
                value = "34",
                label = "Active Neighborhood Collaborations",
                iconRes = null,
                iconTint = null,
            ),
        )

        composeTestRule.setContent {
            AskITTheme {
                ProfileMetricsBar(metrics = longLabelMetrics)
            }
        }

        composeTestRule.onNodeWithTag("profile_metric_metric_1").assertIsDisplayed()
        composeTestRule.onNodeWithText("Community Assistance Points").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_metric_metric_2").assertIsDisplayed()
        composeTestRule.onNodeWithText("Active Neighborhood Collaborations").assertIsDisplayed()
    }

    @Test
    fun metricsBar_rapidClicks_invokesCallbackConsistently() {
        var clickCount = 0
        val metric = listOf(
            ProfileMetricItem(
                id = "clicks",
                value = "0",
                label = "Clicks",
                onClick = { clickCount++ },
            ),
        )

        composeTestRule.setContent {
            AskITTheme {
                ProfileMetricsBar(metrics = metric)
            }
        }

        val node = composeTestRule.onNodeWithTag("profile_metric_clicks")
        repeat(20) {
            node.performClick()
        }

        assertEquals(20, clickCount)
    }

    // =========================================================================
    // 2. ProfileIdentityBlock Stress Tests
    // =========================================================================

    @Test
    fun identityBlock_nullBio_omitsBioNode() {
        composeTestRule.setContent {
            AskITTheme {
                ProfileIdentityBlock(
                    displayName = "Kavitha S",
                    localityLine = "Coimbatore",
                    bio = null,
                )
            }
        }
        composeTestRule.onNodeWithTag("profile_display_name").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_bio").assertDoesNotExist()
    }

    @Test
    fun identityBlock_emptyBio_omitsBioNode() {
        composeTestRule.setContent {
            AskITTheme {
                ProfileIdentityBlock(
                    displayName = "Kavitha S",
                    localityLine = "Coimbatore",
                    bio = "",
                )
            }
        }
        composeTestRule.onNodeWithTag("profile_display_name").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_bio").assertDoesNotExist()
    }

    @Test
    fun identityBlock_blankWhitespaceBio_omitsBioNode() {
        composeTestRule.setContent {
            AskITTheme {
                ProfileIdentityBlock(
                    displayName = "Kavitha S",
                    localityLine = "Coimbatore",
                    bio = "   ",
                )
            }
        }
        composeTestRule.onNodeWithTag("profile_display_name").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_bio").assertDoesNotExist()
    }

    @Test
    fun identityBlock_blankTradeHeadline_omitsTradeHeadlineNode() {
        composeTestRule.setContent {
            AskITTheme {
                ProfileIdentityBlock(
                    displayName = "Vimalan",
                    localityLine = "Peelamedu, Coimbatore",
                    tradeHeadline = "   ",
                    isVerified = false,
                )
            }
        }
        composeTestRule.onNodeWithTag("profile_trade_headline").assertDoesNotExist()
        composeTestRule.onNodeWithTag("profile_verified_badge").assertDoesNotExist()
    }

    @Test
    fun identityBlock_emptyLocality_rendersWithoutCrash() {
        composeTestRule.setContent {
            AskITTheme {
                ProfileIdentityBlock(
                    displayName = "Suresh",
                    localityLine = "",
                )
            }
        }
        composeTestRule.onNodeWithTag("profile_locality").assertIsDisplayed()
    }

    @Test
    fun identityBlock_extremeLengths_rendersGracefullyAtNarrowWidth() {
        val superLongName = "Dr. Bartholomew Alexander Montgomery-Wellington III of South Coimbatore"
        val superLongHeadline = "Senior Master Licensed High-Voltage Commercial & Residential Electrical Infrastructure Contractor"
        val superLongBio = "Over 25 years of specialized field experience diagnosing complex domestic wiring faults, repairing industrial transformers, mentoring apprentice electricians, and serving residential housing complexes."
        val superLongLocality = "Flat 12B, Royal Palms Enclave, Phase 4, Cross Cut Road, Gandhipuram, Coimbatore, Tamil Nadu, 641012 · Joined January 2018"

        composeTestRule.setContent {
            AskITTheme {
                Box(modifier = Modifier.width(320.dp)) {
                    ProfileIdentityBlock(
                        displayName = superLongName,
                        localityLine = superLongLocality,
                        tradeHeadline = superLongHeadline,
                        isVerified = true,
                        bio = superLongBio,
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

    // =========================================================================
    // 3. ProfileActionRow Stress Tests
    // =========================================================================

    @Test
    fun actionRow_visitorFormA_nullRequestService_omitsCTA() {
        var messageCalls = 0
        var followCalls = 0

        val config = ProfileActionConfig.Visitor(
            onMessage = { messageCalls++ },
            isFollowing = false,
            onToggleFollow = { followCalls++ },
            onRequestService = null, // Form A: no request service button
        )

        composeTestRule.setContent {
            AskITTheme {
                ProfileActionRow(config = config)
            }
        }

        composeTestRule.onNodeWithTag("profile_action_message")
            .assertIsDisplayed()
            .performClick()
        assertEquals(1, messageCalls)

        composeTestRule.onNodeWithTag("profile_action_follow")
            .assertIsDisplayed()
            .assertTextEquals("Follow")
            .performClick()
        assertEquals(1, followCalls)

        // Request Service MUST NOT exist for Form A visitor
        composeTestRule.onNodeWithTag("profile_action_request_service").assertDoesNotExist()
    }

    @Test
    fun actionRow_visitorFormB_withRequestService_invokesCallback() {
        var requestCalls = 0

        val config = ProfileActionConfig.Visitor(
            onMessage = {},
            isFollowing = true,
            onToggleFollow = {},
            onRequestService = { requestCalls++ }, // Form B provider
        )

        composeTestRule.setContent {
            AskITTheme {
                ProfileActionRow(config = config)
            }
        }

        composeTestRule.onNodeWithTag("profile_action_follow")
            .assertIsDisplayed()
            .assertTextEquals("Following")

        val requestNode = composeTestRule.onNodeWithTag("profile_action_request_service")
        requestNode.assertIsDisplayed()
        repeat(5) {
            requestNode.performClick()
        }
        assertEquals(5, requestCalls)
    }

    @Test
    fun actionRow_ownerActions_rapidClicksInvokeCallbacks() {
        var editCount = 0
        var shareCount = 0
        var previewCount = 0

        val config = ProfileActionConfig.Owner(
            onEditProfile = { editCount++ },
            onShare = { shareCount++ },
            onViewAsPublic = { previewCount++ },
        )

        composeTestRule.setContent {
            AskITTheme {
                ProfileActionRow(config = config)
            }
        }

        val editNode = composeTestRule.onNodeWithTag("profile_action_edit")
        val shareNode = composeTestRule.onNodeWithTag("profile_action_share")
        val previewNode = composeTestRule.onNodeWithTag("profile_action_view_as_public")

        editNode.assertIsDisplayed()
        shareNode.assertIsDisplayed()
        previewNode.assertIsDisplayed()

        repeat(3) { editNode.performClick() }
        repeat(4) { shareNode.performClick() }
        repeat(5) { previewNode.performClick() }

        assertEquals(3, editCount)
        assertEquals(4, shareCount)
        assertEquals(5, previewCount)
    }

    @Test
    fun actionRow_narrow320dpWidth_rendersAllButtonsWithoutCrash() {
        val ownerConfig = ProfileActionConfig.Owner(
            onEditProfile = {},
            onShare = {},
            onViewAsPublic = {},
        )

        composeTestRule.setContent {
            AskITTheme {
                Box(modifier = Modifier.width(320.dp)) {
                    ProfileActionRow(config = ownerConfig)
                }
            }
        }

        composeTestRule.onNodeWithTag("profile_action_edit").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_action_share").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_action_view_as_public").assertIsDisplayed()
    }

    // =========================================================================
    // 4. ProfilePreviewBanner & CompleteFormBBanner Stress Tests
    // =========================================================================

    @Test
    fun previewBanner_rapidClicks_invokesCallbackReliably() {
        var exitCount = 0

        composeTestRule.setContent {
            AskITTheme {
                Box(modifier = Modifier.width(320.dp)) {
                    ProfilePreviewBanner(onExitPreview = { exitCount++ })
                }
            }
        }

        composeTestRule.onNodeWithTag("profile_preview_banner").assertIsDisplayed()
        composeTestRule.onNodeWithText("Viewing as Public").assertIsDisplayed()

        val exitButton = composeTestRule.onNodeWithTag("profile_exit_preview")
        exitButton.assertIsDisplayed()
        repeat(10) { exitButton.performClick() }

        assertEquals(10, exitCount)
    }

    @Test
    fun completeFormBBanner_rapidClicks_invokesCallbackReliably() {
        var cardClicks = 0

        composeTestRule.setContent {
            AskITTheme {
                Box(modifier = Modifier.width(320.dp)) {
                    CompleteFormBBanner(onClick = { cardClicks++ })
                }
            }
        }

        val banner = composeTestRule.onNodeWithTag("profile_complete_form_b_banner")
        banner.assertIsDisplayed()
        composeTestRule.onNodeWithText("Offer services in your neighborhood").assertIsDisplayed()
        composeTestRule.onNodeWithText("Add Service Card").assertIsDisplayed()

        repeat(10) { banner.performClick() }

        assertEquals(10, cardClicks)
    }
}
