package com.askit.designsystem.people

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.askit.designsystem.theme.AskITTheme
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PersonResultItemTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun rendersCoreInformation() {
        setItem()

        composeTestRule.onNodeWithText("Ravi Kumar").assertIsDisplayed()
        composeTestRule.onNodeWithText("Electrician").assertIsDisplayed()
        composeTestRule
            .onNodeWithText("Fan installation, Wiring +1", useUnmergedTree = true)
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("4.8 (36)").assertIsDisplayed()
        composeTestRule.onNodeWithText("2.4 km").assertIsDisplayed()
        composeTestRule.onNodeWithText("From ₹500").assertIsDisplayed()
        composeTestRule.onNodeWithText("Available today").assertIsDisplayed()
    }

    @Test
    fun additionalServices_hideWhenEmpty() {
        setItem(additionalServices = emptyList())

        composeTestRule.onAllNodesWithText("Fan installation", substring = true).assertCountEquals(0)
    }

    @Test
    fun additionalServices_showOneAndTwoInOrder() {
        setItem(additionalServices = listOf("Fan installation", "Wiring"))

        composeTestRule.onNodeWithText("Fan installation, Wiring").assertIsDisplayed()
    }

    @Test
    fun additionalServices_limitToTwoAndAppendRemainingCount() {
        setItem(
            additionalServices = listOf(
                "Fan installation",
                "Wiring",
                "Appliance repair",
                "Lighting",
            ),
        )

        composeTestRule
            .onNodeWithText("Fan installation, Wiring +2", useUnmergedTree = true)
            .assertIsDisplayed()
    }

    @Test
    fun additionalServices_ignoreBlanksAndPrimaryServiceDuplicates() {
        setItem(additionalServices = listOf("", "Electrician", "Wiring"))

        composeTestRule.onNodeWithText("Wiring").assertIsDisplayed()
        composeTestRule.onAllNodesWithText("Electrician, Wiring", substring = true).assertCountEquals(0)
    }

    @Test
    fun optionalPriceAndStatus_hideWhenBlank() {
        setItem(priceLabel = " ", statusLabel = "")

        composeTestRule.onAllNodesWithText("From ₹500").assertCountEquals(0)
        composeTestRule.onAllNodesWithText("Available today").assertCountEquals(0)
    }

    @Test
    fun newPeople_showNewInsteadOfZeroRating() {
        setItem(rating = 0.0, reviewCount = 0)

        composeTestRule.onNodeWithText("New").assertIsDisplayed()
        composeTestRule.onAllNodesWithText("0.0 (0)").assertCountEquals(0)
    }

    @Test
    fun inconsistentRatingData_isSafeAndShowsNew() {
        setItem(rating = Double.NaN, reviewCount = 12)

        composeTestRule.onNodeWithText("New").assertIsDisplayed()
    }

    @Test
    fun nullAvatar_keepsTheWholeRowAsTheOnlyAction() {
        setItem(avatarUrl = null)

        composeTestRule.onNodeWithText("Ravi Kumar").assertHasClickAction()
    }

    @Test
    fun blankAvatar_keepsTheWholeRowAsTheOnlyAction() {
        setItem(avatarUrl = "")

        composeTestRule.onNodeWithText("Ravi Kumar").assertHasClickAction()
    }

    @Test
    fun rowClick_invokesCallbackOnce_andExposesProfileActionLabel() {
        var clicks = 0
        setItem(onClick = { clicks++ })

        val row = composeTestRule.onNodeWithText("Ravi Kumar")
        row.assertHasClickAction()
        row.performClick()

        assertEquals(1, clicks)
        assertEquals(
            "View Ravi Kumar's profile",
            row.fetchSemanticsNode().config.get(SemanticsActions.OnClick).label,
        )
    }

    @Test
    fun avatarStatusAndRating_haveNoNestedActions() {
        setItem()

        composeTestRule.onNodeWithText("Ravi Kumar").assertHasClickAction()
    }

    @Test
    fun rowSemantics_keepPersonInformationAvailable() {
        setItem()

        val row = composeTestRule.onNodeWithText("Ravi Kumar")
        row.assertHasClickAction()
        composeTestRule.onNodeWithText("Ravi Kumar").assertIsDisplayed()
        composeTestRule.onNodeWithText("Electrician").assertIsDisplayed()
        composeTestRule.onNodeWithText("4.8 (36)").assertIsDisplayed()
        composeTestRule.onNodeWithText("2.4 km").assertIsDisplayed()
    }

    @Test
    fun row_remainsReachableAtSupportedWidthsAndLargeText() {
        composeTestRule.setContent {
            AskITTheme(darkTheme = false) {
                androidx.compose.foundation.layout.Column {
                    listOf(320, 360, 412).forEach { width ->
                        Box(modifier = Modifier.width(width.dp)) {
                            PersonResultItem(
                                name = "Ravi $width",
                                avatarUrl = null,
                                primaryService = "Electrician",
                                additionalServices = listOf("Fan installation"),
                                rating = 4.8,
                                reviewCount = 36,
                                locationLabel = "2.4 km",
                                priceLabel = "From ₹500",
                                statusLabel = "Available today",
                                onClick = {},
                            )
                        }
                    }
                    CompositionLocalProvider(LocalDensity provides Density(1f, 2f)) {
                        Box(modifier = Modifier.width(320.dp)) {
                            PersonResultItem(
                                name = "Large font Ravi",
                                avatarUrl = null,
                                primaryService = "Electrician",
                                additionalServices = listOf("Fan installation"),
                                rating = 4.8,
                                reviewCount = 36,
                                locationLabel = "2.4 km",
                                priceLabel = "From ₹500",
                                statusLabel = "Available today",
                                onClick = {},
                            )
                        }
                    }
                }
            }
        }
        listOf("Ravi 320", "Ravi 360", "Ravi 412", "Large font Ravi").forEach { name ->
            composeTestRule.onNodeWithText(name).assertHasClickAction()
        }
        composeTestRule.onAllNodesWithText("Electrician").assertCountEquals(4)
    }

    @Test
    fun tamilResources_areLoadedForNewStateAndProfileAction() {
        composeTestRule.setContent {
            val configuration = Configuration(LocalConfiguration.current).apply {
                setLocale(Locale.forLanguageTag("ta"))
            }
            val tamilContext = LocalContext.current.createConfigurationContext(configuration)
            CompositionLocalProvider(LocalContext provides tamilContext) {
                AskITTheme(darkTheme = false) {
                    PersonResultItem(
                        name = "Priya S.",
                        avatarUrl = null,
                        primaryService = "Home tutor",
                        additionalServices = emptyList(),
                        rating = null,
                        reviewCount = 0,
                        locationLabel = "Kallakurichi",
                        priceLabel = null,
                        statusLabel = null,
                        onClick = {},
                    )
                }
            }
        }

        composeTestRule.onNodeWithText("புதியவர்").assertIsDisplayed()
        composeTestRule.onNodeWithText("Priya S.").assertHasClickAction()
    }

    private fun setItem(
        avatarUrl: String? = null,
        additionalServices: List<String> = listOf("Fan installation", "Wiring", "Appliance repair"),
        rating: Double? = 4.8,
        reviewCount: Int = 36,
        priceLabel: String? = "From ₹500",
        statusLabel: String? = "Available today",
        widthDp: Int = 360,
        onClick: () -> Unit = {},
    ) {
        composeTestRule.setContent {
            AskITTheme(darkTheme = false) {
                Box(modifier = Modifier.width(widthDp.dp)) {
                    PersonResultItem(
                        name = "Ravi Kumar",
                        avatarUrl = avatarUrl,
                        primaryService = "Electrician",
                        additionalServices = additionalServices,
                        rating = rating,
                        reviewCount = reviewCount,
                        locationLabel = "2.4 km",
                        priceLabel = priceLabel,
                        statusLabel = statusLabel,
                        onClick = onClick,
                    )
                }
            }
        }
    }
}
