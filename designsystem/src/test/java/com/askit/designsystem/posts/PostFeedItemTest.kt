package com.askit.designsystem.posts

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.askit.designsystem.theme.AskITTheme
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PostFeedItemTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun rendersAllFiveBodies_withoutFakeSocialActions() {
        composeTestRule.setContent {
            AskITTheme(darkTheme = false) {
                Box(modifier = Modifier.width(360.dp)) {
                    ColumnOfPosts()
                }
            }
        }

        composeTestRule.onNodeWithText("A community update").assertIsDisplayed()
        composeTestRule.onNodeWithTag("post_list").performScrollToNode(
            hasText("A photo caption", substring = true),
        )
        composeTestRule.onNodeWithText("A photo caption", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithTag("post_list").performScrollToNode(
            hasContentDescription("Image 1 of 3", substring = true),
        )
        composeTestRule.onNodeWithContentDescription("Image 1 of 3", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithTag("post_list").performScrollToNode(
            hasTestTag("post_feed_before_after_slider"),
        )
        composeTestRule.onNodeWithTag("post_feed_before_after_slider").assertIsDisplayed()
        composeTestRule.onNodeWithText("Before", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("After", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithTag("post_list").performScrollToNode(
            hasText("Which finish looks better?", substring = true),
        )
        composeTestRule.onNodeWithText("Which finish looks better?", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Matte black").assertIsDisplayed()
        composeTestRule.onNodeWithTag("post_list").performScrollToNode(
            hasText("Closes 24 hours after posting", substring = true),
        )
        composeTestRule.onNodeWithText("Closes 24 hours after posting", substring = true).assertIsDisplayed()
        composeTestRule.onAllNodes(hasClickAction(), useUnmergedTree = true).assertCountEquals(0)
    }

    @Test
    fun carouselPageSemantics_areMeaningful_andPreviewIsNotOneButton() {
        composeTestRule.setContent {
            AskITTheme {
                PostFeedItem(
                    author = PostFeedAuthor("You"),
                    content = PostFeedContent(
                        media = PostFeedMediaContent.Carousel(
                            items = listOf(
                                PostFeedMedia(samplePhoto(), "A blue tool box"),
                                PostFeedMedia(samplePhoto(), "A repaired tap"),
                            ),
                        ),
                    ),
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("A blue tool box. Image 1 of 2").assertIsDisplayed()
        assertFalse(
            composeTestRule
                .onNodeWithText("You")
                .fetchSemanticsNode()
                .config
                .contains(SemanticsActions.OnClick),
        )
    }

    @Composable
    private fun ColumnOfPosts() {
        LazyColumn(modifier = Modifier.testTag("post_list")) {
            item {
                PostFeedItem(
                author = PostFeedAuthor("You"),
                locationLabel = "Karaikal",
                content = PostFeedContent(body = "A community update"),
                )
            }
            item {
                PostFeedItem(
                author = PostFeedAuthor("You"),
                content = PostFeedContent(
                    media = PostFeedMediaContent.Photo(PostFeedMedia(samplePhoto())),
                    body = "A photo caption",
                ),
                )
            }
            item {
                PostFeedItem(
                author = PostFeedAuthor("You"),
                content = PostFeedContent(
                    media = PostFeedMediaContent.Carousel(
                        items = listOf(
                            PostFeedMedia(samplePhoto()),
                            PostFeedMedia(samplePhoto()),
                            PostFeedMedia(samplePhoto()),
                        ),
                    ),
                ),
                )
            }
            item {
                PostFeedItem(
                author = PostFeedAuthor("You"),
                content = PostFeedContent(
                    media = PostFeedMediaContent.BeforeAfter(
                        before = PostFeedMedia(samplePhoto()),
                        after = PostFeedMedia(samplePhoto()),
                    ),
                ),
                )
            }
            item {
                PostFeedItem(
                author = PostFeedAuthor("You"),
                content = PostFeedContent(
                    poll = PostFeedPoll(
                        question = "Which finish looks better?",
                        options = listOf("Matte black", "Natural wood"),
                        closingSummary = "Closes 24 hours after posting",
                    ),
                ),
                )
            }
        }
    }

    private fun samplePhoto(): Bitmap = Bitmap.createBitmap(160, 120, Bitmap.Config.ARGB_8888).also {
        Canvas(it).drawColor(Color.rgb(51, 112, 148))
    }
}
