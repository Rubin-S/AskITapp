package com.askit.app.home.stories

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import com.askit.app.home.model.Story
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
class StoryViewerScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun viewer_light() = capture(darkTheme = false, fileName = "story_viewer_light_360")

    @Test
    fun viewer_dark() = capture(darkTheme = true, fileName = "story_viewer_dark_360")

    private fun capture(darkTheme: Boolean, fileName: String) {
        val firstAuthorStories = listOf(
            story(id = "story-1", author = "faraan_141"),
            story(id = "story-2b", author = "faraan_141"),
        )
        val state = StoryViewerUiState(
            isLoading = false,
            groups = listOf(
                StoryGroup(
                    authorName = "faraan_141",
                    authorAvatarUrl = null,
                    stories = firstAuthorStories,
                ),
                StoryGroup(
                    authorName = "nitpuducherry",
                    authorAvatarUrl = null,
                    stories = listOf(story(id = "story-3x", author = "nitpuducherry")),
                ),
            ),
            groupIndex = 0,
            itemIndex = 0,
            paused = true,
        )

        composeTestRule.setContent {
            AskITTheme(darkTheme = darkTheme) {
                Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
                    StoryViewerScreen(
                        state = state,
                        onNext = {},
                        onPrevious = {},
                        onPauseChanged = {},
                        onLikeClick = {},
                        onSelectGroup = {},
                        onDismiss = {},
                    )
                }
            }
        }
        composeTestRule.onRoot().captureRoboImage(
            filePath = "src/test/screenshots/$fileName.png",
        )
    }

    private fun story(id: String, author: String) = Story(
        id = id,
        authorName = author,
        authorAvatarUrl = null,
        mediaUrl = "https://example.com/$id.jpg",
        caption = "Finished electrical rewiring",
        createdAtMillis = System.currentTimeMillis() - 3_600_000,
    )
}
