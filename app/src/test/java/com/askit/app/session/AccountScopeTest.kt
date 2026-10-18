package com.askit.app.session

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class AccountScopeTest {
    private class Box(val owner: String?, val items: MutableList<String> = mutableListOf())

    @Test fun startsSignedOutAndIgnoresBlankIds() {
        val scope = AccountScope(retainInactive = false) { Box(it) }
        assertNull(scope.accountId)
        assertFalse(scope.activate(null))
        assertFalse(scope.activate("  "))
        assertNull(scope.current.owner)
    }

    @Test fun productionDropsTheOutgoingAccount() {
        val scope = AccountScope(retainInactive = false) { Box(it) }
        scope.activate("a")
        scope.current.items += "a's secret"
        scope.activate(null)
        scope.activate("a")
        assertEquals(emptyList<String>(), scope.current.items)
        assertEquals(emptySet<String>(), scope.retainedAccounts)
    }

    @Test fun switchingNeverHandsOneAccountsValueToAnother() {
        val scope = AccountScope(retainInactive = true) { Box(it) }
        scope.activate("a")
        val a = scope.current
        scope.activate("b")
        assertNotSame(a, scope.current)
        assertEquals("b", scope.current.owner)
    }

    @Test fun previewKeepsInactiveAccountsButNeverTheSignedOutOne() {
        val scope = AccountScope(retainInactive = true) { Box(it) }
        scope.current.items += "anonymous"
        scope.activate("a")
        val a = scope.current
        a.items += "kept"
        scope.activate("b")
        assertSame(a, scope.peek("a"))
        assertEquals(setOf("a"), scope.retainedAccounts)
        assertTrue(scope.activate("a"))
        assertSame(a, scope.current)
        scope.activate(null)
        assertEquals(emptyList<String>(), scope.current.items)
    }
}
