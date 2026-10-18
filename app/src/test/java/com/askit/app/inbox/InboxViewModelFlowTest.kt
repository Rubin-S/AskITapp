package com.askit.app.inbox

import app.cash.turbine.test
import com.askit.app.R
import com.askit.app.testing.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class InboxViewModelFlowTest {
    @get:Rule val main = MainDispatcherRule()

    private val priya = ChatContact("priya", "Priya")
    private var access = MessageAccess.Allowed

    private fun viewModel(retain: Boolean = false) =
        InboxViewModel(seed = { InboxSeed(contacts = listOf(priya)) }, retainInactiveAccounts = retain).apply {
            configureAccess { _, _, _ -> access }
        }

    @Test fun deniedMessagingEmitsTheRecipientsPolicyAsFeedback() = runTest {
        val vm = viewModel()
        vm.activateAccount("me")
        access = MessageAccess.ContactsOnly
        vm.feedback.test {
            assertNull(vm.startConversation(priya))
            assertEquals(R.string.messages_contacts_only, awaitItem())
            access = MessageAccess.VerifiedOnly
            assertNull(vm.startConversation(priya))
            assertEquals(MessageAccess.VerifiedOnly.messageRes, awaitItem())
            access = MessageAccess.Allowed
            assertNotNull(vm.startConversation(priya))
            expectNoEvents()
        }
        assertEquals(1, vm.conversations.size)
    }

    @Test fun signOutDropsConversationsAndUnreadCount() = runTest {
        val vm = viewModel()
        vm.activateAccount("me")
        val id = requireNotNull(vm.startConversation(priya))
        assertTrue(vm.sendText(id, "Hello"))
        assertEquals(1, vm.conversations.size)
        vm.activateAccount(null)
        assertTrue(vm.conversations.isEmpty())
        assertEquals(0, vm.unreadCount)
        // Signed out: nothing can be sent and no stale thread comes back on the next sign-in.
        assertFalse(vm.sendText(id, "Still there?"))
        vm.activateAccount("me")
        assertTrue(vm.conversations.isEmpty())
    }

    @Test fun storiesCanOnlyBeSentFromTheActiveAccount() = runTest {
        val vm = viewModel()
        vm.activateAccount("me")
        val story = com.askit.app.home.model.Story(
            "story",
            "Author",
            content = com.askit.app.story.StoryDraft(),
            createdAtMillis = System.currentTimeMillis(),
        )
        vm.feedback.test {
            assertFalse(vm.sendStory("someone-else", priya, story))
            assertFalse(vm.sendStory(null, priya, story))
            assertTrue(vm.sendStory("me", priya, story))
            expectNoEvents()
        }
        assertEquals(1, vm.conversations.size)
    }

    @Test fun aBlockFromTheOtherRetainedAccountIsReportedAsBlocked() = runTest {
        val vm = viewModel(retain = true)
        vm.activateAccount("priya")
        val toMe = requireNotNull(vm.startConversation(ChatContact("me", "Me")))
        vm.block(toMe)
        vm.activateAccount("me")
        vm.feedback.test {
            assertNull(vm.startConversation(priya))
            assertEquals(MessageAccess.Blocked.messageRes, awaitItem())
        }
    }
}
