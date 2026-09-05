package com.askit.app.auth

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.askit.designsystem.theme.AskITTheme
import org.junit.Assert.assertEquals
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
class AuthPhoneScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun displaysAllElements_inLightTheme() {
        composeTestRule.setContent {
            AskITTheme(darkTheme = false) {
                AuthPhoneScreen(
                    onBack = {},
                    onGetOtp = {},
                )
            }
        }

        composeTestRule.onNodeWithTag("auth_phone_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("auth_phone_back").assertIsDisplayed()
        composeTestRule.onNodeWithTag("auth_phone_logo").assertIsDisplayed()
        composeTestRule.onNodeWithTag("auth_phone_headline").assertIsDisplayed()
        composeTestRule.onNodeWithTag("auth_phone_subtitle").assertIsDisplayed()
        composeTestRule.onNodeWithTag("auth_phone_input_container").assertIsDisplayed()
        composeTestRule.onNodeWithTag("auth_phone_country_picker_btn").assertIsDisplayed()
        composeTestRule.onNodeWithTag("auth_phone_text_field").assertIsDisplayed()
        composeTestRule.onNodeWithTag("auth_phone_btn_get_otp").assertIsDisplayed()
        composeTestRule.onNodeWithTag("auth_phone_disclaimer").assertIsDisplayed()
    }

    @Test
    fun displaysAllElements_inDarkTheme() {
        composeTestRule.setContent {
            AskITTheme(darkTheme = true) {
                AuthPhoneScreen(
                    onBack = {},
                    onGetOtp = {},
                )
            }
        }

        composeTestRule.onNodeWithTag("auth_phone_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("auth_phone_back").assertIsDisplayed()
        composeTestRule.onNodeWithTag("auth_phone_logo").assertIsDisplayed()
        composeTestRule.onNodeWithTag("auth_phone_headline").assertIsDisplayed()
        composeTestRule.onNodeWithTag("auth_phone_subtitle").assertIsDisplayed()
        composeTestRule.onNodeWithTag("auth_phone_btn_get_otp").assertIsDisplayed()
        composeTestRule.onNodeWithTag("auth_phone_disclaimer").assertIsDisplayed()
    }

    @Test
    fun getOtpButton_disabledWhenEmpty_andEnabledWhenValid() {
        var submittedPhone = ""
        composeTestRule.setContent {
            AskITTheme {
                AuthPhoneScreen(
                    onBack = {},
                    onGetOtp = { submittedPhone = it },
                )
            }
        }

        // Initially disabled
        composeTestRule.onNodeWithTag("auth_phone_btn_get_otp").assertIsNotEnabled()

        // Enter 10 digits
        composeTestRule.onNodeWithTag("auth_phone_text_field").performTextInput("9898989898")
        composeTestRule.onNodeWithTag("auth_phone_btn_get_otp").assertIsEnabled()

        // Click Get OTP
        composeTestRule.onNodeWithTag("auth_phone_btn_get_otp").performClick()
        assertEquals("+91 9898989898", submittedPhone)
    }

    @Test
    fun backButton_triggersCallback() {
        var backCalled = false
        composeTestRule.setContent {
            AskITTheme {
                AuthPhoneScreen(
                    onBack = { backCalled = true },
                    onGetOtp = {},
                )
            }
        }

        composeTestRule.onNodeWithTag("auth_phone_back").performClick()
        assertTrue(backCalled)
    }

    @Test
    fun countryPicker_opensAndSelectsCountry() {
        var submittedPhone = ""
        composeTestRule.setContent {
            AskITTheme {
                AuthPhoneScreen(
                    onBack = {},
                    onGetOtp = { submittedPhone = it },
                )
            }
        }

        // Open country picker
        composeTestRule.onNodeWithTag("auth_phone_country_picker_btn").performClick()
        composeTestRule.onNodeWithTag("auth_country_picker_sheet").assertIsDisplayed()

        // Select United States (+1)
        composeTestRule.onNodeWithTag("country_item_US").performClick()

        // Enter phone number
        composeTestRule.onNodeWithTag("auth_phone_text_field").performTextInput("2125551234")
        composeTestRule.onNodeWithTag("auth_phone_btn_get_otp").performClick()

        assertEquals("+1 2125551234", submittedPhone)
    }
}
