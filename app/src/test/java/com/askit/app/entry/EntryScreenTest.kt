package com.askit.app.entry

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.askit.designsystem.theme.AskITTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w360dp-h800dp-normal-long-notround-any-xxxhdpi-keyshidden-nonav")
class EntryScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun displaysAllEntryElements_inLightTheme() {
        composeTestRule.setContent {
            AskITTheme(darkTheme = false) {
                EntryScreen(
                    onGetStarted = {},
                    onLogin = {},
                )
            }
        }

        composeTestRule.onNodeWithTag("entry_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("entry_orbital_constellation").assertIsDisplayed()
        composeTestRule.onNodeWithTag("entry_center_logo_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("entry_headline").assertIsDisplayed()
        composeTestRule.onNodeWithTag("entry_subtitle").assertIsDisplayed()
        composeTestRule.onNodeWithTag("entry_trust_badge").assertIsDisplayed()
        composeTestRule.onNodeWithTag("entry_btn_get_started").assertIsDisplayed()
        composeTestRule.onNodeWithTag("entry_btn_login").assertIsDisplayed()
        composeTestRule.onNodeWithTag("entry_legal_disclaimer").assertIsDisplayed()
    }

    @Test
    fun displaysAllEntryElements_inDarkTheme() {
        composeTestRule.setContent {
            AskITTheme(darkTheme = true) {
                EntryScreen(
                    onGetStarted = {},
                    onLogin = {},
                )
            }
        }

        composeTestRule.onNodeWithTag("entry_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("entry_orbital_constellation").assertIsDisplayed()
        composeTestRule.onNodeWithTag("entry_center_logo_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("entry_headline").assertIsDisplayed()
        composeTestRule.onNodeWithTag("entry_subtitle").assertIsDisplayed()
        composeTestRule.onNodeWithTag("entry_trust_badge").assertIsDisplayed()
        composeTestRule.onNodeWithTag("entry_btn_get_started").assertIsDisplayed()
        composeTestRule.onNodeWithTag("entry_btn_login").assertIsDisplayed()
        composeTestRule.onNodeWithTag("entry_legal_disclaimer").assertIsDisplayed()
    }

    @Test
    fun getStartedButton_triggersCallback() {
        var getStartedCalled = false
        composeTestRule.setContent {
            AskITTheme {
                EntryScreen(
                    onGetStarted = { getStartedCalled = true },
                    onLogin = {},
                )
            }
        }

        composeTestRule.onNodeWithTag("entry_btn_get_started").performClick()
        assertTrue(getStartedCalled)
    }

    @Test
    fun loginButton_triggersCallback() {
        var loginCalled = false
        composeTestRule.setContent {
            AskITTheme {
                EntryScreen(
                    onGetStarted = {},
                    onLogin = { loginCalled = true },
                )
            }
        }

        composeTestRule.onNodeWithTag("entry_btn_login").performClick()
        assertTrue(loginCalled)
    }
}


