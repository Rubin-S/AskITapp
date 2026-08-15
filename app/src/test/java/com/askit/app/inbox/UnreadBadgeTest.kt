package com.askit.app.inbox

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.lifecycle.SavedStateHandle
import com.askit.app.AskITApp
import com.askit.app.explore.ExploreViewModel
import com.askit.app.jobs.JobsStore
import com.askit.app.jobs.JobsViewModel
import com.askit.app.session.SessionProfileStore
import com.askit.designsystem.theme.AskITTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w360dp-h800dp-normal-long-notround-any-xxxhdpi-keyshidden-nonav")
class UnreadBadgeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun bottomBar_exposesUnreadCount() {
        val store = InboxStore()
        store.setUnread("priya", 2)
        val inboxViewModel = InboxViewModel(store)
        inboxViewModel.refresh()
        composeTestRule.setContent {
            AskITTheme {
                AskITApp(
                    exploreViewModel = ExploreViewModel(SavedStateHandle()),
                    inboxViewModel = inboxViewModel,
                    jobsViewModel = JobsViewModel(
                        JobsStore(SessionProfileStore(), seedIncomingLeads = false),
                        SessionProfileStore(),
                    ),
                )
            }
        }
        composeTestRule.onNodeWithContentDescription("Messages, 2 unread messages").assertIsDisplayed()
        assertEquals(2, inboxViewModel.unreadCount)
    }
}
