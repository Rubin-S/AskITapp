package com.askit.designsystem.people

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.askit.designsystem.theme.AskITTheme
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w360dp-h800dp-normal-long-notround-any-xxxhdpi-keyshidden-nonav")
class PersonResultItemScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun person_result_items_light() {
        captureItems(darkTheme = false, fileName = "person_result_items_light")
    }

    @Test
    fun person_result_items_dark() {
        captureItems(darkTheme = true, fileName = "person_result_items_dark")
    }

    private fun captureItems(darkTheme: Boolean, fileName: String) {
        composeTestRule.setContent {
            AskITTheme(darkTheme = darkTheme) {
                Surface {
                    Column(modifier = Modifier.width(360.dp).fillMaxWidth()) {
                        EstablishedPerson()
                        HorizontalDivider()
                        OrdinaryPerson()
                        HorizontalDivider()
                        NewPerson()
                        HorizontalDivider()
                        LongContentPerson()
                    }
                }
            }
        }

        composeTestRule.onRoot().captureRoboImage(
            filePath = "src/test/screenshots/$fileName.png",
        )
    }

    @Composable
    private fun EstablishedPerson() {
        PersonResultItem(
            name = "Ravi Kumar",
            avatarUrl = null,
            primaryService = "Electrician",
            additionalServices = listOf("Fan installation", "Wiring", "Appliance repair"),
            rating = 4.8,
            reviewCount = 36,
            locationLabel = "2.4 km",
            priceLabel = "From ₹500",
            statusLabel = "Available today",
            onClick = {},
        )
    }

    @Composable
    private fun NewPerson() {
        PersonResultItem(
            name = "Priya S.",
            avatarUrl = "",
            primaryService = "Home tutor",
            additionalServices = listOf("Mathematics", "Science"),
            rating = null,
            reviewCount = 0,
            locationLabel = "Kallakurichi",
            priceLabel = null,
            statusLabel = null,
            onClick = {},
        )
    }

    @Composable
    private fun OrdinaryPerson() {
        PersonResultItem(
            name = "Arun Kumar",
            avatarUrl = null,
            primaryService = null,
            additionalServices = emptyList(),
            rating = 4.8,
            reviewCount = 36,
            locationLabel = "Kallakurichi",
            priceLabel = "From â‚¹500",
            statusLabel = "Available today",
            onClick = {},
        )
    }

    @Composable
    private fun LongContentPerson() {
        PersonResultItem(
            name = "A very long professional name that should truncate cleanly",
            avatarUrl = null,
            primaryService = "A service with a long descriptive title",
            additionalServices = emptyList(),
            rating = 4.9,
            reviewCount = 18,
            locationLabel = "A long locality label for a narrow screen",
            priceLabel = "Quote required",
            statusLabel = null,
            onClick = {},
        )
    }
}
