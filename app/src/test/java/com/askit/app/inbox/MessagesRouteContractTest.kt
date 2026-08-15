package com.askit.app.inbox

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.askit.app.jobs.JobsStore
import com.askit.app.jobs.ui.JobDetail
import com.askit.app.session.SessionProfileStore
import com.askit.designsystem.theme.AskITTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w360dp-h800dp-normal-long-notround-any-xxxhdpi-keyshidden-nonav")
class MessagesRouteContractTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun openingChat_marksConversationRead() {
        val store = InboxStore()
        store.setUnread("priya", 2)
        val viewModel = InboxViewModel(store)
        viewModel.refresh()
        assertEquals(2, viewModel.unreadCount)
        viewModel.openThread("priya")
        assertEquals(0, viewModel.store.conversation("priya")?.unreadCount)
        assertEquals(0, viewModel.unreadCount)
    }

    @Test
    fun receiverAccept_thenViewAsOtherParty_showsSeekerCode() {
        val profile = SessionProfileStore()
        val store = JobsStore(profile, seedIncomingLeads = true)
        val id = "lead-incoming-service"
        store.accept(id)
        assertEquals(com.askit.app.jobs.JobStatus.OnTheWay, store.job(id)?.status)
        assertEquals(true, store.canEnterOtp(store.job(id)!!))
        var viewAsOther by mutableStateOf(false)
        composeTestRule.setContent {
            AskITTheme {
                JobDetail(
                    job = store.job(id)!!,
                    store = store,
                    viewAsOtherParty = viewAsOther,
                    clock = { 0L },
                    onBack = {},
                    onShareCode = {},
                    onEnterCode = {},
                    onReview = {},
                )
            }
        }
        composeTestRule.onNodeWithTag("job_enter_code").assertIsDisplayed()
        composeTestRule.onNodeWithTag("job_view_as_other").assertIsDisplayed()
        viewAsOther = true
        store.toggleViewAsOtherParty()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("job_show_code").assertIsDisplayed()
    }
}
