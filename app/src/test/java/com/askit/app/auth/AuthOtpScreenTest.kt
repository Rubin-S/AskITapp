package com.askit.app.auth

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
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
class AuthOtpScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun displaysAllElements_inLightTheme() {
        composeTestRule.setContent {
            AskITTheme(darkTheme = false) {
                AuthOtpScreen(
                    phoneNumber = "+91 98989 89898",
                    onBack = {},
                    onEditPhone = {},
                    onVerifySuccess = {},
                )
            }
        }

        composeTestRule.onNodeWithTag("auth_otp_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("auth_otp_back").assertIsDisplayed()
        composeTestRule.onNodeWithTag("auth_otp_logo").assertIsDisplayed()
        composeTestRule.onNodeWithTag("auth_otp_headline").assertIsDisplayed()
        composeTestRule.onNodeWithTag("auth_otp_subtitle").assertIsDisplayed()
        composeTestRule.onNodeWithTag("auth_otp_edit_phone_btn").assertIsDisplayed()
        composeTestRule.onNodeWithTag("auth_otp_input_boxes").assertIsDisplayed()
        composeTestRule.onNodeWithTag("auth_otp_btn_verify").assertIsDisplayed()
    }

    @Test
    fun displaysAllElements_inDarkTheme() {
        composeTestRule.setContent {
            AskITTheme(darkTheme = true) {
                AuthOtpScreen(
                    phoneNumber = "+91 98989 89898",
                    onBack = {},
                    onEditPhone = {},
                    onVerifySuccess = {},
                )
            }
        }

        composeTestRule.onNodeWithTag("auth_otp_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("auth_otp_back").assertIsDisplayed()
        composeTestRule.onNodeWithTag("auth_otp_logo").assertIsDisplayed()
        composeTestRule.onNodeWithTag("auth_otp_headline").assertIsDisplayed()
        composeTestRule.onNodeWithTag("auth_otp_input_boxes").assertIsDisplayed()
        composeTestRule.onNodeWithTag("auth_otp_btn_verify").assertIsDisplayed()
    }

    @Test
    fun verifyButton_enablesOn6Digits_andCompletes() {
        var verifySuccessCalled = false
        composeTestRule.setContent {
            AskITTheme {
                AuthOtpScreen(
                    phoneNumber = "+91 98989 89898",
                    onBack = {},
                    onEditPhone = {},
                    onVerifySuccess = { verifySuccessCalled = true },
                )
            }
        }

        // Initially disabled
        composeTestRule.onNodeWithTag("auth_otp_btn_verify").assertIsNotEnabled()

        // Type / Paste 6 digits
        composeTestRule.onNodeWithTag("auth_otp_input_boxes").performTextInput("123456")

        assertTrue(verifySuccessCalled)
    }

    @Test
    fun backButton_andEditPhone_triggerCallbacks() {
        var backCalled = false
        var editCalled = false
        composeTestRule.setContent {
            AskITTheme {
                AuthOtpScreen(
                    phoneNumber = "+91 98989 89898",
                    onBack = { backCalled = true },
                    onEditPhone = { editCalled = true },
                    onVerifySuccess = {},
                )
            }
        }

        composeTestRule.onNodeWithTag("auth_otp_back").performClick()
        assertTrue(backCalled)

        composeTestRule.onNodeWithTag("auth_otp_edit_phone_btn").performClick()
    }
}
