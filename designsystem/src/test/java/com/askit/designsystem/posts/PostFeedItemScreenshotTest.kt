package com.askit.designsystem.posts

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
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
class PostFeedItemScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun text_light() = captureVariant(
        darkTheme = false,
        fileName = "post_feed_text_light_360",
        content = PostFeedContent.Text(
            "A neighbourhood update with a useful paragraph that remains readable without social controls.",
        ),
    )

    @Test
    fun text_dark() = captureVariant(
        darkTheme = true,
        fileName = "post_feed_text_dark_360",
        content = PostFeedContent.Text("A dark-theme community update."),
    )

    @Test
    fun photo_light() = captureVariant(
        darkTheme = false,
        fileName = "post_feed_photo_light_360",
        content = PostFeedContent.Photo(
            image = media("A repaired tap"),
            caption = "The finished repair is ready for the customer.",
        ),
    )

    @Test
    fun photo_dark() = captureVariant(
        darkTheme = true,
        fileName = "post_feed_photo_dark_360",
        content = PostFeedContent.Photo(
            image = media("A repaired tap"),
            caption = "The finished repair is ready for the customer.",
        ),
    )

    @Test
    fun carousel_light() = captureVariant(
        darkTheme = false,
        fileName = "post_feed_carousel_light_360",
        content = PostFeedContent.Carousel(
            items = listOf(media("A blue tool box"), media("A repaired tap"), media("Clean fittings")),
            caption = "Three steps from inspection to finish.",
        ),
    )

    @Test
    fun carousel_dark() = captureVariant(
        darkTheme = true,
        fileName = "post_feed_carousel_dark_360",
        content = PostFeedContent.Carousel(
            items = listOf(media("A blue tool box"), media("A repaired tap")),
        ),
    )

    @Test
    fun before_after_light() = captureVariant(
        darkTheme = false,
        fileName = "post_feed_before_after_light_360",
        content = PostFeedContent.BeforeAfter(
            before = media("Before the repair"),
            after = media("After the repair"),
            caption = "A clear before and after comparison.",
            beforeNote = "Leaking joint",
            afterNote = "New sealed joint",
        ),
    )

    @Test
    fun before_after_dark() = captureVariant(
        darkTheme = true,
        fileName = "post_feed_before_after_dark_360",
        content = PostFeedContent.BeforeAfter(
            before = media("Before the repair"),
            after = media("After the repair"),
        ),
    )

    @Test
    fun poll_light() = captureVariant(
        darkTheme = false,
        fileName = "post_feed_poll_light_360",
        content = PostFeedContent.Poll(
            question = "Which finish looks better?",
            options = listOf("Matte black", "Natural wood", "White"),
            description = "Choose the finish for the next project.",
            closingSummary = "Closes 24 hours after posting",
        ),
    )

    @Test
    fun poll_dark() = captureVariant(
        darkTheme = true,
        fileName = "post_feed_poll_dark_360",
        content = PostFeedContent.Poll(
            question = "Which finish looks better?",
            options = listOf("Matte black", "Natural wood"),
            closingSummary = "Closes 24 hours after posting",
        ),
    )

    @Test
    @Config(qualifiers = "w320dp-h800dp-normal-long-notround-any-xxxhdpi-keyshidden-nonav")
    fun carousel_narrow() = captureVariant(
        darkTheme = false,
        fileName = "post_feed_carousel_narrow_320",
        content = PostFeedContent.Carousel(
            items = listOf(media("A blue tool box"), media("A repaired tap")),
        ),
        contentWidth = 320.dp,
    )

    @Test
    @Config(qualifiers = "w800dp-h600dp-normal-long-notround-any-xxxhdpi-keyshidden-nonav")
    fun before_after_wide() = captureVariant(
        darkTheme = false,
        fileName = "post_feed_before_after_wide_800",
        content = PostFeedContent.BeforeAfter(
            before = media("Before the repair"),
            after = media("After the repair"),
        ),
        contentWidth = 800.dp,
    )

    @Test
    @Config(qualifiers = "ta-rIN-w360dp-h800dp-normal-long-notround-any-xxxhdpi-keyshidden-nonav")
    fun poll_tamil() = captureVariant(
        darkTheme = false,
        fileName = "post_feed_poll_tamil_360",
        content = PostFeedContent.Poll(
            question = "Which finish looks better?",
            options = listOf("Matte black", "Natural wood"),
            closingSummary = "Closes 24 hours after posting",
        ),
    )

    private fun captureVariant(
        darkTheme: Boolean,
        fileName: String,
        content: PostFeedContent,
        contentWidth: Dp = 360.dp,
    ) {
        composeTestRule.setContent {
            AskITTheme(darkTheme = darkTheme) {
                Surface {
                    PostFeedItem(
                        author = PostFeedAuthor("You"),
                        locationLabel = "Karaikal",
                        content = content,
                        modifier = Modifier.width(contentWidth).fillMaxWidth(),
                    )
                }
            }
        }
        composeTestRule.onRoot().captureRoboImage(
            filePath = "src/test/screenshots/$fileName.png",
        )
    }

    private fun media(description: String): PostFeedMedia = PostFeedMedia(
        model = samplePhoto(),
        contentDescription = description,
    )

    private fun samplePhoto(): Bitmap = Bitmap.createBitmap(240, 160, Bitmap.Config.ARGB_8888).also {
        Canvas(it).drawColor(Color.rgb(51, 112, 148))
        Canvas(it).drawRect(
            28f,
            24f,
            212f,
            136f,
            Paint().apply { color = Color.rgb(237, 176, 82) },
        )
    }
}
