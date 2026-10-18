package com.askit.app.inbox

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.asSharedFlow

class InboxViewModel(
    private val seed: () -> InboxSeed = { InboxSeed.Empty },
) : ViewModel() {
    var store by mutableStateOf(InboxStore())
        private set
    private var accountId: String? = null
    private val accountStores = mutableMapOf<String, InboxStore>()

    private var resolveAccess: (String, String, Boolean) -> MessageAccess = { _, _, _ -> MessageAccess.Unavailable }
    private val _feedback = kotlinx.coroutines.flow.MutableSharedFlow<Int>(extraBufferCapacity = 1)
    val feedback = _feedback.asSharedFlow()

    fun configureAccess(resolver: (senderId: String, recipientId: String, isRecipientContact: Boolean) -> MessageAccess) {
        resolveAccess = resolver
    }

    private fun newStore(ownerId: String): InboxStore = InboxStore(seed = seed(), messageAccess = { recipientId ->
        val recipientStore = accountStores[recipientId]
        val access = when {
            accountId != ownerId -> MessageAccess.Unavailable
            store.conversation(recipientId)?.blocked == true || recipientStore?.conversation(ownerId)?.blocked == true -> MessageAccess.Blocked
            else -> resolveAccess(ownerId, recipientId, (recipientStore?.contacts ?: seed().contacts).any { it.id == ownerId })
        }
        access.messageRes?.let { _feedback.tryEmit(it) }
        access
    })

    fun activateAccount(id: String?) {
        val next = id?.takeIf(String::isNotBlank)
        if (next == accountId) return
        accountId?.let { accountStores[it] = store }
        accountId = next
        store = next?.let { accountStores.getOrPut(it) { newStore(it) } } ?: InboxStore()
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
