package com.askit.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.lifecycle.SavedStateHandle
import com.askit.app.createpost.CreatePostViewModel
import com.askit.app.explore.ExploreViewModel
import com.askit.app.listservice.ListServiceViewModel
import com.askit.app.posttask.PostTaskViewModel
import com.askit.designsystem.theme.AskITTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MainActivitySmokeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun bottomBar_isDisplayed() {
        composeTestRule.setContent {
            AskITTheme(darkTheme = false) {
                AskITApp(ExploreViewModel(SavedStateHandle()))
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
