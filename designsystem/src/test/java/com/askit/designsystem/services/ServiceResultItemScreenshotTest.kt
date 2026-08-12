package com.askit.designsystem.services

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import com.askit.designsystem.theme.AskITTheme
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
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
class ServiceResultItemScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun service_result_items_light() {
        captureItems(darkTheme = false, fileName = "service_result_items_light")
    }

    @Test
    fun service_result_items_dark() {
        captureItems(darkTheme = true, fileName = "service_result_items_dark")
    }

    @Test
    @Config(qualifiers = "w320dp-h800dp-normal-long-notround-any-xxxhdpi-keyshidden-nonav")
    fun service_result_items_narrow_large_text() {
        captureItems(darkTheme = false, fileName = "service_result_items_narrow_large_text")
    }

    private fun captureItems(darkTheme: Boolean, fileName: String) {
        composeTestRule.setContent {
            AskITTheme(darkTheme = darkTheme) {
                Surface {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        RichService()
                        HorizontalDivider()
                        SparseService()
                    }
                }
            }
        }
        composeTestRule.onRoot().captureRoboImage(
            filePath = "src/test/screenshots/$fileName.png",
        )
    }

    @Composable
    private fun RichService() {
        ServiceResultItem(
            serviceTitle = "Assemble the new wardrobe",
            category = "Furniture assembly",
            description = "I assemble flat-pack wardrobes and align the doors carefully.",
            providerName = "Rubin S.",
            priceLabel = "₹800–₹1,500",
            coverageLabel = "Serves Anna Nagar and nearby areas",
            deliveryModes = listOf(
                "At customer location",
                "At provider location · Adyar",
                "Up to 10 km",
            ),
            portfolioModels = listOf(samplePhoto(), samplePhoto()),
            onClick = {},
        )
    }

    @Composable
    private fun SparseService() {
        ServiceResultItem(
            serviceTitle = "Remote tap advice",
            category = "Plumbing",
            description = "I help customers diagnose tap problems over a video call.",
            priceLabel = "Contact for quote",
            deliveryModes = listOf("Remote"),
        )
    }

    private fun samplePhoto(): Bitmap = Bitmap.createBitmap(240, 160, Bitmap.Config.ARGB_8888).also {
        Canvas(it).drawColor(Color.rgb(50, 100, 150))
        Canvas(it).drawRect(
            30f,
            24f,
            210f,
            136f,
            Paint().apply { color = Color.rgb(237, 176, 82) },
        )
    }
}
