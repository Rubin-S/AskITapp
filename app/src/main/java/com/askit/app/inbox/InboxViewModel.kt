package com.askit.app.inbox

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.askit.app.session.AccountScope
import kotlinx.coroutines.flow.asSharedFlow

class InboxViewModel(
    private val seed: () -> InboxSeed = { InboxSeed.Empty },
    /**
     * Keep each inactive account's conversations in memory. Only the client preview does, so its
     * local personas can message each other; production drops them on sign-out or account switch.
     */
    retainInactiveAccounts: Boolean = false,
) : ViewModel() {
    private val accounts = AccountScope(retainInactiveAccounts) { id -> id?.let(::newStore) ?: InboxStore() }
    var store by mutableStateOf(accounts.current)
        private set
    private val accountId: String? get() = accounts.accountId

    private var resolveAccess: (String, String, Boolean) -> MessageAccess = { _, _, _ -> MessageAccess.Unavailable }
    private val _feedback = kotlinx.coroutines.flow.MutableSharedFlow<Int>(extraBufferCapacity = 1)
    val feedback = _feedback.asSharedFlow()

    fun configureAccess(resolver: (senderId: String, recipientId: String, isRecipientContact: Boolean) -> MessageAccess) {
        resolveAccess = resolver
    }

    private fun newStore(ownerId: String): InboxStore = InboxStore(seed = seed(), messageAccess = { recipientId ->
        val recipientStore = accounts.peek(recipientId)
        val access = when {
            accountId != ownerId -> MessageAccess.Unavailable

            store.conversation(
                recipientId,
            )?.blocked == true || recipientStore?.conversation(ownerId)?.blocked == true -> MessageAccess.Blocked

            else -> resolveAccess(ownerId, recipientId, (recipientStore?.contacts ?: seed().contacts).any { it.id == ownerId })
        }
        access.messageRes?.let { _feedback.tryEmit(it) }
        access
    })

    /** Switches to [id]'s conversations (null = signed out); see [AccountScope] for what is kept. */
    fun activateAccount(id: String?) {
        if (!accounts.activate(id)) return
        store = accounts.current
        refresh()
    }

    fun sendStory(account: String?, contact: ChatContact, story: com.askit.app.home.model.Story): Boolean {
        if (account.isNullOrBlank() || account != accountId) return false
        val sent = store.sendStory(contact, story)
        if (sent) refresh()
        return sent
    }

    var conversations by mutableStateOf(store.conversations())
        private set
    var unreadCount by mutableIntStateOf(store.unreadCount())
        private set

    fun refresh() {
        conversations = store.conversations()
        unreadCount = store.unreadCount()
    }

    fun openThread(conversationId: String) {
        store.markRead(conversationId)
        refresh()
    }

    fun sendText(conversationId: String, body: String): Boolean {
        val sent = store.sendText(conversationId, body)
        if (sent) refresh()
        return sent
    }

    fun sendPhoto(conversationId: String, photoUri: String): Boolean {
        val sent = store.sendPhoto(conversationId, photoUri)
        if (sent) refresh()
        return sent
    }

    fun setMuted(conversationId: String, muted: Boolean) {
        store.setMuted(conversationId, muted)
        refresh()
    }

    fun block(conversationId: String) {
        store.block(conversationId)
        refresh()
    }

    fun report(conversationId: String) {
        store.report(conversationId)
        refresh()
    }

    fun moderate(contact: ChatContact, report: Boolean) {
        store.moderate(contact, report)
        refresh()
    }

    fun startConversation(contact: ChatContact): String? {
        val id = store.startConversation(contact)
        refresh()
        return id
    }
}
