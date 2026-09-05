package com.askit.app.auth

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
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
class FormAScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun displaysAllElements_inLightTheme() {
        composeTestRule.setContent {
            AskITTheme(darkTheme = false) {
                FormAScreen(
                    phoneNumber = "+91 98989 89898",
                    onBack = {},
                    onSubmitSuccess = { _, _, _, _ -> },
                )
            }
        }

        composeTestRule.onNodeWithTag("form_a_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("form_a_back").assertIsDisplayed()
        composeTestRule.onNodeWithTag("form_a_headline").assertIsDisplayed()
        composeTestRule.onNodeWithTag("form_a_subtitle").assertIsDisplayed()
        composeTestRule.onNodeWithTag("form_a_input_name").assertIsDisplayed()
        composeTestRule.onNodeWithTag("form_a_location_btn").assertIsDisplayed()
        composeTestRule.onNodeWithTag("form_a_input_city").assertIsDisplayed()
        composeTestRule.onNodeWithTag("form_a_input_pincode").assertIsDisplayed()
        composeTestRule.onNodeWithTag("form_a_interests_flow_row").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("form_a_btn_submit").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun displaysAllElements_inDarkTheme() {
        composeTestRule.setContent {
            AskITTheme(darkTheme = true) {
                FormAScreen(
                    phoneNumber = "+91 98989 89898",
                    onBack = {},
                    onSubmitSuccess = { _, _, _, _ -> },
                )
            }
        }

        composeTestRule.onNodeWithTag("form_a_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("form_a_headline").assertIsDisplayed()
        composeTestRule.onNodeWithTag("form_a_btn_submit").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun submitButton_validatesInput_andSubmits() {
        var submittedName = ""
        var submittedCity = ""
        var submittedPincode = ""
        var submittedInterests: List<String> = emptyList()

        composeTestRule.setContent {
            AskITTheme {
                FormAScreen(
                    phoneNumber = "+91 98989 89898",
                    onBack = {},
                    onSubmitSuccess = { name, city, pincode, interests ->
                        submittedName = name
                        submittedCity = city
                        submittedPincode = pincode
                        submittedInterests = interests
                    },
                )
            }
        }

        // Initially disabled
        composeTestRule.onNodeWithTag("form_a_btn_submit").assertIsNotEnabled()

        // Enter Full Name
        composeTestRule.onNodeWithTag("form_a_input_name").performTextInput("Elena Vance")
        composeTestRule.onNodeWithTag("form_a_btn_submit").assertIsNotEnabled() // City still required

        // Enter City
        composeTestRule.onNodeWithTag("form_a_input_city").performTextInput("Coimbatore")
        composeTestRule.onNodeWithTag("form_a_input_pincode").performTextInput("641001")

        // Now button should be enabled
        composeTestRule.onNodeWithTag("form_a_btn_submit").assertIsEnabled()

        // Submit
        composeTestRule.onNodeWithTag("form_a_btn_submit").performScrollTo().performClick()

        assertEquals("Elena Vance", submittedName)
        assertEquals("Coimbatore", submittedCity)
        assertEquals("641001", submittedPincode)
        assertTrue(submittedInterests.contains("Home repairs"))
    }

    @Test
    fun interestChips_toggleSelection() {
        composeTestRule.setContent {
            AskITTheme {
                FormAScreen(
                    phoneNumber = "+91 98989 89898",
                    onBack = {},
                    onSubmitSuccess = { _, _, _, _ -> },
                )
            }
        }

        // Click "Electrical" chip to toggle on
        composeTestRule.onNodeWithTag("chip_Electrical").performScrollTo().performClick()
        // Click "Other" chip to show custom interest text field
        composeTestRule.onNodeWithTag("chip_Other").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("form_a_input_custom_interest").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun backButton_triggersCallback() {
        var backCalled = false
        composeTestRule.setContent {
            AskITTheme {
                FormAScreen(
                    phoneNumber = "+91 98989 89898",
                    onBack = { backCalled = true },
                    onSubmitSuccess = { _, _, _, _ -> },
                )
            }
        }

        composeTestRule.onNodeWithTag("form_a_back").performClick()
        assertTrue(backCalled)
    }
}
