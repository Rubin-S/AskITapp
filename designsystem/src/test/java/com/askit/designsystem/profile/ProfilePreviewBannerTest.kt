package com.askit.designsystem.profile

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
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
class ProfilePreviewBannerTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun banner_rendersViewingAsPublic_andExitPreviewButton() {
        var exitClicked = false

        composeTestRule.setContent {
            AskITTheme {
                ProfilePreviewBanner(
                    onExitPreview = { exitClicked = true },
                )
            }
        }

        composeTestRule.onNodeWithTag("profile_preview_banner").assertIsDisplayed()
        composeTestRule.onNodeWithText("Viewing as Public").assertIsDisplayed()

        val exitButton = composeTestRule.onNodeWithTag("profile_exit_preview")
        exitButton.assertIsDisplayed().assertTextEquals("Exit Preview").performClick()

        assertEquals(true, exitClicked)
    }
}
