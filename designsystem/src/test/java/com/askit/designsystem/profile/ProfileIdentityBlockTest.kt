package com.askit.designsystem.profile

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.askit.designsystem.theme.AskITTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ProfileIdentityBlockTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun formAMember_rendersNameBioLocality_withoutBadgeOrTrade() {
        composeTestRule.setContent {
            AskITTheme {
                ProfileIdentityBlock(
                    displayName = "Meera Raman",
                    localityLine = "Gandhipuram, Coimbatore · Joined 2023",
                    tradeHeadline = null,
                    isVerified = false,
                    bio = "Homeowner · Hiring local pros for home care.",
                )
            }
        }

        composeTestRule.onNodeWithTag("profile_identity_block").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_display_name")
            .assertIsDisplayed()
            .assertTextEquals("Meera Raman")
        composeTestRule.onNodeWithTag("profile_bio")
            .assertIsDisplayed()
            .assertTextEquals("Homeowner · Hiring local pros for home care.")
        composeTestRule.onNodeWithTag("profile_locality")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Gandhipuram, Coimbatore · Joined 2023").assertIsDisplayed()

        // Badge and Trade Headline should NOT exist for unverified Form A member
        composeTestRule.onNodeWithTag("profile_verified_badge").assertDoesNotExist()
        composeTestRule.onNodeWithTag("profile_trade_headline").assertDoesNotExist()
    }

    @Test
    fun formBProvider_rendersVerifiedBadgeAndTradeHeadline() {
        composeTestRule.setContent {
            AskITTheme {
                ProfileIdentityBlock(
                    displayName = "Priya Sharma",
                    localityLine = "RS Puram, Coimbatore · Joined 2022",
                    tradeHeadline = "Certified Electrician",
                    isVerified = true,
                    bio = "Licensed electrical expert with 10+ years experience.",
                )
            }
        }

        composeTestRule.onNodeWithTag("profile_identity_block").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_display_name")
            .assertIsDisplayed()
            .assertTextEquals("Priya Sharma")
        composeTestRule.onNodeWithTag("profile_verified_badge").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_trade_headline")
            .assertIsDisplayed()
            .assertTextEquals("Certified Electrician")
        composeTestRule.onNodeWithTag("profile_bio").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_locality").assertIsDisplayed()
    }

    @Test
    fun nullBio_omitsBioNode() {
        composeTestRule.setContent {
            AskITTheme {
                ProfileIdentityBlock(
                    displayName = "Arun Kumar",
                    localityLine = "Coimbatore",
                    bio = null,
                )
            }
        }

        composeTestRule.onNodeWithTag("profile_display_name").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_bio").assertDoesNotExist()
    }

    @Test
    @Suppress("DEPRECATION")
    fun legacyOverload_rendersCorrectly() {
        composeTestRule.setContent {
            AskITTheme {
                ProfileIdentityBlock(
                    displayName = "Legacy User",
                    bio = "Legacy bio",
                    locationLine = "Legacy City · Joined 2021",
                )
            }
        }

        composeTestRule.onNodeWithTag("profile_display_name").assertTextEquals("Legacy User")
        composeTestRule.onNodeWithTag("profile_bio").assertTextEquals("Legacy bio")
        composeTestRule.onNodeWithText("Legacy City · Joined 2021").assertIsDisplayed()
    }
}
