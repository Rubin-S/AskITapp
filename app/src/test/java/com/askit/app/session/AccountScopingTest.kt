package com.askit.app.session

import com.askit.app.inbox.ChatContact
import com.askit.app.inbox.InboxViewModel
import com.askit.app.inbox.MessageAccess
import com.askit.app.jobs.JobWorkMode
import com.askit.app.jobs.JobsStore
import com.askit.app.testing.MainDispatcherRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** One account's jobs and conversations must never be visible to, or writable by, another account. */
class AccountScopingTest {
    @get:Rule val main = MainDispatcherRule()

    @Test fun jobsAreClearedOnSignOutAndInvisibleToTheNextAccount() {
        val store = JobsStore(SessionProfileStore())
        store.activateAccount("a")
        store.requestService("Fix wiring", "Priya", JobWorkMode.OnSite)
        assertEquals(1, store.jobs.value.size)

        store.activateAccount(null)
        assertTrue(store.jobs.value.isEmpty())
        store.activateAccount("b")
        assertTrue(store.jobs.value.isEmpty())
        store.activateAccount("a")
        assertTrue(store.jobs.value.isEmpty())
    }

    @Test fun conversationsAreClearedOnSignOutAndInvisibleToTheNextAccount() {
        val inbox = allowedInbox()
        inbox.activateAccount("a")
        val thread = checkNotNull(inbox.startConversation(friend))
        assertTrue(inbox.sendText(thread, "my address is 12 Beach Rd"))
        assertEquals(1, inbox.conversations.size)

        inbox.activateAccount(null)
        assertTrue(inbox.conversations.isEmpty())
        assertEquals(0, inbox.unreadCount)
        inbox.activateAccount("b")
        assertTrue(inbox.conversations.isEmpty())
        assertTrue(inbox.store.messages(thread).isEmpty())
    }

    @Test fun aStaleStoreFromThePreviousAccountCannotSendAfterASwitch() {
        val inbox = allowedInbox()
        inbox.activateAccount("a")
        val stale = inbox.store
        val thread = checkNotNull(stale.startConversation(friend))
        inbox.activateAccount("b")
        assertFalse(stale.sendText(thread, "sent as a after switching to b"))
        assertNull(stale.startConversation(ChatContact("other", "Other")))
    }

    private val friend = ChatContact("friend", "Friend")

    private fun allowedInbox() = InboxViewModel().apply { configureAccess { _, _, _ -> MessageAccess.Allowed } }
}
