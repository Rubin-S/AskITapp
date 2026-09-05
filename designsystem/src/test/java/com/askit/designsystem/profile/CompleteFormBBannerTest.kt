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
class CompleteFormBBannerTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun banner_rendersMotivationalTexts_andTriggersOnClick() {
        var clicked = false

        composeTestRule.setContent {
            AskITTheme {
                CompleteFormBBanner(
                    onClick = { clicked = true },
                )
            }
        }

        composeTestRule.onNodeWithTag("profile_complete_form_b_banner").assertIsDisplayed()
        composeTestRule.onNodeWithText("Offer services in your neighborhood").assertIsDisplayed()
        composeTestRule.onNodeWithText("Add Service Card").assertIsDisplayed()

        composeTestRule.onNodeWithTag("profile_complete_form_b_banner").performClick()
        assertEquals(true, clicked)
    }
}
