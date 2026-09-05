package com.askit.designsystem.profile

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.askit.designsystem.theme.AskITTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ProviderDashboardBannerTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun banner_rendersEnglishTexts_andTriggersOnClick() {
        var clicked = false

        composeTestRule.setContent {
            AskITTheme {
                ProviderDashboardBanner(
                    onClick = { clicked = true },
                )
            }
        }

        composeTestRule.onNodeWithTag("profile_provider_dashboard_banner").assertIsDisplayed()
        composeTestRule.onNodeWithText("Provider Dashboard").assertIsDisplayed()
        composeTestRule.onNodeWithText("Manage incoming service requests, active jobs, and your trust score.").assertIsDisplayed()
        composeTestRule.onNodeWithText("Open Dashboard").assertIsDisplayed()

        composeTestRule.onNodeWithTag("profile_provider_dashboard_banner").performClick()
        assertEquals(true, clicked)
    }

    @Test
    @Config(qualifiers = "ta-rIN-w360dp-h800dp-normal-long-notround-any-xxxhdpi-keyshidden-nonav")
    fun banner_rendersTamilTexts_correctly() {
        composeTestRule.setContent {
            AskITTheme {
                ProviderDashboardBanner(
                    onClick = {},
                )
            }
        }

        composeTestRule.onNodeWithTag("profile_provider_dashboard_banner").assertIsDisplayed()
        composeTestRule.onNodeWithText("சேவை வழங்குநர் டாஷ்போர்டு").assertIsDisplayed()
        composeTestRule.onNodeWithText("உள்வரும் கோரிக்கைகள், நடப்பு பணிகள் மற்றும் உங்கள் நம்பிக்கை மதிப்பெண்ணை நிர்வகிக்கவும்.").assertIsDisplayed()
        composeTestRule.onNodeWithText("டாஷ்போர்டைத் திற").assertIsDisplayed()
    }
}
