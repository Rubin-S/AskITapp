package com.askit.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.lifecycle.SavedStateHandle
import com.askit.app.createpost.CreatePostViewModel
import com.askit.app.explore.ExploreViewModel
import com.askit.app.listservice.ListServiceViewModel
import com.askit.app.posttask.PostTaskViewModel
import androidx.compose.ui.test.onNodeWithTag
import com.askit.app.navigation.AppDestination
import com.askit.designsystem.theme.AskITTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w360dp-h800dp-normal-long-notround-any-xxxhdpi-keyshidden-nonav")
class MainActivitySmokeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun entryScreen_isDisplayedInitially_andTransitionsToHomeOnGetStarted() {
        composeTestRule.setContent {
            AskITTheme(darkTheme = false) {
                AskITApp(
                    exploreViewModel = ExploreViewModel(SavedStateHandle()),
                    initialRoute = AppDestination.Entry,
                )
            }
        }

        composeTestRule.onNodeWithTag("entry_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("entry_center_logo_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("entry_orbital_constellation").assertIsDisplayed()
        composeTestRule.onNodeWithTag("entry_headline").assertIsDisplayed()
        composeTestRule.onNodeWithTag("entry_btn_get_started").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("entry_btn_login").performScrollTo().assertIsDisplayed()

        // Tap Get started -> navigates to Mobile Number Auth
        composeTestRule.onNodeWithTag("entry_btn_get_started").performClick()
        composeTestRule.onNodeWithTag("auth_phone_screen").assertIsDisplayed()

        // Enter phone number & tap Get OTP -> navigates to 6-digit OTP Screen
        composeTestRule.onNodeWithTag("auth_phone_text_field").performTextInput("9898989898")
        composeTestRule.onNodeWithTag("auth_phone_btn_get_otp").performClick()
        composeTestRule.onNodeWithTag("auth_otp_screen").assertIsDisplayed()

        // Enter 6 digits OTP -> navigates to Form A (Profile Setup)
        composeTestRule.onNodeWithTag("auth_otp_input_boxes").performTextInput("123456")
        composeTestRule.onNodeWithTag("form_a_screen").assertIsDisplayed()

        // Fill Form A & Unlock Dashboard -> navigates to Home
        composeTestRule.onNodeWithTag("form_a_input_name").performTextInput("Elena Vance")
        composeTestRule.onNodeWithTag("form_a_input_city").performTextInput("Coimbatore")
        composeTestRule.onNodeWithTag("form_a_btn_submit").performScrollTo().performClick()

        composeTestRule.onNodeWithContentDescription("Home").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Create").assertIsDisplayed()
    }

    @Test
    fun bottomBar_isDisplayed() {
        composeTestRule.setContent {
            AskITTheme(darkTheme = false) {
                AskITApp(
                    exploreViewModel = ExploreViewModel(SavedStateHandle()),
                    initialRoute = AppDestination.Home,
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Home").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Create").assertIsDisplayed()
    }

    @Test
    fun createSheet_listService_opensListService() {
        composeTestRule.setContent {
            AskITTheme(darkTheme = false) {
                AskITApp(
                    exploreViewModel = ExploreViewModel(SavedStateHandle()),
                    listServiceViewModel = ListServiceViewModel(SavedStateHandle()),
                    initialRoute = AppDestination.Home,
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Create").performClick()
        composeTestRule.onNodeWithText("List a service").performClick()
        composeTestRule.onNodeWithText("List a service").assertIsDisplayed()
    }

    @Test
    fun createSheet_postTask_opensPostTask() {
        composeTestRule.setContent {
            AskITTheme(darkTheme = false) {
                AskITApp(
                    exploreViewModel = ExploreViewModel(SavedStateHandle()),
                    postTaskViewModel = PostTaskViewModel(SavedStateHandle()),
                    initialRoute = AppDestination.Home,
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Create").performClick()
        composeTestRule.onNodeWithText("Post a task").performClick()
        composeTestRule.onNodeWithText("Post a task").assertIsDisplayed()
    }

    @Test
    fun createSheet_createPost_opensCreatePost() {
        composeTestRule.setContent {
            AskITTheme(darkTheme = false) {
                AskITApp(
                    exploreViewModel = ExploreViewModel(SavedStateHandle()),
                    createPostViewModel = CreatePostViewModel(SavedStateHandle()),
                    initialRoute = AppDestination.Home,
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Create").performClick()
        composeTestRule.onNodeWithText(
            "Create a post",
            useUnmergedTree = true,
        ).performScrollTo().performClick()
        composeTestRule.onNodeWithText("Create post").assertIsDisplayed()
    }
}
