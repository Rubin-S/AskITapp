package com.askit.designsystem.services

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.askit.designsystem.theme.AskITTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ServiceResultItemTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun rendersServiceOwnedInformation_withoutInventedPersonMetadata() {
        setItem()

        listOf(
            "Repair leaking taps",
            "Plumbing",
            "I repair leaking taps and replace worn washers.",
            "Provided by Rubin S.",
            "Starting at ₹1500",
            "Serves Anna Nagar and nearby areas",
            "At customer location",
            "Up to 10 km",
        ).forEach { value ->
            composeTestRule.onNodeWithText(value, useUnmergedTree = true).assertIsDisplayed()
        }
        composeTestRule.onAllNodesWithText("4.8", substring = true).assertCountEquals(0)
        composeTestRule.onAllNodesWithText("Verified", substring = true).assertCountEquals(0)
        composeTestRule.onAllNodesWithText("km away", substring = true).assertCountEquals(0)
    }

    @Test
    fun noPortfolio_omitsMediaRegion() {
        setItem(portfolioModels = emptyList())
        composeTestRule.onAllNodesWithContentDescription("2 portfolio photos").assertCountEquals(0)
    }

    @Test
    fun richPortfolio_exposesOneCount() {
        setItem(portfolioModels = listOf(samplePhoto(), samplePhoto()))
        composeTestRule
            .onNodeWithText("2 portfolio photos", useUnmergedTree = true)
            .assertIsDisplayed()
    }

    @Test
    fun wholeCard_isTheOnlyAction_andActionLabelIsServiceSpecific() {
        var clicks = 0
        setItem(onClick = { clicks++ })

        val title = composeTestRule.onNodeWithText("Repair leaking taps")
        title.assertHasClickAction()
        title.performClick()
        assertEquals(1, clicks)
        assertEquals(
            "View service: Repair leaking taps",
            title.fetchSemanticsNode().config.get(SemanticsActions.OnClick).label,
        )
        composeTestRule.onAllNodes(hasClickAction(), useUnmergedTree = true).assertCountEquals(1)
    }

    @Test
    fun nonClickableCustomerPreview_retainsContentWithoutAction() {
        setItem(onClick = null)

        val title = composeTestRule.onNodeWithText("Repair leaking taps")
        title.assertIsDisplayed()
        assertFalse(
            title.fetchSemanticsNode().config.contains(SemanticsActions.OnClick),
        )
    }

    private fun setItem(
        portfolioModels: List<Any> = emptyList(),
        onClick: (() -> Unit)? = {},
    ) {
        composeTestRule.setContent {
            AskITTheme {
                Column {
                    ServiceResultItem(
                        serviceTitle = "Repair leaking taps",
                        category = "Plumbing",
                        description = "I repair leaking taps and replace worn washers.",
                        providerName = "Rubin S.",
                        priceLabel = "Starting at ₹1500",
                        coverageLabel = "Serves Anna Nagar and nearby areas",
                        deliveryModes = listOf("At customer location", "Up to 10 km"),
                        portfolioModels = portfolioModels,
                        onClick = onClick,
                    )
                }
            }
        }
        composeTestRule.waitForIdle()
    }

    private fun samplePhoto(): Bitmap = Bitmap.createBitmap(48, 48, Bitmap.Config.ARGB_8888).also {
        Canvas(it).drawColor(Color.rgb(50, 100, 150))
    }
}
