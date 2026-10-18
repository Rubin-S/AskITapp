package com.askit.app.session

/**
 * Holds one [T] per account and exposes the active account's as [current].
 *
 * Switching account never hands one account's value to another. With [retainInactive] false
 * (production) the outgoing account's value is dropped, so signing out or switching user clears
 * it. The client preview passes true so its local personas keep their data while you switch
 * between them. The signed-out (null) value is never retained.
 */
class AccountScope<T : Any>(
    private val retainInactive: Boolean,
    private val create: (accountId: String?) -> T,
) {
    var accountId: String? = null
        private set
    var current: T = create(null)
        private set
    private val retained = mutableMapOf<String, T>()

    /** The value for [accountId] if it is active or retained; never creates one. */
    fun peek(accountId: String): T? = if (accountId == this.accountId) current else retained[accountId]

    /** Accounts whose values are kept while inactive (always empty when not retaining). */
    val retainedAccounts: Set<String> get() = retained.keys.toSet()

    /**
     * Makes [accountId] (null or blank = signed out) the active account.
     * Returns false when it already was, true when [current] changed.
     */
    fun activate(accountId: String?): Boolean {
        val next = accountId?.takeIf(String::isNotBlank)
        if (next == this.accountId) return false
        this.accountId?.let { outgoing -> if (retainInactive) retained[outgoing] = current }
        this.accountId = next
        current = next?.let(retained::remove) ?: create(next)
        return true
    }
}
