package com.askit.app.inbox

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class InboxAccountScopeTest {
    @Test fun signingOutDropsTheAccountsConversationsInProduction() {
        val inbox = InboxViewModel()
        inbox.configureAccess { _, _, _ -> MessageAccess.Allowed }
        inbox.activateAccount("a")
        assertNotNull(inbox.startConversation(ChatContact("x", "X")))
        assertEquals(1, inbox.conversations.size)
        inbox.activateAccount(null)
        assertTrue(inbox.conversations.isEmpty())
        inbox.activateAccount("a")
        assertTrue(inbox.conversations.isEmpty())
    }

    @Test fun theNextAccountNeverSeesThePreviousAccountsThreads() {
        val inbox = InboxViewModel(retainInactiveAccounts = true)
        inbox.configureAccess { _, _, _ -> MessageAccess.Allowed }
        inbox.activateAccount("a")
        inbox.startConversation(ChatContact("x", "X"))
        inbox.activateAccount("b")
        assertTrue(inbox.conversations.isEmpty())
        inbox.activateAccount("a")
        assertEquals(1, inbox.conversations.size)
    }
}
