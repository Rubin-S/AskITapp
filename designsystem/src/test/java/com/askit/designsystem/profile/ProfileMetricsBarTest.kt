package com.askit.designsystem.profile

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
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
class ProfileMetricsBarTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun formA_rendersThreeMetrics_withValuesAndLabels() {
        val formAMetrics = listOf(
            ProfileMetricItem(id = "activity", value = "14", label = "Activity"),
            ProfileMetricItem(id = "followers", value = "120", label = "Followers"),
            ProfileMetricItem(id = "following", value = "45", label = "Following"),
        )

        composeTestRule.setContent {
            AskITTheme {
                ProfileMetricsBar(metrics = formAMetrics)
            }
        }

        composeTestRule.onNodeWithTag("profile_metrics_bar").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_metric_activity").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_metric_followers").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_metric_following").assertIsDisplayed()

        composeTestRule.onNodeWithText("14").assertIsDisplayed()
        composeTestRule.onNodeWithText("Activity").assertIsDisplayed()
        composeTestRule.onNodeWithText("120").assertIsDisplayed()
        composeTestRule.onNodeWithText("Followers").assertIsDisplayed()
        composeTestRule.onNodeWithText("45").assertIsDisplayed()
        composeTestRule.onNodeWithText("Following").assertIsDisplayed()
    }

    @Test
    fun formB_rendersFourMetrics_withRatingIconAndValues() {
        val formBMetrics = listOf(
            ProfileMetricItem(
                id = "rating",
                value = "4.9 (48)",
                label = "Rating",
                iconRes = R.drawable.ic_star_filled,
            ),
            ProfileMetricItem(id = "completed_jobs", value = "48", label = "Completed"),
            ProfileMetricItem(id = "followers", value = "320", label = "Followers"),
            ProfileMetricItem(id = "following", value = "88", label = "Following"),
        )

        composeTestRule.setContent {
            AskITTheme {
                ProfileMetricsBar(metrics = formBMetrics)
            }
        }

        composeTestRule.onNodeWithTag("profile_metrics_bar").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_metric_rating").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_metric_completed_jobs").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_metric_followers").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_metric_following").assertIsDisplayed()

        composeTestRule.onNodeWithText("4.9 (48)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Completed").assertIsDisplayed()
    }

    @Test
    fun metricItem_invokesOnClickWhenClicked() {
        var clicked = false
        val metrics = listOf(
            ProfileMetricItem(
                id = "followers",
                value = "100",
                label = "Followers",
                onClick = { clicked = true },
            ),
        )

        composeTestRule.setContent {
            AskITTheme {
                ProfileMetricsBar(metrics = metrics)
            }
        }

        composeTestRule.onNodeWithTag("profile_metric_followers").performClick()
        assertEquals(true, clicked)
    }
}
