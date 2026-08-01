package com.askit.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.lifecycle.SavedStateHandle
import com.askit.app.explore.ExploreViewModel
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
}
