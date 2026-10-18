package com.askit.app.screenshots

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import com.askit.app.posttask.PostTaskAccessSection
import com.askit.app.posttask.PostTaskBudgetMode
import com.askit.app.posttask.PostTaskFormState
import com.askit.app.posttask.PostTaskOptionalSection
import com.askit.app.posttask.PostTaskReview
import com.askit.app.posttask.PostTaskTimingMode
import com.askit.app.posttask.PostTaskWorkMode
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w360dp-h1200dp-mdpi")
class PostTaskScreenshotTest {
    private val filled = PostTaskFormState(
        categoryId = "electrician",
        title = "Fix two ceiling fans",
        details = "Both fans wobble and one makes a grinding noise. Ladder available.",
        workMode = PostTaskWorkMode.AT_MY_LOCATION,
        publicAreaLabel = "Anna Nagar, Chennai",
        latitude = 13.085,
        longitude = 80.21,
        privateAddressOrLandmark = "Flat 4B, near the park gate",
        timingMode = PostTaskTimingMode.FLEXIBLE,
        budgetMode = PostTaskBudgetMode.RANGE,
        minimumBudget = "500",
        maximumBudget = "900",
        accessInstructions = "Ring the bell twice; the dog is friendly.",
    )

    @Test fun review() = snapshot("post_task_review") {
        Column(Modifier.verticalScroll(rememberScrollState())) {
            PostTaskReview(state = filled, onEdit = {})
        }
    }

    @Test fun accessInstructionsExpanded() = snapshot("post_task_access_expanded") {
        PostTaskAccessSection(
            state = filled.copy(expandedOptionalSections = setOf(PostTaskOptionalSection.ACCESS_INSTRUCTIONS)),
            onToggle = {},
            onUpdateAccessInstructions = {},
        )
    }
}
