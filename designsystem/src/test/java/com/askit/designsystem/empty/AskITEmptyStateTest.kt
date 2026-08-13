package com.askit.designsystem.empty

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.askit.designsystem.R
import com.askit.designsystem.theme.AskITTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AskITEmptyStateTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun action_invokesCallback() {
        var clicks = 0
        composeTestRule.setContent {
            AskITTheme(darkTheme = false) {
                AskITEmptyState(
                    iconRes = R.drawable.ic_home_outlined,
                    title = "Nothing here yet",
                    supporting = "Post a task to get started.",
                    actionLabel = "Create",
                    onAction = { clicks++ },
                )
            }
        }

        composeTestRule.onNodeWithText("Nothing here yet").assertIsDisplayed()
        composeTestRule.onNodeWithText("Create").assertHasClickAction().performClick()
        assertEquals(1, clicks)
    }
}
