package com.askit.designsystem.tasks

import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.askit.designsystem.theme.AskITTheme
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class TaskResultItemTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun rendersCoreInformation() {
        setItem()

        composeTestRule.onNodeWithText("Repair laptop charging port").assertIsDisplayed()
        composeTestRule
            .onNodeWithText("Computer repair", useUnmergedTree = true)
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText("Laptop only charges when the cable is held at an angle.")
            .assertIsDisplayed()
        listOf("Budget range", "Kallakurichi", "Needed Monday", "Arun P.", "Posted 2h ago")
            .forEach { value ->
                composeTestRule.onNodeWithText(value, useUnmergedTree = true).assertIsDisplayed()
            }
    }

    @Test
    fun rendersEveryStatus() {
        composeTestRule.setContent {
            AskITTheme {
                Column {
                    TaskResultStatus.entries.forEach { status ->
                        TaskResultItem(
                            title = "Task ${status.name}",
                            category = "A long category label for a task with detailed scope",
                            summary = null,
                            budgetLabel = "Budget",
                            locationLabel = "Location",
                            timingLabel = "Timing",
                            posterName = "Poster",
                            postedLabel = "Posted",
                            status = status,
                            onClick = {},
                        )
                    }
                }
            }
        }

        listOf("Open", "Applied", "Filled", "Closed", "Expired", "Unavailable").forEach {
            if (it == "Open") {
                composeTestRule.onAllNodesWithText(it, useUnmergedTree = true).assertCountEquals(0)
            } else {
                composeTestRule.onNodeWithText(it, useUnmergedTree = true).assertExists()
            }
        }
    }

    @Test
    fun optionalSummary_isOmittedWhenNull() {
        setItem(summary = null)
        composeTestRule.onAllNodesWithText("Laptop only", substring = true).assertCountEquals(0)
    }

    @Test
    fun optionalSummary_isOmittedWhenBlank() {
        setItem(summary = "   ")
        composeTestRule.onAllNodesWithText("Laptop only", substring = true).assertCountEquals(0)
    }

    @Test
    fun blankMetadata_doesNotCreateMalformedSeparators() {
        setItem(
            category = "",
            summary = null,
            budgetLabel = "",
            locationLabel = "Kallakurichi",
            timingLabel = " ",
            posterName = "Arun P.",
            postedLabel = "Posted 2h ago",
        )

        composeTestRule.onNodeWithText("Kallakurichi").assertIsDisplayed()
        composeTestRule.onNodeWithText("Arun P.").assertIsDisplayed()
        composeTestRule.onNodeWithText("Posted 2h ago").assertIsDisplayed()
        composeTestRule.onAllNodesWithText("\u00B7", substring = true).assertCountEquals(0)
    }

    @Test
    fun photos_areProminentAndExposeCount() {
        setItem(photoModels = listOf(samplePhoto(), samplePhoto()))

        composeTestRule.onNodeWithContentDescription("2 photos").assertIsDisplayed()
    }

    @Test
    fun noPhotos_omitMediaRegion() {
        setItem(photoModels = emptyList())

        composeTestRule.onAllNodesWithText("2 photos").assertCountEquals(0)
        composeTestRule.onNodeWithText("Repair laptop charging port").assertIsDisplayed()
    }

    @Test
    fun rowClick_invokesCallbackOnce_andExposesTaskActionLabel() {
        var clicks = 0
        setItem(onClick = { clicks++ })

        val row = composeTestRule.onNodeWithText("Repair laptop charging port")
        row.assertHasClickAction()
        row.performClick()

        assertEquals(1, clicks)
        assertEquals(
            "View task: Repair laptop charging port",
            row.fetchSemanticsNode().config.get(SemanticsActions.OnClick).label,
        )
    }

    @Test
    fun taskWithoutAction_isNotClickableAndRetainsTitle() {
        setItem(onClick = null)

        composeTestRule.onNodeWithText("Repair laptop charging port").assertIsDisplayed()
        assertFalse(
            composeTestRule
                .onNodeWithText("Repair laptop charging port")
                .fetchSemanticsNode()
                .config
                .contains(SemanticsActions.OnClick),
        )
    }

    @Test
    fun row_isTheOnlyClickableNode_andHasNoLongClick() {
        setItem()

        composeTestRule
            .onAllNodes(hasClickAction(), useUnmergedTree = true)
            .assertCountEquals(1)
        val row = composeTestRule.onNodeWithText("Repair laptop charging port")
        assertFalse(
            row
                .fetchSemanticsNode()
                .config
                .contains(SemanticsActions.OnLongClick),
        )
    }

    @Test
    fun noNestedApplyOrMessageActionsExist() {
        setItem()

        composeTestRule.onAllNodesWithText("Apply", substring = true).assertCountEquals(0)
        composeTestRule.onAllNodesWithText("Message", substring = true).assertCountEquals(0)
    }

    @Test
    fun longCategory_preservesStatusInMergedSemantics_withoutIndependentStatusAction() {
        setItem(
            category = "A long category label for a task with detailed scope",
            status = TaskResultStatus.Unavailable,
        )

        composeTestRule
            .onNodeWithText("Unavailable", substring = true)
            .assertIsDisplayed()
        val status = composeTestRule.onNodeWithText(
            "Unavailable",
            substring = true,
            useUnmergedTree = true,
        )
        assertFalse(status.fetchSemanticsNode().config.contains(SemanticsActions.OnClick))
    }

    @Test
    fun row_remainsReachableAtSupportedWidthsAndLargeText() {
        composeTestRule.setContent {
            AskITTheme {
                Column {
                    listOf(320, 360, 412).forEach { width ->
                        Box(modifier = Modifier.width(width.dp)) {
                            TaskResultItem(
                                title = "Task $width",
                                category = "A long category label for a task with detailed scope",
                                summary = "A short task summary.",
                                budgetLabel = "Budget range",
                                locationLabel = "Kallakurichi",
                                timingLabel = "Needed Monday",
                                posterName = "Arun P.",
                                postedLabel = "Posted 2h ago",
                                status = TaskResultStatus.Open,
                                onClick = {},
                            )
                        }
                    }
                    CompositionLocalProvider(LocalDensity provides Density(1f, 2f)) {
                        Box(modifier = Modifier.width(320.dp)) {
                            TaskResultItem(
                                title = "Large text task",
                                category = "A long category label for a task with detailed scope",
                                summary = "A summary that can grow to two lines.",
                                budgetLabel = "Budget range",
                                locationLabel = "Kallakurichi",
                                timingLabel = "Needed Monday",
                                posterName = "Arun P.",
                                postedLabel = "Posted 2h ago",
                                status = TaskResultStatus.Open,
                                onClick = {},
                            )
                        }
                    }
                }
            }
        }

        listOf("Task 320", "Task 360", "Task 412", "Large text task").forEach { title ->
            composeTestRule.onNodeWithText(title).assertHasClickAction()
        }
        composeTestRule
            .onAllNodesWithText("Open", useUnmergedTree = true)
            .assertCountEquals(0)
    }

    @Test
    fun tamilResources_areLoadedForStatusAndTaskAction() {
        composeTestRule.setContent {
            val configuration = Configuration(LocalConfiguration.current).apply {
                setLocale(Locale.forLanguageTag("ta"))
            }
            val tamilContext = LocalContext.current.createConfigurationContext(configuration)
            CompositionLocalProvider(LocalContext provides tamilContext) {
                AskITTheme {
                    Box(modifier = Modifier.width(320.dp)) {
                        TaskResultItem(
                            title = "Repair laptop charging port",
                            category = "A long category label for a task with detailed scope",
                            summary = null,
                            budgetLabel = "Budget range",
                            locationLabel = "Kallakurichi",
                            timingLabel = "Needed Monday",
                            posterName = "Arun P.",
                            postedLabel = "Posted 2h ago",
                            status = TaskResultStatus.Unavailable,
                            onClick = {},
                        )
                    }
                }
            }
        }

        composeTestRule
            .onNodeWithText("A long category label for a task with detailed scope", useUnmergedTree = true)
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText("கிடைக்கவில்லை", useUnmergedTree = true)
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Repair laptop charging port").assertHasClickAction()
        assertEquals(
            "பணியைப் பார்க்கவும்: Repair laptop charging port",
            composeTestRule
                .onNodeWithText("Repair laptop charging port")
                .fetchSemanticsNode()
                .config
                .get(SemanticsActions.OnClick)
                .label,
        )
    }

    private fun setItem(
        title: String = "Repair laptop charging port",
        category: String = "Computer repair",
        summary: String? = "Laptop only charges when the cable is held at an angle.",
        budgetLabel: String = "Budget range",
        locationLabel: String = "Kallakurichi",
        timingLabel: String = "Needed Monday",
        posterName: String = "Arun P.",
        postedLabel: String = "Posted 2h ago",
        status: TaskResultStatus = TaskResultStatus.Open,
        photoModels: List<Any> = emptyList(),
        onClick: (() -> Unit)? = {},
    ) {
        composeTestRule.setContent {
            AskITTheme {
                Box(modifier = Modifier.width(360.dp)) {
                    TaskResultItem(
                        title = title,
                        category = category,
                        summary = summary,
                        budgetLabel = budgetLabel,
                        locationLabel = locationLabel,
                        timingLabel = timingLabel,
                        posterName = posterName,
                        postedLabel = postedLabel,
                        status = status,
                        photoModels = photoModels,
                        onClick = onClick,
                    )
                }
            }
        }
    }

    private fun samplePhoto(): Bitmap = Bitmap.createBitmap(160, 120, Bitmap.Config.ARGB_8888).also {
        Canvas(it).drawColor(Color.rgb(51, 112, 148))
        Canvas(it).drawRect(
            20f,
            20f,
            140f,
            100f,
            Paint().apply { color = Color.rgb(237, 176, 82) },
        )
    }
}
