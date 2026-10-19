package com.askit.app.inbox

import org.junit.Assert.*
import org.junit.Test

class MessagingPrivacyTest {
    @Test fun recipientPolicyRequiresExplicitContactOrVerification() {
        assertEquals(MessageAccess.Allowed, messagingAccess("sender", "recipient", "Everyone", false, false))
        assertEquals(MessageAccess.ContactsOnly, messagingAccess("sender", "recipient", "Contacts only", false, true))
        assertEquals(MessageAccess.Allowed, messagingAccess("sender", "recipient", "Contacts only", true, false))
        assertEquals(MessageAccess.VerifiedOnly, messagingAccess("sender", "recipient", "Verified users only", true, false))
        assertEquals(MessageAccess.Allowed, messagingAccess("sender", "recipient", "Verified users only", false, true))
        assertEquals(MessageAccess.Unavailable, messagingAccess("sender", "recipient", null, true, true))
        assertEquals(MessageAccess.Unavailable, messagingAccess("sender", "recipient", "unknown", true, true))
        assertEquals(MessageAccess.Unavailable, messagingAccess("sender", "sender", "Everyone", true, true))
        assertEquals(MessageAccess.Unavailable, messagingAccess("", "recipient", "Everyone", true, true))
    }

    @Test fun restrictionsPreventCreationAndRecheckExistingTextPhotoAndStorySends() {
        var access = MessageAccess.ContactsOnly
        val store = InboxStore(messageAccess = { access })
        val contact = ChatContact("recipient", "Recipient")
        assertNull(store.startConversation(contact))
        assertTrue(store.conversations().isEmpty())
        access = MessageAccess.Allowed
        val id = requireNotNull(store.startConversation(contact))
        assertTrue(store.sendText(id, "Allowed"))
        access = MessageAccess.VerifiedOnly
        assertFalse(store.sendText(id, "Denied"))
        assertFalse(store.sendPhoto(id, "content://photo"))
        assertEquals(1, store.messages(id).size)
        store.moderate(contact, report = false)
        access = MessageAccess.Allowed
        assertNull(store.startConversation(contact))
        assertFalse(store.sendText(id, "Blocked"))
        assertEquals(1, store.messages(id).size)
    }

    @Test fun unconfiguredMessagingFailsClosedButModerationDoesNotRequireMessagePermission() {
        val store = InboxStore()
        val contact = ChatContact("recipient", "Recipient")
        assertNull(store.startConversation(contact))
        store.moderate(contact, report = true)
        assertTrue(requireNotNull(store.conversation(contact.id)).reported)
        assertFalse(store.sendText(contact.id, "Denied"))
        assertFalse(store.sendPhoto(contact.id, "content://photo"))
    }

    @Test fun sharedStoriesUseTheSameLivePolicy() {
        var access = MessageAccess.ContactsOnly
        val store = InboxStore(InboxSeed(contacts = listOf(ChatContact("priya", "Priya"))), messageAccess = { access })
        val contact = store.contacts.first()
        val story = com.askit.app.home.model.Story(
            "story",
            "Author",
            content = com.askit.app.story.StoryDraft(),
            createdAtMillis = System.currentTimeMillis(),
        )
        assertFalse(store.sendStory(contact, story))
        access = MessageAccess.Allowed
        assertTrue(store.sendStory(contact, story))
        access = MessageAccess.VerifiedOnly
        assertFalse(store.sendStory(contact, story))
        // Stores start empty now, so only the one allowed share is in the thread.
        assertEquals(1, store.messages(contact.id).size)
        assertEquals("story", store.messages(contact.id).single().sharedStory?.storyId)
    }

    @Test fun switchingAccountsPreservesRecipientBlocksAndRejectsStaleStoreWrites() {
        // Cross-account blocks only exist where accounts are kept side by side (client preview).
        val inbox = InboxViewModel(retainInactiveAccounts = true)
        inbox.configureAccess { _, _, _ -> MessageAccess.Allowed }
        inbox.activateAccount("recipient")
        inbox.moderate(ChatContact("sender", "Sender"), report = false)
        val staleStore = inbox.store
        inbox.activateAccount("sender")
        assertNull(inbox.startConversation(ChatContact("recipient", "Recipient")))
        assertNull(staleStore.startConversation(ChatContact("third", "Third")))
        inbox.activateAccount(null)
        assertNull(inbox.startConversation(ChatContact("third", "Third")))
    }

    @Test fun contactsAreReadFromTheRecipientRatherThanTheSender() {
        val inbox = InboxViewModel(
            seed = { InboxSeed(contacts = listOf(ChatContact("priya", "Priya"))) },
            retainInactiveAccounts = true,
        )
        inbox.configureAccess { sender, recipient, contact -> messagingAccess(sender, recipient, "Contacts only", contact, false) }
        inbox.activateAccount("priya")
        assertNotNull(inbox.startConversation(ChatContact("recipient", "Recipient")))
        inbox.activateAccount("recipient")
        assertNull(inbox.startConversation(ChatContact("priya", "Priya")))
    }

    @Test fun productionCannotPretendToSavePrivacyWithoutTheAccountService() {
        val store = com.askit.app.session.SessionProfileStore()
        val capabilities = com.askit.app.profile.ProfileCapabilities.forBuild(clientPreview = false)
        val profile = com.askit.app.profile.ProfileViewModel(store, capabilities)
        // Release hides the control (capabilities.editProfile is false) and the view model refuses the write.
        assertFalse(capabilities.editProfile)
        profile.updateWhoCanMessage("Contacts only")
        assertEquals("Everyone", store.profile.value.whoCanMessage)
    }
}
