package com.askit.app

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.askit.app.posttask.PostTaskBudgetMode
import com.askit.app.posttask.PostTaskOptionalSection
import com.askit.app.posttask.PostTaskRoute
import com.askit.app.posttask.PostTaskTimingMode
import com.askit.app.posttask.PostTaskViewModel
import com.askit.app.posttask.PostTaskWorkMode
import com.askit.designsystem.theme.AskITTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Tapping Review on an invalid form moves focus to the first field that needs attention. */
@RunWith(AndroidJUnit4::class)
class PostTaskReviewFocusTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    private fun show(viewModel: PostTaskViewModel) = compose.setContent {
        AskITTheme { PostTaskRoute(viewModel = viewModel, onBack = {}) }
    }

    @Test fun emptyFormFocusesTheTitle() {
        val viewModel = PostTaskViewModel().apply {
            activateAccount("u1")
            selectCategory("electrician")
        }
        show(viewModel)
        compose.onNodeWithTag("post_task_review").performClick()
        compose.onNodeWithTag("post_task_title").performScrollTo().assertIsFocused()
    }

    @Test fun missingFixedBudgetExpandsTheBudgetSectionAndFocusesIt() {
        val viewModel = PostTaskViewModel().apply {
            activateAccount("u1")
            selectCategory("electrician")
            updateTitle("Fix two ceiling fans")
            updateDetails("Both fans wobble and one makes a grinding noise.")
            selectWorkMode(PostTaskWorkMode.REMOTE)
            selectTiming(PostTaskTimingMode.FLEXIBLE)
            selectBudgetMode(PostTaskBudgetMode.FIXED)
            updateFixedBudget("")
        }
        show(viewModel)
        compose.onNodeWithTag("post_task_review").performClick()
        compose.waitForIdle()
        assertTrue(PostTaskOptionalSection.BUDGET in viewModel.formState.value.expandedOptionalSections)
        compose.onNodeWithTag("post_task_fixed_budget").performScrollTo().assertIsFocused()
    }
}
