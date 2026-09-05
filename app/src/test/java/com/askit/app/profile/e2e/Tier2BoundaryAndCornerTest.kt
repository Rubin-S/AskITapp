package com.askit.app.profile.e2e

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
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
 * Tier 2: Boundary & Corner Cases Test Suite
 *
 * Covers boundary values and corner conditions:
 * - Zero followers/following & extreme counts (100k, 1M, Int.MAX_VALUE)
 * - Max ratings (5.0★) vs min (0.0) vs unrated/null
 * - Completed jobs boundaries (0 vs 1 vs thousands vs null)
 * - Empty bio, whitespace bio, missing avatar url (null/empty/fallback)
 * - Extremely long display names, long localities, long trade headlines
 * - Special characters, emojis, punctuation across bio and name
 * - Rapid preview toggle on/off, state persistence across preview
 *
 * Exactly 35 test cases.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w360dp-h800dp-normal-long-notround-any-xxxhdpi-keyshidden-nonav")
class Tier2BoundaryAndCornerTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // =========================================================================
    // Group 1: Followers & Following Boundaries (6 tests)
    // =========================================================================

    @Test
    fun t2_boundary_zeroFollowers_rendersZeroWithoutCrashing() {
        composeTestRule.setContent {
            AskITTheme {
                ProfileE2ETestScaffold(state = ProfileE2EState(followerCount = 0))
            }
        }
        composeTestRule.onNodeWithTag("profile_metric_followers").assertIsDisplayed()
        composeTestRule.onNodeWithText("0").assertIsDisplayed()
    }

    @Test
    fun t2_boundary_zeroFollowing_rendersZeroWithoutCrashing() {
        composeTestRule.setContent {
            AskITTheme {
                ProfileE2ETestScaffold(state = ProfileE2EState(followingCount = 0))
            }
        }
        composeTestRule.onNodeWithTag("profile_metric_following").assertIsDisplayed()
        composeTestRule.onNodeWithText("0").assertIsDisplayed()
    }

    @Test
    fun t2_boundary_zeroFollowersAndFollowing_bothDisplayedAccurately() {
        composeTestRule.setContent {
            AskITTheme {
                ProfileE2ETestScaffold(state = ProfileE2EState(followerCount = 0, followingCount = 0))
            }
        }
        composeTestRule.onNodeWithTag("profile_metric_followers").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_metric_following").assertIsDisplayed()
    }

    @Test
    fun t2_boundary_largeFollowerCount_100k_rendersCorrectly() {
        composeTestRule.setContent {
            AskITTheme {
                ProfileE2ETestScaffold(state = ProfileE2EState(followerCount = 100_000))
            }
        }
        composeTestRule.onNodeWithTag("profile_metric_followers").assertIsDisplayed()
        composeTestRule.onNodeWithText("100000").assertIsDisplayed()
    }

    @Test
    fun t2_boundary_millionFollowers_rendersWithoutOverflow() {
        composeTestRule.setContent {
            AskITTheme {
                ProfileE2ETestScaffold(state = ProfileE2EState(followerCount = 1_000_000))
            }
        }
        composeTestRule.onNodeWithTag("profile_metric_followers").assertIsDisplayed()
        composeTestRule.onNodeWithText("1000000").assertIsDisplayed()
    }

    @Test
    fun t2_boundary_maxIntegerFollowers_doesNotCrash() {
        composeTestRule.setContent {
            AskITTheme {
                ProfileE2ETestScaffold(state = ProfileE2EState(followerCount = Int.MAX_VALUE))
            }
        }
        composeTestRule.onNodeWithTag("profile_metric_followers").assertIsDisplayed()
        composeTestRule.onNodeWithText(Int.MAX_VALUE.toString()).assertIsDisplayed()
    }

    // =========================================================================
    // Group 2: Rating & Review Count Boundaries (Form B) (5 tests)
    // =========================================================================

    @Test
    fun t2_boundary_maxRating_5_0_rendersStarAndValue() {
        composeTestRule.setContent {
            AskITTheme {
                ProfileE2ETestScaffold(state = ProfileE2EState(isProvider = true, rating = 5.0))
            }
        }
        composeTestRule.onNodeWithTag("profile_metric_rating").assertIsDisplayed()
        composeTestRule.onNodeWithText("★ 5.0").assertIsDisplayed()
    }

    @Test
    fun t2_boundary_minRating_0_0_rendersAccurately() {
        composeTestRule.setContent {
            AskITTheme {
                ProfileE2ETestScaffold(state = ProfileE2EState(isProvider = true, rating = 0.0))
            }
        }
        composeTestRule.onNodeWithTag("profile_metric_rating").assertIsDisplayed()
        composeTestRule.onNodeWithText("★ 0.0").assertIsDisplayed()
    }

    @Test
    fun t2_boundary_nullRating_rendersPlaceholder() {
        composeTestRule.setContent {
            AskITTheme {
                ProfileE2ETestScaffold(state = ProfileE2EState(isProvider = true, rating = null))
            }
        }
        composeTestRule.onNodeWithTag("profile_metric_rating").assertIsDisplayed()
        composeTestRule.onNodeWithText("★ —").assertIsDisplayed()
    }

    @Test
    fun t2_boundary_fractionalRating_4_85_formatsAccurately() {
        composeTestRule.setContent {
            AskITTheme {
                ProfileE2ETestScaffold(state = ProfileE2EState(isProvider = true, rating = 4.85))
            }
        }
        composeTestRule.onNodeWithTag("profile_metric_rating").assertIsDisplayed()
        composeTestRule.onNodeWithText("★ 4.9").assertIsDisplayed()
    }

    @Test
    fun t2_boundary_largeReviewRating_rendersCleanly() {
        composeTestRule.setContent {
            AskITTheme {
                ProfileE2ETestScaffold(
                    state = ProfileE2EState(isProvider = true, rating = 4.95, completedJobsCount = 999),
                )
            }
        }
        composeTestRule.onNodeWithTag("profile_metric_rating").assertIsDisplayed()
        composeTestRule.onNodeWithText("999").assertIsDisplayed()
    }

    // =========================================================================
    // Group 3: Completed Jobs Boundaries (Form B) (4 tests)
    // =========================================================================

    @Test
    fun t2_boundary_zeroCompletedJobs_rendersZero() {
        composeTestRule.setContent {
            AskITTheme {
                ProfileE2ETestScaffold(state = ProfileE2EState(isProvider = true, completedJobsCount = 0))
            }
        }
        composeTestRule.onNodeWithTag("profile_metric_jobs").assertIsDisplayed()
        composeTestRule.onNodeWithText("0").assertIsDisplayed()
    }

    @Test
    fun t2_boundary_singleCompletedJob_rendersOne() {
        composeTestRule.setContent {
            AskITTheme {
                ProfileE2ETestScaffold(state = ProfileE2EState(isProvider = true, completedJobsCount = 1))
            }
        }
        composeTestRule.onNodeWithTag("profile_metric_jobs").assertIsDisplayed()
        composeTestRule.onNodeWithText("1").assertIsDisplayed()
    }

    @Test
    fun t2_boundary_thousandCompletedJobs_rendersAccurately() {
        composeTestRule.setContent {
            AskITTheme {
                ProfileE2ETestScaffold(state = ProfileE2EState(isProvider = true, completedJobsCount = 1000))
            }
        }
        composeTestRule.onNodeWithTag("profile_metric_jobs").assertIsDisplayed()
        composeTestRule.onNodeWithText("1000").assertIsDisplayed()
    }

    @Test
    fun t2_boundary_nullCompletedJobs_rendersZeroFallback() {
        composeTestRule.setContent {
            AskITTheme {
                ProfileE2ETestScaffold(state = ProfileE2EState(isProvider = true, completedJobsCount = null))
            }
        }
        composeTestRule.onNodeWithTag("profile_metric_jobs").assertIsDisplayed()
        composeTestRule.onNodeWithText("0").assertIsDisplayed()
    }

    // =========================================================================
    // Group 4: Display Name Boundaries (4 tests)
    // =========================================================================

    @Test
    fun t2_boundary_emptyDisplayName_rendersWithoutCrashing() {
        composeTestRule.setContent {
            AskITTheme {
                ProfileE2ETestScaffold(state = ProfileE2EState(displayName = ""))
            }
        }
        composeTestRule.onNodeWithTag("profile_display_name").assertExists()
    }

    @Test
    fun t2_boundary_singleCharacterDisplayName_renders() {
        composeTestRule.setContent {
            AskITTheme {
                ProfileE2ETestScaffold(state = ProfileE2EState(displayName = "K"))
            }
        }
        composeTestRule.onNodeWithTag("profile_display_name").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_display_name").assertTextEquals("K")
    }

    @Test
    fun t2_boundary_extremelyLongDisplayName_renders() {
        val longName = "A".repeat(100)
        composeTestRule.setContent {
            AskITTheme {
                ProfileE2ETestScaffold(state = ProfileE2EState(displayName = longName))
            }
        }
        composeTestRule.onNodeWithTag("profile_display_name").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_display_name").assertTextContains(longName)
    }

    @Test
    fun t2_boundary_displayNameWithSpecialCharacters_emojis() {
        val specialName = "Ramesh Kumar 🔧⚡️"
        composeTestRule.setContent {
            AskITTheme {
                ProfileE2ETestScaffold(state = ProfileE2EState(displayName = specialName))
            }
        }
        composeTestRule.onNodeWithTag("profile_display_name").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_display_name").assertTextContains(specialName)
    }

    // =========================================================================
    // Group 5: Bio Boundaries (4 tests)
    // =========================================================================

    @Test
    fun t2_boundary_emptyBio_hidesBioNode() {
        composeTestRule.setContent {
            AskITTheme {
                ProfileE2ETestScaffold(state = ProfileE2EState(bio = ""))
            }
        }
        composeTestRule.onNodeWithTag("profile_bio").assertDoesNotExist()
    }

    @Test
    fun t2_boundary_blankWhitespaceBio_hidesBioNode() {
        composeTestRule.setContent {
            AskITTheme {
                ProfileE2ETestScaffold(state = ProfileE2EState(bio = "    \n   "))
            }
        }
        composeTestRule.onNodeWithTag("profile_bio").assertDoesNotExist()
    }

    @Test
    fun t2_boundary_multilineLongBio_rendersCompletely() {
        val multilineBio = "Line 1: Expert in plumbing\nLine 2: 10+ years experience\nLine 3: 24/7 emergency service"
        composeTestRule.setContent {
            AskITTheme {
                ProfileE2ETestScaffold(state = ProfileE2EState(bio = multilineBio))
            }
        }
        composeTestRule.onNodeWithTag("profile_bio").assertExists()
        composeTestRule.onNodeWithTag("profile_bio").assertTextContains("Line 1: Expert in plumbing", substring = true)
    }

    @Test
    fun t2_boundary_bioWithSpecialSymbols_commasDashesAmpersand() {
        val formattedBio = "A & B Services — fast, reliable & guaranteed! (100% satisfaction)"
        composeTestRule.setContent {
            AskITTheme {
                ProfileE2ETestScaffold(state = ProfileE2EState(bio = formattedBio))
            }
        }
        composeTestRule.onNodeWithTag("profile_bio").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_bio").assertTextContains(formattedBio)
    }

    // =========================================================================
    // Group 6: Avatar URL Boundaries (4 tests)
    // =========================================================================

    @Test
    fun t2_boundary_nullAvatarUrl_rendersFallbackAvatar() {
        composeTestRule.setContent {
            AskITTheme {
                ProfileE2ETestScaffold(state = ProfileE2EState(avatarUrl = null))
            }
        }
        composeTestRule.onNodeWithTag("profile_avatar").assertIsDisplayed()
    }

    @Test
    fun t2_boundary_emptyAvatarUrl_rendersFallbackAvatar() {
        composeTestRule.setContent {
            AskITTheme {
                ProfileE2ETestScaffold(state = ProfileE2EState(avatarUrl = ""))
            }
        }
        composeTestRule.onNodeWithTag("profile_avatar").assertIsDisplayed()
    }

    @Test
    fun t2_boundary_malformedUriAvatar_doesNotCrash() {
        composeTestRule.setContent {
            AskITTheme {
                ProfileE2ETestScaffold(state = ProfileE2EState(avatarUrl = "ht!tp://:::invalid"))
            }
        }
        composeTestRule.onNodeWithTag("profile_avatar").assertIsDisplayed()
    }

    @Test
    fun t2_boundary_validAvatarUrl_rendersAvatar() {
        composeTestRule.setContent {
            AskITTheme {
                ProfileE2ETestScaffold(
                    state = ProfileE2EState(avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb"),
                )
            }
        }
        composeTestRule.onNodeWithTag("profile_avatar").assertIsDisplayed()
    }

    // =========================================================================
    // Group 7: Locality & Trade Headline Boundaries (4 tests)
    // =========================================================================

    @Test
    fun t2_boundary_emptyLocality_rendersWithoutCrashing() {
        composeTestRule.setContent {
            AskITTheme {
                ProfileE2ETestScaffold(state = ProfileE2EState(localityLine = ""))
            }
        }
        composeTestRule.onNodeWithTag("profile_locality").assertIsDisplayed()
    }

    @Test
    fun t2_boundary_extremelyLongLocality_renders() {
        val longLocality = "Flat 4B, Tower 12, Express Greens, Sector 78, Noida, Uttar Pradesh, 201301 · Joined 2021"
        composeTestRule.setContent {
            AskITTheme {
                ProfileE2ETestScaffold(state = ProfileE2EState(localityLine = longLocality))
            }
        }
        composeTestRule.onNodeWithTag("profile_locality").assertExists()
        composeTestRule.onNodeWithText("Tower 12", substring = true).assertExists()
    }

    @Test
    fun t2_boundary_nullTradeHeadline_formA_hidesHeadline() {
        composeTestRule.setContent {
            AskITTheme {
                ProfileE2ETestScaffold(state = ProfileE2EState(isProvider = false, tradeHeadline = null))
            }
        }
        composeTestRule.onNodeWithTag("profile_trade_headline").assertDoesNotExist()
    }

    @Test
    fun t2_boundary_emptyTradeHeadline_hidesHeadline() {
        composeTestRule.setContent {
            AskITTheme {
                ProfileE2ETestScaffold(state = ProfileE2EState(isProvider = true, tradeHeadline = ""))
            }
        }
        composeTestRule.onNodeWithTag("profile_trade_headline").assertDoesNotExist()
    }

    // =========================================================================
    // Group 8: Public Preview & State Toggles Corner Cases (4 tests)
    // =========================================================================

    @Test
    fun t2_boundary_rapidPreviewToggle_maintainsStateConsistency() {
        val isPreview = mutableStateOf(false)
        composeTestRule.setContent {
            AskITTheme {
                ProfileE2ETestScaffold(
                    state = ProfileE2EState(isOwner = true, isPublicPreview = isPreview.value),
                    actions = ProfileE2EActions(
                        onViewAsPublic = { isPreview.value = true },
                        onExitPreview = { isPreview.value = false },
                    ),
                )
            }
        }

        // Toggle back and forth 3 times
        repeat(3) {
            composeTestRule.onNodeWithTag("profile_action_view_as_public").performClick()
            composeTestRule.waitForIdle()
            assertTrue(isPreview.value)
            composeTestRule.onNodeWithTag("profile_preview_banner").assertIsDisplayed()

            composeTestRule.onNodeWithTag("profile_exit_preview").performClick()
            composeTestRule.waitForIdle()
            assertFalse(isPreview.value)
            composeTestRule.onNodeWithTag("profile_action_edit").assertIsDisplayed()
        }
    }

    @Test
    fun t2_boundary_formB_previewWithToggleFollow_updatesVisitorState() {
        val isFollowing = mutableStateOf(false)
        composeTestRule.setContent {
            AskITTheme {
                ProfileE2ETestScaffold(
                    state = ProfileE2EState(
                        isOwner = true,
                        isPublicPreview = true,
                        isProvider = true,
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

        assertTrue("Follow state must toggle to true", isFollowing.value)
    }

    @Test
    fun t2_boundary_tabSelectionPreserved_acrossPreviewToggle() {
        val isPreview = mutableStateOf(false)
        val activeTab = mutableStateOf(1) // "About" tab in Form A
        composeTestRule.setContent {
            AskITTheme {
                ProfileE2ETestScaffold(
                    state = ProfileE2EState(
                        isOwner = true,
                        isPublicPreview = isPreview.value,
                        isProvider = false,
                        selectedTabIndex = activeTab.value,
                    ),
                    actions = ProfileE2EActions(
                        onViewAsPublic = { isPreview.value = true },
                        onExitPreview = { isPreview.value = false },
                        onTabSelected = { activeTab.value = it },
                    ),
                )
            }
        }

        // Initially on About tab
        composeTestRule.onNodeWithTag("profile_content_about").assertExists()

        // Enter preview mode
        composeTestRule.onNodeWithTag("profile_action_view_as_public").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("profile_content_about").assertExists()

        // Exit preview mode
        composeTestRule.onNodeWithTag("profile_exit_preview").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("profile_content_about").assertExists()
        assertEquals("Active tab index must be preserved across preview toggle", 1, activeTab.value)
    }

    @Test
    fun t2_boundary_previewBanner_alwaysDisplaysExitButton() {
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
}
