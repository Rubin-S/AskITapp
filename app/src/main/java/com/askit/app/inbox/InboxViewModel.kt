package com.askit.app.inbox

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class InboxViewModel(
    val store: InboxStore = InboxStore(),
) : ViewModel() {
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

    fun sendText(conversationId: String, body: String) {
        store.sendText(conversationId, body)
        refresh()
    }

    fun sendPhoto(conversationId: String, photoUri: String) {
        store.sendPhoto(conversationId, photoUri)
        refresh()
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

    fun startConversation(contact: ChatContact): String {
        val id = store.startConversation(contact)
        refresh()
        return id
    }
}
