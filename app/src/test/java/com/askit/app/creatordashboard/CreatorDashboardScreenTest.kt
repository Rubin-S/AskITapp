package com.askit.app.creatordashboard

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.askit.app.session.SessionProfileStore
import com.askit.designsystem.theme.AskITTheme
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w360dp-h800dp-normal-long-notround-any-xxxhdpi-keyshidden-nonav")
class CreatorDashboardScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun screenRenders_allSectionsAndKpis_inEnglish() {
        val viewModel = CreatorDashboardViewModel(SessionProfileStore())
        val listState = LazyListState()
        var postCreatedTopic: String? = null
        var openedChatId: String? = null

        composeTestRule.setContent {
            AskITTheme(darkTheme = false) {
                CreatorDashboardRoute(
                    viewModel = viewModel,
                    onBack = {},
                    onCreatePost = { postCreatedTopic = it },
                    onOpenChat = { openedChatId = it },
                    onOpenPostDetail = {},
                    onOpenProfile = {},
                    lazyListState = listState,
                )
            }
        }

        // Top bar & time range
        composeTestRule.onNodeWithText("Creator Dashboard").assertIsDisplayed()
        composeTestRule.onNodeWithText("Week").assertIsDisplayed()
        composeTestRule.onNodeWithText("Month").assertIsDisplayed()

        // Hero KPI
        composeTestRule.onNodeWithText("Total Reach").assertIsDisplayed()
        composeTestRule.onAllNodesWithText("18.4k")[0].assertIsDisplayed()
        composeTestRule.onNodeWithText("Engagement Rate").assertIsDisplayed()
        composeTestRule.onAllNodesWithText("4.8%")[0].assertIsDisplayed()

        // Sections: scroll to discovery
        composeTestRule.runOnIdle { runBlocking { listState.scrollToItem(3) } }
        composeTestRule.onNodeWithText("Discovery & Reach").assertIsDisplayed()

        // Scroll to AI Insight
        composeTestRule.runOnIdle { runBlocking { listState.scrollToItem(8) } }
        composeTestRule.onNodeWithText("Prime posting window today").assertIsDisplayed()
        composeTestRule.onNodeWithText("Create Trending Post").assertIsDisplayed()
        composeTestRule.onNodeWithText("Create Trending Post").performClick()
        assertEquals("Community help", postCreatedTopic)

        // Audience Milestone
        composeTestRule.runOnIdle { runBlocking { listState.scrollToItem(9) } }
        composeTestRule.onNodeWithText("Audience Growth").assertIsDisplayed()

        // Recent Activity
        composeTestRule.runOnIdle { runBlocking { listState.scrollToItem(10) } }
        composeTestRule.onNodeWithText("Recent Activity").assertIsDisplayed()
        composeTestRule.onNodeWithText("New inquiry from Arun K.").assertIsDisplayed()
        composeTestRule.onNodeWithText("New inquiry from Arun K.").performClick()
        assertEquals("conv-arun-k", openedChatId)
    }

    @Test
    fun timeRangeSwitching_updatesUi() {
        val viewModel = CreatorDashboardViewModel(SessionProfileStore())

        composeTestRule.setContent {
            AskITTheme(darkTheme = false) {
                CreatorDashboardRoute(
                    viewModel = viewModel,
                    onBack = {},
                    onCreatePost = {},
                    onOpenChat = {},
                    onOpenPostDetail = {},
                    onOpenProfile = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Today").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onAllNodesWithText("1,240")[0].assertIsDisplayed()
        composeTestRule.onAllNodesWithText("5.2%")[0].assertIsDisplayed()

        composeTestRule.onNodeWithText("Month").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onAllNodesWithText("74.8k")[0].assertIsDisplayed()
    }

    @Test
    @Config(qualifiers = "ta-rIN-w360dp-h800dp-normal-long-notround-any-xxxhdpi-keyshidden-nonav")
    fun screenRenders_tamilNativeCopywritingAccurately() {
        val viewModel = CreatorDashboardViewModel(SessionProfileStore())
        val listState = LazyListState()

        composeTestRule.setContent {
            AskITTheme(darkTheme = false) {
                CreatorDashboardRoute(
                    viewModel = viewModel,
                    onBack = {},
                    onCreatePost = {},
                    onOpenChat = {},
                    onOpenPostDetail = {},
                    onOpenProfile = {},
                    lazyListState = listState,
                )
            }
        }

        composeTestRule.onNodeWithText("படைப்பாளர் டாஷ்போர்டு").assertIsDisplayed()
        composeTestRule.onNodeWithText("வாரம்").assertIsDisplayed()
        composeTestRule.onNodeWithText("மொத்த பார்வை வீச்சு").assertIsDisplayed()
        composeTestRule.onNodeWithText("ஈடுபாடு விகிதம்").assertIsDisplayed()

        composeTestRule.runOnIdle { runBlocking { listState.scrollToItem(3) } }
        composeTestRule.onNodeWithText("கண்டுபிடிப்பு & பார்வை வீச்சு").assertIsDisplayed()

        composeTestRule.runOnIdle { runBlocking { listState.scrollToItem(8) } }
        composeTestRule.onNodeWithText("பிரபலமான பதிவை உருவாக்கவும்").assertIsDisplayed()
        composeTestRule.onNodeWithText("பார்வையாளர்கள் வளர்ச்சி").assertIsDisplayed()

        composeTestRule.runOnIdle { runBlocking { listState.scrollToItem(10) } }
        composeTestRule.onNodeWithText("சமீபத்திய செயல்பாடு").assertIsDisplayed()
    }
}
