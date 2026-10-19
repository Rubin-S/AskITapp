package com.askit.app.inbox

import com.askit.app.session.MessagingAudience
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Who may message whom follows the recipient's setting, fails closed, and is rechecked on every send. */
class MessagingPrivacyTest {
    private val friend = ChatContact("friend", "Friend")

    @Test fun theRecipientsSettingDecidesWhoMayStartAConversation() {
        fun access(preference: MessagingAudience?, contact: Boolean = false, verified: Boolean = false) =
            messagingAccess("me", "them", preference?.storedValue, contact, verified)

        assertEquals(MessageAccess.Allowed, access(MessagingAudience.Everyone))
        assertEquals(MessageAccess.ContactsOnly, access(MessagingAudience.ContactsOnly, verified = true))
        assertEquals(MessageAccess.Allowed, access(MessagingAudience.ContactsOnly, contact = true))
        assertEquals(MessageAccess.VerifiedOnly, access(MessagingAudience.VerifiedOnly, contact = true))
        assertEquals(MessageAccess.Allowed, access(MessagingAudience.VerifiedOnly, verified = true))
    }

    @Test fun unknownSettingsSelfAndBlankIdsFailClosed() {
        assertEquals(MessageAccess.Unavailable, messagingAccess("me", "them", null, true, true))
        assertEquals(MessageAccess.Unavailable, messagingAccess("me", "them", "everyone", true, true))
        assertEquals(MessageAccess.Unavailable, messagingAccess("me", "me", "Everyone", true, true))
        assertEquals(MessageAccess.Unavailable, messagingAccess("", "them", "Everyone", true, true))
    }

    @Test fun everySendRechecksTheLivePolicy() {
        var access = MessageAccess.Allowed
        val inbox = InboxStore(InboxSeed(contacts = listOf(friend)), messageAccess = { access })
        val thread = checkNotNull(inbox.startConversation(friend))
        assertTrue(inbox.sendText(thread, "hello"))

        access = MessageAccess.ContactsOnly
        assertFalse(inbox.sendText(thread, "still there?"))
        assertFalse(inbox.sendPhoto(thread, "content://photo/1"))
        assertEquals(1, inbox.messages(thread).size)
    }

    @Test fun blockingStopsMessagesButReportingAndBlockingWorkWithoutPermission() {
        val inbox = InboxStore(InboxSeed(contacts = listOf(friend)))
        // Default policy is Unavailable: nobody can be messaged until the account service says so.
        assertNull(inbox.startConversation(friend))
        inbox.moderate(friend, report = false)
        assertTrue(inbox.conversation(friend.id)?.blocked == true)

        val open = InboxStore(InboxSeed(contacts = listOf(friend)), messageAccess = { MessageAccess.Allowed })
        val thread = checkNotNull(open.startConversation(friend))
        open.block(thread)
        assertFalse(open.sendText(thread, "hi"))
        assertNull(open.startConversation(friend))
    }
}
