package com.askit.app.profile

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.askit.app.session.SessionProfile
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
class ProfileSettingsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun displaysAllSettingsSectionsAndHeader() {
        val profile = SessionProfile(
            displayName = "Meera Raman",
            username = "meera.raman",
            phoneNumber = "9876543298",
            activeRole = "Both",
            hasListedService = true,
        )
        composeTestRule.setContent {
            AskITTheme {
                ProfileSettingsScreen(
                    profile = profile,
                    onBack = {},
                    onNavigateToEditProfile = {},
                )
            }
        }

        composeTestRule.onNodeWithTag("profile_settings_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("settings_profile_header_card").assertIsDisplayed()
        composeTestRule.onNodeWithText("Meera Raman").assertIsDisplayed()
        composeTestRule.onNodeWithText("@meera.raman").assertIsDisplayed()

        // Account rows
        composeTestRule.onNodeWithTag("settings_row_edit_profile").assertIsDisplayed()
        composeTestRule.onNodeWithTag("settings_row_phone_number").assertIsDisplayed()

        // Preferences rows
        composeTestRule.onNodeWithTag("settings_toggle_push").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("settings_toggle_alerts").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("settings_row_language").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("settings_row_location").performScrollTo().assertIsDisplayed()

        // Provider rows
        composeTestRule.onNodeWithTag("settings_row_service_listing").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("settings_row_availability").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("settings_row_creator_hub").performScrollTo().assertIsDisplayed()

        // Seeker rows
        composeTestRule.onNodeWithTag("settings_row_saved").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("settings_row_history").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("settings_row_my_jobs").performScrollTo().assertIsDisplayed()

        // Support rows & buttons
        composeTestRule.onNodeWithTag("settings_row_help").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("settings_row_contact_support").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("settings_row_terms").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("settings_btn_logout").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("settings_btn_clear_data").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun backButton_invokesOnBack() {
        var backCalled = false
        composeTestRule.setContent {
            AskITTheme {
                ProfileSettingsScreen(
                    profile = SessionProfile(),
                    onBack = { backCalled = true },
                    onNavigateToEditProfile = {},
                )
            }
        }
        composeTestRule.onNodeWithTag("settings_back").performClick()
        assertTrue(backCalled)
    }

    @Test
    fun editProfileRow_invokesNavigateToEditProfile() {
        var editProfileCalled = false
        composeTestRule.setContent {
            AskITTheme {
                ProfileSettingsScreen(
                    profile = SessionProfile(),
                    onBack = {},
                    onNavigateToEditProfile = { editProfileCalled = true },
                )
            }
        }
        composeTestRule.onNodeWithTag("settings_row_edit_profile").performClick()
        assertTrue(editProfileCalled)
    }

    @Test
    fun headerCard_invokesNavigateToEditProfile() {
        var editProfileCalled = false
        composeTestRule.setContent {
            AskITTheme {
                ProfileSettingsScreen(
                    profile = SessionProfile(),
                    onBack = {},
                    onNavigateToEditProfile = { editProfileCalled = true },
                )
            }
        }
        composeTestRule.onNodeWithTag("settings_profile_header_card").performClick()
        assertTrue(editProfileCalled)
    }

    @Test
    fun navigationActions_triggerExpectedCallbacks() {
        var listServiceCalled = false
        var createPostCalled = false
        var jobRequestsCalled = false
        var supportChatCalled = false

        composeTestRule.setContent {
            AskITTheme {
                ProfileSettingsScreen(
                    profile = SessionProfile(hasListedService = true),
                    onBack = {},
                    onNavigateToEditProfile = {},
                    onNavigateToListService = { listServiceCalled = true },
                    onNavigateToCreatePost = { createPostCalled = true },
                    onNavigateToJobRequests = { jobRequestsCalled = true },
                    onNavigateToSupportChat = { supportChatCalled = true },
                )
            }
        }

        composeTestRule.onNodeWithTag("settings_row_service_listing").performScrollTo().performClick()
        assertTrue(listServiceCalled)

        composeTestRule.onNodeWithTag("settings_row_creator_hub").performScrollTo().performClick()
        assertTrue(createPostCalled)

        composeTestRule.onNodeWithTag("settings_row_my_jobs").performScrollTo().performClick()
        assertTrue(jobRequestsCalled)

        composeTestRule.onNodeWithTag("settings_row_contact_support").performScrollTo().performClick()
        assertTrue(supportChatCalled)
    }

    @Test
    fun clearDataButton_opensConfirmation_andCallsClear() {
        var clearDataCalled = false
        composeTestRule.setContent {
            AskITTheme {
                ProfileSettingsScreen(
                    profile = SessionProfile(),
                    onBack = {},
                    onNavigateToEditProfile = {},
                    onClearAppData = { clearDataCalled = true },
                )
            }
        }

        composeTestRule.onNodeWithTag("settings_btn_clear_data").performScrollTo().performClick()
        composeTestRule.onNodeWithText("Clear data").performClick()
        assertTrue(clearDataCalled)
    }
}
