package com.askit.app.inbox

import com.askit.app.home.model.isAvailable

data class ChatContact(
    val id: String,
    val name: String,
    val avatarUrl: String? = null,
)

data class ChatMessage(
    val id: String,
    val conversationId: String,
    val body: String,
    val photoUri: String? = null,
    val fromLocalUser: Boolean,
    val sentAtMillis: Long,
    val sharedStory: StoryReference? = null,
)

data class StoryReference(val storyId: String, val authorName: String)

data class Conversation(
    val id: String,
    val contact: ChatContact,
    val preview: String,
    val unreadCount: Int,
    val muted: Boolean = false,
    val blocked: Boolean = false,
    val reported: Boolean = false,
    val updatedAtMillis: Long,
    val previewIsPhoto: Boolean = false,
    val previewIsStory: Boolean = false,
)

class InboxStore(
    seed: Boolean = true,
    private val clock: () -> Long = System::currentTimeMillis,
    private val messageAccess: (String) -> MessageAccess = { MessageAccess.Unavailable },
) {
    private val conversations = linkedMapOf<String, Conversation>()
    private val messages = mutableMapOf<String, MutableList<ChatMessage>>()
    private var nextMessageId = 1

    val contacts: List<ChatContact> = if (seed) seededContacts() else emptyList()

    init {
        if (seed) {
            seededConversations().forEach { conversation ->
                conversations[conversation.id] = conversation
            }
            seededMessages().forEach { message ->
                messages.getOrPut(message.conversationId) { mutableListOf() }.add(message)
            }
        }
    }

    fun conversations(): List<Conversation> =
        conversations.values.sortedByDescending { it.updatedAtMillis }

    fun unreadCount(): Int = conversations.values.sumOf { it.unreadCount }

    fun messages(conversationId: String): List<ChatMessage> =
        messages[conversationId].orEmpty()

    fun conversation(id: String): Conversation? = conversations[id]

    fun markRead(conversationId: String) {
        val current = conversations[conversationId] ?: return
        conversations[conversationId] = current.copy(unreadCount = 0)
    }

    fun setUnread(conversationId: String, count: Int) {
        val current = conversations[conversationId] ?: return
        conversations[conversationId] = current.copy(unreadCount = count.coerceAtLeast(0))
    }

    fun setMuted(conversationId: String, muted: Boolean) {
        val current = conversations[conversationId] ?: return
        conversations[conversationId] = current.copy(muted = muted)
    }

    fun block(conversationId: String) {
        val current = conversations[conversationId] ?: return
        conversations[conversationId] = current.copy(blocked = true)
    }

    fun report(conversationId: String) {
        val current = conversations[conversationId] ?: return
        conversations[conversationId] = current.copy(reported = true)
    }

    fun sendText(conversationId: String, body: String): Boolean {
        val trimmed = body.trim()
        if (trimmed.isEmpty()) return false
        return appendMessage(conversationId, body = trimmed, photoUri = null)
    }

    fun sendPhoto(conversationId: String, photoUri: String): Boolean {
        if (photoUri.isBlank()) return false
        return appendMessage(conversationId, body = "", photoUri = photoUri)
    }

    fun sendStory(contact: ChatContact, story: com.askit.app.home.model.Story): Boolean {
        if (contacts.none { it.id == contact.id } || !story.isAvailable()) return false
        if (conversations[contact.id]?.blocked == true) return false
        val id = startConversation(contact) ?: return false
        return appendMessage(id, body = "", photoUri = null, sharedStory = StoryReference(story.id, story.authorName))
    }

    fun startConversation(contact: ChatContact): String? {
        if (messageAccess(contact.id) != MessageAccess.Allowed || conversations[contact.id]?.blocked == true) return null
        return getOrCreateConversation(contact)
    }

    // Reporting or blocking a person must remain possible when messaging is restricted.
    fun moderate(contact: ChatContact, report: Boolean) {
        val id = getOrCreateConversation(contact)
        if (report) report(id) else block(id)
    }

    private fun getOrCreateConversation(contact: ChatContact): String {
        conversations[contact.id]?.let { return it.id }
        val conversation = Conversation(
            id = contact.id,
            contact = contact,
            preview = "",
            unreadCount = 0,
            updatedAtMillis = clock(),
        )
        conversations[contact.id] = conversation
        messages[contact.id] = mutableListOf()
        return contact.id
    }

    private fun appendMessage(conversationId: String, body: String, photoUri: String?, sharedStory: StoryReference? = null): Boolean {
        val conversation = conversations[conversationId] ?: return false
        if (messageAccess(conversation.contact.id) != MessageAccess.Allowed || conversation.blocked) return false
        val message = ChatMessage(
            id = "msg-${nextMessageId++}",
            conversationId = conversationId,
            body = body,
            photoUri = photoUri,
            fromLocalUser = true,
            sentAtMillis = clock(),
            sharedStory = sharedStory,
        )
        messages.getOrPut(conversationId) { mutableListOf() }.add(message)
        conversations[conversationId] = conversation.copy(
            preview = body,
            previewIsPhoto = photoUri != null,
            previewIsStory = sharedStory != null,
            unreadCount = 0,
            updatedAtMillis = message.sentAtMillis,
        )
        return true
    }
}

internal fun seededContacts(): List<ChatContact> = listOf(
    ChatContact(id = "priya", name = "Priya"),
    ChatContact(id = "karthik", name = "Karthik"),
    ChatContact(id = "meena", name = "Meena"),
    ChatContact(id = "arun", name = "Arun Kumar"),
)

internal fun seededConversations(): List<Conversation> = listOf(
    Conversation(
        id = "priya",
        contact = ChatContact(id = "priya", name = "Priya"),
        preview = "I can come tomorrow morning.",
        unreadCount = 0,
        updatedAtMillis = 2_000L,
    ),
    Conversation(
        id = "karthik",
        contact = ChatContact(id = "karthik", name = "Karthik"),
        preview = "Thanks for the details.",
        unreadCount = 0,
        updatedAtMillis = 1_000L,
    ),
)

internal fun seededMessages(): List<ChatMessage> = listOf(
    ChatMessage(
        id = "m1",
        conversationId = "priya",
        body = "Hi, is the tap still leaking?",
        fromLocalUser = false,
        sentAtMillis = 1_000L,
    ),
    ChatMessage(
        id = "m2",
        conversationId = "priya",
        body = "I can come tomorrow morning.",
        fromLocalUser = false,
        sentAtMillis = 2_000L,
    ),
    ChatMessage(
        id = "m3",
        conversationId = "karthik",
        body = "Thanks for the details.",
        fromLocalUser = false,
        sentAtMillis = 1_000L,
    ),
)
